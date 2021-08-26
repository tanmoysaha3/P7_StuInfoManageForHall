package com.example.p7_stuinfomanageforhall;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

public class DashBoardSuperAdmin extends Base {

    LayoutInflater inflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        inflater=(LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView=inflater.inflate(R.layout.activity_dash_board_super_admin,null,false);
        drawerLayout.addView(contentView,0);

        navView.getMenu().clear();
        navView.inflateMenu(R.menu.nav_menu_super);
    }
}