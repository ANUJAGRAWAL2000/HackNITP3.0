package com.example.chatapp.RequestReceived;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
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
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RequestFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RequestFragment extends Fragment {

    private RecyclerView rvRequestReceived;
    private TextView tvRequestReceived;
    private RequestAdapter requestAdapter;
    private List<RequestReceived> requestList;

    private View progressBar;
    private FirebaseUser getCurrentUser;
    private DatabaseReference databaseReferenceNode,databaseReferenceFriendRequest;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public RequestFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RequestFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RequestFragment newInstance(String param1, String param2) {
        RequestFragment fragment = new RequestFragment();
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
        return inflater.inflate(R.layout.fragment_request, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvRequestReceived=view.findViewById(R.id.rvRequestLists);
        tvRequestReceived=view.findViewById(R.id.tvRequestList);
        progressBar=view.findViewById(R.id.pbRequestReceived);

        rvRequestReceived.setLayoutManager(new LinearLayoutManager(getActivity()));

        requestList=new ArrayList<>();
        requestAdapter=new RequestAdapter(getActivity(),requestList);
        rvRequestReceived.setAdapter(requestAdapter);

        getCurrentUser= FirebaseAuth.getInstance().getCurrentUser();

        databaseReferenceNode= FirebaseDatabase.getInstance().getReference().child(Node.Users);

        databaseReferenceFriendRequest=FirebaseDatabase.getInstance().getReference().child(Node.Friend_Requests).child(getCurrentUser.getUid());

        tvRequestReceived.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        databaseReferenceFriendRequest.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.GONE);

                requestList.clear();

                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.exists()) {
                        String requestType = ds.child(Node.Request_Type).getValue().toString();
                        if (requestType.equals("received")) {
                            final String userId = ds.getKey();
                            databaseReferenceNode.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    String UserName = dataSnapshot.child(Node.Name).getValue().toString();
                                    String PhotoName = "";
                                    if (dataSnapshot.child(Node.Photo).getValue() != null) {
                                        PhotoName = dataSnapshot.child(Node.Photo).getValue().toString();
                                    }
                                    RequestReceived requestReceived = new RequestReceived(userId, UserName, PhotoName);
                                    requestList.add(requestReceived);
                                    requestAdapter.notifyDataSetChanged();
                                    tvRequestReceived.setVisibility(View.GONE);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(getActivity(), "Failed to Fetch List: %1$s" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
                {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getActivity(),"Failed to Fetch List: %1$s"+ databaseError.getMessage(),Toast.LENGTH_SHORT).show();
                }
        });
    }
}