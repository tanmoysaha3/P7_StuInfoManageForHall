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
import android.util.Log;
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

public class StuLogin extends AppCompatActivity {

    private static final String TAG = "Log - StuLogin";

    private static final int WAIT_TIME = 3 * 60 * 1000;
    private int loginAttempts = 3;

    EditText emailStuLogin, passStuLogin;
    ImageView passVisibilityStuLogin;
    Button stuLoginB, adminLoginTextB;
    TextView stuRegText, stuResetPassText, wrongCredStuLogin;
    ProgressBar stuLoginPBar;

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    FirebaseUser fUser;

    String email, pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stu_login);

        emailStuLogin=findViewById(R.id.emailStuLogin);
        passStuLogin=findViewById(R.id.passStuLogin);
        passVisibilityStuLogin=findViewById(R.id.passVisibilityStuLogin);
        stuLoginB=findViewById(R.id.stuLoginB);
        adminLoginTextB=findViewById(R.id.adminLoginTextB);
        stuRegText=findViewById(R.id.stuRegText);
        stuResetPassText=findViewById(R.id.stuResetPassText);
        wrongCredStuLogin=findViewById(R.id.wrongCredStuLogin);
        stuLoginPBar=findViewById(R.id.stuLoginPBar);

        fAuth=FirebaseAuth.getInstance();
        fStore=FirebaseFirestore.getInstance();

        passVisibilityStuLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (passStuLogin.getTransformationMethod().equals(PasswordTransformationMethod.getInstance())){
                    passVisibilityStuLogin.setImageResource(R.drawable.ic_baseline_visibility_24);
                    passStuLogin.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
                else {
                    passVisibilityStuLogin.setImageResource(R.drawable.ic_baseline_visibility_off_24);
                    passStuLogin.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });

        stuLoginB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (loginAttempts == 0) {
                    Toast.makeText(StuLogin.this, "Your attempt reach 0, please wait 3 minutes to log again", Toast.LENGTH_SHORT).show();
                    return;
                }

                email=emailStuLogin.getText().toString().trim();
                pass=passStuLogin.getText().toString();

                if (email.isEmpty()){
                    emailStuLogin.setError("Email is required");
                    return;
                }
                if (pass.isEmpty()){
                    passStuLogin.setError("Password is required");
                    return;
                }
                stuLoginPBar.setVisibility(View.VISIBLE);

                fAuth.signInWithEmailAndPassword(email,pass).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Log.d(TAG,"Successfully logged in");
                        stuLoginPBar.setVisibility(View.INVISIBLE);
                        fUser=fAuth.getCurrentUser();
                        if (fUser.isEmailVerified()){
                            Log.d(TAG,"User verified - starting dashboard activity");
                            startActivity(new Intent(getApplicationContext(),DashBoardStudent.class));
                            finish();
                        }
                        else {
                            fUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG,"Verification email has been sent");
                                    showMessage();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG,"Error in sending verification email"+e.getMessage());
                                    Toast.makeText(StuLogin.this, "Error in sending verification email", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG,"Login Failed"+e.getMessage());
                        stuLoginPBar.setVisibility(View.INVISIBLE);
                        wrongCredStuLogin.setVisibility(View.VISIBLE);
                        loginAttempts--;

                        if (loginAttempts==2){
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    loginAttempts=3;
                                }
                            }, WAIT_TIME);
                        }
                    }
                });
            }
        });

        stuRegText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),StuRegister.class));
                finish();
            }
        });

        stuResetPassText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText resetMail = new EditText(v.getContext());
                AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext());
                passwordResetDialog.setTitle("Reset Password");
                passwordResetDialog.setMessage("Enter your email to receive the password link");
                passwordResetDialog.setView(resetMail);

                passwordResetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String mail=resetMail.getText().toString();
                        fAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG,"Reset Link Sent to Your Email");
                                Toast.makeText(StuLogin.this,"Reset Link Sent to Your Email", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG,"Error! Reset Link is not Sent" + e.getMessage());
                                Toast.makeText(StuLogin.this, "Error! Reset Link is not Sent" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                passwordResetDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                passwordResetDialog.create().show();
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
                        finish();
                    }
                }).setNegativeButton("Later!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        fAuth.signOut();
                        finish();
                    }
                });
        warning.show();
    }
}