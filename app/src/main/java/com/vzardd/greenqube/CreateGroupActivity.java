package com.vzardd.greenqube;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class CreateGroupActivity extends AppCompatActivity {

    ImageView ivGroupDp;
    TextView tvRemoveGroupDp;
    EditText etGroupName;
    Button btnCreateGroup;
    Uri imageUri;
    DatabaseReference rootRef;
    FirebaseAuth mAuth;
    StorageReference storageRef;
    final int PICK_IMAGE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        getSupportActionBar().setTitle("Create Group");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initViews();
        ivGroupDp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,PICK_IMAGE);
            }
        });

        tvRemoveGroupDp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageUri = null;
                tvRemoveGroupDp.setVisibility(View.INVISIBLE);
                ivGroupDp.setImageResource(R.mipmap.ic_group);
            }
        });

        btnCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(etGroupName.getText().toString().trim().isEmpty())
                {
                    etGroupName.setError("Group name can't be empty!");
                }
                else {
                    createGroupInFirebase();
                }
            }
        });
    }

    //To create group in firebase
    public void createGroupInFirebase() {
        ProgressDialog dialog = new ProgressDialog(CreateGroupActivity.this);
        dialog.setMessage("Please wait...");
        dialog.setCancelable(false);
        dialog.show();
        String groupName = etGroupName.getText().toString().trim();
        String key = rootRef.child("GroupInfo").push().getKey();
        rootRef.child("GroupInfo").child(key).setValue(groupName)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        HashMap<String,Object> group = new HashMap<String, Object>();
                        group.put("key",key);
                        group.put("lastmessage","");
                        group.put("timestamp", ServerValue.TIMESTAMP);
                        rootRef.child("Groups").child(mAuth.getCurrentUser().getUid()).push().setValue(group)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        if(imageUri!=null)
                                        {
                                            storageRef.child("Groups").child(key+".jpg").putFile(imageUri)
                                                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                        @Override
                                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                            HashMap<String,Object> admin = new HashMap<String, Object>();
                                                            admin.put("uid",mAuth.getCurrentUser().getUid());
                                                            admin.put("admin",true);
                                                            rootRef.child("Groupmembers").child(key).push().setValue(admin)
                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            Toast.makeText(CreateGroupActivity.this, "Group Created successfully!", Toast.LENGTH_SHORT).show();
                                                                            dialog.dismiss();
                                                                            finish();
                                                                        }
                                                                    });
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Toast.makeText(CreateGroupActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                                            dialog.dismiss();
                                                        }
                                                    });
                                        }
                                        else {
                                            Toast.makeText(CreateGroupActivity.this, "Group Created successfully!", Toast.LENGTH_SHORT).show();
                                            HashMap<String,Object> admin = new HashMap<String, Object>();
                                            admin.put("uid",mAuth.getCurrentUser().getUid());
                                            admin.put("admin",true);
                                            rootRef.child("Groupmembers").child(key).push().setValue(admin)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            dialog.dismiss();
                                                            finish();
                                                        }
                                                    });
                                        }
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CreateGroupActivity.this, "Something went wrong! Check your internet connection and try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //Initializing views
    public void initViews() {
        ivGroupDp = findViewById(R.id.ivGroupDp);
        tvRemoveGroupDp = findViewById(R.id.tvRemoveGroupDp);
        tvRemoveGroupDp.setVisibility(View.INVISIBLE);
        etGroupName = findViewById(R.id.etGroupName);
        btnCreateGroup = findViewById(R.id.btnCreateGroup);
        imageUri = null;
        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        storageRef = FirebaseStorage.getInstance().getReference();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home)
        {
            onBackPressed();
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == PICK_IMAGE && data!=null)
        {
            imageUri = data.getData();
            Glide.with(CreateGroupActivity.this).load(imageUri.toString()).into(ivGroupDp);
            tvRemoveGroupDp.setVisibility(View.VISIBLE);
        }
    }
}