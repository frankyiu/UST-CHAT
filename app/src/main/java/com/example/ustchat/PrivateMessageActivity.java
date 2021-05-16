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

public class PrivateMessageActivity extends AppCompatActivity {
    Toolbar toolbar;
    BottomNavigationView bottomNavigationView;

    RecyclerView pmRecyclerView;
    PrivateMessageRecyclerAdapter privateMessageRecyclerAdapter;
    List<PrivateMessageRecord> privateMessageRecords;
    private static String JSON_URL = "https://jsonkeeper.com/b/IVWS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_message);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Private Message"); //??
        bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setSelectedItemId(R.id.private_message);
        BadgeDrawable badge = bottomNavigationView.getOrCreateBadge(R.id.private_message);
        //TO-DO : hardcode for now
        badge.setNumber(1);

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
        extractPMRecords();
    }

    private void extractPMRecords() {
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, JSON_URL, null,new Response.Listener<JSONArray>() {
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
                        pmRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        privateMessageRecyclerAdapter = new PrivateMessageRecyclerAdapter(getApplicationContext(), privateMessageRecords);
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
}
