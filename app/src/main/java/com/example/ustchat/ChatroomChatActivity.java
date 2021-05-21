package com.example.ustchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.net.wifi.aware.SubscribeConfig;
import android.os.Bundle;
import android.os.StrictMode;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ChatroomChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";

    Toolbar toolbar;
    String chatroomTitle;
    String targetUserId;
    RecyclerView recyclerView;
    ChatroomChatRecyclerAdapter adapter;
    List<ChatroomChatRecord> chatroomChatRecords;

    LinearLayout llQuoteArea;
    String quotedText;
//    String quotedImgString;

    String chatId;
    FirebaseAuth mAuth;
    DatabaseReference mDatabaseRef;
    StorageReference mStoreRef;

    private static String JSON_URL = "https://jsonkeeper.com/b/DY95";
    private static String username = "CPEG guy";
    private static boolean userRepliedBefore = true;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom_chat);

        Bundle bundle = getIntent().getExtras();
        chatroomTitle = ""; // or other values
        quotedText = "";
        targetUserId = "";
        if(bundle != null) {

            chatroomTitle = bundle.getString("chatroomTitle");
            chatId = bundle.getString("chatId");
        }

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(chatroomTitle);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        llQuoteArea = findViewById(R.id.ll_chat_input_area_quote);

        recyclerView = findViewById(R.id.recycler_view_chatroom_chat);
        chatroomChatRecords = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mStoreRef = FirebaseStorage.getInstance().getReference();
        //check name first
        getNameListener(new Callback() {
            @Override
            public void callback() {
                mDatabaseRef.child("chatroom/"+chatId+"/viewCount/").setValue(ServerValue.increment(1));
                extractChatroomChatRecords();
                ChatInputAreaFragment chatInputAreaFragment = new ChatInputAreaFragment(chatId, chatroomTitle, username, false, userRepliedBefore);
                getSupportFragmentManager().beginTransaction().replace(R.id.fl_input_area, chatInputAreaFragment).commit();
            }
        });

        // TO-DO: hardcode for now
//        ChatInputAreaFragment chatInputAreaFragment = new ChatInputAreaFragment(username, userRepliedBefore);
//        getSupportFragmentManager().beginTransaction().replace(R.id.fl_input_area, chatInputAreaFragment).commit();
    }

    private void getNameListener(Callback callback) {
        if(mAuth.getCurrentUser() != null) {
            String id= mAuth.getCurrentUser().getUid();
            Query query = mDatabaseRef.child("nameToId/" + chatId).orderByChild("id").equalTo(id);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            userRepliedBefore = true;
                            username = postSnapshot.getKey();
                        }
                    } else {
                        userRepliedBefore = false;
                        username = null;
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
                    chatRecord.setTime(postSnapshot.child("timeStamp").getValue(Long.class));
                    chatRecord.setId(postSnapshot.getKey());
                    if(username !=null){
                        chatRecord.setUser(username.equals(chatRecord.getName()));

                    }
                    chatroomChatRecords.add(chatRecord);
                    recyclerView.setLayoutManager(new LinearLayoutManager(ChatroomChatActivity.this));
                    adapter = new ChatroomChatRecyclerAdapter(ChatroomChatActivity.this, chatroomChatRecords);
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Log.d(TAG, "cancel"+databaseError);
            }
        });
