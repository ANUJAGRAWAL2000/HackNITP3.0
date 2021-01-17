package com.example.chatapp.Chat;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapp.Common.Node;
import com.example.chatapp.MainActivity;
import com.example.chatapp.Profile.ProfileActivity;
import com.example.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatFragment extends Fragment {

    private RecyclerView rvChats;
    private TextView tvChats;
    private chatListAdapter chatAdapter;
    private List<ChatListModel> chatModelList;
    private View progressBar;
    private Query query;

    List<String> userIds;

    private ChildEventListener childEventListener;
    private DatabaseReference databaseReferenceChats,databaseReferenceUsers;
    private FirebaseUser currentUser;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ChatFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChatFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatFragment newInstance(String param1, String param2) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvChats=view.findViewById(R.id.rvChats);
        tvChats=view.findViewById(R.id.tvChat);

        chatModelList=new ArrayList<>();
        chatAdapter=new chatListAdapter(getActivity(),chatModelList);

        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        rvChats.setLayoutManager(linearLayoutManager);
        rvChats.setAdapter(chatAdapter);

        progressBar=view.findViewById(R.id.pbchatlist);

        userIds=new ArrayList<>();

        currentUser= FirebaseAuth.getInstance().getCurrentUser();
        databaseReferenceChats= FirebaseDatabase.getInstance().getReference().child(Node.Chats).child(currentUser.getUid());
        databaseReferenceUsers=FirebaseDatabase.getInstance().getReference().child(Node.Users);

        query=databaseReferenceChats.orderByChild(Node.TIME_STAMP);

        childEventListener=new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                progressBar.setVisibility(View.VISIBLE);
                updateList(dataSnapshot,true,dataSnapshot.getKey());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                //When the User will send Message so we have to show UnreadCount that's why we will call the updateList again but
                //this time we will pass isNew parameter false because this is not the new one..
                updateList(dataSnapshot,false,dataSnapshot.getKey());
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        query.addChildEventListener(childEventListener);

        tvChats.setVisibility(View.VISIBLE);
    }

    public void updateList(DataSnapshot dataSnapshot,boolean isNew,String userId)
    {
        progressBar.setVisibility(View.GONE);
        tvChats.setVisibility(View.GONE);

        final String lastMessage,lastMessageTime,unreadCount,Hide;

        lastMessage=dataSnapshot.child(Node.Last_Message).getValue()!=null?
                dataSnapshot.child(Node.Last_Message).getValue().toString():
                "";


        lastMessageTime=dataSnapshot.child(Node.Last_Message_Time).getValue()!=null?
                dataSnapshot.child(Node.Last_Message_Time).getValue().toString():
                "";

        unreadCount=dataSnapshot.child(Node.Unread_Count).getValue()==null?"0":dataSnapshot.child(Node.Unread_Count).getValue().toString();

                Hide=dataSnapshot.child(Node.HIDE).getValue()!=null?dataSnapshot.child(Node.HIDE).getValue().toString():"false";

                if(Hide=="false") {
                    databaseReferenceUsers.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String fullName = dataSnapshot.child(Node.Name).getValue() != null ? dataSnapshot.child(Node.Name).getValue().toString() : "";
                            String photoName = dataSnapshot.child(Node.Photo).getValue() != null ? dataSnapshot.child(Node.Photo).getValue().toString() : "";

                            ChatListModel chatListModel = new ChatListModel(userId, fullName, photoName, lastMessage, lastMessageTime, unreadCount);

                            if (isNew == true) {
                                chatModelList.add(chatListModel);
                                userIds.add(userId);
                            } else {
                                //if the isNew false then we will update only the chat User which is SendingMessages to us..


                                //so we will get the Index of that user which is Sending Messages
                                int IndexOfClickedUser = userIds.indexOf(userId);
                                chatModelList.set(IndexOfClickedUser, chatListModel);
                            }
                            chatAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(getActivity(), "Failed to Fetch chat List : %1$s" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
    }
}