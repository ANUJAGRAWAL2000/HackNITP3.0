package com.example.chatapp.SelectFriend;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapp.Common.Constants;
import com.example.chatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class SelectFriendAdapter extends RecyclerView.Adapter<SelectFriendAdapter.SelectFriendViewHolder> {

    private Context context;
    private List<SelectFriendModel> selectFriendModelList;

    public SelectFriendAdapter(Context context, List<SelectFriendModel> selectFriendModelList) {
        this.context = context;
        this.selectFriendModelList = selectFriendModelList;
    }

    @NonNull
    @Override
    public SelectFriendAdapter.SelectFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.selectfriendlayout,parent,false);
        return new SelectFriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectFriendAdapter.SelectFriendViewHolder holder, int position) {

        SelectFriendModel selectFriendModel=selectFriendModelList.get(position);

        holder.tvSelectFriend.setText(selectFriendModel.getUserName());

        StorageReference photoRef= FirebaseStorage.getInstance().getReference().child(Constants.IMAGE_FOLDER+"/"+selectFriendModel.getPhotoName());

        if(selectFriendModel.getPhotoName()!=null) {
            photoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(context)
                            .load(uri)
                            .placeholder(R.drawable.ic_profile)
                            .error(R.drawable.ic_profile)
                            .into(holder.ivSelectFriend);
                }
            });
        }

        holder.llSelectFriend.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                if(context instanceof SelectFriendActivity)
                {
                    ((SelectFriendActivity)context).returnSelectedFriend(selectFriendModel.getUserName(),selectFriendModel.getUserId(),selectFriendModel.getUserId()+".jpg");
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return selectFriendModelList.size();
    }

    public class SelectFriendViewHolder extends RecyclerView.ViewHolder{

        private LinearLayout llSelectFriend;
        private TextView tvSelectFriend;
        private ImageView ivSelectFriend;

        public SelectFriendViewHolder(@NonNull View itemView) {
            super(itemView);

            llSelectFriend=itemView.findViewById(R.id.llSelectFriend);
            ivSelectFriend=itemView.findViewById(R.id.ivSelectFriend);
            tvSelectFriend=(TextView) itemView.findViewById(R.id.tvSelectFriend);

        }
    }

}
