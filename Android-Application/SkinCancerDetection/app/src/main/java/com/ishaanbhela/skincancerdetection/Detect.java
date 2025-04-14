package com.ishaanbhela.skincancerdetection;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Detect extends AppCompatActivity {

    private ImageView imageView;
    private Button btnGallery, btnCamera, btnDetect;
    private TextView txtModelOutput, txtGeminiOutput;
    private Bitmap selectedImage;
    private FirebaseFirestore db;
    private ProgressBar progBar;
    Uri PDFURI = null;
    String pdfText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect);

        imageView = findViewById(R.id.imagePreview);
        btnGallery = findViewById(R.id.btnGallery);
        btnCamera = findViewById(R.id.btnCamera);
        btnDetect = findViewById(R.id.btnDetect);
        txtModelOutput = findViewById(R.id.tvClassification);
        txtGeminiOutput = findViewById(R.id.tvGeminiOutput);
        progBar = findViewById(R.id.progBar);

        db = FirebaseFirestore.getInstance();

        btnGallery.setOnClickListener(view -> pickImageFromGallery());
        btnCamera.setOnClickListener(view -> captureImageFromCamera());
        btnDetect.setOnClickListener(view -> sendImageToApi());
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void captureImageFromCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(intent);
    }


    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    try {
                        selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                        imageView.setImageBitmap(selectedImage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
    );

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        selectedImage = (Bitmap) extras.get("data");
                        imageView.setImageBitmap(selectedImage);
                    }
                }
            }
    );


    private void sendImageToApi() {
        progBar.setVisibility(VISIBLE);
        if (selectedImage == null) {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
            return;
        }

        String encodedImage = encodeImageToBase64(selectedImage);
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("image", encodedImage);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                "https://u5qc5kbpxuebrdrkhd26nwons40fysnc.lambda-url.ap-south-1.on.aws/",
                jsonRequest,
                response -> {
                    try {
                        String prediction = response.getString("class_name");
                        String gemini_insights = response.getString("gemini_insight");
                        txtModelOutput.setText(prediction);
                        txtGeminiOutput.setText(gemini_insights);

                        progBar.setVisibility(INVISIBLE);
                        saveToFirestore(encodedImage, prediction, gemini_insights);
                        btnDetect.setEnabled(false);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Toast.makeText(this, "Upload failed! " + error.getMessage(), Toast.LENGTH_LONG).show();
                    error.printStackTrace();
                }
        );

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                120000, // 120 seconds timeout
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        queue.add(jsonObjectRequest);
    }



    private void saveToFirestore(String base64Image, String modelOutput, String geminiOutput) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();  // Get the authenticated user's UID
        String monthYear = new SimpleDateFormat("MM-yyyy", Locale.getDefault()).format(new Date());

        Map<String, Object> formEntry = new HashMap<>();
        formEntry.put("base64Image", base64Image);
        formEntry.put("modelOutput", modelOutput);
        formEntry.put("geminiOutput", geminiOutput);
        formEntry.put("monthYear", monthYear);
        formEntry.put("timestamp", new Date()); // Store timestamp for ordering
        formEntry.put("UserID", userId);
        formEntry.put("DoctorReview", "Not Reviewed Yet.");
        formEntry.put("reviewed", "False");

        // Save inside: Forms -> UserID -> Random Document ID
        db.collection("Forms") // Top-level collection
                .add(formEntry) // Create a random document inside "UserForms"
                .addOnSuccessListener(documentReference ->
                        Toast.makeText(this, "Saved to Firestore", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error saving to Firestore", Toast.LENGTH_SHORT).show());
    }

    private String encodeImageToBase64(Bitmap image) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
}