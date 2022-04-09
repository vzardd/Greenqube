package com.vzardd.greenqube.homefragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.vzardd.greenqube.R;
import com.vzardd.greenqube.chatclasses.ChatListClass;
import com.vzardd.greenqube.chatclasses.ChatScreenActivity;
import com.vzardd.greenqube.chatclasses.ChooseChat;

public class ChatFrag extends Fragment {
    DatabaseReference rootRef;
    FirebaseAuth mAuth;
    StorageReference storageRef;
    TextView tvChatEmpty;
    ImageView ivChatEmpty;
    FloatingActionButton fabNewChat;
    RecyclerView rvChatList;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.chat_fragment,container,false);
        initViews(v);
        checkIfChatExists();
        fabNewChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatFrag.this.getActivity(), ChooseChat.class);
                startActivity(intent);
            }
        });
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<ChatListClass> options = new FirebaseRecyclerOptions.Builder<ChatListClass>()
                .setQuery(rootRef.child("ChatLists").child(mAuth.getCurrentUser().getUid()).orderByChild("timestamp"),ChatListClass.class)
                .build();
        FirebaseRecyclerAdapter<ChatListClass,ChatListViewHolder> adapter = new FirebaseRecyclerAdapter<ChatListClass, ChatListViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ChatListViewHolder holder, int position, @NonNull ChatListClass model) {
                setChatView(holder,position,model);
                holder.chatBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(ChatFrag.this.getActivity(), ChatScreenActivity.class);
                        intent.putExtra("uid",model.getUid());
                        startActivity(intent);
                    }
                });
                holder.ivDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ChatFrag.this.getActivity());
                        builder.setTitle("Are you sure?");
                        builder.setMessage("This chat will be deleted permanently!");
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        builder.setPositiveButton("Delete Anyway", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                deleteChat(model.getUid());
                                dialogInterface.dismiss();
                            }
                        });
                        builder.setCancelable(false);
                        builder.show();
                    }
                });
            }

            @NonNull
            @Override
            public ChatListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_list_row_layout,parent,false);
                return new ChatListViewHolder(v);
            }
        };
        rvChatList.setAdapter(adapter);
        adapter.startListening();
    }

    private void setChatView(ChatListViewHolder holder, int position, ChatListClass model) {
        rootRef.child("Users").child(model.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    String name = (String) snapshot.child("name").getValue();
                    if(name!=null)
                    {
                        holder.tvUsername.setText(name);
                        if(model.getLastmessage().length()<30) {
                            holder.tvLastMessage.setText(model.getLastmessage());
                        }
                        else {
                            holder.tvLastMessage.setText(model.getLastmessage().substring(0,30)+"...");
                        }
                        storageRef.child("Users").child(model.getUid()).child("avatar.jpg").getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Glide.with(ChatFrag.this.getActivity()).load(uri.toString()).into(holder.ivRowDp);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        holder.ivRowDp.setImageResource(R.mipmap.profileavatar);
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

    //Initializing views
    public void initViews(View v)
    {
        rootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
        tvChatEmpty = v.findViewById(R.id.tvChatEmpty);
        ivChatEmpty = v.findViewById(R.id.ivChatEmpty);
        fabNewChat = v.findViewById(R.id.fabNewChat);
        rvChatList = v.findViewById(R.id.rvChatList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(ChatFrag.this.getActivity());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        rvChatList.setHasFixedSize(true);
        rvChatList.setLayoutManager(layoutManager);
    }

    //Checking if chat exists
    public void checkIfChatExists()
    {
        rootRef.child("ChatLists").child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    rvChatList.setVisibility(View.VISIBLE);
                    tvChatEmpty.setVisibility(View.GONE);
                    ivChatEmpty.setVisibility(View.GONE);
                }
                else {
                    rvChatList.setVisibility(View.GONE);
                    tvChatEmpty.setVisibility(View.VISIBLE);
                    ivChatEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //Chat list view holder
    public static class ChatListViewHolder extends RecyclerView.ViewHolder{
        ImageView ivRowDp,ivDelete;
        TextView tvUsername,tvLastMessage;
        ConstraintLayout chatBox;

        public ChatListViewHolder(@NonNull View itemView) {
            super(itemView);
            ivRowDp = itemView.findViewById(R.id.ivChatListDp);
            tvUsername = itemView.findViewById(R.id.tvChatListName);
            tvLastMessage = itemView.findViewById(R.id.tvChatLastMessage);
            chatBox = itemView.findViewById(R.id.clChatBox);
            ivDelete = itemView.findViewById(R.id.ivDelete);
        }
    }

    //Deleting chat
    public void deleteChat(String uid)
    {
        rootRef.child("Chats").child(mAuth.getCurrentUser().getUid()).child(uid).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        rootRef.child("ChatLists").child(mAuth.getCurrentUser().getUid()).child(uid).removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                    }
                                });
                    }
                });
    }
}
