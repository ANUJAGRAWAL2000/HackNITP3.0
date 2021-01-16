package com.example.chatapp.SelectFriend;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.chatapp.Common.Extras;
import com.example.chatapp.Common.Node;
import com.example.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SelectFriendActivity extends AppCompatActivity {

    private RecyclerView rvSelectedFriend;
    private View ProgressBar;
    private DatabaseReference databaseReferenceChats,databaseReferenceUsers;
    private FirebaseUser currentUser;
    private List<SelectFriendModel> selectFriendModelList;
    private SelectFriendAdapter selectFriendAdapter;
    private ValueEventListener valueEventListener;

    private String selectedMessage,selectedMessageId,selectedMessageType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_friend);

        if(getIntent().hasExtra(Extras.Message))
        {
        selectedMessage=getIntent().getStringExtra(Extras.Message);
        selectedMessageId=getIntent().getStringExtra(Extras.Message_ID);
        selectedMessageType=getIntent().getStringExtra(Extras.Message_TYPE);
        }

        rvSelectedFriend=findViewById(R.id.rvSelectFriend);
        ProgressBar=findViewById(R.id.pbSelectFriend);

        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        rvSelectedFriend.setLayoutManager(linearLayoutManager);

        selectFriendModelList=new ArrayList<>();
        selectFriendAdapter=new SelectFriendAdapter(this,selectFriendModelList);
        rvSelectedFriend.setAdapter(selectFriendAdapter);

        ProgressBar.setVisibility(View.VISIBLE);
        currentUser= FirebaseAuth.getInstance().getCurrentUser();

        databaseReferenceChats=FirebaseDatabase.getInstance().getReference().child(Node.Chats).child(currentUser.getUid());
        databaseReferenceUsers=FirebaseDatabase.getInstance().getReference().child(Node.Users);

        valueEventListener=new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds:dataSnapshot.getChildren())
                {
                   final String userID=ds.getKey();
                   databaseReferenceUsers.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String UserName=dataSnapshot.child(Node.Name).getValue()!=null?
                                    dataSnapshot.child(Node.Name).getValue().toString():
                                    " ";

                            String PhotoName=dataSnapshot.child(Node.Photo).getValue()!=null
                                    ?userID+".jpg":"";

                            SelectFriendModel selectFriendModel=new SelectFriendModel(userID,UserName,PhotoName);

                            selectFriendModelList.add(selectFriendModel);

                            selectFriendAdapter.notifyDataSetChanged();

                            ProgressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
Toast.makeText(SelectFriendActivity.this,
        "Failed to Fetch Friend List : %1$s"+databaseError.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(SelectFriendActivity.this,
                        "Failed to Fetch Friend List : %1$s"+databaseError.getMessage(),Toast.LENGTH_SHORT).show();
            }
        };

        databaseReferenceChats.addValueEventListener(valueEventListener);
    }

    public void returnSelectedFriend(String UserName,String UserID,String PhotoName)
    {
        databaseReferenceChats.removeEventListener(valueEventListener);

        Intent I=new Intent();
        I.putExtra(Extras.UserKey,UserID);
        I.putExtra(Extras.UserName,UserName);
        I.putExtra(Extras.PhotoName,PhotoName);

        I.putExtra(Extras.Message,selectedMessage);
        I.putExtra(Extras.Message_ID,selectedMessageId);
        I.putExtra(Extras.Message_TYPE,selectedMessageType);

        //SetResult Function for returning
        setResult(Activity.RESULT_OK,I);
        //Finish For Ending the SelectFriendActivity
        finish();
    }
}