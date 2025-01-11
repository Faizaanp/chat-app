package com.example.mychatapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GroupChatAdapter extends RecyclerView.Adapter<GroupChatAdapter.MyViewHolder> {
    private Context context;
    private List<GroupMessageModel> groupMessageModels;
    private List<String> groupIds;


    public GroupChatAdapter(Context context) {
        this.context = context;
        this.groupMessageModels = new ArrayList<>();
        this.groupIds = new ArrayList<>();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_row,parent ,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        GroupMessageModel messageModel = groupMessageModels.get(position);
        groupIds.add(messageModel.getGroupId());
        holder.groupname.setText(messageModel.getGroupName());
        holder.groupcreator.setText(messageModel.getGroupCreator());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, GroupChatActivity.class);
                intent.putExtra("groupIds", messageModel.getGroupIds());
                intent.putExtra("groupUserNames",messageModel.getGroupUserNames());
                intent.putExtra("groupName", messageModel.getGroupName());
                intent.putExtra("groupCreator", messageModel.getGroupCreator());
                intent.putExtra("groupSender", messageModel.getSenderId());
                context.startActivity(intent);
            }
        });
    }



    public void add(GroupMessageModel groupMessageModel){
        if(!groupIds.contains(groupMessageModel.getGroupId())){
            groupMessageModels.add(groupMessageModel);
            notifyDataSetChanged();
        }
    }
    public void clear(){
        groupMessageModels.clear();
        notifyDataSetChanged();
    }

    private String formatTime(String timestamp) {
        long timeInMillis = Long.parseLong(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timeInMillis));
    }

    private String getCurrentGroupId() {
        SharedPreferences preferences = context.getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid(), Context.MODE_PRIVATE);
        return preferences.getString("groupIds", "");
    }

    @Override
    public int getItemCount() {
        return groupMessageModels.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
//        TextView message, sender, time;
        TextView groupname, groupcreator;

        public MyViewHolder(View itemView) {
            super(itemView);
            groupname = itemView.findViewById(R.id.groupname);
            groupcreator = itemView.findViewById(R.id.groupcreator);
        }
    }
}
