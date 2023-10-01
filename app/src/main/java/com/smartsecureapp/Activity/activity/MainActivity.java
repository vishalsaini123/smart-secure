package com.smartsecureapp.Activity.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

    TimerFragment timerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (timerFragment==null)
        {
            timerFragment = new TimerFragment();
        }

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(this);
        if (getIntent().hasExtra("location")) {
            Bundle bundle = new Bundle();
            bundle.putString("location",getIntent().getStringExtra("location"));
            Fragment fragment = new LocationFragment();
            fragment.setArguments(bundle);
            bottomNavigationView.setSelectedItemId(R.id.navigation_location);
            getSupportFragmentManager().beginTransaction().replace(R.id.item_container, fragment).commit();
        }
        else {
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.item_container, new EmergencyFragment()).commit();
            }
        }



    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle extras = intent.getExtras();
        if (extras!=null)
        {
            Log.e("seeee","e"+extras.getString("timer"));

            if (extras.containsKey("location")) {
                bottomNavigationView.setSelectedItemId(R.id.navigation_location);
                Bundle bundle = new Bundle();
                bundle.putString("location",extras.getString("location"));
                Fragment fragment = new LocationFragment();
                fragment.setArguments(bundle);
                getSupportFragmentManager().beginTransaction().replace(R.id.item_container, fragment).commit();
            }
            if (extras.containsKey("timer")) {
                bottomNavigationView.setSelectedItemId(R.id.navigation_timer);
                Fragment fragment = new TimerFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.item_container, fragment).commit();
            }
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
                getSupportFragmentManager().beginTransaction().replace(R.id.item_container, timerFragment).commit();
                break;
            case R.id.navigation_setting:
                getSupportFragmentManager().beginTransaction().replace(R.id.item_container, new SettingFragment()).commit();
                break;

        }
        return true;
    }
}