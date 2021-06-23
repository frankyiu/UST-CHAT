package com.example.ustchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
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
import java.util.List;

public class PrivateMessageChatActivity extends AppCompatActivity {
    private static final String TAG = "PMChatActivity";
    Toolbar toolbar;
    private String chatroomTitle;
    private String username;
    private String targetUsername;
    private String chatId;
    private String publicChatId;

    String quotedText;
    TextView tvTitle;
    TextView tvTargetUsername;

    RecyclerView recyclerView;
    PrivateChatRecyclerAdapter adapter;
    List<PrivateChatRecord> privateChatRecords;
    FirebaseAuth mAuth;
    DatabaseReference mDatabaseRef;
    StorageReference mStoreRef;
    LinearLayout llQuoteArea;
    private ValueEventListener mListener;
//    private static String JSON_URL = "https://jsonkeeper.com/b/VKKN";

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_message_chat);

        Bundle bundle = getIntent().getExtras();
        chatroomTitle = "Default Chatroom";
        username = "Student12345";
        targetUsername = "Student12345";
        chatId = "";
        publicChatId = "";
        quotedText="";
        if(bundle != null) {
            chatId = bundle.getString("chatId");
            chatroomTitle = bundle.getString("chatroomTitle");
            username = bundle.getString("username");
            targetUsername = bundle.getString("targetUsername");
            if(chatId.isEmpty()){
                publicChatId = bundle.getString("publicChatId");
            }
        }
        mAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        toolbar = findViewById(R.id.toolbar_pm_chat);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        tvTitle = toolbar.findViewById(R.id.tv_pm_chat_title);
        tvTitle.setText(chatroomTitle);
        tvTargetUsername = toolbar.findViewById(R.id.tv_pm_chat_name);
        tvTargetUsername.setText(targetUsername);

        llQuoteArea = findViewById(R.id.ll_chat_input_area_quote);

        recyclerView = findViewById(R.id.recycler_view_pm_chat);
        privateChatRecords = new ArrayList<>();

        mAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mStoreRef = FirebaseStorage.getInstance().getReference();
        if(!chatId.isEmpty()) {
            getBothUserName(new Callback() {
                @Override
                public void callback() {
                    tvTargetUsername.setText(targetUsername);
                    extractPrivateChatRecords();
                    ChatInputAreaFragment chatInputAreaFragment = new ChatInputAreaFragment(chatId, chatroomTitle, username, true, true);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fl_input_area, chatInputAreaFragment).commit();
                }
            });
        }else{
            ChatInputAreaFragment chatInputAreaFragment = new ChatInputAreaFragment(chatId, chatroomTitle, username, true, true);
            getSupportFragmentManager().beginTransaction().replace(R.id.fl_input_area, chatInputAreaFragment).commit();
        }

    }

    public String getQuotedText() {
        return quotedText;
    }

    @Override
    public void onDestroy() {
        mDatabaseRef.child("users/" + mAuth.getCurrentUser().getUid() + "/privateChat/" + chatId + "/unread/").removeEventListener(mListener);
        super.onDestroy();
        return;
    }

    private void extractPrivateChatRecords() {
        Query query = mDatabaseRef.child("message/"+chatId);
        query.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                privateChatRecords.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    PrivateChatRecord chatRecord = postSnapshot.getValue(PrivateChatRecord.class);
                    chatRecord.setId(postSnapshot.getKey());
                    chatRecord.setTime(postSnapshot.child("timeStamp").getValue(Long.class));
                    if(username !=null){
                        chatRecord.setUser(username.equals(chatRecord.getName()));
                    }
                    privateChatRecords.add(chatRecord);
                }
                //clear unread
                //update view
                recyclerView.setLayoutManager(new LinearLayoutManager(PrivateMessageChatActivity.this));
                adapter = new PrivateChatRecyclerAdapter(PrivateMessageChatActivity.this, privateChatRecords);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Log.d(TAG, "cancel"+databaseError);
            }
        });
        Query query2 = mDatabaseRef.child("users/"+mAuth.getCurrentUser().getUid()+"/privateChat/"+chatId+"/unread/");
        mListener  = query2.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                mDatabaseRef.child("users/"+mAuth.getCurrentUser().getUid()+"/privateChat/"+chatId+"/unread/").setValue(0);
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
//                        JSONObject privateChatJObject = response.getJSONObject(i);
//                        PrivateChatRecord privateChatRecord = new PrivateChatRecord();
//                        privateChatRecord.setText(privateChatJObject.getString("text"));
//                        privateChatRecord.setImage(privateChatJObject.getString("image"));
//                        privateChatRecord.setQuotedID(privateChatJObject.getString("quoted_id"));
//                        privateChatRecord.setTime(privateChatJObject.getString("time"));
//                        privateChatRecord.setUser(privateChatJObject.getBoolean("is_user"));
//                        privateChatRecords.add(privateChatRecord);
//                        recyclerView.setLayoutManager(new LinearLayoutManager(PrivateMessageChatActivity.this));
//                        adapter = new PrivateChatRecyclerAdapter(PrivateMessageChatActivity.this, privateChatRecords);
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

    private void getBothUserName(Callback callback){
        Query query = mDatabaseRef.child("nameToId/"+chatId);
        query.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String id = postSnapshot.child("id").getValue(String.class);
                    Log.d(TAG, "username"+ username);
                    if(!id.equals(mAuth.getCurrentUser().getUid())){
                        targetUsername = postSnapshot.getKey();
                    }else{
                        username = postSnapshot.getKey();
                    }
                }
                Log.d(TAG, "name"+ username+targetUsername);
                callback.callback();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Log.d(TAG, "cancel"+databaseError);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

    public void searchPrivateChatRecordByID(String chatID) {
        // TO-DO: should search for the privateChatRecord in the backend

    }

    public void openReplyHandlerDialog(PrivateChatRecord privateChatRecord) {
        Dialog dialog = new PrivateReplyHandlerDialog(PrivateMessageChatActivity.this, privateChatRecord, null);
        dialog.show();
    }

    public void quote(PrivateChatRecord privateChatRecord) {
        llQuoteArea.setVisibility(View.VISIBLE);
        TextView tvQuotedText = llQuoteArea.findViewById(R.id.tv_chat_input_area_quote_text);
        ImageView tvQuotedImage = llQuoteArea.findViewById(R.id.iv_chat_input_area_quote_image);

        if (privateChatRecord.getImage().isEmpty()) {
            tvQuotedImage.setVisibility(View.GONE);
            tvQuotedText.setText(privateChatRecord.getText());
            quotedText = privateChatRecord.getText();
        }
        else {
            tvQuotedImage.setVisibility(View.VISIBLE);
            tvQuotedText.setText("");
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

    public void deleteMessage(String id) {
        Log.d(TAG, "deleteMessage: "+id);
        mDatabaseRef.child("message/" + chatId+"/"+id).removeValue();
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
                    Long timeStamp = postSnapshot.child("timeStamp").getValue(Long.class);
                    if (latestReply.equals("")){
                        latestReply = "{photo}";
                    }
                    mDatabaseRef.child("privateChat/"+ chatId+"/latestName/").setValue(latestName);
                    mDatabaseRef.child("privateChat/"+ chatId+"/latestReply/").setValue(latestReply);
                    mDatabaseRef.child("privateChat/"+ chatId+"/latestReplyTime/").setValue(timeStamp);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Log.d(TAG, "cancel"+databaseError);
            }
        });
    }


    private void sendTextReply(String message, String name){
        if(!message.equals("") && !name.equals("")) {
            ChatroomChatRecord chat = new ChatroomChatRecord(name, message, "", ServerValue.TIMESTAMP, quotedText, true);
            mDatabaseRef.child("message/" + chatId).push().setValue(chat, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                    updateChatMeta(name, message);
                    notifyTargetUser(message);
                }
            });
        }
    }
    private void notifyTargetUser(String message){
        Query query = mDatabaseRef.child("nameToId/"+chatId);
        query.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String targetUserId = postSnapshot.child("id").getValue(String.class);
                    if(!targetUserId.equals(mAuth.getCurrentUser().getUid())){
                        NotiMessage msg = new NotiMessage(username, message , chatroomTitle);
                        mDatabaseRef.child("users/"+targetUserId+"/privateChat/"+chatId+"/unread/").setValue(ServerValue.increment(1));
                        mDatabaseRef.child("users/"+targetUserId+"/notiMessage/").push().setValue(msg);
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Log.d(TAG, "cancel"+databaseError);
            }
        });
    }


    private void updateChatMeta(String name, String message){
        mDatabaseRef.child("privateChat/" + chatId+"/latestName").setValue(name);
        mDatabaseRef.child("privateChat/" + chatId+"/latestReply").setValue(message);
        mDatabaseRef.child("privateChat/" + chatId+"/latestReplyTime").setValue(ServerValue.TIMESTAMP);
    }


    public void createPrivateChat(Callback callback){
        PrivateMessageRecord PM_Chat = new PrivateMessageRecord();
        PM_Chat.setTitle(chatroomTitle);
        mDatabaseRef.child("privateChat/").push().setValue(PM_Chat,  new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError,
                                   DatabaseReference databaseReference) {
                String pChatId = databaseReference.getKey();
                chatId = pChatId;
                subscribeChatForBothUser(callback);
            }
        });
    }

    public void subscribeChatForBothUser(Callback callback){
        Query query = mDatabaseRef.child("nameToId/"+publicChatId+"/"+targetUsername);
        query.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                String targetUserId = dataSnapshot.child("id").getValue(String.class);
                mDatabaseRef.child("nameToId/" + chatId).child(username+"/id").setValue(mAuth.getCurrentUser().getUid());
                mDatabaseRef.child("nameToId/" + chatId).child(targetUsername+"/id").setValue(targetUserId);
                //subscribe PrivateChat
                mDatabaseRef.child("users/"+targetUserId+"/privateChat/"+chatId+"/unread/").setValue(0);
                mDatabaseRef.child("users/"+mAuth.getCurrentUser().getUid()+"/privateChat/"+chatId+"/unread/").setValue(0);

                extractPrivateChatRecords();
                callback.callback();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Log.d(TAG, "cancel"+databaseError);
            }
        });
    }
    public void invokeSendTextReply(String message, String name){
        if(chatId.isEmpty()){
            createPrivateChat(new Callback(){
                @Override
                public void callback() {
                    sendTextReply(message, name);
                }
            });
        }else{
            sendTextReply(message, name);
        }
    }
    public void invokeSendImageReply(Uri imageUri, String name) {
        if(chatId.isEmpty()){
            createPrivateChat(new Callback(){
                @Override
                public void callback() {
                    sendImageReply(imageUri, name);
                }
            });
        }else{
            sendImageReply(imageUri, name);
        }
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
                            notifyTargetUser("{photo}");
                        }
                    });
                }
            });
        }
    }
}

