package com.vzardd.greenqube.homefragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.vzardd.greenqube.ApplicationClass;
import com.vzardd.greenqube.R;
import com.vzardd.greenqube.UserClass;
import com.vzardd.greenqube.chatclasses.ChatScreenActivity;
import com.vzardd.greenqube.sendNotification;

import java.util.ArrayList;
import java.util.HashMap;

public class UsersFrag extends Fragment {
    DatabaseReference dbRef;
    StorageReference storageRef;
    DatabaseReference friendsRef;
    FirebaseAuth mAuth;
    RecyclerView rvUsersList;
    FloatingActionButton fabSearch;
    Query query;
    EditText etSearch;
    String userId,myname;
    String searchQuery;
    FirebaseRecyclerAdapter<UserClass,UserViewHolder> userAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.users_fragment,container,false);
        dbRef = FirebaseDatabase.getInstance().getReference().child("Users");
        storageRef = FirebaseStorage.getInstance().getReference().child("Users");
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        mAuth = FirebaseAuth.getInstance();
        fabSearch = v.findViewById(R.id.fabSearchUser);
        etSearch = v.findViewById(R.id.etSearchUser);
        searchQuery = "";
        findMyName();
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                searchQuery = etSearch.getText().toString();
                if(searchQuery.length()!=0) {
                    Query query = dbRef.orderByChild("name").startAt(searchQuery).endAt(searchQuery + "\uf8ff");
                    FirebaseRecyclerOptions<UserClass> options = new FirebaseRecyclerOptions.Builder<UserClass>()
                            .setQuery(query, UserClass.class)
                            .build();
                    userAdapter.updateOptions(options);
                }
                else {
                    Query query = dbRef.orderByChild("name");
                    FirebaseRecyclerOptions<UserClass> options = new FirebaseRecyclerOptions.Builder<UserClass>()
                            .setQuery(query, UserClass.class)
                            .build();
                    userAdapter.updateOptions(options);
                }
            }
        });
        fabSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchQuery = etSearch.getText().toString();
                Query query = dbRef.orderByChild("name").startAt(searchQuery).endAt(searchQuery+"\uf8ff");
                FirebaseRecyclerOptions<UserClass> options = new FirebaseRecyclerOptions.Builder<UserClass>()
                        .setQuery(query, UserClass.class)
                        .build();
                userAdapter.updateOptions(options);
            }
        });
        rvUsersList = v.findViewById(R.id.rvUsersList);
        rvUsersList.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        rvUsersList.setHasFixedSize(true);
        return v;
    }

    public void findMyName() {
        dbRef.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && snapshot.getValue()!=null)
                {
                    myname = (String) snapshot.child("name").getValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<UserClass> options = new FirebaseRecyclerOptions.Builder<UserClass>()
                .setQuery(dbRef, UserClass.class)
                .build();
        userAdapter =
                new FirebaseRecyclerAdapter<UserClass, UserViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull UserViewHolder holder, int position, @NonNull UserClass model) {
                        if(!model.getUid().equals(mAuth.getCurrentUser().getUid())) {
                            holder.userBox.setVisibility(View.VISIBLE);
                            holder.tvUsername.setText(model.getName());
                            storageRef.child(model.getUid()).child("avatar.jpg").getDownloadUrl()
                                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            Glide.with(UsersFrag.this.getActivity()).load(uri.toString()).into(holder.ivRowDp);
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            holder.ivRowDp.setImageResource(R.mipmap.profileavatar);
                                        }
                                    });
                            friendsRef.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.child(model.getUid()).exists())
                                    {
                                        String status = (String) snapshot.child(model.getUid()).child("status").getValue();
                                        if(status!= null && status.equals("friends"))
                                        {
                                            holder.ivStatus.setVisibility(View.VISIBLE);
                                        }
                                        else {
                                            holder.ivStatus.setVisibility(View.GONE);
                                        }
                                    }
                                    else {
                                        holder.ivStatus.setVisibility(View.GONE);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                            holder.userBox.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    viewProfile(model);
                                }
                            });


                        }
                        else {
                            holder.userBox.setVisibility(View.GONE);
                        }
                    }

                    @NonNull
                    @Override
                    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View usrView = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_row_layout,parent,false);
                        return new UserViewHolder(usrView);
                    }
                };
        rvUsersList.setAdapter(userAdapter);
        userAdapter.startListening();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder{
        ImageView ivRowDp,ivStatus;
        TextView tvUsername;
        LinearLayout userBox;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            ivRowDp = itemView.findViewById(R.id.ivRowDp);
            ivStatus = itemView.findViewById(R.id.ivRequestIcon);
            tvUsername = itemView.findViewById(R.id.tvRowName);
            userBox = itemView.findViewById(R.id.userBox);
        }
    }

    //View Profile
    public void viewProfile(UserClass model)
    {
        View profileView = LayoutInflater.from(UsersFrag.this.getActivity()).inflate(R.layout.view_profile,null);

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
        tvName.setText(model.getName());
        if(model.getBio().trim().length()==0)
        {
            tvBio.setText("Hey there!");
        }
        else {
            tvBio.setText(model.getBio());
        }
        storageRef.child(model.getUid()).child("avatar.jpg")
                .getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(UsersFrag.this.getActivity()).load(uri.toString()).into(ivProfileDp);
                    }
                });
        friendsRef.child(mAuth.getCurrentUser().getUid()).child(model.getUid()).child("status")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists())
                        {
                            switch (snapshot.getValue().toString())
                            {
                                case "sent":
                                    btnMessage.setVisibility(View.GONE);
                                    btnAcceptReq.setVisibility(View.GONE);
                                    btnDeclineReq.setVisibility(View.GONE);
                                    btnAddFriend.setVisibility(View.VISIBLE);
                                    btnAddFriend.setText("Request Sent");
                                    break;
                                case "friends":
                                    btnMessage.setVisibility(View.VISIBLE);
                                    btnAcceptReq.setVisibility(View.GONE);
                                    btnDeclineReq.setVisibility(View.GONE);
                                    btnAddFriend.setVisibility(View.GONE);
                                    break;
                                case "received":
                                    btnMessage.setVisibility(View.GONE);
                                    btnAcceptReq.setVisibility(View.VISIBLE);
                                    btnDeclineReq.setVisibility(View.VISIBLE);
                                    btnAddFriend.setVisibility(View.GONE);
                                    break;
                                default:
                                    btnMessage.setVisibility(View.GONE);
                                    btnAcceptReq.setVisibility(View.GONE);
                                    btnDeclineReq.setVisibility(View.GONE);
                                    btnAddFriend.setVisibility(View.VISIBLE);
                                    btnAddFriend.setText("Add as Friend");
                            }
                        }
                        else{
                            btnMessage.setVisibility(View.GONE);
                            btnAcceptReq.setVisibility(View.GONE);
                            btnDeclineReq.setVisibility(View.GONE);
                            btnAddFriend.setVisibility(View.VISIBLE);
                            btnAddFriend.setText("Add as Friend");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(UsersFrag.this.getActivity(), "Failed!", Toast.LENGTH_SHORT).show();
                    }
                });

        //Showing Dialog

        AlertDialog.Builder builder = new AlertDialog.Builder(UsersFrag.this.getActivity());
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

        btnAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(btnAddFriend.getText().toString().toLowerCase().contains("add")) {
                    addFriend(model.getUid(), myAlert);
                }
                else {
                    cancelRequest(model.getUid(),myAlert);
                }
            }
        });

        btnAcceptReq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                acceptRequest(model.getUid(),myAlert);
            }
        });

        btnDeclineReq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                declineRequest(model.getUid(),myAlert);
            }
        });

        btnMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myAlert.dismiss();
                Intent intent = new Intent(UsersFrag.this.getActivity(), ChatScreenActivity.class);
                intent.putExtra("uid",model.getUid());
                startActivity(intent);
            }
        });
    }

    //Sending Friend Request
    public void addFriend(String uid,AlertDialog dialog)
    {
        HashMap<String,Object> friendStatus = new HashMap<String, Object>();
        friendStatus.put("status","received");
        friendStatus.put("uid",mAuth.getCurrentUser().getUid());
        friendsRef.child(uid).child(mAuth.getCurrentUser().getUid()).setValue(friendStatus)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        HashMap<String,Object> friendStatus = new HashMap<String, Object>();
                        friendStatus.put("status","sent");
                        friendStatus.put("uid",uid);
                        friendsRef.child(mAuth.getCurrentUser().getUid()).child(uid).setValue(friendStatus)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        getUserId(uid);
                                        dialog.dismiss();
                                    }
                                });
                    }
                });
    }

    public void getUserId(String uid) {
        FirebaseDatabase.getInstance().getReference().child("Tokens").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && snapshot.getValue()!=null)
                {
                    userId = snapshot.getValue().toString();

                    sendNotification notification = new sendNotification(myname ,"sent you a friend request.",userId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //Cancel sent Friend Request
    public void cancelRequest(String uid,AlertDialog dialog)
    {
        friendsRef.child(uid).child(mAuth.getCurrentUser().getUid()).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        friendsRef.child(mAuth.getCurrentUser().getUid()).child(uid).removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        dialog.dismiss();
                                    }
                                });
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
                                        dialog.dismiss();
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
                                        dialog.dismiss();
                                    }
                                });
                    }
                });
    }
}
