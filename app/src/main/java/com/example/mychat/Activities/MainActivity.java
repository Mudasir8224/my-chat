package com.example.mychat.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Switch;

import com.bumptech.glide.Glide;
import com.example.mychat.Fragments.ChatsFragment;
import com.example.mychat.Fragments.ProfileFragment;
import com.example.mychat.Fragments.UsersFragment;
import com.example.mychat.Models.Chat;
import com.example.mychat.Models.User;
import com.example.mychat.R;
import com.example.mychat.Views.ViewPagerAdapter;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    ShapeableImageView imageViewProfile;
    MaterialTextView textViewUserName;
    Toolbar toolbar;
    FirebaseUser firebaseUser;
    DatabaseReference databaseReference;
    TabLayout tabLayout;
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        setupToolbar();
        userDataSetup();
        setupFragments();
    }

    private void initViews(){
        imageViewProfile = findViewById(R.id.imageViewProfile);
        textViewUserName = findViewById(R.id.textViewUserName);
         toolbar = findViewById(R.id.toolbar);
         tabLayout = findViewById(R.id.tab_layout);
         viewPager = findViewById(R.id.view_pager);
    }

    private void setupToolbar(){
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

    }

    private void userDataSetup(){
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    textViewUserName.setText(user.getUserName());
                }
                if (user != null) {
                    if (user.getImageURL().equals("default")){
                        imageViewProfile.setImageResource(R.mipmap.ic_launcher);
                    }else {
                        Glide.with(getApplicationContext()).load(user.getImageURL()).into(imageViewProfile);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void setupFragments(){
//        databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
//        databaseReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
               ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
//                int unread = 0;
//                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
//                    Chat chat = dataSnapshot.getValue(Chat.class);
//                    if (chat.getReceiver().equals(firebaseUser.getUid()) && !chat.isSeen()){
//                        unread++;
//                    }
//                }
//                if (unread == 0){
                    viewPagerAdapter.addFragment(new ChatsFragment(),"Chats");
//                }else {
//                    viewPagerAdapter.addFragment(new ChatsFragment(),"("+unread+") Chats");
//                }
                viewPagerAdapter.addFragment(new UsersFragment(),"Users");
                viewPagerAdapter.addFragment(new ProfileFragment(),"Profile");
                viewPager.setAdapter(viewPagerAdapter);
                tabLayout.setupWithViewPager(viewPager);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout:
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(MainActivity.this,StartActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    return true;
        }
        return false;
    }

    private void status(String status){
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("status",status);
        databaseReference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        status("offline");
    }
}