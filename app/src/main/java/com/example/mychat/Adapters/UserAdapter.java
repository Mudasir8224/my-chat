package com.example.mychat.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mychat.Activities.MessageActivity;
import com.example.mychat.Models.Chat;
import com.example.mychat.Models.User;
import com.example.mychat.R;
import com.example.mychat.Views.ViewPagerAdapter;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolderUsers>  {
    Context context;
    List<User> mUserList;
    private boolean isChat;
    String theLastMessage;

    public UserAdapter(Context context, List<User> mUserList,boolean isChat) {
        this.context = context;
        this.mUserList = mUserList;
        this.isChat = isChat;
    }

    @NonNull
    @Override
    public ViewHolderUsers onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item,parent,false);
        return new UserAdapter.ViewHolderUsers(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderUsers holder, int position) {
        final User user = mUserList.get(position);
        holder.textViewUserName.setText(user.getUserName());
        if (user.getImageURL().equals("default")){
            holder.imageView.setImageResource(R.mipmap.ic_launcher);
        }else {
            Glide.with(context).load(user.getImageURL()).into(holder.imageView);
        }

        if (isChat){
            lastMessage(user.getId(),holder.textViewLastMessage);
        }else {
            holder.textViewLastMessage.setVisibility(View.GONE);
        }

        if (isChat) {
            if (user.getStatus().equals("online")) {
                holder.imageViewOnline.setVisibility(View.VISIBLE);
                holder.imageViewOffline.setVisibility(View.GONE);
            } else {
                holder.imageViewOffline.setVisibility(View.VISIBLE);
                holder.imageViewOnline.setVisibility(View.GONE);
            }
            }else{
            holder.imageViewOffline.setVisibility(View.GONE);
            holder.imageViewOnline.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MessageActivity.class);
                intent.putExtra("userId",user.getId());
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mUserList.size();
    }

    public class ViewHolderUsers extends RecyclerView.ViewHolder{
        public MaterialTextView textViewUserName;
        public ShapeableImageView imageView;
        public ShapeableImageView imageViewOnline;
        public ShapeableImageView imageViewOffline;
        public MaterialTextView textViewLastMessage;

        public ViewHolderUsers(@NonNull View itemView) {
            super(itemView);
            textViewUserName = itemView.findViewById(R.id.textViewUserName);
            imageView = itemView.findViewById(R.id.imageView);
            imageViewOnline = itemView.findViewById(R.id.imageViewOnline);
            imageViewOffline = itemView.findViewById(R.id.imageViewOffline);
            textViewLastMessage = itemView.findViewById(R.id.textViewLastMessage);

        }
    }

    private void lastMessage(final String userId, final TextView textViewLastMessage){
        theLastMessage = "default";
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren() ){
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(firebaseUser.getUid()) || chat.getSender().equals(userId)
                            || chat.getReceiver().equals(userId) || chat.getSender().equals(firebaseUser.getUid())){

                        theLastMessage = chat.getMessage();
                    }
                }
                switch (theLastMessage){
                    case "default":
                        textViewLastMessage.setText("No Message");
                        break;

                    default:
                        textViewLastMessage.setText(theLastMessage);
                        break;
                }
                theLastMessage = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
