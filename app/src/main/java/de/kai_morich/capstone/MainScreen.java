package de.kai_morich.capstone;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import de.kai_morich.simple_bluetooth_le_terminal.R;

public class MainScreen extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener {

    Fragment_map fragmentMap;
    Fragment_main fragmentMain;
    DevicesFragment fragment;
    Fragment_set fragmentSet;
    String user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fragmentMap = new Fragment_map();
        fragmentMain = new Fragment_main();
        fragmentSet = new Fragment_set();
        fragment = new DevicesFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, new DevicesFragment(), "devices").commit();
        getSupportFragmentManager().addOnBackStackChangedListener(this);

        Intent intent = getIntent();
        user = intent.getStringExtra("user");

        if (savedInstanceState == null){

            BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNav);

            bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.map:
                            FragmentManager manager = getSupportFragmentManager();
                            FragmentTransaction transaction = manager.beginTransaction();
                            Bundle bundle = new Bundle();
                            bundle.putString("user", user);
                            fragmentMap.setArguments(bundle);
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragment,  fragmentMap, "map").commit();

                            return true;
                        case R.id.main:
//                            getSupportFragmentManager().beginTransaction().replace(R.id.bottom_container, fragmentMain).commit();
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragment, new DevicesFragment(), "devices").commit();
                            return true;
                        case R.id.setting:
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragment,  fragmentSet, "setting").commit();
                            return true;
                    }
                    return false;
                }
            });
        }
        else
            onBackStackChanged();
    }

    @Override
    public void onBackStackChanged() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(getSupportFragmentManager().getBackStackEntryCount()>0);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
