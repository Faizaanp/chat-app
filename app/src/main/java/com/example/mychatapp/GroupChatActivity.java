package com.example.mychatapp;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
        import androidx.recyclerview.widget.LinearLayoutManager;
        import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
        import android.view.View;
        import android.widget.Button;
        import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
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

public class GroupChatActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EditText messageEditText;
    private ImageView sendButton;
    private DatabaseReference dbReferenceSender, dbReferenceReceiver;
    private GroupMessageAdapter groupMessageAdapter;
    private String groupName, groupIds, groupUserNames, groupCreator, groupSender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        recyclerView = findViewById(R.id.chatrecycler_group);
        messageEditText = findViewById(R.id.messageEdit_group);
        sendButton = findViewById(R.id.sendMessageIcon_group);

        groupMessageAdapter = new GroupMessageAdapter(this);

        recyclerView.setAdapter(groupMessageAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        groupName = getIntent().getStringExtra("groupName");
        groupIds = getIntent().getStringExtra("groupIds");
        groupUserNames = getIntent().getStringExtra("groupUserNames");
        groupCreator = getIntent().getStringExtra("groupCreator");
        groupSender = getIntent().getStringExtra("groupSender");
        dbReferenceSender = FirebaseDatabase.getInstance().getReference("group_messages").child(groupSender+groupIds);
        dbReferenceReceiver = FirebaseDatabase.getInstance().getReference("group_messages").child(groupIds+groupSender);


        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
        dbReferenceSender.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<GroupMessageModel> messages = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    GroupMessageModel messageModel = dataSnapshot.getValue(GroupMessageModel.class);
                    messages.add(messageModel);
                }
                groupMessageAdapter.clear();
                for(GroupMessageModel message: messages){
                    groupMessageAdapter.add(message);
                }
                groupMessageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();
        if(!messageText.isEmpty()){
            String groupId = UUID.randomUUID().toString();
            long currentTime = System.currentTimeMillis();
            GroupMessageModel messageModel = new GroupMessageModel(groupId, FirebaseAuth.getInstance().getCurrentUser().getUid(),
                    messageText, String.valueOf(currentTime), groupName,groupUserNames,groupIds, groupCreator);
            groupMessageAdapter.add(messageModel);
            dbReferenceSender.child(groupId).setValue(messageModel)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(GroupChatActivity.this,"Failed to send message", Toast.LENGTH_SHORT).show();
                        }
                    });
            dbReferenceReceiver.child(groupId).setValue(messageModel);
            recyclerView.scrollToPosition(groupMessageAdapter.getItemCount()-1);
            messageEditText.setText("");
        }
    }

    public void onBackPressed() {

        super.onBackPressed();
        Intent intent = new Intent(GroupChatActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}
