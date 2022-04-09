package com.vzardd.greenqube.groupclasses;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.vzardd.greenqube.CreateGroupActivity;
import com.vzardd.greenqube.R;

public class EditGroupActivity extends AppCompatActivity {
    ImageView ivGroupDp;
    TextView tvRemoveGroupDp;
    EditText etGroupName;
    Button btnSaveGroup;
    Uri imageUri;
    DatabaseReference rootRef;
    FirebaseAuth mAuth;
    StorageReference storageRef;
    final int PICK_IMAGE = 3;
    String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_group);
        getSupportActionBar().setTitle("Edit Info");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initViews();
        setGroupDp();
        setGroupName();
        tvRemoveGroupDp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageUri = null;
                tvRemoveGroupDp.setVisibility(View.INVISIBLE);
                ivGroupDp.setImageResource(R.mipmap.ic_group);
            }
        });

        ivGroupDp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,PICK_IMAGE);
            }
        });

        btnSaveGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(etGroupName.getText().toString().trim().isEmpty())
                {
                    etGroupName.setError("Group name can't be empty!");
                }
                else {
                    saveGroup();
                }
            }
        });
    }

    private void saveGroup() {
        rootRef.child("GroupInfo").child(key).setValue(etGroupName.getText().toString().trim())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if(imageUri==null)
                        {
                            removeDp();
                        }
                        else {
                            saveDp();
                        }
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditGroupActivity.this, "Something went wrong! Check your internet connection and try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveDp() {
        storageRef.child("Groups").child(key+".jpg").putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(EditGroupActivity.this, "Changes saved!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void removeDp() {
        storageRef.child("Groups").child(key+".jpg").delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(EditGroupActivity.this, "Changes saved!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setGroupName() {
        rootRef.child("GroupInfo").child(key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && snapshot.getValue()!=null)
                {
                    etGroupName.setText(snapshot.getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setGroupDp() {
        storageRef.child("Groups").child(key+".jpg").getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        imageUri = uri;
                        Glide.with(EditGroupActivity.this).load(uri.toString()).into(ivGroupDp);
                        tvRemoveGroupDp.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        ivGroupDp.setImageResource(R.mipmap.ic_group);
                        tvRemoveGroupDp.setVisibility(View.INVISIBLE);
                    }
                });
    }

    //Initializing views
    public void initViews() {
        ivGroupDp = findViewById(R.id.ivEditGroupDp);
        tvRemoveGroupDp = findViewById(R.id.tvRemoveEditGroupDp);
        tvRemoveGroupDp.setVisibility(View.INVISIBLE);
        etGroupName = findViewById(R.id.etEditGroupName);
        btnSaveGroup = findViewById(R.id.btnSaveGroup);
        imageUri = null;
        key = getIntent().getStringExtra("key");
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
            Glide.with(EditGroupActivity.this).load(imageUri.toString()).into(ivGroupDp);
            tvRemoveGroupDp.setVisibility(View.VISIBLE);
        }
    }
}