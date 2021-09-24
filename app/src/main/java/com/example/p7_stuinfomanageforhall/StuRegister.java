package com.example.p7_stuinfomanageforhall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StuRegister extends AppCompatActivity {

    private static final String TAG = "Log - StuRegister";

    EditText nameStuReg, emailStuReg, passStuReg;
    Button stuRegB;
    TextView stuLoginText, databaseErrorText;
    ProgressBar stuRegPBar;
    ImageView passVisibilityStuReg;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    FirebaseUser fUser;

    String email, pass, tempName, tempAYear, tempDist, tempContactNo,
            tempDept, tempDOB, tempGender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stu_register);

        nameStuReg=findViewById(R.id.nameStuReg);
        emailStuReg=findViewById(R.id.emailStuReg);
        passStuReg=findViewById(R.id.passStuReg);
        stuRegB=findViewById(R.id.stuRegB);
        stuRegPBar=findViewById(R.id.stuRegPBar);
        stuLoginText=findViewById(R.id.stuLoginText);
        databaseErrorText=findViewById(R.id.databaseErrorText);
        passVisibilityStuReg=findViewById(R.id.passVisibilityStuReg);

        fAuth=FirebaseAuth.getInstance();
        fStore=FirebaseFirestore.getInstance();

        passVisibilityStuReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(passStuReg.getTransformationMethod().equals(PasswordTransformationMethod.getInstance())){
                    passVisibilityStuReg.setImageResource(R.drawable.ic_baseline_visibility_24_white);
                    //Show Password
                    passStuReg.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
                else{
                    passVisibilityStuReg.setImageResource(R.drawable.ic_baseline_visibility_off_24_white);
                    //Hide Password
                    passStuReg.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });

        stuRegB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name=nameStuReg.getText().toString();
                email=emailStuReg.getText().toString().trim();
                pass=passStuReg.getText().toString().trim();

                //String regex = "^[0-9]{6}.[a-z]{3}@student.just.edu.bd$";
                String regex = "^[0-9]{6}.[a-z]{3}@storegmail.com$";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(email);

                if (name.isEmpty()){
                    nameStuReg.setError("Name is required");
                    return;
                }
                if (email.isEmpty()){
                    emailStuReg.setError("Email is required");
                    return;
                }
                if (!matcher.matches()){
                    emailStuReg.setError("University email is required");
                    return;
                }
                if (pass.length()<8){
                    passStuReg.setError("Password length need to be at least 8");
                    return;
                }

                String studentId=email.substring(0,email.indexOf("."));
                stuRegPBar.setVisibility(View.VISIBLE);

                fAuth.createUserWithEmailAndPassword(email,pass).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Log.d(TAG,"Successfully created account");
                        DocumentReference stuOfficialDoc=fStore.collection("Students Data").document(studentId);
                        stuOfficialDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()){
                                    DocumentSnapshot stuOfficialSnap=task.getResult();
                                    if (stuOfficialSnap!=null && stuOfficialSnap.exists()){
                                        Log.d(TAG,"Student Id exists in official data");
                                        /*To write all data from official to new
                                        Map<String,Object> stuOfficialMap=new HashMap<>();
                                        stuOfficialMap=stuOfficialSnap.getData();*/
                                        tempName=stuOfficialSnap.getString("Name");
                                        tempAYear=stuOfficialSnap.getString("AcademicYear");
                                        tempDept=stuOfficialSnap.getString("Department");
                                        tempDist=stuOfficialSnap.getString("District");
                                        tempContactNo=stuOfficialSnap.getString("ContactNo");
                                        tempDOB=stuOfficialSnap.getString("DateOfBirth");
                                        tempGender=stuOfficialSnap.getString("Gender");

                                        DocumentReference stuDoc=fStore.collection("Unverified Students").document(studentId);
                                        Map<String,Object> stu=new HashMap<>();
                                        stu.put("Name",tempName);
                                        stu.put("Email",email);
                                        stu.put("StudentId",studentId);
                                        stu.put("AcademicYear",tempAYear);
                                        stu.put("District",tempDist);
                                        stu.put("Department",tempDept);
                                        stu.put("DateOfBirth",tempDOB);
                                        stu.put("ContactNo",tempContactNo);
                                        stu.put("Gender",tempGender);
                                        stu.put("RegTime", FieldValue.serverTimestamp());
                                        stu.put("IsAssigned","0");
                                        stu.put("UniqueSeatId","Empty");
                                        stu.put("NullValue","Null");
                                        //stu.put("IsAssigned","No");
                                        //stu.put("AssignedSeat","Empty");
                                        stuDoc.set(stu).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "Student profile created");
                                                Map<String,Object> info=new HashMap<>();
                                                info.put("Registered","Yes");
                                                info.put("RegTime",FieldValue.serverTimestamp());
                                                stuOfficialDoc.update(info).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d(TAG,"Official info updated");
                                                        fUser=fAuth.getCurrentUser();
                                                        fUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Log.d(TAG, "Verification email has been sent");
                                                                stuRegPBar.setVisibility(View.INVISIBLE);
                                                                showMessage();
                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Log.d(TAG,"Error in sending verification email");
                                                                Toast.makeText(StuRegister.this, "Error in sending verification email"+e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                stuRegPBar.setVisibility(View.INVISIBLE);
                                                            }
                                                        });
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.d(TAG, "Error in official info updating"+e.getMessage());
                                                        stuRegPBar.setVisibility(View.GONE);
                                                    }
                                                });
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d(TAG, "Error in creating student profile"+e.getMessage());
                                                stuRegPBar.setVisibility(View.GONE);
                                            }
                                        });
                                    }
                                    else {
                                        Log.d(TAG, "Student Id don't exists in official database");
                                        databaseErrorText.setVisibility(View.VISIBLE);
                                        stuRegPBar.setVisibility(View.INVISIBLE);
                                    }
                                }
                                else {
                                    Log.d(TAG, "Accessing official database failed with "+task.getException());
                                    stuRegPBar.setVisibility(View.INVISIBLE);
                                }
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Registration process failed with "+e.getMessage());
                        Toast.makeText(StuRegister.this, "Failed with "+e, Toast.LENGTH_SHORT).show();
                        stuRegPBar.setVisibility(View.INVISIBLE);
                    }
                });
            }
        });

        stuLoginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),StuLogin.class));
                finish();
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
                        Intent intent=new Intent(getApplicationContext(), EmailVerification.class);
                        intent.putExtra("Email",email);
                        intent.putExtra("Pass",pass);
                        startActivity(intent);
                        finish();
                    }
                }).setNegativeButton("Later!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG,"User logged out");
                        fAuth.signOut();
                        finish();
                    }
                })/*.setNeutralButton("Resend", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //fUser=fAuth.getCurrentUser();
                        fUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(StuRegister.this, "Verification email has been sent", Toast.LENGTH_SHORT).show();
                                stuRegPBar.setVisibility(View.INVISIBLE);
                                showMessage();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(StuRegister.this, "Error in sending verification email"+e, Toast.LENGTH_SHORT).show();
                                stuRegPBar.setVisibility(View.INVISIBLE);
                            }
                        });
                    }
                })*/;
        warning.show();
    }
}