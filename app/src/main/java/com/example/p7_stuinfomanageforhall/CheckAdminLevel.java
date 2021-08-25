package com.example.p7_stuinfomanageforhall;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class CheckAdminLevel extends AppCompatActivity {

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    FirebaseUser fUser;
    String x;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fAuth=FirebaseAuth.getInstance();
        fStore=FirebaseFirestore.getInstance();

        fUser = fAuth.getCurrentUser();
        String email=fUser.getEmail();
        String documentId=email.substring(0,email.indexOf("@"));

        DocumentReference documentReference=fStore.collection("Verified Admins").document(documentId);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                x=documentSnapshot.getString("IsAdmin");
                if(x.equals("1")) {
                    //startActivity(new Intent(getApplicationContext(), SuperAdmin.class));
                }
                else if(x.equals("0")){
                    Toast.makeText(CheckAdminLevel.this, "You are not admin", Toast.LENGTH_SHORT).show();

                    //startActivity(new Intent(getApplicationContext(), AdminProfile.class));
                }
                else if(x.equals("2")){
                    //startActivity(new Intent(getApplicationContext(), HallAdmin.class));
                }
                else if(x.equals("3")){
                    //startActivity(new Intent(getApplicationContext(), Official.class));
                }
                finish();
            }
        });
    }
}