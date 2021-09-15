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
                //String regex = "\\S+@gmail.com$";
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
                        Toast.makeText(StuRegister.this, "Account created", Toast.LENGTH_SHORT).show();
                        DocumentReference stuOfficialDoc=fStore.collection("Students Data").document(studentId);
                        stuOfficialDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()){
                                    DocumentSnapshot stuOfficialSnap=task.getResult();
                                    if (stuOfficialSnap!=null && stuOfficialSnap.exists()){
                                        Toast.makeText(StuRegister.this, "Exists", Toast.LENGTH_SHORT).show();
                                        tempName=stuOfficialSnap.getString("Name");
                                        /*To write all data from official to new
                                        Map<String,Object> stuOfficialMap=new HashMap<>();
                                        stuOfficialMap=stuOfficialSnap.getData();*/
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
                                        stu.put("RegTime", FieldValue.serverTimestamp());
                                        //stu.put("IsAssigned","No");
                                        stu.put("IsAssigned","0");
                                        //stu.put("AssignedSeat","Empty");
                                        stu.put("UniqueSeatId","Empty");
                                        stu.put("NullValue","Null");
                                        stu.put("Gender",tempGender);
                                        stuDoc.set(stu).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(StuRegister.this, "Student Profile Created", Toast.LENGTH_SHORT).show();
                                                Map<String,Object> info=new HashMap<>();
                                                info.put("Registered","Yes");
                                                info.put("RegTime",FieldValue.serverTimestamp());
                                                stuOfficialDoc.update(info).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Toast.makeText(StuRegister.this, "Official Info Updated", Toast.LENGTH_SHORT).show();
                                                        fUser=fAuth.getCurrentUser();
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
                                                                Toast.makeText(StuRegister.this, "Error in sending verification email", Toast.LENGTH_SHORT).show();
                                                                stuRegPBar.setVisibility(View.INVISIBLE);
                                                            }
                                                        });
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(StuRegister.this, "Error in info updating"+e, Toast.LENGTH_SHORT).show();
                                                        stuRegPBar.setVisibility(View.GONE);
                                                    }
                                                });
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(StuRegister.this, "Error"+e, Toast.LENGTH_SHORT).show();
                                                stuRegPBar.setVisibility(View.GONE);
                                            }
                                        });
                                    }
                                    else {
                                        Toast.makeText(StuRegister.this, "Don't exists", Toast.LENGTH_SHORT).show();
                                        stuRegPBar.setVisibility(View.INVISIBLE);
                                    }
                                }
                                else {
                                    Toast.makeText(StuRegister.this, "Failed with "+task.getException(), Toast.LENGTH_SHORT).show();
                                    stuRegPBar.setVisibility(View.INVISIBLE);
                                }
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(StuRegister.this, "Failed with "+e, Toast.LENGTH_SHORT).show();
                    }
                });
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
                        fAuth.signOut();
                        finish();
                    }
                }).setNeutralButton("Resend", new DialogInterface.OnClickListener() {
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
                });
        warning.show();
    }
}