package com.example.mychat.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mychat.Activities.MessageActivity;
import com.example.mychat.Models.Chat;
import com.example.mychat.Models.User;
import com.example.mychat.R;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolderMessage>  {
    public static final int MSG_TPE_LEFT = 0;
    public static final int MSG_TPE_RIGHT = 1;
    Context context;
    List<Chat> mChatList;
    private String imageURL;
    FirebaseUser firebaseUser;

    public MessageAdapter(Context context, List<Chat> mChatList,String imageURL) {
        this.context = context;
        this.mChatList = mChatList;
        this.imageURL = imageURL;
    }

    @NonNull
    @Override
    public ViewHolderMessage onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TPE_RIGHT){
            View view = LayoutInflater.from(context).inflate(R.layout.chat_layout_right,parent,false);
            return new MessageAdapter.ViewHolderMessage(view);
        }else {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_layout_left,parent,false);
            return new MessageAdapter.ViewHolderMessage(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderMessage holder, int position) {
        Chat chat = mChatList.get(position);
        holder.textViewShowMessage.setText(chat.getMessage());

        if (imageURL.equals("default")){
            holder.imageViewProfile.setImageResource(R.mipmap.ic_launcher);
        }else {
            Glide.with(context).load(imageURL).into(holder.imageViewProfile);
        }

        if (position == mChatList.size()-1){
            if (chat.isSeen()){
                holder.textViewSeen.setText("Seen");
            }else {
                holder.textViewSeen.setText("Delivered");
            }
        }else {
            holder.textViewSeen.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return mChatList.size();
    }

    public class ViewHolderMessage extends RecyclerView.ViewHolder{
        public MaterialTextView textViewShowMessage;
        public ShapeableImageView imageViewProfile;
        public MaterialTextView textViewSeen;

        public ViewHolderMessage(@NonNull View itemView) {
            super(itemView);
            textViewShowMessage = itemView.findViewById(R.id.textViewShowMessage);
            imageViewProfile = itemView.findViewById(R.id.imageViewProfile);
            textViewSeen = itemView.findViewById(R.id.textViewSeen);
        }
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mChatList.get(position).getSender().equals(firebaseUser.getUid())){
            return MSG_TPE_RIGHT;
        }else {
            return MSG_TPE_LEFT;
        }
    }
}
