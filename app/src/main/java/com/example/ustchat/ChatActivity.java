package com.example.ustchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

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
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";
    Toolbar toolbar;
    EditText etReply;
    EditText etName;

    RecyclerView recyclerView;
    ChatroomChatRecyclerAdapter adapter;
    List<ChatroomChatRecord> chatroomChatRecords;

    LinearLayout llQuoteArea;

    Dialog dialog;
    String chatId;
    String referMessage;
    FirebaseAuth mAuth;
    DatabaseReference mDatabaseRef;
    ImageButton btnSendMessage;
//    private static String JSON_URL = "https://jsonkeeper.com/b/QZRG";

    static String userName = null;
    static boolean userRepliedBefore = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Bundle bundle = getIntent().getExtras();
        String chatroomTitle = ""; // or other values
        if(bundle != null) {
            chatroomTitle = bundle.getString("chatroomTitle");
            chatId = bundle.getString("chatId");
        }
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(chatroomTitle);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        etReply = findViewById(R.id.et_input_area_reply);
        etName = findViewById(R.id.et_input_area_name);
        if (userRepliedBefore) {
            etName.setEnabled(false);
        }
        etName.setText(userName);
        referMessage = "";
        llQuoteArea = findViewById(R.id.ll_chat_input_area_quote);

        recyclerView = findViewById(R.id.recyclerView);
        chatroomChatRecords = new ArrayList<>();
//        view count +1
        mAuth = FirebaseAuth.getInstance();
        mDatabaseRef.child("chatroom/"+chatId+"/viewCount").setValue(ServerValue.increment(1));
        btnSendMessage = findViewById(R.id.iv_input_area_submit);
        btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessageHandler();
            }
        });
//      check userRepliedBefore
        if(mAuth.getCurrentUser() != null) {
            setGetNameListener();
        }
        extractChatroomChatRecords();

    }

    private void SendMessageHandler() {
        String nameWantToUse = etName.getText().toString();
        Query query = mDatabaseRef.child("nameToId/"+chatId).child(nameWantToUse);
        //checkNameExist
        query.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists()){
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        if(!mAuth.getCurrentUser().getUid().equals(postSnapshot.getValue(String.class))) {
                            Log.d(TAG, "other user used your name");
                        }
                    }
                }else{
                    Log.d(TAG, "nameNOtExist");
                    mDatabaseRef.child("nameToId/"+chatId).child(nameWantToUse+"/id/").setValue(mAuth.getCurrentUser().getUid());
                    userRepliedBefore = true;
                    etName.setEnabled(false);
                }
                //update message
                sendMessage();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Log.d(TAG, "cancel"+databaseError);
            }
        });
    }

    private void sendMessage(){
        String message = etReply.getText().toString();
        String name =etName.getText().toString();
        if(!message.equals("") && !name.equals("")) {
            ChatroomChatRecord chat = new ChatroomChatRecord(name, message, ServerValue.TIMESTAMP, referMessage, true);
            mDatabaseRef.child("message/" + chatId).push().setValue(chat);
            //update chatcount
            mDatabaseRef.child("chatroom/" + chatId+"/chatCount").setValue(ServerValue.increment(1));
            mDatabaseRef.child("chatroom/" + chatId+"/latestName").setValue(name);
            mDatabaseRef.child("chatroom/" + chatId+"/latestReply").setValue(message);
            //update UI
            referMessage = "";
            llQuoteArea.setVisibility(View.GONE);
        }
    }

    private void setGetNameListener() {
        String id= mAuth.getCurrentUser().getUid();
        Query query = mDatabaseRef.child("nameToId/"+chatId).orderByChild("id").equalTo(id);
        query.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists()) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        userRepliedBefore = true;
                        userName = postSnapshot.getKey();
                    }
                }else{
                    userRepliedBefore = false;
                    userName = null;
                }
                if (userRepliedBefore) {
                    etName.setEnabled(false);
                }
                etName.setText(userName);
            }
            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Log.d(TAG, "cancel"+databaseError);
            }
        });
    }

    private void extractChatroomChatRecords() {
        Query query = mDatabaseRef.child("message/"+chatId);
        query.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                chatroomChatRecords.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    ChatroomChatRecord chatRecord = postSnapshot.getValue(ChatroomChatRecord.class);
                    chatRecord.setTime( postSnapshot.child("timeStamp").getValue(Long.class));
                    if(userName !=null){
                        chatRecord.setUser(userName.equals(chatRecord.getName()));
                    }
                    chatroomChatRecords.add(chatRecord);
                    recyclerView.setLayoutManager(new LinearLayoutManager(ChatActivity.this));
                    adapter = new ChatroomChatRecyclerAdapter(ChatActivity.this, chatroomChatRecords);
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Log.d(TAG, "cancel"+databaseError);
            }
        });
    }
