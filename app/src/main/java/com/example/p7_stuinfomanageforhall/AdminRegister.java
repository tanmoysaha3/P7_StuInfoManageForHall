package com.example.p7_stuinfomanageforhall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdminRegister extends AppCompatActivity {

    EditText nameAdminReg, emailAdminReg, passAdminReg;
    Button adminRegB;
    TextView adminLoginText;
    ProgressBar adminRegPBar;
    ImageView passVisibilityAdminReg;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    FirebaseUser fUser;

    String email,pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_register);

        nameAdminReg=findViewById(R.id.nameAdminReg);
        emailAdminReg=findViewById(R.id.emailAdminReg);
        passAdminReg=findViewById(R.id.passAdminReg);
        adminRegB=findViewById(R.id.adminRegB);
        adminLoginText=findViewById(R.id.adminLoginText);
        adminRegPBar=findViewById(R.id.adminRegPBar);
        passVisibilityAdminReg=findViewById(R.id.passVisibilityAdminReg);

        fAuth=FirebaseAuth.getInstance();
        fStore=FirebaseFirestore.getInstance();

        passVisibilityAdminReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(passAdminReg.getTransformationMethod().equals(PasswordTransformationMethod.getInstance())){
                    passVisibilityAdminReg.setImageResource(R.drawable.ic_baseline_visibility_24_white);
                    //Show Password
                    passAdminReg.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
                else{
                    passVisibilityAdminReg.setImageResource(R.drawable.ic_baseline_visibility_off_24_white);
                    //Hide Password
                    passAdminReg.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });

        adminRegB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adminRegPBar.setVisibility(View.VISIBLE);
                String name=nameAdminReg.getText().toString();
                email=emailAdminReg.getText().toString().trim();
                pass=passAdminReg.getText().toString().trim();

                //String regex = "\\S+@just.edu.bd$";
                String regex="\\S+@yousmail.com$";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(email);
                
                if (name.isEmpty()){
                    nameAdminReg.setError("Name is required");
                    return;
                }
                if (email.isEmpty()){
                    emailAdminReg.setError("Email is required");
                    return;
                }
                if (!matcher.matches()){
                    emailAdminReg.setError("University email is required");
                    return;
                }
                if (pass.length()<8){
                    passAdminReg.setError("Password length at least 8");
                    return;
                }
                
                String documentId=email.substring(0,email.indexOf("@"));
                fAuth.createUserWithEmailAndPassword(email,pass).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        DocumentReference adminRef=fStore.collection("Unverified Admins").document(documentId);
                        Map<String,Object> admin=new HashMap<>();
                        admin.put("Name",name);
                        admin.put("Email",email);
                        admin.put("IsAdmin","0");
                        adminRef.set(admin).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(AdminRegister.this, "Admin data saved", Toast.LENGTH_SHORT).show();
                                fUser=fAuth.getCurrentUser();
                                fUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(AdminRegister.this, "Verification email has been sent", Toast.LENGTH_SHORT).show();
                                        adminRegPBar.setVisibility(View.INVISIBLE);
                                        showMessage();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(AdminRegister.this, "Error in ", Toast.LENGTH_SHORT).show();
                                        adminRegPBar.setVisibility(View.INVISIBLE);
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(AdminRegister.this, "Error in creating document", Toast.LENGTH_SHORT).show();
                                adminRegPBar.setVisibility(View.INVISIBLE);
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AdminRegister.this, "Error in creating account", Toast.LENGTH_SHORT).show();
                        adminRegPBar.setVisibility(View.INVISIBLE);
                    }
                });
            }
        });

        adminLoginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(getApplicationContext(),AdminLogin.class));
            }
        });
    }

    private void showMessage() {
        AlertDialog.Builder warning=new AlertDialog.Builder(this)
                .setTitle("Verification email has been sent")
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
                        //startActivity(new Intent(getApplicationContext(),EmailVerification.class));
                        finish();
                    }
                }).setNegativeButton("Later!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        fAuth.signOut();
                        //startActivity(new Intent(getApplicationContext(),StuRegister.class));
                        finish();
                    }
                }).setNeutralButton("Resend", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        fUser=fAuth.getCurrentUser();
                        fUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(AdminRegister.this, "Verification email has been sent", Toast.LENGTH_SHORT).show();
                                adminRegPBar.setVisibility(View.INVISIBLE);
                                showMessage();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(AdminRegister.this, "Error in ", Toast.LENGTH_SHORT).show();
                                adminRegPBar.setVisibility(View.INVISIBLE);
                            }
                        });
                    }
                });
        warning.show();
    }
}