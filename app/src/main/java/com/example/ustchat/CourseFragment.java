package com.example.ustchat;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class CourseFragment extends Fragment {
    private static final String ARG_PARAM1 = "cat";
    private String cat;

    RecyclerView recyclerView;
    ChatroomRecyclerAdapter adapter;
    List<ChatroomRecord> chatroomRecords;
    private static String JSON_URL = "https://jsonkeeper.com/b/Z3R2";

    public CourseFragment() { }

    public static CourseFragment newInstance(String cat) {
        CourseFragment fragment = new CourseFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, cat);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            cat = getArguments().getString(ARG_PARAM1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_course, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        chatroomRecords = new ArrayList<>();
        extractChatroomRecords();
        return view;
    }

    private void extractChatroomRecords() {
        RequestQueue queue = Volley.newRequestQueue(getContext());
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, JSON_URL, null,new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject chatroomObject = response.getJSONObject(i);
                        ChatroomRecord chatroomRecord = new ChatroomRecord();
                        chatroomRecord.setTitle(chatroomObject.getString("title"));
                        chatroomRecord.setPosterName(chatroomObject.getString("poster_name"));
                        chatroomRecord.setCreateDate(chatroomObject.getString("create_date"));
                        chatroomRecord.setLatestName(chatroomObject.getString("latest_name"));
                        chatroomRecord.setLatestReply(chatroomObject.getString("latest_reply"));

                        JSONArray tagsJA = chatroomObject.getJSONArray("tag");
                        List<String> tags = new ArrayList<>();
                        for (int j = 0; j < tagsJA.length(); j++) {
                            tags.add(tagsJA.getString(j));
                        }
                        chatroomRecord.setTags(tags);

                        chatroomRecord.setChatCount(chatroomObject.getInt("chat_count"));
                        chatroomRecord.setViewCount(chatroomObject.getInt("view_count"));
                        chatroomRecord.setBookmarked(chatroomObject.getBoolean("bookmarked"));
                        chatroomRecords.add(chatroomRecord);
                        if (getActivity() == null) {
                            return;
                        }
                        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                        adapter = new ChatroomRecyclerAdapter(getActivity(), chatroomRecords);
                        recyclerView.setAdapter(adapter);
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

    public void switchChatActivity(String chatroomTitle) {
        startActivity(new Intent(getContext().getApplicationContext(), ChatroomChatActivity.class));
        ((CourseActivity) getContext()).overridePendingTransition(0, 0);
    }

}