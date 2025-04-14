package com.ishaanbhela.skincancerdetection;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class doctorModule extends AppCompatActivity implements unReviewedFormListAdapter.onUnReviewedItemClicked {

    RecyclerView unReviewedForms;
    TextView doctor;
    List<unRevievedFormListModel> forms = new ArrayList<>();
    Button signout;
    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_doctor_module);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        unReviewedForms = findViewById(R.id.doctorRecyclerView);
        doctor = findViewById(R.id.doctorWelcomeText);
        signout = findViewById(R.id.signOutButton);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        CollectionReference collRef= db.collection("Forms");
        Query query = collRef.whereEqualTo("reviewed", "False");
        query.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                for(QueryDocumentSnapshot docSnap : task.getResult()){
                    forms.add(new unRevievedFormListModel(docSnap.getString("UserID") + " : " + docSnap.getString("MonthYear"), docSnap.getId()));
                }
                unReviewedForms.setLayoutManager(new LinearLayoutManager(this));
                unReviewedFormListAdapter unReviewedAdapter = new unReviewedFormListAdapter(forms, (unReviewedFormListAdapter.onUnReviewedItemClicked) this);
                unReviewedForms.setAdapter(unReviewedAdapter);
            }
        });

        signout.setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(doctorModule.this, MainActivity.class));
            finish();
        });

    }

    @Override
    public void onClick(int position) {
        unRevievedFormListModel clickedItem = forms.get(position);
        Intent i = new Intent(doctorModule.this, DetectHistoryReport.class);
        i.putExtra("documentId", clickedItem.getUID());
        i.putExtra("isDoc", "true");
        startActivity(i);
    }
}