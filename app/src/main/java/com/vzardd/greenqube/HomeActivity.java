package com.vzardd.greenqube;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.core.content.res.ResourcesCompat;
import androidx.viewpager2.widget.ViewPager2;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OSDeviceState;
import com.onesignal.OSNotificationReceivedEvent;
import com.onesignal.OneSignal;

import java.util.Objects;

public class HomeActivity extends AppCompatActivity {
    TabLayout tablayout;
    ViewPager2 viewpager;
    String[] titles;
    FirebaseAuth mAuth;
    DatabaseReference databaseReference;
    AlertDialog.Builder builder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setTitleBar();
        initViews();
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        storeTokenInDatabase();
        titles = new String[]{"Chats", "Groups", "Users"};

        //Setting Viewpager with Tablayout

        viewpager.setAdapter(new PagesAdapter(this));
        TabLayoutMediator mediator = new TabLayoutMediator(tablayout, viewpager, true, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setText(titles[position]);
            }
        });
        mediator.attach();

    }

    //On Start
    @Override
    protected void onStart() {
        super.onStart();
        verifyUserExistance();
    }

    //Setting Title bar
    private void setTitleBar() {
        ActionBar actionbar = getSupportActionBar();
        TextView title = new TextView(this);
        title.setText("Greenqube");
        title.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT));
        title.setTextSize(23);
        title.setTypeface(ResourcesCompat.getFont(this,R.font.audiowide));
        title.setTextColor(Color.parseColor("#FFFFFF"));
        actionbar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionbar.setCustomView(title);
        actionbar.setElevation(0);
    }

    //initializing views
    public void initViews()
    {
        tablayout = findViewById(R.id.tablayout);
        viewpager = findViewById(R.id.viewpager);
    }

    //Options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu,menu);
        return true;
    }

    //To store user token in database
    public void storeTokenInDatabase()
    {
        OSDeviceState deviceState = OneSignal.getDeviceState();
        String userId = deviceState.getUserId();
        databaseReference.child("Tokens").child(mAuth.getCurrentUser().getUid()).setValue(userId)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        OneSignal.disablePush(false);
                    }
                });
    }

    //Options Item Onclick listener
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.friends:
                Intent intent3 = new Intent(HomeActivity.this,FriendsActivity.class);
                startActivity(intent3);
            break;
            case R.id.my_profile: goToProfileActivity(1);
            break;
            case R.id.logout:
                OneSignal.disablePush(true);
                deleteTokenInDatabase(mAuth.getCurrentUser().getUid());
                SplashActivity.auth.signOut();
                Intent intent = new Intent(HomeActivity.this,LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            break;
            case R.id.settings:
                Intent intent1 = new Intent(HomeActivity.this,SettingsActivity.class);
                startActivity(intent1);
            break;
        }
        return true;
    }

    //Deleting user token from database
    public void deleteTokenInDatabase(String uid)
    {
        databaseReference.child("Tokens").child(uid).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                });
    }

    //Verifying users existance
    public void verifyUserExistance()
    {
        databaseReference.child("Users").child(mAuth.getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(!snapshot.child("name").exists())
                        {
                            builder = new AlertDialog.Builder(HomeActivity.this);
                            builder.setTitle("Complete Your Profile First!");
                            builder.setMessage("Just One Step Ahead!");
                            builder.setPositiveButton("Let's Go!", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                    goToProfileActivity(0);
                                }
                            });
                            builder.setCancelable(false);
                            builder.show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(HomeActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    //Go to Profile activity
    public void goToProfileActivity(int i)
    {
        Intent intent = new Intent(HomeActivity.this,ProfileActivity.class);
        intent.putExtra("from",i);
        startActivity(intent);
    }

}