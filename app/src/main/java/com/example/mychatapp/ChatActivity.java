package com.example.mychatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {

    String receiverId, receiverName, senderRoom, receiverRoom;
    DatabaseReference dbReferenceSender, dbReferenceReceiver, userReference;;
    ImageView sendbtn;
    SharedPreferences sharedPreferences;
    EditText messageText;
    RecyclerView recyclerView;
    MessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        userReference = FirebaseDatabase.getInstance().getReference("users");
        receiverName = getIntent().getStringExtra("name");
        receiverId = getIntent().getStringExtra("id");

        sharedPreferences = getSharedPreferences("com.example.mychatapp", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("id", getIntent().getStringExtra("id"));
        editor.apply();


        getSupportActionBar().setTitle(receiverName);
        if(receiverId!=null){
            senderRoom = FirebaseAuth.getInstance().getUid()+receiverId;
            receiverRoom = receiverId + FirebaseAuth.getInstance().getUid();
        }
        sendbtn = findViewById(R.id.sendMessageIcon);
        messageAdapter = new MessageAdapter(this);
        recyclerView = findViewById(R.id.chatrecycler);
        messageText = findViewById(R.id.messageEdit);

        recyclerView.setAdapter(messageAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dbReferenceSender = FirebaseDatabase.getInstance().getReference("chats").child(senderRoom);
        dbReferenceReceiver = FirebaseDatabase.getInstance().getReference("chats").child(receiverRoom);


        dbReferenceSender.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<MessageModel> messages = new ArrayList<>();
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    MessageModel messageModel = dataSnapshot.getValue(MessageModel.class);
                    messages.add(messageModel);
                }
                messageAdapter.clear();
                Collections.sort(messages, new Comparator<MessageModel>() {
                    @Override
                    public int compare(MessageModel o1, MessageModel o2) {
                        return o1.getTimestamp().compareTo(o2.getTimestamp());
                    }
                });
                for(MessageModel message: messages){
                    messageAdapter.add(message);
                }
                messageAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = messageText.getText().toString();
                if(!message.trim().isEmpty()){
                    SendMessage(message);
                }
                else{
                    Toast.makeText(ChatActivity.this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    private void SendMessage(String message){
        String messageId = UUID.randomUUID().toString();
        long currentTime = System.currentTimeMillis();
        MessageModel messageModel = new MessageModel(messageId, FirebaseAuth.getInstance().getUid(), message, String.valueOf(currentTime));
        messageAdapter.add(messageModel);
        dbReferenceSender.child(messageId).setValue(messageModel)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ChatActivity.this,"Failed to send message", Toast.LENGTH_SHORT).show();
                    }
                });
        dbReferenceReceiver.child(messageId).setValue(messageModel);
        recyclerView.scrollToPosition(messageAdapter.getItemCount()-1);
        messageText.setText("");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.logout){
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(ChatActivity.this, SigninActivity.class));
            finish();
            return true;
        }
        return false;
    }
}