package com.vzardd.greenqube;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class SettingsActivity extends AppCompatActivity {
    LinearLayout aboutUs,rateUs,inviteFriend,helpSupport, deleteMyAccount;
    FirebaseAuth mAuth;
    DatabaseReference dbRef;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        initViews();
        aboutUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.greenqubeglobal.com"));
                startActivity(intent);
            }
        });

        rateUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse("https://play.google.com/store/apps/details?id=com.vzardd.greenqube"));
                startActivity(intent);
            }
        });

        inviteFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT,"You're invited to join GreenQube. Join our community now!\nhttps://play.google.com/store/apps/details?id=com.vzardd.greenqube");
                startActivity(intent);
            }
        });

        helpSupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SENDTO,Uri.parse("mailto:info@greenqube.it"));
                startActivity(intent);
            }
        });

        deleteMyAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                accountDeletion();
            }
        });
    }

    // Options menu selected
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home)
        {
            onBackPressed();
        }
        return true;
    }

    // Initializing Views
    public void initViews()
    {
        aboutUs = findViewById(R.id.llAboutUs);
        rateUs = findViewById(R.id.llRateUs);
        inviteFriend = findViewById(R.id.llInvite);
        helpSupport = findViewById(R.id.llHelp);
        deleteMyAccount = findViewById(R.id.llDeleteAccount);
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();
    }

    // Account deletion
    public void accountDeletion()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
        builder.setCancelable(false);
        builder.setTitle("Are you sure?");
        builder.setMessage("Your account will be deleted permanently! All your data will be lost.");
        builder.setIcon(R.drawable.ic_delete);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.setPositiveButton("Confirm!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                deleteAccount();
            }
        });
        builder.show();
    }

    // Actual deletion of account
    public void deleteAccount()
    {
        ProgressDialog dialog = new ProgressDialog(SettingsActivity.this);
        dialog.setTitle("Please wait!");
        dialog.setMessage("Your account is under deletion..");
        dialog.show();
        mAuth.getCurrentUser().delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            dialog.dismiss();
                            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        } else {
                            dialog.dismiss();
                            Toast.makeText(SettingsActivity.this, "Failed! "+task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}