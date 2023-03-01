package com.smartsecureapp.Activity.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.smartsecureapp.Activity.fragment.EmergencyFragment;
import com.smartsecureapp.Activity.fragment.LocationFragment;
import com.smartsecureapp.Activity.fragment.MapsFragment;
import com.smartsecureapp.Activity.fragment.SettingFragment;
import com.smartsecureapp.Activity.fragment.TimerFragment;
import com.smartsecureapp.R;

public class MainActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(this);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.item_container, new EmergencyFragment()).commit();
        }

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.navigation_emergency:
                getSupportFragmentManager().beginTransaction().replace(R.id.item_container, new EmergencyFragment()).commit();
                break;
            case R.id.navigation_location:
                getSupportFragmentManager().beginTransaction().replace(R.id.item_container, new LocationFragment()).commit();
                break;
            case R.id.navigation_timer:
                getSupportFragmentManager().beginTransaction().replace(R.id.item_container, new TimerFragment()).commit();
                break;
            case R.id.navigation_setting:
                getSupportFragmentManager().beginTransaction().replace(R.id.item_container, new SettingFragment()).commit();
                break;

        }
        return true;
    }
}