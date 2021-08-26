package com.example.p7_stuinfomanageforhall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class Base extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    Toolbar baseToolbar;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    NavigationView navView;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    FirebaseUser fUser;
    Button createNewB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        baseToolbar=findViewById(R.id.baseToolbar);
        setSupportActionBar(baseToolbar);
        baseToolbar.setTitleTextColor(ContextCompat.getColor(getApplicationContext(),R.color.white));
        createNewB=findViewById(R.id.createNewB);
        fAuth=FirebaseAuth.getInstance();
        fStore=FirebaseFirestore.getInstance();
        fUser=fAuth.getCurrentUser();

        navView=findViewById(R.id.navView);
        drawerLayout=findViewById(R.id.drawerLayout);

        navView.setNavigationItemSelectedListener(this);
        actionBarDrawerToggle=new ActionBarDrawerToggle(this,drawerLayout,baseToolbar,R.string.open, R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        actionBarDrawerToggle.syncState();

        /*View headerView=navView.getHeaderView(0);
        TextView userName=headerView.findViewById(R.id.userNameNavHeader);
        TextView userEmail=headerView.findViewById(R.id.userEmailNavHeader);
        userName.setText(fUser.getDisplayName());
        userEmail.setText(fUser.getEmail());*/
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);
        switch (item.getItemId()){
            case R.id.uploadWriteStuDataNavMSA:
                startActivity(new Intent(getApplicationContext(),UploadWriteStuData.class));
                break;
            case R.id.dashboardNavMSA:
                //startActivity(new Intent(getApplicationContext(),DashBoardSuperAdmin.class));
                break;
            case R.id.hallStatNavMSA:
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                break;
            case R.id.createHallNavMSA:
                //startActivity(new Intent(getApplicationContext(),Halls.class));
                break;
            case R.id.createFloorNavMSA:
                //startActivity(new Intent(getApplicationContext(),Floors.class));
                break;
            case R.id.createRoomNavMSA:
                //startActivity(new Intent(getApplicationContext(),Rooms.class));
                break;
            case R.id.assignHallAdminNavMSA:
                break;
            case R.id.removeHallAdminNavMSA:
                break;
            case R.id.assignSuperAdminNavMSA:
                break;
            case R.id.logoutNavMSA:
                break;
            case R.id.createSeatNavMHA:
                //startActivity(new Intent(getApplicationContext(),Seats.class));
                break;
            case R.id.dataManageNavMSA:
                //startActivity(new Intent(getApplicationContext(), Management.class));
                break;

        }
        return false;
    }
}