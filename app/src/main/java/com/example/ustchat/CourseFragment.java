package com.example.ustchat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
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
import java.util.Iterator;
import java.util.List;


public class CourseFragment extends Fragment {
    private static final String ARG_PARAM1 = "cat";
    private static final String TAG ="CourseFragment";
    private static final String ARG_PARAM2 = "criteria";
    private String cat;

    FirebaseAuth mAuth;
    DatabaseReference mDatabaseRef;
    RecyclerView recyclerView;
    ChatroomRecyclerAdapter adapter;
    List<ChatroomRecord> chatroomRecords;
    List<String> bookmarkList;
    JSONObject criteria;
    private static String JSON_URL = "https://jsonkeeper.com/b/Z3R2";

    public CourseFragment() {
    }

    public static CourseFragment newInstance(String cat, JSONObject criteria) {
        CourseFragment fragment = new CourseFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, cat);
        if(criteria != null){
            args.putString(ARG_PARAM2, criteria.toString());
        }else{
            args.putString(ARG_PARAM2, "null");
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            cat = getArguments().getString(ARG_PARAM1);
            String criteriaString = getArguments().getString(ARG_PARAM2);
            if(!criteriaString.equals("null")){
                try {
                    criteria = new JSONObject(criteriaString);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        mAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        bookmarkList = new ArrayList<String>();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_course, container, false);
        recyclerView = view.findViewById(R.id.recycler_view_chatroom);
        chatroomRecords = new ArrayList<>();
        extractBookmarkedList(new Callback(){
            @Override
            public void callback() {
                if(!cat.equals("My Bookmarks")) {
                    extractChatroomRecords();
                }else{
                    extractMyBookmarkRecords();
                }
            }
        });
        return view;
    }


    private void extractMyBookmarkRecords() {
        Query query = mDatabaseRef.child("/chatroom");
        query.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                chatroomRecords.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    if(bookmarkList.contains(postSnapshot.getKey())){
                        ChatroomRecord chat = postSnapshot.getValue(ChatroomRecord.class);
                        chat.setCreateDate(postSnapshot.child("timeStamp").getValue(Long.class));
                        chat.setId(postSnapshot.getKey());
                        chat.setBookmarked(true);
                        chatroomRecords.add(chat);
                    }
                }
                Collections.reverse(chatroomRecords);
                updateUI();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Log.d(TAG, "cancel"+databaseError);
            }
        });
    }

    private void extractChatroomRecords() {
        Query query = mDatabaseRef.child("/chatroom").orderByChild("cat").equalTo(cat);
        query.addListenerForSingleValueEvent(new ValueEventListener()
        {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                chatroomRecords.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    ChatroomRecord chat = postSnapshot.getValue(ChatroomRecord.class);
                    chat.setCreateDate(postSnapshot.child("timeStamp").getValue(Long.class));
                    chat.setId(postSnapshot.getKey());
                    Log.d(TAG, "chatRoomrecord"+chat);
                    chatroomRecords.add(chat);
                }

                Collections.reverse(chatroomRecords);
                for(ChatroomRecord record:chatroomRecords){
                    if(bookmarkList.contains(record.getId())){
                        record.setBookmarked(true);
                    }
                }
                //check criteria
                updateUI();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Log.d(TAG, "cancel"+databaseError);
            }
        });
    }

    private void extractBookmarkedList(Callback callback) {
        if(mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            Query query = mDatabaseRef.child("users/" + userId + "/bookmarked/");
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    bookmarkList.clear();
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        String chatId = postSnapshot.getKey();
                        bookmarkList.add(chatId);
                    }
                    callback.callback();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d(TAG, "cancel" + databaseError);
                }
            });
        }else{
            callback.callback();
        }
    }

    private void updateUI(){
        if(criteria != null) {
            try {
                filterChatRoom();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if(getActivity()!=null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            adapter = new ChatroomRecyclerAdapter(getActivity(), chatroomRecords);
            recyclerView.setAdapter(adapter);
        }
    }

    private void filterChatRoom() throws JSONException {
        String title = criteria.getString("title");
        JSONArray tagsJA = criteria.getJSONArray("tags");
        List<String> tags = new ArrayList<>();
        for (int j = 0; j < tagsJA.length(); j++) {
            tags.add(tagsJA.getString(j));
        }
        // filter list
        Iterator<ChatroomRecord> i = chatroomRecords.iterator();

        while (i.hasNext()) {
            ChatroomRecord chat = i.next(); // must be called before you can call i.remove()
            boolean remove = false;
            if (!chat.getTitle().toLowerCase().matches("(.*)"+title.toLowerCase()+"(.*)")){
                remove = true;
            }
            List<String> tempList = chat.getTags() == null? new ArrayList<String>(): chat.getTags();
            for(String tag : tags){
                 if(!tempList.contains(tag)){
                    remove = true;
                }
            }
            if(remove) {
                i.remove();
            }
        }
    }
//    private void extractChatroomRecords() {
//        RequestQueue queue = Volley.newRequestQueue(getContext());
//        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, JSON_URL, null,new Response.Listener<JSONArray>() {
//            @Override
//            public void onResponse(JSONArray response) {
//                for (int i = 0; i < response.length(); i++) {
//                    try {
//                        JSONObject chatroomObject = response.getJSONObject(i);
//                        ChatroomRecord chatroomRecord = new ChatroomRecord();
//                        chatroomRecord.setTitle(chatroomObject.getString("title"));
//                        chatroomRecord.setPosterName(chatroomObject.getString("poster_name"));
//                        chatroomRecord.setCreateDate(chatroomObject.getString("create_date"));
//                        chatroomRecord.setLatestName(chatroomObject.getString("latest_name"));
//                        chatroomRecord.setLatestReply(chatroomObject.getString("latest_reply"));
//
//                        JSONArray tagsJA = chatroomObject.getJSONArray("tag");
//                        List<String> tags = new ArrayList<>();
//                        for (int j = 0; j < tagsJA.length(); j++) {
//                            tags.add(tagsJA.getString(j));
//                        }
//                        chatroomRecord.setTags(tags);
//
//                        chatroomRecord.setChatCount(chatroomObject.getInt("chat_count"));
//                        chatroomRecord.setViewCount(chatroomObject.getInt("view_count"));
//                        chatroomRecord.setBookmarked(chatroomObject.getBoolean("bookmarked"));
//                        chatroomRecords.add(chatroomRecord);
//                        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
//                        adapter = new ChatroomRecyclerAdapter(getActivity().getApplicationContext(), chatroomRecords);
//                        recyclerView.setAdapter(adapter);
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Log.d("tag", "onErrorResponse: " + error.getMessage());
//            }
//        });
//        queue.add(jsonArrayRequest);
//    }

    public void switchChatActivity(String chatroomTitle) {
        startActivity(new Intent(getContext().getApplicationContext(), ChatroomChatActivity.class));
        ((CourseActivity) getContext()).overridePendingTransition(0, 0);
    }

}