package com.example.p7_stuinfomanageforhall;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.preference.PowerPreference;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class CheckUser extends AppCompatActivity {

    private static final String TAG = "Log - CheckUser";

    ProgressBar checkUserPBar;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_user);

        PowerPreference.init(this);

        checkUserPBar=findViewById(R.id.checkUserPBar);
        fAuth=FirebaseAuth.getInstance();
        fStore=FirebaseFirestore.getInstance();
        checkUserPBar.setVisibility(View.VISIBLE);
        Toast.makeText(this, "Wait please", Toast.LENGTH_SHORT).show();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Handler handler=new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isOnline()){
                    if (fAuth.getCurrentUser()!=null){
                        if (fAuth.getCurrentUser().isEmailVerified()){
                            if (PowerPreference.getDefaultFile().getString("IsEmailVerified","0").equals("1")){
                                Toast.makeText(CheckUser.this, "Dashboard", Toast.LENGTH_SHORT).show();
                                String email=fAuth.getCurrentUser().getEmail();
                                String emailDomain=email.substring(email.indexOf("@")+1);
                                //if(emailDomain.equals("student.just.edu.bd")){
                                if(emailDomain.equals("storegmail.com")){
                                    Log.d(TAG,"online & student verified");
                                    startActivity(new Intent(getApplicationContext(), DashBoardStudent.class));
                                    finish();
                                }
                                //else if(emailDomain.equals("just.edu.bd")) {
                                else if(emailDomain.equals("yousmail.com")) {
                                    Log.d(TAG,"online & admin verified");
                                    Intent intent=new Intent(getApplicationContext(),CheckAdminLevel.class);
                                    intent.putExtra("Email",email);
                                    startActivity(intent);
                                }
                            }
                            else {
                                Toast.makeText(CheckUser.this, "EmailVerification", Toast.LENGTH_SHORT).show();
                                fAuth.signOut();
                                startActivity(new Intent(getApplicationContext(),StuLogin.class));
                            }
                            finish();

                        }
                        else {
                            Log.d(TAG,"user not Verified");
                            startActivity(new Intent(getApplicationContext(),StuLogin.class));
                            //startActivity(new Intent(getApplicationContext(),AdminLogin.class));
                        }
                    }
                    else {
                        Log.d(TAG,"User is null");
                        startActivity(new Intent(getApplicationContext(),StuLogin.class));
                        //startActivity(new Intent(getApplicationContext(),AdminLogin.class));
                    }
                    finish();
                }
                else {
                    Log.d(TAG,"User is offline");
                    offlineAlert();
                }
            }
        },2000);
    }

    private boolean isOnline(){
        try {
            int timeoutMs=1500;
            Socket socket=new Socket();
            SocketAddress socketAddress=new InetSocketAddress("8.8.8.8",53);

            socket.connect(socketAddress,timeoutMs);
            socket.close();
            return true;
        } catch (IOException e){
            return false;
        }
    }

    private void offlineAlert(){
        AlertDialog.Builder warning=new AlertDialog.Builder(this)
                .setTitle("Network Error")
                .setMessage("You are not connected with internet")
                .setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        startActivity(getIntent());
                    }
                }).setNegativeButton("Browse Offline", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (fAuth.getCurrentUser()!=null){
                            if (fAuth.getCurrentUser().isEmailVerified()){
                                String email=fAuth.getCurrentUser().getEmail();
                                String emailDomain=email.substring(email.indexOf("@")+1);
                                //if(emailDomain.equals("student.just.edu.bd")){
                                Log.d(TAG, "browsing offline");
                                if(emailDomain.equals("storegmail.com")){
                                    startActivity(new Intent(getApplicationContext(), DashBoardStudent.class));
                                }
                                //else if(emailDomain.equals("gmail.com")) {
                                else if(emailDomain.equals("yousmail.com")) {
                                    startActivity(new Intent(getApplicationContext(), CheckAdminLevel.class));
                                }
                            }
                            else {
                                Toast.makeText(CheckUser.this, "To verify email need internet connection", Toast.LENGTH_SHORT).show();
                            }
                            finish();
                        }
                        else {
                            Toast.makeText(CheckUser.this, "Need internet for first time use \n Connect to internet and restart app.", Toast.LENGTH_SHORT).show();
                            startActivity(getIntent());
                        }
                    }
                });
        warning.show();
    }


}
//Change emails
//CheckUser     - 2*2 times
//AdminRegister - 1 time
//StuRegister   - 1 time