package com.example.chatapp.FriendRequest;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapp.Common.Internet;
import com.example.chatapp.Common.Node;
import com.example.chatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class FindFriendAdapter extends RecyclerView.Adapter<FindFriendAdapter.FindFriendViewHolder>
{

    private Context context;
    private List<FriendRequest> friendRequestList;

    private DatabaseReference friendDatabaseReference;
    private FirebaseUser getCurrentUser;
    private String userId;

    public FindFriendAdapter(Context context, List<FriendRequest> friendRequestList)
    {
        this.context = context;
        this.friendRequestList = friendRequestList;
    }

    @NonNull
    @Override
    public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(context).inflate(R.layout.friendrequest,parent,false);
        return new FindFriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FindFriendViewHolder holder, int position) {
        final FriendRequest findFriend=friendRequestList.get(position);

        holder.UserName.setText(findFriend.getUserName());

        StorageReference fileRef= FirebaseStorage.getInstance().getReference().child("Images/"+findFriend.getPhotoName());
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

        friendDatabaseReference= FirebaseDatabase.getInstance().getReference().child(Node.Friend_Requests);
        getCurrentUser= FirebaseAuth.getInstance().getCurrentUser();

        if(findFriend.isRequestSent())
        {
            holder.requestSent.setVisibility(View.GONE);
            holder.cancelRequest.setVisibility(View.VISIBLE);
        }
        else
        {
            holder.requestSent.setVisibility(View.VISIBLE);
            holder.cancelRequest.setVisibility(View.GONE);
        }

        holder.requestSent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                holder.frprogress.setVisibility(View.VISIBLE);
                holder.requestSent.setVisibility(View.GONE);

                userId=findFriend.getUserId();
                //This is the Id of The person Whom we are sending request..

                friendDatabaseReference.child(getCurrentUser.getUid()).child(userId).child(Node.Request_Type)
                        .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            friendDatabaseReference.child(userId).child(getCurrentUser.getUid()).child(Node.Request_Type)
                                    .setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        Toast.makeText(context,"Request Sent Successfully",Toast.LENGTH_SHORT).show();

                                        //making a Call For SendNotification when we make a Friend Request;

                                        String title="New Friend Request";
                                        String message="Friend request From"+getCurrentUser.getDisplayName();
                                        Internet.SendNotification(getCurrentUser.getUid(),context,title,message);


                                        holder.cancelRequest.setVisibility(View.VISIBLE);
                                        holder.frprogress.setVisibility(View.GONE);
                                    }
                                    else
                                    {
                                        Toast.makeText(context,"Failed to Sent request %1$s"+task.getException(),Toast.LENGTH_SHORT).show();
                                        holder.cancelRequest.setVisibility(View.GONE);
                                        holder.frprogress.setVisibility(View.GONE);
                                        holder.requestSent.setVisibility(View.VISIBLE);
                                    }
                                }
                            });
                        }
                        else
                        {
                            Toast.makeText(context,"Failed to Sent request %1$s"+task.getException(),Toast.LENGTH_SHORT).show();
                            holder.cancelRequest.setVisibility(View.GONE);
                            holder.frprogress.setVisibility(View.GONE);
                            holder.requestSent.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        });

        holder.cancelRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.frprogress.setVisibility(View.VISIBLE);
                holder.cancelRequest.setVisibility(View.GONE);

                userId=findFriend.getUserId();
                //This is the Id of The person Whom we are sending request..

                friendDatabaseReference.child(getCurrentUser.getUid()).child(userId).child(Node.Request_Type)
                        .setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            friendDatabaseReference.child(userId).child(getCurrentUser.getUid()).child(Node.Request_Type)
                                    .setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        Toast.makeText(context,"Request Cancelled Successfully",Toast.LENGTH_SHORT).show();
                                        holder.requestSent.setVisibility(View.VISIBLE);
                                        holder.frprogress.setVisibility(View.GONE);
                                    }
                                    else
                                    {
                                        Toast.makeText(context,"Failed to Cancelled request %1$s"+task.getException(),Toast.LENGTH_SHORT).show();
                                        holder.cancelRequest.setVisibility(View.VISIBLE);
                                        holder.frprogress.setVisibility(View.GONE);
                                        holder.requestSent.setVisibility(View.GONE);
                                    }
                                }
                            });
                        }
                        else
                        {
                            Toast.makeText(context,"Failed to Cancelled request %1$s"+task.getException(),Toast.LENGTH_SHORT).show();
                            holder.cancelRequest.setVisibility(View.VISIBLE);
                            holder.frprogress.setVisibility(View.GONE);
                            holder.requestSent.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });

    }

    @Override
    public int getItemCount()
    {
        return friendRequestList.size();
    }

    public class FindFriendViewHolder extends RecyclerView.ViewHolder
    {
        private ImageView ivProfile;
        private TextView UserName;
        private Button requestSent,cancelRequest;
        private ProgressBar frprogress;

        public FindFriendViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfile=itemView.findViewById(R.id.ImageFriend);
            UserName=itemView.findViewById(R.id.Name);
            requestSent=itemView.findViewById(R.id.FriendRequest);
            cancelRequest=itemView.findViewById(R.id.CancelRequest);
            frprogress=itemView.findViewById(R.id.pvFriendRequest);


        }
    }
}