//        RequestQueue queue = Volley.newRequestQueue(this);
//        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, JSON_URL, null,new Response.Listener<JSONArray>() {
//            @Override
//            public void onResponse(JSONArray response) {
//                for (int i = 0; i < response.length(); i++) {
//                    try {
//                        JSONObject chatroomObject = response.getJSONObject(i);
//                        ChatroomChatRecord chatroomChatRecord = new ChatroomChatRecord();
//                        chatroomChatRecord.setName(chatroomObject.getString("name"));
//                        chatroomChatRecord.setText(chatroomObject.getString("text"));
//                        chatroomChatRecord.setImage(chatroomObject.getString("image"));
//                        chatroomChatRecord.setQuotedID(chatroomObject.getString("quoted_id"));
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
    }

    public String getQuotedText(){
        return quotedText;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }
        return super.onOptionsItemSelected(item);
    }

    public void searchChatroomChatRecordByID(String chatroomID) {
        // TO-DO: should search for the chatroomChatRecord in the backend

    }

    public void openReplyHandlerDialog(ChatroomChatRecord chatroomChatRecord) {
        Dialog dialog = new ReplyHandlerDialog(ChatroomChatActivity.this, chatroomChatRecord, null);
        dialog.show();
    }

    public void quote(ChatroomChatRecord chatroomChatRecord) {
        llQuoteArea.setVisibility(View.VISIBLE);
        TextView tvQuotedText = llQuoteArea.findViewById(R.id.tv_chat_input_area_quote_text);
        ImageView tvQuotedImage = llQuoteArea.findViewById(R.id.iv_chat_input_area_quote_image);

        if (chatroomChatRecord.getImage().isEmpty()) {
            tvQuotedImage.setVisibility(View.GONE);
            tvQuotedText.setText(chatroomChatRecord.getText());
            quotedText = chatroomChatRecord.getText();
        }
        else {
            tvQuotedImage.setVisibility(View.VISIBLE);
            tvQuotedText.setText("");
            quotedText = "{photo}";
//            quotedImgString = chatroomChatRecord.getImage();
        }

        ImageButton ibQuotedCancel = llQuoteArea.findViewById(R.id.iv_chat_input_area_quote_cancel);
        ibQuotedCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                llQuoteArea.setVisibility(View.GONE);
                quotedText = "";
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }


    public void startPrivateMessageChat(String targetUserName) {
        Intent intent = new Intent(ChatroomChatActivity.this, PrivateMessageChatActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("chatId", "");
        bundle.putString("chatroomTitle", chatroomTitle);
        bundle.putString("username", username);
        bundle.putString("targetUsername", targetUserName);
        bundle.putString("publicChatId", chatId);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtras(bundle);
        startActivity(intent);

//        PrivateMessageRecord PM_Chat = new PrivateMessageRecord();
//        PM_Chat.setTitle(chatroomTitle);
//        mDatabaseRef.child("privateChat/").push().setValue(PM_Chat,  new DatabaseReference.CompletionListener() {
//            @Override
//            public void onComplete(DatabaseError databaseError,
//                                   DatabaseReference databaseReference) {
//                String pChatId = databaseReference.getKey();
//                getTargetUserUID(targetUserName, new Callback() {
//                    @Override
//                    public void callback() {
//                        Log.d(TAG, "addNametoId");
//                        mDatabaseRef.child("nameToId/" + pChatId).child(username+"/id").setValue(mAuth.getCurrentUser().getUid());
//                        mDatabaseRef.child("nameToId/" + pChatId).child(targetUserName+"/id").setValue(targetUserId);
//                        //subscribe PrivateChat
//                        mDatabaseRef.child("users/"+targetUserId+"/privateChat/"+pChatId+"/unread/").setValue(0);
//                        mDatabaseRef.child("users/"+mAuth.getCurrentUser().getUid()+"/privateChat/"+pChatId+"/unread/").setValue(0);
//
//                        createPrivateMessageActivity(pChatId, targetUserName);
//                    }
//                });
//            }
//        });
    }



    private void getTargetUserUID(String targetName, Callback callback){
        Log.d(TAG, "nameToId/"+chatId+"/"+targetName);
        Query query = mDatabaseRef.child("nameToId/"+chatId+"/"+targetName);
        query.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                Log.d(TAG, "dataSnapshot"+dataSnapshot);
                targetUserId = dataSnapshot.child("id").getValue(String.class);
                Log.d(TAG, "targetUserId"+targetUserId);
                callback.callback();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Log.d(TAG, "cancel"+databaseError);
            }
        });
    }

    private void createPrivateMessageActivity(String targetUser){
        Intent intent = new Intent(ChatroomChatActivity.this, PrivateMessageChatActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("chatId", "");
        bundle.putString("chatroomTitle", chatroomTitle);
        bundle.putString("username", username);
        bundle.putString("targetUsername", targetUser);
        bundle.putString("PublicChatId", chatId);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void deleteMessage(String id) {
        mDatabaseRef.child("message/" + chatId+"/"+id).removeValue();
        mDatabaseRef.child("chatroom/"+ chatId+"/chatCount/").setValue(ServerValue.increment(-1));
        //update meta
        Query query = mDatabaseRef.child("message/"+ chatId).orderByChild("timeStamp").limitToLast(1);
        query.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "lastmessage"+postSnapshot.getValue());
                    String latestName = postSnapshot.child("name").getValue(String.class);
                    String latestReply = postSnapshot.child("text").getValue(String.class);
                    if (latestReply.equals("")){
                        latestReply = "{photo}";
                    }
                    mDatabaseRef.child("chatroom/"+ chatId+"/latestName/").setValue(latestName);
                    mDatabaseRef.child("chatroom/"+ chatId+"/latestReply/").setValue(latestReply);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Log.d(TAG, "cancel"+databaseError);
            }
        });
    }

    public void sendTextReply(String message, String name){
        if(!message.equals("") && !name.equals("")) {
            ChatroomChatRecord chat = new ChatroomChatRecord(name, message, "", ServerValue.TIMESTAMP, quotedText, true);
            mDatabaseRef.child("message/" + chatId).push().setValue(chat, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                        updateChatMeta(name, message);
                }
            });
        }
    }


    private void updateChatMeta(String name, String message){
        mDatabaseRef.child("chatroom/" + chatId+"/chatCount").setValue(ServerValue.increment(1));
        mDatabaseRef.child("chatroom/" + chatId+"/latestName").setValue(name);
        mDatabaseRef.child("chatroom/" + chatId+"/latestReply").setValue(message);
    }

    public void sendImageReply(Uri imageUri, String name) {
        if(imageUri != null){
            String messageId = mDatabaseRef.child("message/" + chatId).push().getKey();
            String imgPath = "images/"+chatId+"/"+messageId+"_"+imageUri.getLastPathSegment();
            StorageReference imageRef = mStoreRef.child(imgPath);
            UploadTask uploadTask = imageRef.putFile(imageUri);

            // Register observers to listen for when the download is done or if it fails
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    Log.d(TAG, "onFailure: uploadfile fail");
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            ChatroomChatRecord chat = new ChatroomChatRecord(name, "",uri.toString(), ServerValue.TIMESTAMP, "", true);
                            mDatabaseRef.child("message/" + chatId + "/" + messageId).setValue(chat);                             //update chatRoom meta
                            updateChatMeta(name, "{photo}");
                        }
                    });
                }
            });
        }
    }
}

