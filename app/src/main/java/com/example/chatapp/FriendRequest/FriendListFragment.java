package com.example.chatapp.FriendRequest;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapp.Common.Node;
import com.example.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FriendListFragment extends Fragment {

    private RecyclerView rvFindFriend;
    private TextView tvFriend;
    private View ProgressBar;

    private List<FriendRequest> friendRequestList;
    private FindFriendAdapter findFriendAdapter;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference,databaseReferenceFriendRequest;
    private FirebaseUser currentUser;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FriendListFragment() {
        // Required empty public constructor
    }

    public static FriendListFragment newInstance(String param1, String param2) {
        FriendListFragment fragment = new FriendListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_friend_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvFindFriend=view.findViewById(R.id.rvFriendLists);
        tvFriend=view.findViewById(R.id.tvFriendList);
        ProgressBar=view.findViewById(R.id.pbFriendRequest);


        rvFindFriend.setLayoutManager(new LinearLayoutManager(getActivity()));

        friendRequestList=new ArrayList<>();
        findFriendAdapter=new FindFriendAdapter(getActivity(),friendRequestList);
        rvFindFriend.setAdapter(findFriendAdapter);

        databaseReference= FirebaseDatabase.getInstance().getReference().child(Node.Users);
        currentUser=FirebaseAuth.getInstance().getCurrentUser();

        databaseReferenceFriendRequest=FirebaseDatabase.getInstance().getReference().child(Node.Friend_Requests).child(currentUser.getUid());
        tvFriend.setVisibility(View.VISIBLE);
        ProgressBar.setVisibility(View.VISIBLE);

        Query query=databaseReference.orderByChild(Node.Name);

        query.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                friendRequestList.clear();

                for(DataSnapshot db : dataSnapshot.getChildren())
                {
                    String userId=db.getKey();

                    if(userId.equals(currentUser.getUid()))
                        continue;

                    if(db.child(Node.Name).getValue()!=null)
                    {
                        final String FullName =db.child(Node.Name).getValue().toString();

                            String photoName =db.child(Node.Photo).getValue()!=null?
                                    db.child(Node.Photo).getValue().toString():null;

                            databaseReferenceFriendRequest.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        String requestType = dataSnapshot.child(Node.Request_Type).getValue().toString();
                                        if (requestType.equals("sent")) {
                                            friendRequestList.add(new FriendRequest(FullName, photoName, userId, true));
                                            findFriendAdapter.notifyDataSetChanged();
                                        }
                                    } else {
                                        friendRequestList.add(new FriendRequest(FullName, photoName, userId, false));
                                        findFriendAdapter.notifyDataSetChanged();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    ProgressBar.setVisibility(View.GONE);
                                }
                            });
                            tvFriend.setVisibility(View.GONE);
                            ProgressBar.setVisibility(View.GONE);
//                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                ProgressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(),"Failed To Fetch Friends %1$s"+databaseError.getMessage(),Toast.LENGTH_SHORT).show();
                //getContext;
            }
        });
    }
}