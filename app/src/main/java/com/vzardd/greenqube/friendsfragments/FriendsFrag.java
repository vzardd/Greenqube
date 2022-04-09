package com.vzardd.greenqube.friendsfragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.vzardd.greenqube.UserClass;
import com.vzardd.greenqube.chatclasses.ChatScreenActivity;

public class FriendsFrag extends Fragment {
    RecyclerView rvFriendsList;
    DatabaseReference friendsRef,dbRef;
    StorageReference storageRef;
    FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.friends_fragment,container,false);
        mAuth = FirebaseAuth.getInstance();
        rvFriendsList = v.findViewById(R.id.rvFriendsList);
        rvFriendsList.setLayoutManager(new LinearLayoutManager(FriendsFrag.this.getActivity()));
        rvFriendsList.setHasFixedSize(true);
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(mAuth.getCurrentUser().getUid());
        dbRef = FirebaseDatabase.getInstance().getReference().child("Users");
        storageRef = FirebaseStorage.getInstance().getReference().child("Users");
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<FriendClass> options = new FirebaseRecyclerOptions.Builder<FriendClass>()
                .setQuery(friendsRef.orderByChild("status").equalTo("friends"),FriendClass.class)
                .build();
        FirebaseRecyclerAdapter<FriendClass,Viewholder> adapter =
                new FirebaseRecyclerAdapter<FriendClass, Viewholder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull Viewholder holder, int position, @NonNull FriendClass model) {
                        String uid = model.getUid();
                        dbRef.child(uid).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                holder.tvFriendName.setText((CharSequence) snapshot.child("name").getValue());
                                storageRef.child(uid).child("avatar.jpg").getDownloadUrl()
                                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                Glide.with(FriendsFrag.this.getActivity()).load(uri.toString()).into(holder.ivFriendDp);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                holder.ivFriendDp.setImageResource(R.mipmap.profileavatar);
                                            }
                                        });
                                holder.friendsBox.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        viewProfile(uid);
                                    }
                                });
                                holder.btnRemove.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        removeFriend(uid);
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.friends_row_layout,parent,false);
                        return new Viewholder(v);
                    }
                };
        rvFriendsList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class Viewholder extends RecyclerView.ViewHolder{
        LinearLayout friendsBox;
        ImageView ivFriendDp;
        TextView tvFriendName;
        Button btnRemove;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            friendsBox = itemView.findViewById(R.id.llFriendsBox);
            ivFriendDp = itemView.findViewById(R.id.ivFriendsDp);
            tvFriendName = itemView.findViewById(R.id.tvFriendName);
            btnRemove = itemView.findViewById(R.id.btnRemoveFriend);
        }
    }

    //View Profile
    public void viewProfile(String uid)
    {
        View profileView = LayoutInflater.from(FriendsFrag.this.getActivity()).inflate(R.layout.view_profile,null);

        //Initializing Views

        ImageView ivBack,ivProfileDp;
        ivBack = profileView.findViewById(R.id.ivBack);
        ivProfileDp = profileView.findViewById(R.id.ivViewDp);
        TextView tvName, tvBio;
        tvName = profileView.findViewById(R.id.tvViewUsername);
        tvBio = profileView.findViewById(R.id.tvViewBio);
        Button btnAddFriend,btnAcceptReq,btnDeclineReq,btnMessage;
        btnAddFriend = profileView.findViewById(R.id.btnAddFriend);
        btnAcceptReq = profileView.findViewById(R.id.btnAcceptReq);
        btnDeclineReq = profileView.findViewById(R.id.btnDeclineReq);
        btnMessage = profileView.findViewById(R.id.btnViewMessage);

        //Setting Values
        dbRef.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tvName.setText((CharSequence) snapshot.child("name").getValue());
                String bio = (String) snapshot.child("bio").getValue();
                if(bio.length()!=0)
                {
                    tvBio.setText(bio);
                }
                else {
                    tvBio.setText("Hey there!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        storageRef.child(uid).child("avatar.jpg")
                .getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(FriendsFrag.this.getActivity()).load(uri.toString()).into(ivProfileDp);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        ivProfileDp.setImageResource(R.mipmap.profileavatar);
                    }
                });
        btnMessage.setVisibility(View.VISIBLE);
        btnAcceptReq.setVisibility(View.GONE);
        btnDeclineReq.setVisibility(View.GONE);
        btnAddFriend.setVisibility(View.GONE);

        //Showing Dialog

        AlertDialog.Builder builder = new AlertDialog.Builder(FriendsFrag.this.getActivity());
        builder.setCancelable(false);
        builder.setView(profileView);
        AlertDialog myAlert = builder.create();
        myAlert.show();

        //On click listeners for buttons

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myAlert.dismiss();
            }
        });


        btnMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Message here
                myAlert.dismiss();
                Intent intent = new Intent(FriendsFrag.this.getActivity(), ChatScreenActivity.class);
                intent.putExtra("uid",uid);
                startActivity(intent);
            }
        });

    }
    public void removeFriend(String uid)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(FriendsFrag.this.getActivity());
        builder.setTitle("Are you sure?");
        builder.setMessage("Do you want to unfriend this user?");
        builder.setCancelable(false);
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                friendsRef.getParent().child(uid).child(mAuth.getCurrentUser().getUid()).removeValue()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                friendsRef.getParent().child(mAuth.getCurrentUser().getUid()).child(uid).removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                dialogInterface.dismiss();
                                            }
                                        });
                            }
                        });
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }
}
