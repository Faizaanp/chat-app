package com.example.mychatapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.MyViewHolder>{

    //using this adapter we will be able to get users their name and email on the mainActivity
    //when we click on one the chatActivity starts


    //what is an adapter and recyclerview?
    //--> https://stackoverflow.com/questions/59919366/what-is-recyclerview-adaptermyadapter-myviewholder-and-how-it-is-different-fro
    private Context context;
    private List<UserModel> userModelList;
    private List<UserModel> selectedUsers;
    private List<UserModel> userModelListFull; // Full list to support search
    private ImageView imageView;
    private StringBuilder stringIds = new StringBuilder();
    private StringBuilder stringGroupUserNames = new StringBuilder();


    public UsersAdapter(Context context) {
        this.context = context;
        this.userModelList = new ArrayList<>();
        this.userModelListFull = new ArrayList<>();
        this.selectedUsers = new ArrayList<>();
    }

    public void add(UserModel userModel){
        userModelList.add(userModel);
        userModelListFull.add(userModel); // Add to full list
        notifyDataSetChanged();
    }
    public void clear(){
        userModelList.clear();
        userModelListFull.clear();
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public UsersAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //LayoutInflater is used to create a new View (or Layout) object from one of your xml layouts.
        //this creates and view using the user_row xml
        //for reference -> https://stackoverflow.com/questions/3477422/what-does-layoutinflater-in-android-do
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_row,parent ,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersAdapter.MyViewHolder holder, int position) {
        UserModel userModel = userModelList.get(position);
        holder.name.setText(userModel.getUserName());
        holder.email.setText(userModel.getUserEmail());

        if (selectedUsers.contains(userModel)) {
            holder.itemView.setBackgroundResource(R.color.PrimaryAccent);
        } else {
            holder.itemView.setBackgroundResource(R.color.PrimaryVarient);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("id",userModel.getUserID());
                intent.putExtra("name", userModel.getUserName());
                context.startActivity(intent);
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                toggleSelection(userModel);
                return true;
            }
        });

    }

    @Override
    public int getItemCount() {
        return userModelList.size();
    }


    public List<UserModel> getUserModelList(){
        return userModelList;
    }
    public class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView name, email;
        public MyViewHolder(View itemView){
            super(itemView);
            name = itemView.findViewById(R.id.username);
            email = itemView.findViewById(R.id.useremail);
        }
    }

    private void toggleSelection(UserModel userModel) {
        if (selectedUsers.contains(userModel)) {
            selectedUsers.remove(userModel);
        } else {
            selectedUsers.add(userModel);
        }
        if(selectedUsers.size() > 1){
            imageView.setAlpha(1.0f);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showGroupNameDialog();
                }
            });
        }
        notifyDataSetChanged();
    }

    private void showGroupNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final EditText editTextGroupName = new EditText(context);
        editTextGroupName.setHint("Enter group name");

        builder.setView(editTextGroupName)
                .setTitle("Enter Group Name")
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        stringIds.append(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        for(UserModel userModel1: selectedUsers){
                            stringIds.append(userModel1.getUserID());
                            stringGroupUserNames.append(userModel1.getUserName());
                        }
                        String groupName = editTextGroupName.getText().toString();
                        Intent intent = new Intent(context, GroupChatActivity.class);
                        Log.d("checking group ids", stringIds.toString());
                        Log.d("my own id", FirebaseAuth.getInstance().getCurrentUser().getUid());
                        intent.putExtra("groupIds", stringIds.toString());
                        intent.putExtra("groupUserNames", stringGroupUserNames.toString());
                        intent.putExtra("groupName", groupName);
                        intent.putExtra("groupCreator", FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
                        intent.putExtra("groupSender", FirebaseAuth.getInstance().getCurrentUser().getUid());
                        context.startActivity(intent);

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private Filter userFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<UserModel> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(userModelListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (UserModel userModel : userModelListFull) {
                    if (userModel.getUserName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(userModel);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            userModelList.clear();
            userModelList.addAll((List<UserModel>) results.values);
            notifyDataSetChanged();
        }
    };
    //return the search
    public Filter getFilter() {
        return userFilter;
    }

    public void setImageView(ImageView imageView1){
        imageView = imageView1;
    }
}
