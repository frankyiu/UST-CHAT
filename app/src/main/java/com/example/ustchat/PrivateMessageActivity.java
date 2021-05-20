package com.example.ustchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PrivateMessageActivity extends AppCompatActivity implements NavigationNotification {
    private static final String TAG = "PrivateMessageActivity";
    Toolbar toolbar;
    BottomNavigationView bottomNavigationView;
    BadgeDrawable notificationBadge;

    RecyclerView pmRecyclerView;
    PrivateMessageRecyclerAdapter privateMessageRecyclerAdapter;
    List<PrivateMessageRecord> privateMessageRecords;
    Map<String, Integer> myPMChatUnreadMap;
    FirebaseAuth mAuth;
    DatabaseReference mDatabaseRef;
    private static String JSON_URL = "https://jsonkeeper.com/b/IVWS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_message);

        toolbar = findViewById(R.id.toolbar_center_title);
        ((TextView) toolbar.findViewById(R.id.tv_toolbar_center_title)).setText("Private Message");
        toolbar.setTitle("");
        bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setSelectedItemId(R.id.private_message);
        notificationBadge = bottomNavigationView.getOrCreateBadge(R.id.private_message);
        //TO-DO : hardcode for now
        notificationBadge.setNumber(1);
        enableNotificationBadge(Utility.enableNotification);

        setSupportActionBar(toolbar);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.chatroom:
                        startActivity(new Intent(getApplicationContext(), CourseActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.private_message:
                        return true;
                    case R.id.setting:
                        startActivity(new Intent(getApplicationContext(), SettingActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                }
                return false;
            }
        });

        pmRecyclerView = findViewById(R.id.recyclerView);
        privateMessageRecords = new ArrayList<>();
        myPMChatUnreadMap = new HashMap<>();

        mAuth = FirebaseAuth.getInstance();
        mDatabaseRef  = FirebaseDatabase.getInstance().getReference();
        if(mAuth.getCurrentUser() !=null) {
            extractMyPMChatUnreadMap(new Callback() {
                @Override
                public void callback() {
                    extractPMRecords();
                }
            });
        }
    }

    private void extractMyPMChatUnreadMap(Callback callback) {
        Query query = mDatabaseRef.child("users/"+ mAuth.getCurrentUser().getUid()+"/privateChat/");
        query.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                myPMChatUnreadMap.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    myPMChatUnreadMap.put(postSnapshot.getKey(), postSnapshot.child("unread").getValue(Integer.class));
                }
                callback.callback();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Log.d(TAG, "cancel"+databaseError);
            }
        });
    }

    private void extractPMRecords(){
        Query query = mDatabaseRef.child("privateChat/").orderByChild("timeStamp");
        query.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                privateMessageRecords.clear();
                Log.d(TAG, "myMap"+myPMChatUnreadMap);

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    if(myPMChatUnreadMap.containsKey(postSnapshot.getKey())){
                        Log.d(TAG, "parsing");
                        PrivateMessageRecord pmRecord = postSnapshot.getValue(PrivateMessageRecord.class);
                        pmRecord.setId(postSnapshot.getKey());
                        pmRecord.setUnreadCount(myPMChatUnreadMap.get(postSnapshot.getKey()));
                        pmRecord.setLatestReplyTime(postSnapshot.child("latestReplyTime").getValue(Long.class));
                        privateMessageRecords.add(pmRecord);
                    }
                }
                Collections.reverse(privateMessageRecords);
                pmRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                privateMessageRecyclerAdapter = new PrivateMessageRecyclerAdapter(getApplicationContext(), privateMessageRecords);
                pmRecyclerView.setAdapter(privateMessageRecyclerAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Log.d(TAG, "cancel"+databaseError);
            }
        });
    }


    public void enableNotificationBadge(boolean enable) {
        notificationBadge.setVisible(enable);
    }
}