class PrivateReplyHandlerDialog extends Dialog {
    private ListView listView;
    private PrivateChatRecord privateChatRecord;
    private PrivateChatRecord quotedPrivateChatRecord;
    TextView lvName; // To-DO
    Context context;

    public PrivateReplyHandlerDialog(@NonNull Context context, PrivateChatRecord privateChatRecord, PrivateChatRecord quotedPrivateChatRecord) {
        super(context);
        this.context = context;
        this.privateChatRecord = privateChatRecord;
        this.quotedPrivateChatRecord = quotedPrivateChatRecord;
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
        if (privateChatRecord.isUser()) {
            ls.add("Delete");
        }
        ls.add("Cancel");
        setContentView(R.layout.layout_dialog_reply_handler);

//        lvName = findViewById(R.id.lv_reply_handler_name);
//        lvName.setText(privateChatRecord.getName());

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
                        ClipData clip = ClipData.newPlainText("text", privateChatRecord.getText());
                        clipboard.setPrimaryClip(clip);
                        break;
                    case 1: // quote
                        ((PrivateMessageChatActivity) context).quote(privateChatRecord);
                        break;
                    case 2:
                        if (privateChatRecord.isUser()) {
                            // TO-DO: delete the reply (Backend)
                            ((PrivateMessageChatActivity) context).deleteMessage(privateChatRecord.getId());
                        }
                        else { //cancel

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

