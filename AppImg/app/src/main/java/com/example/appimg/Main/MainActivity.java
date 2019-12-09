package com.example.appimg.Main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.example.appimg.ImagesFragment.ImageFragment;
import com.example.appimg.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView mainNav;
    private FrameLayout mainFragment;
    ActionBar actionBar;

    private ImageFragment imgFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainFragment = (FrameLayout) findViewById(R.id.activity_main_fl_container);
        mainNav = (BottomNavigationView) findViewById(R.id.activity_main_bnv_nav);

        imgFragment = new ImageFragment();
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setLogo(R.drawable.ic_img_toolbar);
        actionBar.setDisplayUseLogoEnabled(true);
        setTitle(" Ảnh");
        setFragment(imgFragment);

        mainNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                switch (menuItem.getItemId()){

                    case R.id.nav_img:
                        actionBar.setDisplayShowHomeEnabled(true);
                        actionBar.setLogo(R.drawable.ic_img_toolbar);
                        actionBar.setDisplayUseLogoEnabled(true);
                        setTitle(" Ảnh");
                        setFragment(imgFragment);
                        return true;

                    case R.id.nav_album:
                        actionBar.setDisplayShowHomeEnabled(true);
                        actionBar.setLogo(R.drawable.ic_fol_img_toolbar);
                        actionBar.setDisplayUseLogoEnabled(true);
                        setTitle(" Album");
                        return true;

                    case  R.id.nav_time:
                        actionBar.setDisplayShowHomeEnabled(true);
                        actionBar.setLogo(R.drawable.ic_time_toolbar);
                        actionBar.setDisplayUseLogoEnabled(true);
                        setTitle(" TimeLine");
                        return true;

                    default:
                        return false;
                }
            }
        });
    }

    private void setFragment (Fragment fragment){

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.activity_main_fl_container, fragment);
        fragmentTransaction.commit();
    }
}
