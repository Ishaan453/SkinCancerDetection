FROM python:3.12-slim

WORKDIR /var/task

COPY requirements.txt .

# Install only absolute minimum dependencies
RUN apt-get update && apt-get install -y --no-install-recommends \
    libgomp1 \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Install CPU-only PyTorch with minimal dependencies
RUN pip install --no-cache-dir torch==2.6.0+cpu torchvision==0.21.0+cpu --index-url https://download.pytorch.org/whl/cpu
RUN pip install --no-cache-dir -r requirements.txt

# Copy only necessary files
COPY lambda_handler.py .
COPY best_model.pt .

# Set Lambda entrypoint
ENTRYPOINT ["python", "-m", "awslambdaric"]
CMD ["lambda_handler.lambda_function"]