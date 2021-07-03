package com.example.chatnow;


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

import com.bumptech.glide.Glide;
import com.example.chatnow.Common.Constants;
import com.example.chatnow.Common.NodeNames;
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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class FindFriendsAdapter extends RecyclerView.Adapter<FindFriendsAdapter.FindfriendViewHolder>
{
    private Context context;
    private List<FindFriendsModel> findFriendsModelList;





    private DatabaseReference friendRequestDatabase;
    private FirebaseUser currentUser;
    private String userId;







    public FindFriendsAdapter(Context context, List<FindFriendsModel> findFriendsModelList) {
        this.context = context;
        this.findFriendsModelList = findFriendsModelList;
    }


    @NonNull
    @Override
    public FindFriendsAdapter.FindfriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.find_friends_layout,parent,false);
        return new FindfriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FindFriendsAdapter.FindfriendViewHolder holder, int position)
    {
        FindFriendsModel friendsModel = findFriendsModelList.get(position);
        holder.textViewFullName.setText(friendsModel.getUserName());

        StorageReference fileRef = FirebaseStorage.getInstance().getReference().child(Constants.IMAGE_FOLDER + "/" + friendsModel.getPhotoName());
        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(context)
                        .load(uri)
                        .placeholder(R.drawable.iv_profile)
                        .error(R.drawable.iv_profile)
                        .into(holder.imageViewProfile);
            }
        });






        friendRequestDatabase = FirebaseDatabase.getInstance().getReference().child(NodeNames.FRIEND_REQUESTS);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

      if (friendsModel.isRequestSent())
      {
       holder.buttonSendRequest.setVisibility(View.GONE);
       holder.buttonCancelRequest.setVisibility(View.VISIBLE);
      }
      else {
       holder.buttonSendRequest.setVisibility(View.VISIBLE);
       holder.buttonCancelRequest.setVisibility(View.GONE);
      }


        holder.buttonSendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.buttonSendRequest.setVisibility(View.GONE);
                holder.pbRequest.setVisibility(View.VISIBLE);

                userId = friendsModel.getUserId();


                friendRequestDatabase.child(currentUser.getUid()).child(userId).child(NodeNames.REQUEST_TYPE)
                        .setValue(Constants.REQUEST_STATUS_SENT).addOnCompleteListener(new OnCompleteListener<Void>() {


                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            friendRequestDatabase.child(userId).child(currentUser.getUid()).child(NodeNames.REQUEST_TYPE)
                                    .setValue(Constants.REQUEST_STATUS_RECEIVED).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful())
                                    {
                                        Toast.makeText(context,"Request sent successfully",Toast.LENGTH_SHORT).show();
                                        holder.buttonSendRequest.setVisibility(View.GONE);
                                        holder.pbRequest.setVisibility(View.GONE);
                                        holder.buttonCancelRequest.setVisibility(View.VISIBLE);
                                    }
                                    else
                                        {

                                            Toast.makeText(context,"Failed to send request"+task.getException(),Toast.LENGTH_SHORT).show();

                                            holder.buttonSendRequest.setVisibility(View.VISIBLE);
                                            holder.pbRequest.setVisibility(View.GONE);
                                            holder.buttonCancelRequest.setVisibility(View.GONE);
                                    }
                                }
                            });
                        }
                        else
                        {

                            Toast.makeText(context,"Failed to send request"+task.getException(),Toast.LENGTH_SHORT).show();

                            holder.buttonSendRequest.setVisibility(View.VISIBLE);
                            holder.pbRequest.setVisibility(View.GONE);
                            holder.buttonCancelRequest.setVisibility(View.GONE);

                        }
                    }
                });
            }
        });


        holder.buttonCancelRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.buttonCancelRequest.setVisibility(View.GONE);
                holder.pbRequest.setVisibility(View.VISIBLE);

                userId = friendsModel.getUserId();
                friendRequestDatabase.child(currentUser.getUid()).child(userId).child(NodeNames.REQUEST_TYPE)
                        .setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            friendRequestDatabase.child(userId).child(currentUser.getUid()).child(NodeNames.REQUEST_TYPE)
                                    .setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful())
                                    {
                                        Toast.makeText(context,"Request cancel successfully",Toast.LENGTH_SHORT).show();
                                        holder.buttonSendRequest.setVisibility(View.VISIBLE);
                                        holder.pbRequest.setVisibility(View.GONE);
                                        holder.buttonCancelRequest.setVisibility(View.GONE);
                                    }
                                    else
                                    {

                                        Toast.makeText(context,"Failed to cancel request"+task.getException(),Toast.LENGTH_SHORT).show();

                                        holder.buttonSendRequest.setVisibility(View.GONE);
                                        holder.pbRequest.setVisibility(View.GONE);
                                        holder.buttonCancelRequest.setVisibility(View.VISIBLE);
                                    }
                                }
                            });
                        }
                        else
                        {

                            Toast.makeText(context,"Failed to cancel request"+task.getException(),Toast.LENGTH_SHORT).show();

                            holder.buttonSendRequest.setVisibility(View.GONE);
                            holder.pbRequest.setVisibility(View.GONE);
                            holder.buttonCancelRequest.setVisibility(View.VISIBLE);

                        }
                    }
                });
            }
        });
    }




    @Override
    public int getItemCount() {
        return  findFriendsModelList.size();
    }

    public class FindfriendViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageViewProfile;
        private TextView  textViewFullName;
        private Button buttonSendRequest,buttonCancelRequest;
        private ProgressBar pbRequest;

        public FindfriendViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProfile = itemView.findViewById(R.id.ivProfile);
            textViewFullName = itemView.findViewById(R.id.tvFullName);
            buttonSendRequest = itemView.findViewById(R.id.btnSendRequest);
            buttonCancelRequest = itemView.findViewById(R.id.btnCancelRequest);
            pbRequest = itemView.findViewById(R.id.pbRequest);
        }
    }
}
