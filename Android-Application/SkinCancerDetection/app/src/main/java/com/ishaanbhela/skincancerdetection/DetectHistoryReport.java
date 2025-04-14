package com.ishaanbhela.skincancerdetection;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class DetectHistoryReport extends AppCompatActivity {

    private ImageView formImage;
    private TextView modelOutput;
    private TextView monthYear;
    private EditText doctorReview;
    private TextView geminiOP;
    private Button hiddenButton; // Declare the hidden button
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detect_history_report);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize the UI elements
        formImage = findViewById(R.id.formImage);
        modelOutput = findViewById(R.id.modelOutput);
        monthYear = findViewById(R.id.monthYear);
        doctorReview = findViewById(R.id.doctorReview);
        geminiOP = findViewById(R.id.modelOutput2);
        hiddenButton = findViewById(R.id.hiddenButton); // Initialize hidden button

        // Get the document ID and "isDoc" flag passed from the previous activity
        Intent intent = getIntent();
        String documentId = intent.getStringExtra("documentId");
        String isDoc = intent.getStringExtra("isDoc");

        // Fetch the form data from Firestore using the document ID
        fetchFormData(documentId);

        // Handle Edge-to-Edge layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // If the user is a doctor, enable the review and show the button
        if ("true".equals(isDoc)) {
            doctorReview.setFocusableInTouchMode(true); // Enable the EditText
            doctorReview.setClickable(true);
            hiddenButton.setVisibility(View.VISIBLE); // Show the hidden button
        }

        // Set up the hidden button click listener
        hiddenButton.setOnClickListener(v -> {
            String review = doctorReview.getText().toString().trim();
            if (!review.isEmpty()) {
                // If review is not empty, update it in Firestore
                updateDoctorReview(documentId, review);
            } else {
                Toast.makeText(this, "Please enter a review", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchFormData(String documentId) {
        // Reference to the document in Firestore
        DocumentReference formRef = db.collection("Forms").document(documentId);

        formRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Retrieve data from the document
                        String base64Image = documentSnapshot.getString("base64Image");
                        String modelOutputText = documentSnapshot.getString("modelOutput");
                        String monthYearText = documentSnapshot.getString("monthYear");
                        String doctorReviewText = documentSnapshot.getString("DoctorReview");
                        String gemini = documentSnapshot.getString("geminiOutput");

                        // Set the data to the views
                        modelOutput.setText(modelOutputText);
                        monthYear.setText(monthYearText);
                        doctorReview.setText(doctorReviewText);
                        geminiOP.setText(gemini);

                        // Load the base64 image into the ImageView using Glide
                        loadImage(base64Image);
                    } else {
                        Toast.makeText(this, "Form not found!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching form data", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadImage(String base64Image) {
        // Decode the base64 string and load the image into the ImageView
        if (base64Image != null && !base64Image.isEmpty()) {
            try {
                // Decode the base64 string into a byte array
                byte[] decodedString = android.util.Base64.decode(base64Image, android.util.Base64.DEFAULT);

                // Convert the byte array into a Bitmap
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                // Load the Bitmap into the ImageView using Glide
                Glide.with(this)
                        .load(decodedBitmap)
                        .into(formImage);
            } catch (IllegalArgumentException e) {
                // Handle the exception if the base64 string is invalid
                e.printStackTrace();
                formImage.setImageResource(R.drawable.ic_launcher_background); // Use a placeholder image
            }
        } else {
            formImage.setImageResource(R.drawable.ic_launcher_background); // Use a placeholder image if the base64 string is empty or null
        }
    }

    private void updateDoctorReview(String documentId, String review) {
        // Reference to the document in Firestore
        DocumentReference formRef = db.collection("Forms").document(documentId);

        // Update the doctor's review in Firestore
        formRef.update("DoctorReview", review)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Review submitted successfully", Toast.LENGTH_SHORT).show();
                    doctorReview.setFocusable(false); // Disable the EditText after submission
                    doctorReview.setClickable(false);
                    hiddenButton.setVisibility(View.GONE); // Hide the button after submission
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error submitting review", Toast.LENGTH_SHORT).show();
                });

        formRef.update("Reviewed", "true");
    }
}
