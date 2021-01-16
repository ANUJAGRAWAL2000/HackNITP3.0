package com.example.chatapp.Chat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapp.Common.Constants;
import com.example.chatapp.R;
import com.example.chatapp.SelectFriend.SelectFriendActivity;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private Context context;
    private List<MessageModel> messageModelList;
    private FirebaseAuth mAuth;
    private ActionMode actionMode;
    private ConstraintLayout selectedView;

    public MessageAdapter(android.content.Context context, List<MessageModel> messageModelList) {
        this.context = context;
        this.messageModelList = messageModelList;
    }

    @NonNull
    @Override
    public MessageAdapter.MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
         View view= LayoutInflater.from(context).inflate(R.layout.message_layout,parent,false);
        return new MessageViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.MessageViewHolder holder, int position) {

        MessageModel messageModel=messageModelList.get(position);

        SimpleDateFormat sdf=new SimpleDateFormat("dd-MM-yyyy HH:mm");
        String dateTime=sdf.format(messageModel.getTimestamp());
        String [] splitString=dateTime.split(" ");
        String messageTime=splitString[1];

        String currentUser=mAuth.getCurrentUser().getUid();

        if(messageModel.getMessage_from().equals(currentUser))
        {
            if(messageModel.getMessage_type().equals(Constants.MESSAGE_TYPE_TEXT)  ||  messageModel.getMessage_type().equals("Text"))
            {
                holder.llSend.setVisibility(View.VISIBLE);
                holder.llSentImage.setVisibility(View.GONE);
            }
            else
            {
                holder.llSend.setVisibility(View.GONE);
                holder.llSentImage.setVisibility(View.VISIBLE);
            }
            holder.llReceivedImage.setVisibility(View.GONE);
            holder.llReceived.setVisibility(android.view.View.GONE);
            holder.tvSendMessageTime.setText(messageTime);
            holder.tvSendMessage.setText(messageModel.getMessage());
            holder.tvSentImageTime.setText(messageTime);

            Glide.with(context)
                    .load(messageModel.getMessage())
                    .placeholder(R.drawable.ic_profile)
                    .into(holder.ivSent);
        }
        else
        {
            if(messageModel.getMessage_type().equals(Constants.MESSAGE_TYPE_TEXT)  ||  messageModel.getMessage_type().equals("Text") )
            {
                holder.llReceived.setVisibility(android.view.View.VISIBLE);
                holder.llReceivedImage.setVisibility(View.GONE);
            }
            else
            {
                holder.llReceived.setVisibility(View.GONE);
                holder.llReceivedImage.setVisibility(View.VISIBLE);
            }
            holder.llSentImage.setVisibility(View.GONE);
            holder.llSend.setVisibility(android.view.View.GONE);
            holder.tvReceivedMessageTime.setText(messageTime);
            holder.tvReceivedMessage.setText(messageModel.getMessage());
            holder.tvReceivedImageTime.setText(messageTime);
            Glide.with(context)
                    .load(messageModel.getMessage())
                    .placeholder(R.drawable.ic_profile)
                    .into(holder.ivReceived);
        }

        holder.clMessage.setTag(R.id.TAG_MESSAGE,messageModel.getMessage());
        holder.clMessage.setTag(R.id.TAG_MESSAGE_ID,messageModel.getMessage_id());
        holder.clMessage.setTag(R.id.TAG_MESSAGE_TYPE,messageModel.getMessage_type());

        holder.clMessage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                String messageType=v.getTag(R.id.TAG_MESSAGE_TYPE).toString();
                Uri uri=Uri.parse(v.getTag(R.id.TAG_MESSAGE).toString());
                if(messageModel.getMessage_type().equals(Constants.MESSAGE_TYPE_VIDEO))
                {
                    Intent I=new Intent(Intent.ACTION_VIEW,uri);
                    I.setDataAndType(uri,"video/mp4");
                    context.startActivity(I);
                }
                else if(messageModel.getMessage_type().equals(Constants.MESSAGE_TYPE_IMAGE))
                {
                    Intent I=new Intent(Intent.ACTION_VIEW,uri);
                    I.setDataAndType(uri,"image/jpg");
                    context.startActivity(I);
                }
            }
        });

        holder.clMessage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                //It means that action mode is already instantiate
                if(actionMode!=null)
                return false;

                selectedView=holder.clMessage;

                actionMode=((AppCompatActivity)context).startSupportActionMode(actionModeCallBack);

                //It will also change the color of Action Mode So we will define the color for actionmode in theme.xml File
                holder.clMessage.setBackgroundColor(context.getResources().getColor(R.color.teal_700));

                return true;

            }
        });
    }

    @Override
    public int getItemCount()
    {
        return messageModelList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder
    {

        private LinearLayout llSend,llReceived,llSentImage,llReceivedImage;
        private ImageView ivSent,ivReceived;
        private TextView tvSendMessage,tvSendMessageTime,tvReceivedMessage,tvReceivedMessageTime;
        private TextView tvSentImageTime,tvReceivedImageTime;
        private ConstraintLayout clMessage;

        public MessageViewHolder(@NonNull android.view.View itemView)
        {
            super(itemView);
            llSend=itemView.findViewById(R.id.llSentMessage);
            llReceived=itemView.findViewById(R.id.llReceivedMessage);
            llSentImage=itemView.findViewById(R.id.llSentImage);
            llReceivedImage=itemView.findViewById(R.id.llReceivedImage);
            tvSendMessage=itemView.findViewById(R.id.tvSentMessage);
            tvSendMessageTime=itemView.findViewById(R.id.tvSentMessageTime);
            tvReceivedMessage=itemView.findViewById(R.id.tvReceivedMessage);
            tvReceivedMessageTime=itemView.findViewById(R.id.tvReceivedMessageTime);
            ivSent=itemView.findViewById(R.id.ivSent);
            ivReceived=itemView.findViewById(R.id.ivReceived);
            tvSentImageTime=itemView.findViewById(R.id.tvSentImageTime);
            tvReceivedImageTime=itemView.findViewById(R.id.tvReceivedImageTime);
            mAuth=FirebaseAuth.getInstance();
            clMessage=itemView.findViewById(R.id.clMessage);
        }
    }

    public ActionMode.Callback actionModeCallBack = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            //Here we will set the menu_chat_options to action mode
            MenuInflater inflater=mode.getMenuInflater();
            inflater.inflate(R.menu.menu_chat_options,menu);
            //second parameter menu is the parameter passed to OnCreateActionMode

            //if the messageType is Text then we will not show download menu
            String messageType=String.valueOf(selectedView.getTag(R.id.TAG_MESSAGE_TYPE));
            if(messageType.equals(Constants.MESSAGE_TYPE_TEXT))
            {
                MenuItem itemDownload=menu.findItem(R.id.mnuDownload);
                itemDownload.setVisible(false);
            }
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int itemId=item.getItemId();
            String MessageId=selectedView.getTag(R.id.TAG_MESSAGE_ID).toString();
            String Message=selectedView.getTag(R.id.TAG_MESSAGE).toString();
            String MessageType=selectedView.getTag(R.id.TAG_MESSAGE_TYPE).toString();
            switch (itemId)
            {
                case R.id.mnuDelete:
                    if(context instanceof SendActivity)
                    {
                        ((SendActivity)context).Delete(MessageId,MessageType);
                    }
                    actionMode.finish();
                    break;
                case R.id.mnuShare:
                    if(MessageType.equals(Constants.MESSAGE_TYPE_TEXT))
                    {
                        Intent intentShare=new Intent();
                        intentShare.setAction(Intent.ACTION_SEND);
                        intentShare.putExtra(Intent.EXTRA_TEXT,Message);
                        intentShare.setType("text/plain");
                        context.startActivity(intentShare);
                    }
                    else
                    {
                        if(context instanceof SendActivity)
                        {
                            ((SendActivity)context).Download(MessageId,MessageType,true);
                        }
                    }
                    actionMode.finish();
                    break;
                case R.id.mnuDownload:
                    if(context instanceof SendActivity)
                    {
                        ((SendActivity)context).Download(MessageId,MessageType,false);
                    }
                    actionMode.finish();
                    break;
                case R.id.mnuForward:
                    if(context instanceof SendActivity)
                    {
                        ((SendActivity)context).Forward(MessageId,MessageType,Message);
                    }
                    actionMode.finish();
                    break;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
                actionMode=null;
                //When we will Delete the message so the backgound colour is not removing
                //So to handle that we will set BAckground color.
                selectedView.setBackgroundColor(context.getResources().getColor(R.color.selectedViewBackground));
        }
    };
}