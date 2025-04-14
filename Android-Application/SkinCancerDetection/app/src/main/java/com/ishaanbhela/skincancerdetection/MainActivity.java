package com.ishaanbhela.skincancerdetection;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private EditText username;
    private EditText password;
    Button login;
    TextView signUp;
    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        username = findViewById(R.id.etEmail);
        password = findViewById(R.id.etPassword);
        login = findViewById(R.id.btnLogin);
        signUp = findViewById(R.id.tvRegister);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        login.setOnClickListener(view -> {
            String usern = username.getText().toString();
            String pass = password.getText().toString();
            if(usern.isEmpty()){
                Toast.makeText(this, "username cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            if(pass.isEmpty()){
                Toast.makeText(this, "password cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.signInWithEmailAndPassword(usern, pass)
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            user = auth.getCurrentUser();
                            String userId = user.getUid();
                            DocumentReference docref =  db.collection("userType").document(userId);

                            docref.get().addOnCompleteListener(task1 -> {
                                if(task1.isSuccessful()){
                                    DocumentSnapshot snapshot = task1.getResult();
                                    if(snapshot.exists()){
                                        String userType = (String) snapshot.get("type");

                                        if(userType.equals("patient")){
                                            startActivity(new Intent(this, patientModule.class));
                                        }
                                        else{
                                            startActivity(new Intent(this, doctorModule.class));

                                        }
                                    }
                                }
                                else {
                                    Exception e = task1.getException();
                                    if (e != null) {
                                        e.printStackTrace(); // For debugging
                                        System.out.println(e);
                                    } else {
                                        Toast.makeText(this, "Unknown error occurred.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                        else{
                            Toast.makeText(this, "Incorrect username or password", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}