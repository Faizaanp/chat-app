package com.example.mychatapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GroupMessageAdapter extends RecyclerView.Adapter<GroupMessageAdapter.MyViewHolder> {
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;
    private Context context;
    private List<GroupMessageModel> groupMessageModelList = new ArrayList<>();

    public GroupMessageAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public GroupMessageAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);

        if(viewType==VIEW_TYPE_SENT){
            View view = inflater.inflate(R.layout.group_message_row_sent,parent ,false);
            return new GroupMessageAdapter.MyViewHolder(view);
        }
        else{
            View view = inflater.inflate(R.layout.group_message_row_received,parent ,false);
            return new GroupMessageAdapter.MyViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull GroupMessageAdapter.MyViewHolder holder, int position) {
        GroupMessageModel groupMessageModel = groupMessageModelList.get(position);
        if(getItemViewType(position) == VIEW_TYPE_SENT){
            holder.message_sent.setText(groupMessageModel.getMessageText());
            String senderId = groupMessageModel.getSenderId();
            getUsernameFromFirebase(senderId, holder.sender_sent); // Fetch username and set it in the TextView
            holder.time_sent.setText(formatTime(groupMessageModel.getTimestamp()));
        }
        else{
            holder.message_received.setText(groupMessageModel.getMessageText());
            String senderId = groupMessageModel.getSenderId();
            getUsernameFromFirebase(senderId, holder.receiver_received); // Fetch username and set it in the TextView
            holder.time_received.setText(formatTime(groupMessageModel.getTimestamp()));
        }

    }

    @Override
    public int getItemViewType(int position) {
        if(groupMessageModelList.get(position).getSenderId().equals(FirebaseAuth.getInstance().getUid())){
            return VIEW_TYPE_SENT;
        }
        else{
            return VIEW_TYPE_RECEIVED;
        }
    }

    @Override
    public int getItemCount() {
        return groupMessageModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView message_sent,sender_sent,time_sent;
        private TextView message_received, receiver_received, time_received;
        public MyViewHolder(View itemView){
            super(itemView);
            message_sent = itemView.findViewById(R.id.group_message_sent);
            sender_sent = itemView.findViewById(R.id.group_sender_sent);
            time_sent = itemView.findViewById(R.id.group_time_sent);
            message_received = itemView.findViewById(R.id.group_message_received);
            receiver_received = itemView.findViewById(R.id.group_sender_received);
            time_received = itemView.findViewById(R.id.group_time_received);
        }
    }

    private void getUsernameFromFirebase(String senderId, TextView senderTextView) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(senderId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    UserModel userModel = snapshot.getValue(UserModel.class);
                    if(userModel!=null){
                        senderTextView.setText(userModel.getUserName());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void add(GroupMessageModel groupMessageModel){
        groupMessageModelList.add(groupMessageModel);
        notifyDataSetChanged();
    }
    public void clear(){
        groupMessageModelList.clear();
        notifyDataSetChanged();
    }
    private String formatTime(String timestamp) {
        long timeInMillis = Long.parseLong(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timeInMillis));
    }

}
