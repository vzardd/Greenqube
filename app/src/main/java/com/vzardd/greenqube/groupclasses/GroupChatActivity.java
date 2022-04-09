package com.vzardd.greenqube.groupclasses;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.storage.UploadTask;
import com.vzardd.greenqube.R;
import com.vzardd.greenqube.chatclasses.ChatClass;
import com.vzardd.greenqube.chatclasses.ChatScreenActivity;
import com.vzardd.greenqube.chatclasses.GroupInfoActivity;

import java.util.HashMap;

public class GroupChatActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    StorageReference storageRef;
    DatabaseReference rootRef;
    String key;
    RecyclerView rvGroupMessages;
    EditText etTypedMessage;
    ImageView ivSend,ivAttach;
    int PICK_FILE = 2;
    TextView tvSendFile,tvCancelFile,tvAttachedFileName;
    LinearLayout llAttachedBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        initViews();
        findGroupName();
        ivSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!etTypedMessage.getText().toString().trim().isEmpty())
                {
                    sendMessage(etTypedMessage.getText().toString().trim());
                    etTypedMessage.setText("");
                }
            }
        });

        ivAttach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAttachDialog();
            }
        });

    }

    private void showAttachDialog() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("file/*");
        startActivityForResult(intent,PICK_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==PICK_FILE && resultCode==RESULT_OK && data!=null)
        {
            llAttachedBox.setVisibility(View.VISIBLE);
            String fileName = data.getData().getLastPathSegment();
            if(fileName.length()<13) {
                tvAttachedFileName.setText(fileName);
            }
            else {
                tvAttachedFileName.setText(fileName.substring(0,12)+"...");
            }
            tvCancelFile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    llAttachedBox.setVisibility(View.GONE);
                }
            });
            tvSendFile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Send file function here
                    sendFileToFirebase(data);
                    // llAttachedBox.setVisibility(View.GONE);
                }
            });
        }
    }

    private void sendFileToFirebase(Intent data) {
        tvSendFile.setVisibility(View.GONE);
        tvCancelFile.setVisibility(View.GONE);
        tvAttachedFileName.setText("Sending file, please wait...");
        Object timestamp = ServerValue.TIMESTAMP;
        String fileName = data.getData().getLastPathSegment();
        String type="";
        for(int i=fileName.length()-1;i>=0;--i)
        {
            if(fileName.charAt(i)=='.')
            {
                type="."+type;
                break;
            }
            else {
                type=Character.toString(fileName.charAt(i))+type;
            }
        }
        String storingName = System.currentTimeMillis() +type;
        storageRef.child("Groupmessages").child(key).child(storingName).putFile(data.getData())
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        HashMap<String,Object> msg = new HashMap<String, Object>();
                        msg.put("uid",mAuth.getCurrentUser().getUid());
                        msg.put("type","file");
                        msg.put("message",storingName+":::"+fileName);
                        msg.put("timestamp", timestamp);
                        rootRef.child("Groupmessages").child(key).push().setValue(msg)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        llAttachedBox.setVisibility(View.GONE);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        llAttachedBox.setVisibility(View.GONE);
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        llAttachedBox.setVisibility(View.GONE);
                        Toast.makeText(GroupChatActivity.this, "Something went wrong! Check your internet connection and try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendMessage(String messsage) {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage("Sending...");
        dialog.show();
        HashMap<String,Object> msg = new HashMap<String, Object>();
        msg.put("uid",mAuth.getCurrentUser().getUid());
        msg.put("type","message");
        msg.put("message",messsage);
        msg.put("timestamp", ServerValue.TIMESTAMP);
        rootRef.child("Groupmessages").child(key).push().setValue(msg)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        dialog.dismiss();
                        Toast.makeText(GroupChatActivity.this, "Message Sent", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.dismiss();
                        Toast.makeText(GroupChatActivity.this, "Something went wrong! Check your internet connection and try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<ChatClass> options = new FirebaseRecyclerOptions.Builder<ChatClass>()
                .setQuery(rootRef.child("Groupmessages").child(key).orderByChild("timestamp"),ChatClass.class)
                .build();
        FirebaseRecyclerAdapter<ChatClass,GroupMessageViewHolder> adapter = new FirebaseRecyclerAdapter<ChatClass, GroupMessageViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull GroupMessageViewHolder holder, int position, @NonNull ChatClass model) {
                if(model.getType().equals("message"))
                {
                    if(model.getUid().equals(mAuth.getCurrentUser().getUid()))
                    {
                        sentMessage(holder,model);
                    }
                    else {
                        receivedMessage(holder,model);
                    }
                }
                else {
                    if(model.getUid().equals(mAuth.getCurrentUser().getUid()))
                    {
                        sentFile(holder,model);
                    }
                    else {
                        receivedFile(holder,model);
                    }
                }
                holder.ivSentDownload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
                        {
                            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                            {
                                downloadFile(model);
                            }
                            else {
                                String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                                requestPermissions(permissions,1000);//Storage request
                            }
                        }
                        else {
                            downloadFile(model);
                        }
                    }
                });

                holder.ivReceivedDownload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
                        {
                            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                            {
                                downloadFile(model);
                            }
                            else {
                                String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                                requestPermissions(permissions,1000);//Storage request
                            }
                        }
                        else {
                            downloadFile(model);
                        }
                    }
                });
            }

            @NonNull
            @Override
            public GroupMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_chat_row_layout,parent,false);
                return new GroupMessageViewHolder(v);
            }
        };
        rvGroupMessages.setAdapter(adapter);
        adapter.startListening();
    }

    //Handling permission requests

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case 1000:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
                {
                    Toast.makeText(this, "You can download the file now!", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(this, "Accept Storage permission to download!", Toast.LENGTH_SHORT).show();
                }
        }
    }

    //Downloading file
    public void downloadFile(ChatClass model){
        String fileName = model.getMessage().split(":::")[0];
        storageRef.child("Groupmessages").child(key).child(fileName).getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        startDownloading(model,uri);
                    }
                });
    }

    //Actual downloading of file
    public void startDownloading(ChatClass model,Uri uri)
    {
        Toast.makeText(this, "Downloading...", Toast.LENGTH_SHORT).show();
        String fileName = model.getMessage().split(":::")[1];
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setTitle(fileName);
        request.setDescription("Downloading file...");
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,""+fileName);
        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }

    private void receivedFile(GroupMessageViewHolder holder, ChatClass model) {
        holder.llReceivedMsg.setVisibility(View.GONE);
        holder.llSentMsg.setVisibility(View.GONE);
        holder.llReceivedFile.setVisibility(View.VISIBLE);
        holder.cvSentFile.setVisibility(View.GONE);
        rootRef.child("Users").child(model.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && snapshot.getValue()!=null) {
                    holder.tvFileReceiverName.setText(snapshot.child("name").getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        String name = model.getMessage().split(":::")[1];
        if(name.length()<10) {
            holder.tvReceivedFile.setText(name);
        }else {
            holder.tvReceivedFile.setText(name.substring(0,9)+"...");
        }
    }

    private void sentFile(GroupMessageViewHolder holder, ChatClass model) {
        holder.llReceivedMsg.setVisibility(View.GONE);
        holder.llSentMsg.setVisibility(View.GONE);
        holder.llReceivedFile.setVisibility(View.GONE);
        holder.cvSentFile.setVisibility(View.VISIBLE);
        String name = model.getMessage().split(":::")[1];
        if(name.length()<10) {
            holder.tvSentFile.setText(name);
        }else {
            holder.tvSentFile.setText(name.substring(0,9)+"...");
        }
    }

    private void receivedMessage(GroupMessageViewHolder holder, ChatClass model) {
        holder.llReceivedMsg.setVisibility(View.VISIBLE);
        holder.llSentMsg.setVisibility(View.GONE);
        holder.llReceivedFile.setVisibility(View.GONE);
        holder.cvSentFile.setVisibility(View.GONE);
        holder.tvReceivedMsg.setText(model.getMessage());
        rootRef.child("Users").child(model.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && snapshot.getValue()!=null) {
                    holder.tvMsgReceiverName.setText(snapshot.child("name").getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sentMessage(GroupMessageViewHolder holder, ChatClass model) {
        holder.llReceivedMsg.setVisibility(View.GONE);
        holder.llSentMsg.setVisibility(View.VISIBLE);
        holder.llReceivedFile.setVisibility(View.GONE);
        holder.cvSentFile.setVisibility(View.GONE);
        holder.tvSentMsg.setText(model.getMessage());
    }

    public static class GroupMessageViewHolder extends RecyclerView.ViewHolder{
        TextView tvReceivedMsg,tvSentMsg,tvReceivedFile,tvSentFile,tvMsgReceiverName,tvFileReceiverName;
        ImageView ivReceivedDownload,ivSentDownload;
        CardView cvSentFile;
        LinearLayout llReceivedFile,llReceivedMsg,llSentMsg;

        public GroupMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReceivedFile = itemView.findViewById(R.id.tvReceivedGrpFileName);
            tvReceivedMsg = itemView.findViewById(R.id.tvReceivedGrpMsg);
            tvSentFile = itemView.findViewById(R.id.tvSentGrpFileName);
            tvSentMsg = itemView.findViewById(R.id.tvSentGrpMsg);
            ivReceivedDownload = itemView.findViewById(R.id.ivReceivedGrpDownload);
            ivSentDownload = itemView.findViewById(R.id.ivSentGrpDownload);
            llReceivedFile = itemView.findViewById(R.id.llReceivedFile);
            cvSentFile = itemView.findViewById(R.id.cvSentGrpFile);
            tvMsgReceiverName = itemView.findViewById(R.id.tvReceiverName);
            tvFileReceiverName = itemView.findViewById(R.id.tvReceivedFileUsername);
            llReceivedMsg = itemView.findViewById(R.id.llReceivedGroupMsg);
            llSentMsg = itemView.findViewById(R.id.llSentGroupMsg);
        }
    }


    //Initializing views
    public void initViews() {
        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        storageRef = FirebaseStorage.getInstance().getReference();
        key = getIntent().getStringExtra("key");
        rvGroupMessages = findViewById(R.id.rvGroupMessages);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvGroupMessages.setLayoutManager(layoutManager);
        etTypedMessage = findViewById(R.id.etGroupTypeMessage);
        ivSend = findViewById(R.id.ivGroupSend);
        ivAttach = findViewById(R.id.ivGroupAttach);
        tvSendFile = findViewById(R.id.tvGroupSendFile);
        tvCancelFile = findViewById(R.id.tvGroupCancelFile);
        tvAttachedFileName = findViewById(R.id.tvGroupAttachedFileName);
        llAttachedBox = findViewById(R.id.llGroupAttachedFile);
        llAttachedBox.setVisibility(View.GONE);
    }

    //Finding Group Name
    public void findGroupName() {
        rootRef.child("GroupInfo").child(key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && snapshot.getValue()!=null) {
                    getSupportActionBar().setTitle(snapshot.getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //Options on create

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.group_info_menu,menu);
        return true;
    }

    //Options item selected
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home)
        {
            onBackPressed();
        }
        else if(item.getItemId() == R.id.group_info)
        {
            //Opening Group Info activity
            Intent intent = new Intent(GroupChatActivity.this, GroupInfoActivity.class);
            intent.putExtra("key",getIntent().getStringExtra("key"));
            startActivity(intent);
        }
        return true;
    }
}