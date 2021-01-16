package com.example.chatapp.Chat;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapp.Common.Constants;
import com.example.chatapp.Common.Extras;
import com.example.chatapp.Common.Internet;
import com.example.chatapp.Common.Node;
import com.example.chatapp.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class chatListAdapter extends RecyclerView.Adapter<chatListAdapter.ChatListViewHolder> {

    private Context context;
    private List<ChatListModel> chatListModelList;
    private View ChatToHide;
    private DatabaseReference databaseReferenceChats;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;

    public chatListAdapter(Context context, List<ChatListModel> chatListModelList) {
        this.context = context;
        this.chatListModelList = chatListModelList;
    }

    @NonNull
    @Override
    public chatListAdapter.ChatListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chatlist,parent,false);
        return new ChatListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull chatListAdapter.ChatListViewHolder holder, int position) {

        ChatListModel chatListModel=chatListModelList.get(position);

        if(!chatListModel.getUnReadCount().equals("0"))
        {
        holder.tvUnReadCount.setVisibility(View.VISIBLE);
        holder.tvUnReadCount.setText(chatListModel.getUnReadCount());
        }
        else
        {
        holder.tvUnReadCount.setVisibility(View.GONE);
        }

        if(!chatListModel.getLastMessage().equals("")) {
            String LastMessage=chatListModel.getLastMessage();
            LastMessage=LastMessage.length()>30?LastMessage.substring(0,30):LastMessage;
            holder.tvLastMessage.setText(LastMessage);
        }

        if(!TextUtils.isEmpty(chatListModel.getLastMessageTime())) {
            holder.tvLastMessageTime.setText(Internet.MessageTime(Long.parseLong(chatListModel.getLastMessageTime())));
        }

        holder.fullName.setText(chatListModel.getUserName());

        StorageReference fileRef= FirebaseStorage.getInstance().getReference().child("Images/"+chatListModel.getPhotoName());
        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(context)
                        .load(uri)
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .into(holder.ivProfile);
            }
        });

        holder.llChatList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent I=new Intent(context, SendActivity.class);
                I.putExtra(Extras.UserKey,chatListModel.getUserId());
                I.putExtra(Extras.UserName,chatListModel.getUserName());
                I.putExtra(Extras.PhotoName,chatListModel.getPhotoName());
                context.startActivity(I);
            }
        });

        //TODO

        holder.llChatList.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                databaseReferenceChats.child(firebaseUser.getUid()).child(chatListModel.getUserId()).child(Node.HIDE).setValue(Constants.TRUE);
//                view.setVisibility(View.GONE);
                return true;
            }
        });

    }

    @Override
    public int getItemCount() {
        return chatListModelList.size();
    }


    public class ChatListViewHolder extends RecyclerView.ViewHolder{

        private LinearLayout llChatList;
        private TextView fullName,tvLastMessage,tvUnReadCount,tvLastMessageTime;
        private ImageView ivProfile;

        public ChatListViewHolder(@NonNull View itemView) {
            super(itemView);

            llChatList=itemView.findViewById(R.id.llChatList);
            fullName=itemView.findViewById(R.id.tvFullName);
            tvLastMessage=itemView.findViewById(R.id.tvLastMessage);
            tvUnReadCount=itemView.findViewById(R.id.tvUnreadCount);
            tvLastMessageTime=itemView.findViewById(R.id.tvLastMessageTime);
            ivProfile=itemView.findViewById(R.id.ivProfile);
            databaseReferenceChats= FirebaseDatabase.getInstance().getReference().child(Node.Chats);
            mAuth=FirebaseAuth.getInstance();
            firebaseUser=mAuth.getCurrentUser();
        }
    }
}
