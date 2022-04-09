package com.vzardd.greenqube.friendsfragments;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.vzardd.greenqube.homefragments.UsersFrag;

import java.util.HashMap;

public class RequestsFrag extends Fragment {
    RecyclerView rvRequests;
    DatabaseReference dbRef,userRef,friendsRef;
    StorageReference storageRef;
    FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.requests_fragment,container,false);
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(mAuth.getCurrentUser().getUid());
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        storageRef = FirebaseStorage.getInstance().getReference().child("Users");
        rvRequests = v.findViewById(R.id.rvRequestsList);
        rvRequests.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        rvRequests.setHasFixedSize(true);
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<FriendClass> options = new FirebaseRecyclerOptions.Builder<FriendClass>()
                .setQuery(dbRef.orderByChild("status").equalTo("received"),FriendClass.class)
                .build();
        FirebaseRecyclerAdapter<FriendClass,RequestViewHolder> adapter = new FirebaseRecyclerAdapter<FriendClass, RequestViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull RequestViewHolder holder, int position, @NonNull FriendClass model) {
                String uid = model.getUid();
                userRef.child(uid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        holder.tvUsername.setText((CharSequence) snapshot.child("name").getValue());
                        storageRef.child(uid).child("avatar.jpg").getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Glide.with(RequestsFrag.this.getActivity()).load(uri.toString()).into(holder.ivRequestDp);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        holder.ivRequestDp.setImageResource(R.mipmap.profileavatar);
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                holder.requestBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        viewProfile(uid);
                    }
                });
                holder.ivCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        declineRequest(uid,null);
                    }
                });

                holder.ivAccept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        acceptRequest(uid,null);
                    }
                });
            }

            @NonNull
            @Override
            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.request_row_layout,parent,false);
                return new RequestViewHolder(v);
            }
        };
        rvRequests.setAdapter(adapter);
        adapter.startListening();
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder{
        LinearLayout requestBox;
        ImageView ivRequestDp,ivAccept,ivCancel;
        TextView tvUsername;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            requestBox = itemView.findViewById(R.id.llRequestBox);
            ivRequestDp = itemView.findViewById(R.id.ivRequestDp);
            ivAccept = itemView.findViewById(R.id.ivAccept);
            ivCancel = itemView.findViewById(R.id.ivReject);
            tvUsername = itemView.findViewById(R.id.tvRequestName);
        }
    }

    //View Profile
    public void viewProfile(String uid)
    {
        View profileView = LayoutInflater.from(RequestsFrag.this.getActivity()).inflate(R.layout.view_profile,null);

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
        userRef.child(uid).addValueEventListener(new ValueEventListener() {
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
                        Glide.with(RequestsFrag.this.getActivity()).load(uri.toString()).into(ivProfileDp);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        ivProfileDp.setImageResource(R.mipmap.profileavatar);
                    }
                });
        btnMessage.setVisibility(View.GONE);
        btnAcceptReq.setVisibility(View.VISIBLE);
        btnDeclineReq.setVisibility(View.VISIBLE);
        btnAddFriend.setVisibility(View.GONE);

        //Showing Dialog

        AlertDialog.Builder builder = new AlertDialog.Builder(RequestsFrag.this.getActivity());
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


        btnAcceptReq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                acceptRequest(uid,myAlert);
            }
        });

        btnDeclineReq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                declineRequest(uid,myAlert);
            }
        });

    }

    //Accepting Friend Request
    public void acceptRequest(String uid,AlertDialog dialog)
    {
        HashMap<String,Object> friendStatus = new HashMap<String, Object>();
        friendStatus.put("status","friends");
        friendStatus.put("uid",mAuth.getCurrentUser().getUid());
        friendsRef.child(uid).child(mAuth.getCurrentUser().getUid()).setValue(friendStatus)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        HashMap<String,Object> friendStatus = new HashMap<String, Object>();
                        friendStatus.put("status","friends");
                        friendStatus.put("uid",uid);
                        friendsRef.child(mAuth.getCurrentUser().getUid()).child(uid).setValue(friendStatus)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        if(dialog!=null) {
                                            dialog.dismiss();
                                        }
                                    }
                                });
                    }
                });
    }

    //Declining Friend Request
    public void declineRequest(String uid,AlertDialog dialog)
    {
        friendsRef.child(uid).child(mAuth.getCurrentUser().getUid()).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        friendsRef.child(mAuth.getCurrentUser().getUid()).child(uid).removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        if(dialog!=null) {
                                            dialog.dismiss();
                                        }
                                    }
                                });
                    }
                });
    }
}
