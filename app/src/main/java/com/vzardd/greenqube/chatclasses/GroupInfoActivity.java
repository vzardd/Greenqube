package com.vzardd.greenqube.chatclasses;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.vzardd.greenqube.HomeActivity;
import com.vzardd.greenqube.R;
import com.vzardd.greenqube.friendsfragments.Participant;
import com.vzardd.greenqube.groupclasses.AddParticipantsActivity;
import com.vzardd.greenqube.groupclasses.EditGroupActivity;
import com.vzardd.greenqube.groupclasses.GroupChatActivity;

public class GroupInfoActivity extends AppCompatActivity {

    ImageView ivGroupDp,ivEdit;
    TextView tvGroupName, tvAddParticipants,tvExitGroup,tvDeleteGroup;
    RecyclerView rvParticipants;
    FirebaseAuth mAuth;
    StorageReference storageRef;
    DatabaseReference rootRef;
    boolean amIAdmin = false;
    String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Group Info");
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        initViews();
        setGroupIcon();
        setGroupName();
        ivEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupInfoActivity.this, EditGroupActivity.class);
                intent.putExtra("key",key);
                startActivity(intent);
            }
        });

        tvAddParticipants.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupInfoActivity.this, AddParticipantsActivity.class);
                intent.putExtra("key",key);
                startActivity(intent);
            }
        });

        tvExitGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exitGroup();
            }
        });

        tvDeleteGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(amIAdmin){
                    deleteGroup();
                }
                else {
                    Toast.makeText(GroupInfoActivity.this, "You don't have permission to delete this group!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("Are you sure, you want to delete this group?");
        builder.setMessage("All your data messages will be lost permanently");
        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.setPositiveButton("Delete anyway!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteGroupFromFirebase();
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    private void deleteGroupFromFirebase() {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait...");
        dialog.setCancelable(false);
        dialog.show();
        rootRef.child("Groupmembers").child(key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && snapshot.getValue()!=null)
                {
                    for (DataSnapshot snapshot1 : snapshot.getChildren())
                    {
                        String id = snapshot1.child("uid").getValue().toString();
                        rootRef.child("Groups").child(id).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists() && snapshot.getValue()!=null)
                                {
                                    for(DataSnapshot snapshot2 : snapshot.getChildren())
                                    {
                                        if(snapshot2.child("key").getValue().toString().equals(key))
                                        {
                                            String k = snapshot2.getKey();
                                            rootRef.child("Groups").child(id).child(k).removeValue()
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Log.d("Success!:",k);
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.d("Failed!",k);
                                                        }
                                                    });
                                            break;
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                    rootRef.child("Groupmembers").child(key).removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    rootRef.child("GroupInfo").child(key).removeValue()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    rootRef.child("Groupmessages").child(key).removeValue()
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    dialog.dismiss();
                                                                    Toast.makeText(GroupInfoActivity.this, "Group deleted successfully!", Toast.LENGTH_SHORT).show();
                                                                    Intent intent = new Intent(GroupInfoActivity.this,HomeActivity.class);
                                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                    startActivity(intent);
                                                                }
                                                            });
                                                }
                                            });
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GroupInfoActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }

    private void exitGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Are you sure?");
        builder.setMessage("Do you want to exit this group?");
        builder.setCancelable(false);
        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                exitGroupFromFirebase(mAuth.getCurrentUser().getUid());
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    private void exitGroupFromFirebase(String uid) {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait...");
        dialog.setCancelable(false);
        dialog.show();
        rootRef.child("Groupmembers").child(key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && snapshot.getValue()!=null)
                {
                    String k = null;
                    for(DataSnapshot snapshot1 : snapshot.getChildren())
                    {
                        if(snapshot1.child("uid").getValue().toString().equals(uid))
                        {
                            k = snapshot1.getKey();
                            break;
                        }
                    }
                    if(k!=null)
                    {
                        rootRef.child("Groupmembers").child(key).child(k).removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        rootRef.child("Groups").child(uid).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if(snapshot.exists() && snapshot.getValue()!=null)
                                                {
                                                    String k = null;
                                                    for(DataSnapshot snapshot1 : snapshot.getChildren())
                                                    {
                                                        if(snapshot1.child("key").getValue().toString().equals(key))
                                                        {
                                                            k = snapshot1.getKey();
                                                            break;
                                                        }
                                                    }
                                                    if(k!=null)
                                                    {
                                                        rootRef.child("Groups").child(uid).child(k).removeValue()
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        dialog.dismiss();
                                                                        Toast.makeText(GroupInfoActivity.this, "Removed Successfully!", Toast.LENGTH_SHORT).show();
                                                                        if(uid.equals(mAuth.getCurrentUser().getUid()))
                                                                        {
                                                                            Intent intent = new Intent(GroupInfoActivity.this, HomeActivity.class);
                                                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                            startActivity(intent);
                                                                        }
                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        dialog.dismiss();
                                                                        Toast.makeText(GroupInfoActivity.this, "Something went wrong! Check your internet connection and try again.", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        dialog.dismiss();
                                        Toast.makeText(GroupInfoActivity.this, "Something went wrong! Check your internet connection and try again.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        setGroupName();
        setGroupIcon();
        FirebaseRecyclerOptions<Participant> options = new FirebaseRecyclerOptions.Builder<Participant>()
                .setQuery(rootRef.child("Groupmembers").child(key),Participant.class)
                .build();
        FirebaseRecyclerAdapter<Participant,MemViewHolder> adapter = new FirebaseRecyclerAdapter<Participant, MemViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MemViewHolder holder, int position, @NonNull Participant model) {

                if(model.getUid().equals(mAuth.getCurrentUser().getUid()))
                {
                    holder.tvParticipantName.setText("You");
                    if(model.isAdmin())
                    {
                        amIAdmin = true;
                    }
                    else {
                        amIAdmin = false;
                    }
                }
                else {
                    setName(holder,model);
                }
                if(model.isAdmin())
                {
                    holder.tvAdmin.setVisibility(View.VISIBLE);
                }
                else {
                    holder.tvAdmin.setVisibility(View.GONE);
                }
                setUserDp(holder,model);

                holder.clParticipantBox.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        if(amIAdmin && !model.getUid().equals(mAuth.getCurrentUser().getUid()))
                        {
                            AlertDialog.Builder builder = new AlertDialog.Builder(GroupInfoActivity.this);
                            builder.setTitle("Are you sure?");
                            builder.setMessage("Do you want to remove this user from the group?");
                            builder.setCancelable(false);
                            builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });
                            builder.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    exitGroupFromFirebase(model.getUid());
                                    dialogInterface.dismiss();
                                }
                            });
                            builder.show();
                        }
                        return true;
                    }
                });
            }

            @NonNull
            @Override
            public MemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_participant_row_layout,parent,false);
                return new MemViewHolder(v);
            }
        };
        rvParticipants.setAdapter(adapter);
        adapter.startListening();
    }

    private void setUserDp(MemViewHolder holder, Participant model) {
        storageRef.child("Users").child(model.getUid()).child("avatar.jpg").getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(GroupInfoActivity.this).load(uri.toString()).into(holder.ivParticipantDp);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        holder.ivParticipantDp.setImageResource(R.mipmap.profileavatar);
                    }
                });
    }

    private void setName(MemViewHolder holder, Participant model) {
        rootRef.child("Users").child(model.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && snapshot.getValue()!=null)
                {
                    holder.tvParticipantName.setText(snapshot.child("name").getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static class MemViewHolder extends RecyclerView.ViewHolder{
        ImageView ivParticipantDp;
        TextView tvParticipantName,tvAdmin;
        ConstraintLayout clParticipantBox;

        public MemViewHolder(@NonNull View itemView) {
            super(itemView);
            ivParticipantDp = itemView.findViewById(R.id.ivParticipantDp);
            tvParticipantName = itemView.findViewById(R.id.tvParticipantName);
            tvAdmin = itemView.findViewById(R.id.tvAdmin);
            clParticipantBox = itemView.findViewById(R.id.clParticipantBox);
        }
    }

    public void setGroupName() {
        rootRef.child("GroupInfo").child(key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && snapshot.getValue()!=null)
                {
                    tvGroupName.setText(snapshot.getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void setGroupIcon() {
        storageRef.child("Groups").child(key+".jpg").getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(GroupInfoActivity.this).load(uri.toString()).into(ivGroupDp);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        ivGroupDp.setImageResource(R.mipmap.ic_group);
                    }
                });
    }

    public void initViews() {
        ivGroupDp = findViewById(R.id.ivGroupIcon);
        ivEdit = findViewById(R.id.ivEditGroupInfo);
        tvGroupName = findViewById(R.id.tvGroupInfoName);
        tvAddParticipants = findViewById(R.id.tvAddParticipants);
        tvExitGroup = findViewById(R.id.tvExit);
        tvDeleteGroup = findViewById(R.id.tvDeleteGroup);
        rvParticipants = findViewById(R.id.rvParticipants);
        rvParticipants.setLayoutManager(new LinearLayoutManager(this));
        rvParticipants.setHasFixedSize(true);
        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        key = getIntent().getStringExtra("key");
        storageRef = FirebaseStorage.getInstance().getReference();
    }

    //Options item selected
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home)
        {
            onBackPressed();
        }
        return true;
    }
}