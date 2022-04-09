package com.vzardd.greenqube.groupclasses;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.vzardd.greenqube.FriendClass;
import com.vzardd.greenqube.R;
import com.vzardd.greenqube.chatclasses.ChatScreenActivity;
import com.vzardd.greenqube.chatclasses.ChooseChat;

import java.util.HashMap;

public class AddParticipantsActivity extends AppCompatActivity {

    TextView tvFriendsEmpty;
    RecyclerView rvChooseFriendsList;
    DatabaseReference rootRef;
    FirebaseAuth mAuth;
    StorageReference storageRef;
    String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_participants);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Add Participants");
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        initViews();
        checkIfFriendsExist();
    }

    //On Start


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<FriendClass> options = new FirebaseRecyclerOptions.Builder<FriendClass>()
                .setQuery(rootRef.child("Friends").child(mAuth.getCurrentUser().getUid()).orderByChild("status").equalTo("friends"),FriendClass.class)
                .build();
        FirebaseRecyclerAdapter<FriendClass,ChooseFriendViewHolder> chooseFriendsAdapter = new FirebaseRecyclerAdapter<FriendClass, ChooseFriendViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ChooseFriendViewHolder holder, int position, @NonNull FriendClass model) {
                setRowViews(holder,model.uid);
                holder.tvAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(holder.tvAdd.getText().toString().equals("ADD"))
                        {
                            addParticipantToGroup(holder,position,model);
                        }
                    }
                });
            }

            @NonNull
            @Override
            public ChooseFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.choose_participants_row_layout,parent,false);
                return new ChooseFriendViewHolder(v);
            }
        };
        rvChooseFriendsList.setAdapter(chooseFriendsAdapter);
        chooseFriendsAdapter.startListening();
    }

    private void addParticipantToGroup(ChooseFriendViewHolder holder, int position, FriendClass model) {
        ProgressDialog dialog = new ProgressDialog(AddParticipantsActivity.this);
        dialog.setMessage("Please wait...");
        dialog.show();
        HashMap<String,Object> member = new HashMap<String, Object>();
        member.put("uid",model.getUid());
        member.put("admin",false);
        rootRef.child("Groupmembers").child(key).push().setValue(member)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        HashMap<String,Object> group = new HashMap<String, Object>();
                        group.put("key",key);
                        group.put("lastmessage","You were added to this group.");
                        group.put("timestamp", ServerValue.TIMESTAMP);
                        rootRef.child("Groups").child(model.getUid()).push().setValue(group)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(AddParticipantsActivity.this, "Participant Added.", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                        holder.tvAdd.setText("ADDED!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        dialog.dismiss();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.dismiss();
                        Toast.makeText(AddParticipantsActivity.this, "Something went wrong! Check your internet connection and try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setRowViews(ChooseFriendViewHolder holder, String uid) {
        rootRef.child("Users").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = (String) snapshot.child("name").getValue();
                if(name!=null)
                {
                    holder.tvFriendName.setText(name);
                }
                storageRef.child("Users").child(uid).child("avatar.jpg").getDownloadUrl()
                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Glide.with(AddParticipantsActivity.this).load(uri.toString()).into(holder.ivRowDp);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                holder.ivRowDp.setImageResource(R.mipmap.profileavatar);
                            }
                        });
                rootRef.child("Groupmembers").child(key).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists() && snapshot.getValue()!=null)
                        {
                            boolean flag = true;
                            for(DataSnapshot snapshot1 : snapshot.getChildren())
                            {
                                if(snapshot1.child("uid").getValue().toString().equals(uid))
                                {
                                    holder.tvAdd.setText("ADDED!");
                                    flag = false;
                                    break;
                                }
                            }
                            if(flag)
                            {
                                holder.tvAdd.setText("ADD");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //Initializing views
    private void initViews() {
        rvChooseFriendsList = findViewById(R.id.rvChooseParticipantsList);
        rvChooseFriendsList.setLayoutManager(new LinearLayoutManager(AddParticipantsActivity.this));
        rvChooseFriendsList.setHasFixedSize(true);
        tvFriendsEmpty = findViewById(R.id.tvGroupFriendsEmpty);
        rootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
        key = getIntent().getStringExtra("key");
    }

    //Viewholder for friends recycler adapter
    public static class ChooseFriendViewHolder extends RecyclerView.ViewHolder{
        ImageView ivRowDp;
        TextView tvFriendName,tvAdd;

        public ChooseFriendViewHolder(@NonNull View itemView) {
            super(itemView);
            ivRowDp = itemView.findViewById(R.id.ivChooseMemDp);
            tvFriendName = itemView.findViewById(R.id.tvChooseMemName);
            tvAdd = itemView.findViewById(R.id.tvAddMember);
        }
    }

    //Check if the user have friends
    public void checkIfFriendsExist()
    {
        rootRef.child("Friends").child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    boolean flag=false;
                    for(DataSnapshot snapshot1 : snapshot.getChildren())
                    {
                        if(snapshot1.child("status").exists() && snapshot1.child("status").getValue().toString().equals("friends"))
                        {
                            tvFriendsEmpty.setVisibility(View.GONE);
                            rvChooseFriendsList.setVisibility(View.VISIBLE);
                            flag=true;
                            break;
                        }
                    }
                    if(!flag)
                    {
                        tvFriendsEmpty.setVisibility(View.VISIBLE);
                        rvChooseFriendsList.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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