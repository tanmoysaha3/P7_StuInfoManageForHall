package com.example.p7_stuinfomanageforhall;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Source;
import com.preference.PowerPreference;

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

        PowerPreference.init(this);

        fUser = fAuth.getCurrentUser();
        String email=fUser.getEmail();
        String documentId=email.substring(0,email.indexOf("@"));
        Toast.makeText(this, "email"+email, Toast.LENGTH_SHORT).show();

        DocumentReference documentReference=fStore.collection("Verified Admins").document(documentId);

        /*documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(CheckAdminLevel.this, "Listen failed."+e, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    Toast.makeText(CheckAdminLevel.this, "Current data: " + snapshot.getData(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CheckAdminLevel.this, "Current data: null", Toast.LENGTH_SHORT).show();
                }
            }
        });*/
        //docRef.addSnapshotListener don't need "this" before new EventListener
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                x=documentSnapshot.getString("IsAdmin");
                String y=documentSnapshot.getString("AssignedHallId");
                PowerPreference.getDefaultFile().putString("AdminAssignedHallId",y);
                //Toast.makeText(CheckAdminLevel.this, "source "+documentSnapshot.getMetadata().isFromCache(), Toast.LENGTH_SHORT).show();
                if(x.equals("1")) {
                    startActivity(new Intent(getApplicationContext(), DashBoardSuperAdmin.class));
                }
                else if(x.equals("0")){
                    Toast.makeText(CheckAdminLevel.this, "You are not admin", Toast.LENGTH_SHORT).show();

                    //startActivity(new Intent(getApplicationContext(), AdminProfile.class));
                }
                else if(x.equals("2")){
                    startActivity(new Intent(getApplicationContext(), DashBoardHallAdmin.class));
                }
                else if(x.equals("3")){
                    //startActivity(new Intent(getApplicationContext(), Official.class));
                }
                finish();
            }
        });
    }
}