//    private void extractChatroomChatRecords() {
//        RequestQueue queue = Volley.newRequestQueue(this);
//        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, JSON_URL, null,new Response.Listener<JSONArray>() {
//            @Override
//            public void onResponse(JSONArray response) {
//                for (int i = 0; i < response.length(); i++) {
//                    try {
//                        JSONObject chatroomObject = response.getJSONObject(i);
//                        ChatroomChatRecord chatroomChatRecord = new ChatroomChatRecord();
//                        chatroomChatRecord.setName(chatroomObject.getString("name"));
//                        chatroomChatRecord.setContent(chatroomObject.getString("content"));
//                        chatroomChatRecord.setQuotedContent(chatroomObject.getString("quoted_content"));
//                        chatroomChatRecord.setTime(chatroomObject.getString("time"));
//                        chatroomChatRecord.setUser(chatroomObject.getBoolean("is_user"));
//                        chatroomChatRecords.add(chatroomChatRecord);
//                        recyclerView.setLayoutManager(new LinearLayoutManager(ChatActivity.this));
//                        adapter = new ChatroomChatRecyclerAdapter(ChatActivity.this, chatroomChatRecords);
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

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View view = getCurrentFocus();
        if (view instanceof EditText) {
            ((EditText) view).clearFocus();
            InputMethodManager inputMethodManager =(InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }
        return super.onOptionsItemSelected(item);
    }

    public void openDialog(ChatroomChatRecord chatroomChatRecord) {
        dialog = new ReplyHandlerDialog(ChatActivity.this, chatroomChatRecord);
        dialog.show();
    }

    public void quote(ChatroomChatRecord chatroomChatRecord) {
        llQuoteArea.setVisibility(View.VISIBLE);
        TextView tvQuotedText = llQuoteArea.findViewById(R.id.tv_chat_input_area_quote_text);
        tvQuotedText.setText(chatroomChatRecord.getContent());
        ImageButton ibQuotedCancel = llQuoteArea.findViewById(R.id.iv_chat_input_area_quote_cancel);
        ibQuotedCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                llQuoteArea.setVisibility(View.GONE);
            }
        });
        referMessage = chatroomChatRecord.getContent();
    }

}

class ReplyHandlerDialog extends Dialog {

    private ListView listView;
    private ChatroomChatRecord referChatroomChatRecord;
    Context context;

    public ReplyHandlerDialog(@NonNull Context context, ChatroomChatRecord chatroomChatRecord) {
        super(context);
        referChatroomChatRecord = chatroomChatRecord;
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Window dialogWindow = getWindow();
        dialogWindow.setGravity(Gravity.BOTTOM);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = AbsListView.LayoutParams.MATCH_PARENT;
        lp.y = 20;
        dialogWindow.setAttributes(lp);
        dialogWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        List<String> ls = new ArrayList<>();
        ls.add("Copy");
        ls.add("Reply");
        ls.add("Send a private message");
        ls.add("Cancel");
        setContentView(R.layout.layout_dialog_reply_handler);
        listView = findViewById(R.id.lv_reply_handler_choices);
        ArrayAdapter adapter = new ArrayAdapter<String>(getContext(), R.layout.layout_dialog_reply_handler_item, ls);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch(position){
                    case 0: //copy
                        ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("text", referChatroomChatRecord.getContent());
                        clipboard.setPrimaryClip(clip);
                        break;
                    case 1: //reply
                        ((ChatActivity) context).quote(referChatroomChatRecord);
                        break;
                    case 2: //send a private message
                        //intent = new Intent(Activity.this,thirdActivity.class);
                        break;
                    case 3: //cancel
                        break;
                }
                dismiss();
            }
        });
    }

}