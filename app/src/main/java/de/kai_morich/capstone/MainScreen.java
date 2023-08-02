package de.kai_morich.capstone;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import de.kai_morich.simple_bluetooth_le_terminal.R;

public class MainScreen extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener {

    Fragment_map fragmentMap;
    Fragment_main fragmentMain;
    DevicesFragment fragment;
    Fragment_set fragmentSet;

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
        if (savedInstanceState == null){

            BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNav);

            bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.map:
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
