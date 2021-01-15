package com.example.chatapp.RequestReceived;

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
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {

    private Context context;
    private List<RequestReceived> requestList;
    private DatabaseReference databaseReferenceFriendRequest,databaseReferenceChats;
    private FirebaseUser getCurrentUser;

    public RequestAdapter(Context context, List<RequestReceived> requestList)
    {
        this.context = context;
        this.requestList = requestList;
    }

    private void HandleException(RequestViewHolder Holder,Exception exception)
    {
        Toast.makeText(context,"Failed to Accept Request: %1$s"+exception,Toast.LENGTH_SHORT).show();
        Holder.progressRequest.setVisibility(View.GONE);
        Holder.Accept.setVisibility(View.VISIBLE);
        Holder.Deny.setVisibility(View.VISIBLE);
    }

    @NonNull
    @Override
    public RequestAdapter.RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.request_received,parent,false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestAdapter.RequestViewHolder holder, int position) {

        final RequestReceived request=requestList.get(position);

        holder.tvFullName.setText(request.getUserName());

        StorageReference fileRef= FirebaseStorage.getInstance().getReference().child("Images/"+request.getPhotoName());
        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(context)
                        .load(uri)
                        .placeholder(R.drawable.ic_user_2)
                        .error(R.drawable.ic_user_2)
                        .into(holder.Profile);
            }
        });

        databaseReferenceFriendRequest= FirebaseDatabase.getInstance().getReference().child(Node.Friend_Requests);

        getCurrentUser= FirebaseAuth.getInstance().getCurrentUser();

        databaseReferenceChats=FirebaseDatabase.getInstance().getReference().child(Node.Chats);

        holder.Accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                holder.progressRequest.setVisibility(View.VISIBLE);
                holder.Accept.setVisibility(View.GONE);
                holder.Deny.setVisibility(View.GONE);

                final String userId=request.getUserId();

                databaseReferenceChats.child(getCurrentUser.getUid()).child(userId)
                        .child(Node.TIME_STAMP).setValue(ServerValue.TIMESTAMP)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            databaseReferenceChats.child(userId).child(getCurrentUser.getUid()).child(Node.TIME_STAMP).setValue(ServerValue.TIMESTAMP)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        databaseReferenceFriendRequest.child(getCurrentUser.getUid()).child(userId).child(Node.Request_Type).setValue("Accepted")
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful())
                                                {
                                                    databaseReferenceFriendRequest.child(userId).child(getCurrentUser.getUid()).child(Node.Request_Type).setValue("Accepted")
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful())
                                                            {

                                                                String title="New Friend Accepted";
                                                                String message="Friend request Accepted By"+getCurrentUser.getDisplayName();


                                                                Internet.SendNotification(getCurrentUser.getUid(),context,title,message);


                                                                holder.progressRequest.setVisibility(View.GONE);
                                                                holder.Accept.setVisibility(View.VISIBLE);
                                                                holder.Deny.setVisibility(View.VISIBLE);
                                                            }
                                                            else
                                                            {
                                                                HandleException(holder,task.getException());
                                                            }
                                                        }
                                                    });
                                                }
                                                else
                                                {
                                                    HandleException(holder,task.getException());
                                                }
                                            }
                                        });
                                        }
                                    else
                                    {
                                        HandleException(holder,task.getException());
                                    }
                                }
                            });
                        }
                        else
                        {
                            HandleException(holder,task.getException());
                        }
                    }
                });
                }
        });

        holder.Deny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.progressRequest.setVisibility(View.VISIBLE);
                holder.Accept.setVisibility(View.GONE);
                holder.Deny.setVisibility(View.GONE);

                final String userId=request.getUserId();
                databaseReferenceFriendRequest.child(getCurrentUser.getUid()).child(userId).child(Node.Request_Type).setValue(null)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            databaseReferenceFriendRequest.child(userId).child(getCurrentUser.getUid()).child(Node.Request_Type).setValue(null)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                Toast.makeText(context,"Denied Request Successfully",Toast.LENGTH_SHORT).show();

                                                String title="Friend Request Denied";
                                                String message="Friend request denied By"+getCurrentUser.getDisplayName();

                                                Internet.SendNotification(getCurrentUser.getUid(),context,title,message);


                                                holder.progressRequest.setVisibility(View.GONE);
                                                holder.Accept.setVisibility(View.VISIBLE);
                                                holder.Deny.setVisibility(View.VISIBLE);
                                            }
                                            else
                                            {
                                                Toast.makeText(context,"Failed to Deny Request : %1$s"+task.getException(),Toast.LENGTH_SHORT).show();
                                                holder.progressRequest.setVisibility(View.GONE);
                                                holder.Accept.setVisibility(View.VISIBLE);
                                                holder.Deny.setVisibility(View.VISIBLE);
                                            }
                                        }
                                    });
                        }
                        else
                        {
                            Toast.makeText(context,"Failed to Deny Request : %1$s"+task.getException(),Toast.LENGTH_SHORT).show();
                            holder.progressRequest.setVisibility(View.GONE);
                            holder.Accept.setVisibility(View.VISIBLE);
                            holder.Deny.setVisibility(View.VISIBLE);
                        }
                    }
                });


            }
        });
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    public class RequestViewHolder extends RecyclerView.ViewHolder
    {
        private TextView tvFullName;
        private ImageView Profile;
        private Button Accept,Deny;
        private ProgressBar progressRequest;

        public RequestViewHolder(@NonNull View itemView)
        {
            super(itemView);
            tvFullName=itemView.findViewById(R.id.Name);
            Profile=itemView.findViewById(R.id.ImageRequest);
            Accept=itemView.findViewById(R.id.Accept);
            Deny=itemView.findViewById(R.id.Deny);
            progressRequest=itemView.findViewById(R.id.pbRequest);

        }
    }
}
