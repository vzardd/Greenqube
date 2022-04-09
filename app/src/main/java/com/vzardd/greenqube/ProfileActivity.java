package com.vzardd.greenqube;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {
    ImageView ivAvatar;
    TextView tvRemoveDp;
    EditText etFullName,etBio;
    Button btnSaveProfile;
    FirebaseAuth mAuth;
    DatabaseReference dbRef;
    StorageReference storageRef;
    ProgressDialog dialog;
    boolean flag;
    Uri imageUri;
    final int PICK_IMAGE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().setTitle("My Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initViews();
        flag=false;
        tvRemoveDp.setVisibility(View.GONE);
        ifProfileExists();
        //To remove existing dp
        tvRemoveDp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Delete Avatar in Database and Storage here
                storageRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("avatar.jpg")
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(ProfileActivity.this, "Image removed successfully!", Toast.LENGTH_SHORT).show();
                                tvRemoveDp.setVisibility(View.GONE);
                                flag = false;
                                ivAvatar.setImageResource(R.mipmap.profileavatar);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                ivAvatar.setImageResource(R.mipmap.profileavatar);
                                tvRemoveDp.setVisibility(View.GONE);
                                flag=false;
                            }
                        });
            }
        });

        //choosing Avatar
        ivAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseAvatar();
            }
        });

        //Saving Changes
        btnSaveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveProfile();
            }
        });
    }

    //Options menu
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home)
        {
            ProfileActivity.this.finish();
        }
        return true;
    }

    // Initializing Views
    public void initViews()
    {
        ivAvatar = findViewById(R.id.ivProfilePic);
        tvRemoveDp = findViewById(R.id.tvRemoveDp);
        etFullName = findViewById(R.id.etFullName);
        etBio = findViewById(R.id.etBio);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        storageRef = FirebaseStorage.getInstance().getReference();
    }

    //If Profile Exists Already
    public void ifProfileExists()
    {
        int from = getIntent().getIntExtra("from",0);
        if(from==1)
        {
            dbRef.child("Users").child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.child("name").exists())
                    {
                        etFullName.setText((CharSequence) snapshot.child("name").getValue());
                    }
                    if (snapshot.child("bio").exists())
                    {
                        etBio.setText((CharSequence) snapshot.child("bio").getValue());
                    }
                    try {
                        dialog = new ProgressDialog(ProfileActivity.this);
                        dialog.setMessage("Please wait...");
                        dialog.setCancelable(false);
                        dialog.show();
                    } catch (Exception e) { e.printStackTrace(); }
                    storageRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("avatar.jpg")
                            .getDownloadUrl()
                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    imageUri = uri;
                                    try {
                                        Glide.with(ProfileActivity.this).load(uri.toString()).into(ivAvatar);
                                    } catch (Exception e) { e.printStackTrace(); }
                                    dialog.dismiss();
                                    tvRemoveDp.setVisibility(View.VISIBLE);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    dialog.dismiss();
                                }
                            });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ProfileActivity.this, "Something went wrong! Check your internet connection and Try again.", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    //Choosing Profile Picture
    public void chooseAvatar()
    {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent,PICK_IMAGE);
    }

    //On Activity Result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data!=null)
        {
            imageUri = data.getData();
            ivAvatar.setImageURI(imageUri);
            flag = true;
            tvRemoveDp.setVisibility(View.VISIBLE);
        }
    }

    // Saving Profile to database
    public void saveProfile()
    {
        if(etFullName.getText().toString().isEmpty())
        {
            etFullName.setError("Full Name cannot be empty!");
            return;
        }
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait...");
        dialog.setCancelable(false);
        dialog.show();
        String name = etFullName.getText().toString().trim();
        String bio = etBio.getText().toString().trim();
        HashMap<String,Object> user = new HashMap<String, Object>();
        user.put("uid",mAuth.getCurrentUser().getUid());
        user.put("name",name);
        user.put("bio",bio);
        if(flag) {
            storageRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("avatar.jpg").putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        }
                    });
        }
        dbRef.child("Users").child(mAuth.getCurrentUser().getUid()).setValue(user);
        dialog.dismiss();
        Toast.makeText(ProfileActivity.this, "Changes Saved!", Toast.LENGTH_SHORT).show();
        ProfileActivity.this.finish();
    }
}