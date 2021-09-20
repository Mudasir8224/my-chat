package com.example.mychat.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mychat.Adapters.MessageAdapter;
import com.example.mychat.Fragments.ApiService;
import com.example.mychat.Models.Chat;
import com.example.mychat.Models.User;
import com.example.mychat.Notification.Client;
import com.example.mychat.Notification.Data;
import com.example.mychat.Notification.MyResponse;
import com.example.mychat.Notification.Sender;
import com.example.mychat.Notification.Token;
import com.example.mychat.R;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {
    ShapeableImageView imageViewProfile;
    MaterialTextView textViewUserName;
    Toolbar toolbar;
    TextInputEditText editTextSend;
    ImageButton imageButtonSend;

    FirebaseUser firebaseUser;
    DatabaseReference databaseReference;
    Intent intent;
    String userId;

    MessageAdapter messageAdapter;
    List<Chat> mChatList;
    RecyclerView recyclerViewMessage;
    ValueEventListener seenListener;
    ApiService apiService;
    boolean notify = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        initViews();
        setupToolbar();
        getUserData();
        clickEvents();
        setupRv();
    }

    private void initViews(){
        imageViewProfile = findViewById(R.id.imageViewProfile);
        textViewUserName = findViewById(R.id.textViewUserName);
        toolbar = findViewById(R.id.toolbar);
        editTextSend = findViewById(R.id.editTextSend);
        imageButtonSend = findViewById(R.id.imageButtonSend);
        recyclerViewMessage = findViewById(R.id.recyclerViewMessage);
        apiService = Client.getClient("https://fcm.googleapis.com/").create(ApiService.class);
    }

    private void setupToolbar(){
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MessageActivity.this,MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });
    }

    private void getUserData(){
        intent = getIntent();
        userId = intent.getStringExtra("userId");
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                textViewUserName.setText(user.getUserName());
                if (user.getImageURL().equals("default")){
                    imageViewProfile.setImageResource(R.mipmap.ic_launcher);
                }else {
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(imageViewProfile);
                }
                readMessage(firebaseUser.getUid(),userId,user.getImageURL());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        seenMessage(userId);
    }

    private void clickEvents(){
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        imageButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;
                String message = editTextSend.getText().toString();
                if (!message.equals("")){
                    sendMessage(firebaseUser.getUid(),userId,message);
                }else {
                    Toast.makeText(MessageActivity.this, "You can't send empty message", Toast.LENGTH_SHORT).show();
                }
                editTextSend.setText("");
            }
        });
    }

    private void sendMessage(String sender, final String receiver, String message){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("sender",sender);
        hashMap.put("receiver",receiver);
        hashMap.put("message",message);
        hashMap.put("isSeen",false);
        databaseReference.child("Chats").push().setValue(hashMap);
         //add user to chat fragment
        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("chatList")
                .child(firebaseUser.getUid())
                .child(userId);
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()){
                    chatRef.child("id").setValue(userId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        final String msg = message;
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
             User user = snapshot.getValue(User.class);
             if (notify){
                 sendNotification(receiver,user.getUserName(),msg);
             }

             notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendNotification(String receiver, final String userName, final String msg) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Token token = dataSnapshot.getValue(Token.class);
                    Data data = new Data(firebaseUser.getUid(),R.mipmap.ic_launcher,userName+" : "+msg,"New Message",userId);
                    Sender sender = new Sender(data,token.getToken());

                      apiService.sendNotification(sender).enqueue(new Callback<MyResponse>() {
                          @Override
                          public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                              if (response.code() == 200){
                                  if (response.body().success != 1){
                                      Toast.makeText(MessageActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                                  }
                              }
                          }

                          @Override
                          public void onFailure(Call<MyResponse> call, Throwable t) {

                          }
                      });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setupRv(){
        recyclerViewMessage.setHasFixedSize(true);
        LinearLayoutManager linearLayout = new LinearLayoutManager(getApplicationContext());
        linearLayout.setStackFromEnd(true);
        recyclerViewMessage.setLayoutManager(linearLayout);
    }

    private void readMessage(final String myId, final String userId, final String imageURL){
        mChatList = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mChatList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(myId) || chat.getSender().equals(userId)
                            || chat.getReceiver().equals(userId) && chat.getSender().equals(myId)){
                        mChatList.add(chat);
                    }
                    messageAdapter = new MessageAdapter(MessageActivity.this,mChatList,imageURL);
                    recyclerViewMessage.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void status(String status){
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("status",status);
        databaseReference.updateChildren(hashMap);
    }

    private void seenMessage(final String userId){
        databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Chat chat = snapshot.getValue(Chat.class);
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    try {
                        if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userId)){
                            HashMap<String,Object> hashMap = new HashMap<>();
                            hashMap.put("isSeen",true);
                            dataSnapshot.getRef().updateChildren(hashMap);
                        }
                    }catch (NullPointerException e){
                        Log.d("Seen",e.toString());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }


    private void currentUser(String userId){
        SharedPreferences.Editor editor = getSharedPreferences("Pref",MODE_PRIVATE).edit();
        editor.putString("currentUser",userId);
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        currentUser(userId);
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        databaseReference.removeEventListener(seenListener);
        currentUser("none");
        status("offline");
    }

}