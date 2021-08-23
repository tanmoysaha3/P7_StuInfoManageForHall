package com.example.p7_stuinfomanageforhall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class EmailVerification extends AppCompatActivity {

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    FirebaseUser fUser;

    String email,pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent data=getIntent();
        email=data.getStringExtra("Email");
        pass=data.getStringExtra("Pass");

        fAuth=FirebaseAuth.getInstance();
        fStore=FirebaseFirestore.getInstance();
        //fUser=fAuth.getCurrentUser();

        fAuth.signInWithEmailAndPassword(email,pass).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                fUser=fAuth.getCurrentUser();
                String emailDomain=email.substring(email.indexOf("@")+1);
                if (fUser.isEmailVerified()){
                    Toast.makeText(EmailVerification.this, "Verification completed", Toast.LENGTH_SHORT).show();
                    //if(emailDomain.equals("student.just.edu.bd")) {
                    if(emailDomain.equals("storegmail.com")) {
                        String studentId=email.substring(0,email.indexOf("."));
                        DocumentReference stuRef = fStore.collection("Verified Students").document(studentId);
                        stuRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()){
                                    DocumentSnapshot stuSnap=task.getResult();
                                    if (stuSnap!=null && stuSnap.exists()){
                                        Toast.makeText(EmailVerification.this, "Document exists", Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        moveFirestoreDocument(fStore.collection("Unverfied Students").document(studentId),
                                                fStore.collection("Verified Students").document(studentId));
                                        Toast.makeText(EmailVerification.this, "Document moved", Toast.LENGTH_SHORT).show();
                                    }
                                    //startActivity(new Intent(getApplicationContext(), StuProfile.class));
                                    finish();
                                }
                                else {
                                    Toast.makeText(EmailVerification.this, "Document check failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }
                    //else if(emailDomain.equals("just.edu.bd"))
                    //else if(emailDomain.equals("gmail.com")) {
                    else if(emailDomain.equals("yousmail.com")) {
                        String documentId=email.substring(0,email.indexOf("@"));
                        DocumentReference adminRef=fStore.collection("Verified Admins").document(documentId);
                        adminRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()){
                                    DocumentSnapshot adminSnap=task.getResult();
                                    if (adminSnap!=null && adminSnap.exists()){
                                        Toast.makeText(EmailVerification.this, "Document exists", Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        moveFirestoreDocument (fStore.collection("Unverified Admins").document(documentId),
                                                fStore.collection("Verified Admins").document(documentId));
                                        Toast.makeText(EmailVerification.this, "Document Moved", Toast.LENGTH_SHORT).show();
                                    }
                                    //startActivity(new Intent(getApplicationContext(),AdminProfile.class));
                                    finish();
                                }
                                else {
                                    Toast.makeText(EmailVerification.this, "Document check failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
                else{
                    Toast.makeText(EmailVerification.this, "Still verification isn't completed", Toast.LENGTH_SHORT).show();
                    showMessage();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EmailVerification.this, "Error when sign in.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showMessage() {
        AlertDialog.Builder warning=new AlertDialog.Builder(this)
                .setTitle("Verification email is sent")
                .setMessage("A verification email has been sent to your email address. To complete verification - click on the link included in email &" +
                        " then click on \"Done\".")
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        fAuth.signOut();
                        Intent intent=new Intent(getApplicationContext(),EmailVerification.class);
                        intent.putExtra("Email",email);
                        intent.putExtra("Pass",pass);
                        startActivity(intent);
                        finish();
                    }
                }).setNegativeButton("Later!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        fAuth.signOut();
                        //startActivity(new Intent(getApplicationContext(),StuLogin.class));
                        finish();
                    }
                }).setNeutralButton("Resend", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        fUser=fAuth.getCurrentUser();
                        fUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(EmailVerification.this, "Verification email has been sent", Toast.LENGTH_SHORT).show();
                                showMessage();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(EmailVerification.this, "Error in ", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
        warning.show();
    }

    private void moveFirestoreDocument(DocumentReference fromPath, DocumentReference toPath) {
        fromPath.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot firstSnap=task.getResult();
                    if (firstSnap!=null && firstSnap.exists()){
                        toPath.set(firstSnap.getData())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(EmailVerification.this, "New document written", Toast.LENGTH_SHORT).show();
                                        fromPath.delete()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Toast.makeText(EmailVerification.this, "Old document deleted", Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(EmailVerification.this, "Error in deleting old document", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(EmailVerification.this, "Error in creating new document", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                    else {
                        Toast.makeText(EmailVerification.this, "Document don't exists", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(EmailVerification.this, "Task failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}