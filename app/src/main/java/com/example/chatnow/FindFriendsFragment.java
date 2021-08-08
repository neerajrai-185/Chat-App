package com.example.chatnow;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.example.chatnow.Common.Constants;
import com.example.chatnow.Common.NodeNames;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class FindFriendsFragment extends Fragment {
    private RecyclerView recyclerViewFindFriends;
    private FindFriendsAdapter findFriendsAdapter;
    private List<FindFriendsModel> findFriendsModelList;
    private TextView textViewEmptyFriendList;

    private DatabaseReference databaseReference,databaseReferenceFriendRequests;
    private FirebaseUser currentUser;
    private View viewProgressBar;



    public FindFriendsFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_find_friends, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerViewFindFriends = view.findViewById(R.id.rvFindFriends);
        viewProgressBar = view.findViewById(R.id.progressBar);

        textViewEmptyFriendList = view. findViewById(R.id.tvEmptyFriendsList);

        recyclerViewFindFriends .setLayoutManager(new LinearLayoutManager(getActivity()));

        findFriendsModelList = new ArrayList<>();
        findFriendsAdapter = new FindFriendsAdapter(getActivity(),findFriendsModelList);

        recyclerViewFindFriends.setAdapter(findFriendsAdapter);




        databaseReference = FirebaseDatabase.getInstance().getReference().child(NodeNames.USERS);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();


        databaseReferenceFriendRequests = FirebaseDatabase.getInstance().getReference().child(NodeNames.FRIEND_REQUESTS).child(currentUser.getUid());



        textViewEmptyFriendList.setVisibility(View.VISIBLE);
        viewProgressBar.setVisibility(View.VISIBLE);



        Query query = databaseReference.orderByChild(NodeNames.NAME);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                findFriendsModelList.clear();

                for (DataSnapshot ds : snapshot.getChildren())
                {
                    String userId = ds.getKey();

                    if (userId.equals(currentUser.getUid()))
                        continue;
                    if (ds.child(NodeNames.NAME).getValue()!=null)
                    {
                       final String fullName = ds.child(NodeNames.NAME).getValue().toString();
                        final String photoName = ds.child(NodeNames.PHOTO).getValue()!=null? ds.child(NodeNames.PHOTO).getValue().toString():"";//Here, I have used ternary Operator.



                        databaseReferenceFriendRequests.child(userId).addListenerForSingleValueEvent(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists())
                                {

                                    String requestType = snapshot.child(NodeNames.REQUEST_TYPE).getValue().toString();
                                    if (requestType.equals(Constants.REQUEST_STATUS_SENT))
                                    {
                                        findFriendsModelList.add(new FindFriendsModel(fullName,photoName,userId,true));
                                        findFriendsAdapter.notifyDataSetChanged();
                                    }
                                }

                                else
                                    {
                                        findFriendsModelList.add(new FindFriendsModel(fullName,photoName,userId,false));
                                        findFriendsAdapter.notifyDataSetChanged();


                                    }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                                viewProgressBar.setVisibility(View.GONE);
                            }
                        });



                        textViewEmptyFriendList.setVisibility(View.GONE);
                        viewProgressBar.setVisibility(View.GONE);
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                viewProgressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(),"Failed to fetch friends"+error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });




    }
}