class ReplyHandlerDialog extends Dialog {
    private ListView listView;
    private ChatroomChatRecord chatroomChatRecord;
    private ChatroomChatRecord quotedChatroomChatRecord;
    TextView lvName;
    Context context;

    public ReplyHandlerDialog(@NonNull Context context, ChatroomChatRecord chatroomChatRecord, ChatroomChatRecord quotedChatroomChatRecord) {
        super(context);
        this.context = context;
        this.chatroomChatRecord = chatroomChatRecord;
        this.quotedChatroomChatRecord = quotedChatroomChatRecord;
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
        if (chatroomChatRecord.isUser()) {
            ls.add("Delete");
        } else {
            ls.add("Send a private message");
        }
        ls.add("Cancel");
        setContentView(R.layout.layout_dialog_reply_handler);

        lvName = findViewById(R.id.lv_reply_handler_name);
        lvName.setText(chatroomChatRecord.getName());

        listView = findViewById(R.id.lv_reply_handler_choices);
        ArrayAdapter adapter = new ArrayAdapter<String>(getContext(), R.layout.layout_dialog_reply_handler_item, ls);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: // copy
                        ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                        // TO-DO: if the it is an image...
                        ClipData clip = ClipData.newPlainText("text", chatroomChatRecord.getText());
                        clipboard.setPrimaryClip(clip);
                        break;
                    case 1: // quote
                        ((ChatroomChatActivity) context).quote(chatroomChatRecord);
                        break;
                    case 2:
                        if (chatroomChatRecord.isUser()) {
                            // TO-DO: delete the reply (Backend)
                            ((ChatroomChatActivity) context).deleteMessage(chatroomChatRecord.getId());
                        }
                        else {
                            // TO-DO: send a private message
                            // should create a new json
                            ((ChatroomChatActivity) context).startPrivateMessageChat(chatroomChatRecord.getName());
                        }
                        break;
                    case 3: // cancel
                        break;
                }
                dismiss();
            }
        });
    }

}

