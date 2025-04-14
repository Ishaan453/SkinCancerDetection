import json
import base64
import torch
import torchvision.models as models
from torchvision import transforms
from PIL import Image
from io import BytesIO
import numpy as np
import torch.nn.functional as F
from torchcam.methods import GradCAM
import google.generativeai as genai
import matplotlib.pyplot as plt

# Initialize model & CAM
model = None
cam_extractor = None
num_classes = 2

def load_model():
    global model, cam_extractor
    if model is None:
        model = models.resnet18(weights=None)
        model.fc = torch.nn.Linear(model.fc.in_features, num_classes)
        model.load_state_dict(torch.load('best_model.pt', map_location=torch.device('cpu')))
        model.eval()
        cam_extractor = GradCAM(model, target_layer='layer4')

# Transforms
visual_transform = transforms.Resize((224, 224))
model_transform = transforms.Compose([
    transforms.Resize((224, 224)),
    transforms.ToTensor(),
    transforms.Normalize(mean=[0.485, 0.456, 0.406],
                         std=[0.229, 0.224, 0.225])
])

def apply_colormap(cam_image):
    cm = plt.get_cmap('jet')
    cam_image = cm(cam_image)
    cam_image = (cam_image[:, :, :3] * 255).astype('uint8')
    return Image.fromarray(cam_image)

def create_gradcam_visualization(original_image, cam_tensor):
    vis_img = original_image.resize((224, 224))
    cam_np = cam_tensor.cpu().numpy()
    cam_np = (cam_np - cam_np.min()) / (cam_np.max() - cam_np.min() + 1e-8)
    return apply_colormap(cam_np)

def query_gemini_flash(transformed_image_bytes, gradcam_image_bytes, class_name):
    """
    Queries the Gemini API with both the model input image and the Grad-CAM heatmap.
    Gemini will respond with a user-friendly explanation based on what the model is focusing on.
    """

    # API key and model configuration
    api_key = "API-KEY"
    genai.configure(api_key=api_key)
    model = genai.GenerativeModel("models/gemini-1.5-flash")

    # Compose the prompt and input images
    prompt_text = (
        f"This is a Grad-CAM heatmap for a ResNet18 model that predicted the class '{class_name}'. "
        "You are speaking directly to a user of a mobile app who just uploaded an image. "
        "They do not know anything about artificial intelligence, heatmaps, or how the system works internally. "
        "All they care about is understanding, in plain and friendly language, what the AI system noticed in the image that led to its prediction.\n\n"
        "You are provided two images:\n"
        "1. A resized (224x224) version of the image that the AI model actually used.\n"
        "2. A heatmap (from Grad-CAM) that shows which parts of the image the model focused on to make its decision.\n\n"
        "Use this information to explain what the AI model saw — for example, textures, shapes, density, or anything unusual — "
        "and how those visual cues helped it decide if the case is benign or malignant. "
        "Do not explain heatmaps, AI terms, or colors. Do not talk about how the system works. "
        "Only describe what the AI is seeing as if you're a friendly expert talking to a non-technical person. "
        "Keep it brief, clear, and easy to understand. Please restrict your response to 300 words or less."
    )

    parts = [
        {"text": prompt_text},
        {
            "inline_data": {
                "mime_type": "image/jpeg",
                "data": base64.b64encode(transformed_image_bytes).decode("utf-8")
            }
        },
        {
            "inline_data": {
                "mime_type": "image/jpeg",
                "data": base64.b64encode(gradcam_image_bytes).decode("utf-8")
            }
        }
    ]

    # Call Gemini and handle response
    try:
        response = model.generate_content({"parts": parts})
        return response.text
    except Exception as e:
        return f"Failed to query Gemini: {str(e)}"

def lambda_function(event, context):
    load_model()
    try:
        if 'body' in event:
            image_data = json.loads(event['body']).get('image')
        else:
            image_data = event.get('image')
        if not image_data:
            return {'statusCode': 400, 'body': json.dumps("No image data provided")}

        img_data = base64.b64decode(image_data)
        original_img = Image.open(BytesIO(img_data)).convert('RGB')

        # Process images
        input_tensor = model_transform(original_img).unsqueeze(0)
        visual_img = visual_transform(original_img)

        # Prediction
        output = model(input_tensor)
        pred = torch.argmax(output, dim=1).item()
        class_name = 'Malignant' if pred == 1 else 'Benign'

        # GradCAM
        with torch.enable_grad():
            model.zero_grad()
            activation_map = cam_extractor(pred, output)[0]

        upsampled_map = F.interpolate(activation_map.unsqueeze(0), size=(224, 224), mode='bilinear', align_corners=False).squeeze()
        upsampled_map = upsampled_map.clamp(min=0)
        if upsampled_map.max() > 0:
            upsampled_map = upsampled_map / upsampled_map.max()

        gradcam_image = create_gradcam_visualization(original_img, upsampled_map)

        # Convert both images to bytes
        with BytesIO() as buf1:
            visual_img.save(buf1, format='JPEG')
            transformed_image_bytes = buf1.getvalue()

        with BytesIO() as buf2:
            gradcam_image.save(buf2, format='JPEG')
            gradcam_image_bytes = buf2.getvalue()

        # Query Gemini
        gemini_response = query_gemini_flash(transformed_image_bytes, gradcam_image_bytes, class_name)

        return {
            'statusCode': 200,
            'body': json.dumps({
                'predicted_class': pred,
                'class_name': class_name,
                'gemini_insight': gemini_response
            })
        }

    except Exception as e:
        return {'statusCode': 500, 'body': json.dumps(f"Error: {str(e)}")}
