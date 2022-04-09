package com.vzardd.greenqube.chatclasses;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.vzardd.greenqube.FriendClass;
import com.vzardd.greenqube.R;

public class ChooseChat extends AppCompatActivity {
    TextView tvFriendsEmpty;
    RecyclerView rvChooseFriendsList;
    DatabaseReference rootRef;
    FirebaseAuth mAuth;
    StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_chat);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().setTitle("Choose a friend...");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initViews();
        checkIfFriendsExist();

    }

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
                holder.friendBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(ChooseChat.this,ChatScreenActivity.class);
                        intent.putExtra("uid",model.getUid());
                        startActivity(intent);
                        finish();
                    }
                });
            }

            @NonNull
            @Override
            public ChooseFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.choose_friend_row_layout,parent,false);
                return new ChooseFriendViewHolder(v);
            }
        };
        rvChooseFriendsList.setAdapter(chooseFriendsAdapter);
        chooseFriendsAdapter.startListening();
    }

    //Initializing views
    public void initViews()
    {
        rvChooseFriendsList = findViewById(R.id.rvChooseFriendsList);
        rvChooseFriendsList.setLayoutManager(new LinearLayoutManager(ChooseChat.this));
        rvChooseFriendsList.setHasFixedSize(true);
        tvFriendsEmpty = findViewById(R.id.tvFriendsEmpty);
        rootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
    }

    //On Options Item selected
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home)
        {
            onBackPressed();
        }
        return true;
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

    //Viewholder for friends recycler adapter
    public static class ChooseFriendViewHolder extends RecyclerView.ViewHolder{
        ImageView ivRowDp;
        TextView tvFriendName;
        LinearLayout friendBox;

        public ChooseFriendViewHolder(@NonNull View itemView) {
            super(itemView);
            ivRowDp = itemView.findViewById(R.id.ivChooseDp);
            tvFriendName = itemView.findViewById(R.id.tvChooseName);
            friendBox = itemView.findViewById(R.id.llChooseFriendBox);
        }
    }

    //Setting row views
    public void setRowViews(ChooseFriendViewHolder holder,String uid)
    {
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
                                Glide.with(ChooseChat.this).load(uri.toString()).into(holder.ivRowDp);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                holder.ivRowDp.setImageResource(R.mipmap.profileavatar);
                            }
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}