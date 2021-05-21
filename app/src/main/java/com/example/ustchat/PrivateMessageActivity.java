package com.example.ustchat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PrivateMessageActivity extends AppCompatActivity implements NavigationNotification {
    Toolbar toolbar;
    BottomNavigationView bottomNavigationView;
    BadgeDrawable notificationBadge;

    RecyclerView pmRecyclerView;
    PrivateMessageRecyclerAdapter privateMessageRecyclerAdapter;
    List<PrivateMessageRecord> privateMessageRecords;
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

        pmRecyclerView = findViewById(R.id.recycler_view_pm);
        privateMessageRecords = new ArrayList<>();
        extractPMRecords();
    }

    private void extractPMRecords() {
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, JSON_URL, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject pmRecordObject = response.getJSONObject(i);
                        PrivateMessageRecord privateMessageRecord = new PrivateMessageRecord();
                        privateMessageRecord.setTitle(pmRecordObject.getString("title"));
                        privateMessageRecord.setUsername(pmRecordObject.getString("username"));
                        privateMessageRecord.setTargetName(pmRecordObject.getString("targetName"));
                        privateMessageRecord.setLatestName(pmRecordObject.getString("latestName"));
                        privateMessageRecord.setLatestReply(pmRecordObject.getString("latestReply"));
                        privateMessageRecord.setLatestReplyTime(pmRecordObject.getString("latestTime"));
                        privateMessageRecord.setUnreadCount(pmRecordObject.getInt("unreadCount"));
                        privateMessageRecords.add(privateMessageRecord);
                        pmRecyclerView.setLayoutManager(new LinearLayoutManager(PrivateMessageActivity.this));
                        privateMessageRecyclerAdapter = new PrivateMessageRecyclerAdapter(PrivateMessageActivity.this, privateMessageRecords);
                        pmRecyclerView.setAdapter(privateMessageRecyclerAdapter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("tag", "onErrorResponse: " + error.getMessage());
            }
        });
        queue.add(jsonArrayRequest);
    }

    public void enableNotificationBadge(boolean enable) {
        notificationBadge.setVisible(enable);
    }
}
