package com.vzardd.greenqube.homefragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telecom.GatewayInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.vzardd.greenqube.CreateGroupActivity;
import com.vzardd.greenqube.R;
import com.vzardd.greenqube.groupclasses.GroupChatActivity;
import com.vzardd.greenqube.groupclasses.GroupInfo;

import static android.app.Activity.RESULT_OK;

public class GroupsFrag extends Fragment {

    TextView tvGroupEmpty;
    ImageView ivGroupEmpty;
    RecyclerView rvGroupList;
    FloatingActionButton fabCreateGroup;
    DatabaseReference rootRef;
    StorageReference storageRef;
    FirebaseAuth mAuth;
    final int CREATE_GROUP = 2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.group_fragment,container,false);
        initViews(v);
        checkIfChatEmpty();
        fabCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCreateGroupActivity();
            }
        });
        return v;
    }

    //Start Create Group Activity
    public void startCreateGroupActivity() {
        Intent intent = new Intent(this.getActivity(), CreateGroupActivity.class);
        startActivity(intent);
    }

    //Check if Group List is empty
    public void checkIfChatEmpty() {
        rootRef.child("Groups").child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && snapshot.getValue()!=null)
                {
                    tvGroupEmpty.setVisibility(View.GONE);
                    ivGroupEmpty.setVisibility(View.GONE);
                    rvGroupList.setVisibility(View.VISIBLE);
                }
                else {
                    tvGroupEmpty.setVisibility(View.VISIBLE);
                    ivGroupEmpty.setVisibility(View.VISIBLE);
                    rvGroupList.setVisibility(View.GONE);
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
        FirebaseRecyclerOptions<GroupInfo> options = new FirebaseRecyclerOptions.Builder<GroupInfo>()
                .setQuery(rootRef.child("Groups").child(mAuth.getCurrentUser().getUid()).orderByChild("timestamp"),GroupInfo.class)
                .build();
        FirebaseRecyclerAdapter<GroupInfo,GroupViewHolder> adapter = new FirebaseRecyclerAdapter<GroupInfo, GroupViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull GroupViewHolder holder, int position, @NonNull GroupInfo model) {
                setRowItem(holder,position,model);
                holder.groupBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(GroupsFrag.this.getActivity(), GroupChatActivity.class);
                        intent.putExtra("key",model.getKey());
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_list_row_layout,parent,false);
                return new GroupViewHolder(v);
            }
        };
        rvGroupList.setAdapter(adapter);
        adapter.startListening();
    }

    public void setRowItem(GroupViewHolder holder, int position, GroupInfo model) {
        try {
            String key = model.getKey();
            rootRef.child("GroupInfo").child(model.getKey()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists() && snapshot.getValue() != null) {
                        holder.tvGroupName.setText(snapshot.getValue().toString());
                    }
                    if (model.getLastmessage().length() != 0) {
                        holder.tvLastMessage.setText(model.getLastmessage());
                    }
                    storageRef.child("Groups").child(key + ".jpg").getDownloadUrl()
                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Glide.with(GroupsFrag.this.getActivity()).load(uri.toString()).into(holder.ivGroupDp);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    holder.ivGroupDp.setImageResource(R.mipmap.ic_group);
                                }
                            });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        catch (Exception e)
        {

        }
    }

    //Group View holder
    public static class GroupViewHolder extends RecyclerView.ViewHolder{

        ImageView ivGroupDp;
        TextView tvGroupName,tvLastMessage;
        ConstraintLayout groupBox;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            ivGroupDp = itemView.findViewById(R.id.ivGroupListDp);
            tvGroupName = itemView.findViewById(R.id.tvGroupListName);
            tvLastMessage = itemView.findViewById(R.id.tvGroupLastMessage);
            groupBox = itemView.findViewById(R.id.clGroupBox);
        }
    }

    //Initializing views
    public void initViews(View v) {
        tvGroupEmpty = v.findViewById(R.id.tvGroupEmpty);
        ivGroupEmpty = v.findViewById(R.id.ivGroupEmpty);
        rvGroupList = v.findViewById(R.id.rvGroupsList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getActivity());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        rvGroupList.setLayoutManager(layoutManager);
        rvGroupList.setHasFixedSize(true);
        fabCreateGroup = v.findViewById(R.id.fabCreateGroup);
        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        storageRef = FirebaseStorage.getInstance().getReference();
    }

}
