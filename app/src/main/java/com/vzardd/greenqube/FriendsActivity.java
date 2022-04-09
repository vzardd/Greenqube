package com.vzardd.greenqube;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TableLayout;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.database.core.view.View;
import com.vzardd.greenqube.friendsfragments.FriendsPagerAdapter;

public class FriendsActivity extends AppCompatActivity {

    ViewPager2 friendsPager;
    TabLayout tabLayout;
    String [] pageTitles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        getSupportActionBar().setTitle("My Friends");
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        initViews();
        friendsPager.setAdapter(new FriendsPagerAdapter(this));
        TabLayoutMediator mediator = new TabLayoutMediator(tabLayout, friendsPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setText(pageTitles[position]);
            }
        });
        mediator.attach();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home)
        {
            onBackPressed();
        }
        return true;
    }

    //Initializing Views
    public void initViews()
    {
        friendsPager = findViewById(R.id.friendsPager);
        tabLayout = findViewById(R.id.friendsTab);
        pageTitles = new String[]{"Friends","Requests"};
    }
}