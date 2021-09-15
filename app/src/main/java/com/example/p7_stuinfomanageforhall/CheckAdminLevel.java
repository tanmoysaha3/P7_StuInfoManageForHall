package com.example.p7_stuinfomanageforhall;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

    private static final String TAG = "TAG";

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
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot documentSnapshot=task.getResult();
                    if (documentSnapshot.exists()){
                        Toast.makeText(CheckAdminLevel.this, "Exists", Toast.LENGTH_SHORT).show();
                        x=documentSnapshot.getString("IsAdmin");
                        String y=documentSnapshot.getString("AssignedHallId");
                        PowerPreference.getDefaultFile().putString("AdminAssignedHallId",y);
                        String z=documentSnapshot.getString("AssignedHallType");
                        PowerPreference.getDefaultFile().putString("AdminAssignedHallType",z);
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
                    else {
                        Toast.makeText(CheckAdminLevel.this, "Not exists", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(),AdminLogin.class));
                    }
                }
                else {
                    Toast.makeText(CheckAdminLevel.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}