package com.vzardd.greenqube.chatclasses;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.vzardd.greenqube.sendNotification;

import java.util.HashMap;

public class ChatScreenActivity extends AppCompatActivity {
    DatabaseReference rootRef;
    FirebaseAuth mAuth;
    RecyclerView rvChatMessages;
    EditText etTypedMessage;
    ImageView ivSend,ivAttach;
    int PICK_FILE = 2;
    String uid,myname;
    StorageReference storageRef;
    String username,userId;
    TextView tvSendFile,tvCancelFile,tvAttachedFileName;
    LinearLayout llAttachedBox;
    FirebaseRecyclerAdapter<ChatClass,ChatViewHolder> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_screen);
        uid = getIntent().getStringExtra("uid");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        initViews();
        llAttachedBox.setVisibility(View.GONE);
        setTitleName();
        getUserId();
        findMyName();
        ivSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(etTypedMessage.getText().toString().trim().length()!=0)
                {
                    sendMessage(etTypedMessage.getText().toString().trim());
                    rvChatMessages.scrollToPosition(adapter.getItemCount());
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

    public void findMyName() {
        rootRef.child("Users").child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
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

    public void getUserId() {
        rootRef.child("Tokens").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && snapshot.getValue()!=null)
                {
                    userId = snapshot.getValue().toString();
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
        FirebaseRecyclerOptions<ChatClass> options = new FirebaseRecyclerOptions.Builder<ChatClass>()
                .setQuery(rootRef.child("Chats").child(mAuth.getCurrentUser().getUid()).child(uid).orderByChild("timestamp"),ChatClass.class)
                .build();
        adapter = new FirebaseRecyclerAdapter<ChatClass, ChatViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ChatViewHolder holder, int position, @NonNull ChatClass model) {
                if(model.getUid().equals(mAuth.getCurrentUser().getUid()))
                {
                    if(model.getType().equals("message"))
                    {
                        sentMessage(holder,position,model);
                    }
                    else {
                        sentFile(holder,position,model);
                    }
                }
                else {
                    if(model.getType().equals("message"))
                    {
                        receivedMessage(holder,position,model);
                    }
                    else {
                        receivedFile(holder,position,model);
                    }
                }
                holder.tvSentMsg.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ChatScreenActivity.this);
                        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                deleteMessage(model);
                                dialogInterface.dismiss();
                            }
                        });
                        builder.setNegativeButton("Copy", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                copyMessage(model);
                                dialogInterface.dismiss();
                            }
                        });
                        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        builder.setCancelable(false);
                        builder.show();
                        return true;
                    }
                });
                holder.tvReceivedMsg.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ChatScreenActivity.this);
                        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                deleteMessage(model);
                                dialogInterface.dismiss();
                            }
                        });
                        builder.setNegativeButton("Copy", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                copyMessage(model);
                                dialogInterface.dismiss();
                            }
                        });
                        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        builder.setCancelable(false);
                        builder.show();
                        return true;
                    }
                });
                holder.cvSentFile.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ChatScreenActivity.this);
                        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                deleteMessage(model);
                                dialogInterface.dismiss();
                            }
                        });
                        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        builder.setCancelable(false);
                        builder.show();
                        return true;
                    }
                });
                holder.cvReceivedFile.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ChatScreenActivity.this);
                        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                deleteMessage(model);
                                dialogInterface.dismiss();
                            }
                        });
                        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        builder.setCancelable(false);
                        builder.show();
                        return true;
                    }
                });

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
            public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_row_layout,parent,false);
                return new ChatViewHolder(v);
            }
        };
        rvChatMessages.setAdapter(adapter);
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
        if(model.getUid().equals(mAuth.getCurrentUser().getUid())) {
            storageRef.child("Chats").child(model.getUid()).child(uid).child(fileName).getDownloadUrl()
                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            startDownloading(model,uri);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ChatScreenActivity.this, "Something went wrong! Check your internet connection and try again!", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        else {
            storageRef.child("Chats").child(uid).child(mAuth.getCurrentUser().getUid()).child(fileName).getDownloadUrl()
                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            startDownloading(model,uri);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ChatScreenActivity.this, "Something went wrong! Check your internet connection and try again!", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
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

    //Options Item Selected
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home)
        {
            onBackPressed();
        }
        return true;
    }

    //Initializing views
    public void initViews(){
        etTypedMessage = findViewById(R.id.etTypeMessage);
        ivSend = findViewById(R.id.ivSend);
        ivAttach = findViewById(R.id.ivAttach);
        llAttachedBox = findViewById(R.id.llAttachedFile);
        tvSendFile = findViewById(R.id.tvSendFile);
        tvCancelFile = findViewById(R.id.tvCancelFile);
        tvAttachedFileName = findViewById(R.id.tvAttachedFileName);
        rootRef = FirebaseDatabase.getInstance().getReference();
        storageRef = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        rvChatMessages = findViewById(R.id.rvChatMessages);
        LinearLayoutManager layoutManager = new LinearLayoutManager(ChatScreenActivity.this);
        layoutManager.setStackFromEnd(true);
        rvChatMessages.setLayoutManager(layoutManager);
    }

    //Setting Title name
    public void setTitleName()
    {
        rootRef.child("Users").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    String name = (String) snapshot.child("name").getValue();
                    if(name!=null)
                    {
                        username = name;
                        getSupportActionBar().setTitle(name);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //Chat viewholder
    public static class ChatViewHolder extends RecyclerView.ViewHolder{
        TextView tvReceivedMsg,tvSentMsg,tvReceivedFile,tvSentFile;
        ImageView ivReceivedDownload,ivSentDownload;
        CardView cvReceivedFile,cvSentFile;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReceivedFile = itemView.findViewById(R.id.tvReceivedFileName);
            tvReceivedMsg = itemView.findViewById(R.id.tvReceivedMessage);
            tvSentFile = itemView.findViewById(R.id.tvSentFileName);
            tvSentMsg = itemView.findViewById(R.id.tvSentMessage);
            ivReceivedDownload = itemView.findViewById(R.id.ivReceivedDownload);
            ivSentDownload = itemView.findViewById(R.id.ivSentDownload);
            cvReceivedFile = itemView.findViewById(R.id.cvReceivedFile);
            cvSentFile = itemView.findViewById(R.id.cvSentFile);
        }
    }

    //Sent Message
    public void sentMessage(ChatViewHolder holder,int position,ChatClass model)
    {
        holder.tvReceivedMsg.setVisibility(View.GONE);
        holder.tvSentMsg.setVisibility(View.VISIBLE);
        holder.cvSentFile.setVisibility(View.GONE);
        holder.cvReceivedFile.setVisibility(View.GONE);
        holder.tvSentMsg.setText(model.getMessage());
    }

    //Received Message
    public void receivedMessage(ChatViewHolder holder,int position,ChatClass model)
    {
        holder.tvReceivedMsg.setVisibility(View.VISIBLE);
        holder.tvSentMsg.setVisibility(View.GONE);
        holder.cvSentFile.setVisibility(View.GONE);
        holder.cvReceivedFile.setVisibility(View.GONE);
        holder.tvReceivedMsg.setText(model.getMessage());
    }

    //Sent Message
    public void sentFile(ChatViewHolder holder,int position,ChatClass model)
    {
        holder.tvReceivedMsg.setVisibility(View.GONE);
        holder.tvSentMsg.setVisibility(View.GONE);
        holder.cvSentFile.setVisibility(View.VISIBLE);
        holder.cvReceivedFile.setVisibility(View.GONE);
        String name = model.getMessage().split(":::")[1];
        if(name.length()<10) {
            holder.tvSentFile.setText(name);
        }else {
            holder.tvSentFile.setText(name.substring(0,9)+"...");
        }
    }

    //Received Message
    public void receivedFile(ChatViewHolder holder,int position,ChatClass model)
    {
        holder.tvReceivedMsg.setVisibility(View.GONE);
        holder.tvSentMsg.setVisibility(View.GONE);
        holder.cvSentFile.setVisibility(View.GONE);
        holder.cvReceivedFile.setVisibility(View.VISIBLE);
        String name = model.getMessage().split(":::")[1];
        if(name.length()<10) {
            holder.tvReceivedFile.setText(name);
        }else {
            holder.tvReceivedFile.setText(name.substring(0,9)+"...");
        }
    }

    //Store message to firebase
    public void sendMessage(String message)
    {
        etTypedMessage.setText("");
        HashMap<String,Object> msg = new HashMap<String, Object>();
        msg.put("uid",mAuth.getCurrentUser().getUid());
        msg.put("type","message");
        msg.put("message",message);
        msg.put("timestamp", ServerValue.TIMESTAMP);
        rootRef.child("Chats").child(mAuth.getCurrentUser().getUid()).child(uid).push().setValue(msg)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        rootRef.child("Chats").child(uid).child(mAuth.getCurrentUser().getUid()).push().setValue(msg)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        HashMap<String,Object> chat = new HashMap<String, Object>();
                                        chat.put("uid",uid);
                                        chat.put("lastmessage",message);
                                        chat.put("timestamp",ServerValue.TIMESTAMP);
                                        rootRef.child("ChatLists").child(mAuth.getCurrentUser().getUid()).child(uid).setValue(chat)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        HashMap<String,Object> chat = new HashMap<String, Object>();
                                                        chat.put("uid",mAuth.getCurrentUser().getUid());
                                                        chat.put("lastmessage",message);
                                                        chat.put("timestamp",ServerValue.TIMESTAMP);
                                                        rootRef.child("ChatLists").child(uid).child(mAuth.getCurrentUser().getUid()).setValue(chat)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        Toast.makeText(ChatScreenActivity.this, "Message Sent!", Toast.LENGTH_SHORT).show();
                                                                        sendNotification notification = new sendNotification(myname,message,userId);
                                                                    }
                                                                });
                                                    }
                                                });
                                    }
                                });
                    }
                });
    }


    //Deleting a message
    public void deleteMessage(ChatClass model)
    {
        rootRef.child("Chats").child(mAuth.getCurrentUser().getUid()).child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    for(DataSnapshot snapshot1 : snapshot.getChildren())
                    {
                        if(snapshot1!=null && snapshot1.child("message").getValue().toString().equals(model.getMessage()) && snapshot1.child("timestamp").getValue().toString().equals(model.getTimestamp().toString())){
                            String key = snapshot1.getKey();
                            rootRef.child("Chats").child(mAuth.getCurrentUser().getUid()).child(uid).child(key).removeValue()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(ChatScreenActivity.this, "Message Deleted.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //copying a message
    public void copyMessage(ChatClass model)
    {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboardManager.setPrimaryClip(ClipData.newPlainText("msg",model.getMessage()));
        Toast.makeText(this, "Message copied to clipboard!", Toast.LENGTH_SHORT).show();
    }

    //Showing attach dialog
    public void showAttachDialog()
    {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("file/*");
        startActivityForResult(intent,PICK_FILE);
    }

    //Storing data to firebase
    public void sendFileToFirebase(Intent data)
    {
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
        storageRef.child("Chats").child(mAuth.getCurrentUser().getUid()).child(uid).child(storingName).putFile(data.getData())
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        HashMap<String,Object> msg = new HashMap<String, Object>();
                        msg.put("uid",mAuth.getCurrentUser().getUid());
                        msg.put("type","file");
                        msg.put("message",storingName+":::"+fileName);
                        msg.put("timestamp", timestamp);
                        rootRef.child("Chats").child(mAuth.getCurrentUser().getUid()).child(uid).push().setValue(msg)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        rootRef.child("Chats").child(uid).child(mAuth.getCurrentUser().getUid()).push().setValue(msg)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        HashMap<String,Object> chat = new HashMap<String, Object>();
                                                        chat.put("uid",uid);
                                                        chat.put("lastmessage",fileName);
                                                        chat.put("timestamp",timestamp);
                                                        rootRef.child("ChatLists").child(mAuth.getCurrentUser().getUid()).child(uid).setValue(chat)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        HashMap<String,Object> chat = new HashMap<String, Object>();
                                                                        chat.put("uid",mAuth.getCurrentUser().getUid());
                                                                        chat.put("lastmessage",fileName);
                                                                        chat.put("timestamp",timestamp);
                                                                        rootRef.child("ChatLists").child(uid).child(mAuth.getCurrentUser().getUid()).setValue(chat)
                                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                    @Override
                                                                                    public void onSuccess(Void aVoid) {
                                                                                        sendNotification notification = new sendNotification(myname,fileName,userId);
                                                                                        Toast.makeText(ChatScreenActivity.this, "File Sent!", Toast.LENGTH_SHORT).show();
                                                                                        llAttachedBox.setVisibility(View.GONE);
                                                                                    }
                                                                                });
                                                                    }
                                                                });
                                                    }
                                                });
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    //On Activity Result

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==PICK_FILE && resultCode==RESULT_OK && data!=null)
        {
            llAttachedBox.setVisibility(View.VISIBLE);
            String fileName = data.getData().getLastPathSegment();
            if(fileName.length()<18) {
                tvAttachedFileName.setText(fileName);
            }
            else {
                tvAttachedFileName.setText(fileName.substring(0,17)+"...");
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
}