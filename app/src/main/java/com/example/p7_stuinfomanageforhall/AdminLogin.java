package com.example.p7_stuinfomanageforhall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminLogin extends AppCompatActivity {

    private static final int WAIT_TIME = 3 * 60 * 1000;
    private int loginAttempts = 3;

    EditText emailAdminLogin, passAdminLogin;
    Button adminLoginB;
    TextView wrongCredAdminLogin, adminRegText, adminResetPassText;
    ImageView passVisibilityAdminLogin;
    ProgressBar adminLoginPBar;

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    FirebaseUser fUser;

    String email, pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        emailAdminLogin=findViewById(R.id.emailAdminLogin);
        passAdminLogin=findViewById(R.id.passAdminLogin);
        adminLoginB=findViewById(R.id.adminLoginB);
        wrongCredAdminLogin=findViewById(R.id.wrongCredAdminLogin);
        adminRegText=findViewById(R.id.adminRegText);
        adminResetPassText=findViewById(R.id.adminResetPassText);
        passVisibilityAdminLogin=findViewById(R.id.passVisibilityAdminLogin);
        adminLoginPBar=findViewById(R.id.adminLoginPBar);

        fAuth=FirebaseAuth.getInstance();
        fStore=FirebaseFirestore.getInstance();

        passVisibilityAdminLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(passAdminLogin.getTransformationMethod().equals(PasswordTransformationMethod.getInstance())){
                    passVisibilityAdminLogin.setImageResource(R.drawable.ic_baseline_visibility_24);
                    //Show Password
                    passAdminLogin.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
                else{
                    passVisibilityAdminLogin.setImageResource(R.drawable.ic_baseline_visibility_off_24);
                    //Hide Password
                    passAdminLogin.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });

        adminLoginB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (loginAttempts==0){
                    Toast.makeText(AdminLogin.this, "Your attempt reach maximum, please wait 3 minutes to try again", Toast.LENGTH_SHORT).show();
                    return;
                }

                email=emailAdminLogin.getText().toString().trim();
                pass=passAdminLogin.getText().toString().trim();

                if (email.isEmpty()){
                    emailAdminLogin.setError("Email is required");
                    return;
                }
                if (pass.isEmpty()){
                    passAdminLogin.setError("Password is required");
                    return;
                }
                adminLoginPBar.setVisibility(View.VISIBLE);

                fAuth.signInWithEmailAndPassword(email,pass).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Toast.makeText(AdminLogin.this, "Logged in successfully", Toast.LENGTH_SHORT).show();
                        FirebaseUser fUser=fAuth.getCurrentUser();
                        if (fUser.isEmailVerified()){
                            Toast.makeText(AdminLogin.this, "Already verified", Toast.LENGTH_SHORT).show();
                            Intent intent=new Intent(getApplicationContext(),CheckAdminLevel.class);
                            intent.putExtra("Email",email);
                            startActivity(intent);
                        }
                        else {
                            fUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(AdminLogin.this, "Verification email has been sent", Toast.LENGTH_SHORT).show();
                                    adminLoginPBar.setVisibility(View.INVISIBLE);
                                    showMessage();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(AdminLogin.this, "Error in ", Toast.LENGTH_SHORT).show();
                                    adminLoginPBar.setVisibility(View.INVISIBLE);
                                }
                            });
                        }
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AdminLogin.this, "Admin Login Error"+e, Toast.LENGTH_SHORT).show();
                        adminLoginPBar.setVisibility(View.INVISIBLE);
                        loginAttempts--;

                        if (loginAttempts==2){
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    loginAttempts=3;
                                }
                            },WAIT_TIME);
                        }
                    }
                });
            }
        });

        adminRegText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),AdminRegister.class));
            }
        });

        adminResetPassText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText resetMail=new EditText(v.getContext());
                AlertDialog.Builder passResetDialog=new AlertDialog.Builder(v.getContext());
                passResetDialog.setTitle("Reset Password");
                passResetDialog.setMessage("Enter your email to receive the password link");
                passResetDialog.setView(resetMail);

                passResetDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String mail=resetMail.getText().toString().trim();
                        fAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(AdminLogin.this, "Password reset link has been sent to your email", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(AdminLogin.this, "Error during sending reset link"+e, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                passResetDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(AdminLogin.this, "Password reset cancelled", Toast.LENGTH_SHORT).show();
                    }
                });
                passResetDialog.create().show();
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
                                Toast.makeText(AdminLogin.this, "Verification email has been sent", Toast.LENGTH_SHORT).show();
                                adminLoginPBar.setVisibility(View.INVISIBLE);
                                showMessage();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(AdminLogin.this, "Error in ", Toast.LENGTH_SHORT).show();
                                adminLoginPBar.setVisibility(View.INVISIBLE);
                            }
                        });
                    }
                });
        warning.show();
    }
}