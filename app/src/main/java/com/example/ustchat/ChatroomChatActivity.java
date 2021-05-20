package com.example.ustchat;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ChatroomChatActivity extends AppCompatActivity {
    Toolbar toolbar;
    String chatroomTitle;

    RecyclerView recyclerView;
    ChatroomChatRecyclerAdapter adapter;
    List<ChatroomChatRecord> chatroomChatRecords;

    LinearLayout llQuoteArea;

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
        if (bundle != null) {
            chatroomTitle = bundle.getString("chatroomTitle");
        }

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(chatroomTitle);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        llQuoteArea = findViewById(R.id.ll_chat_input_area_quote);

        recyclerView = findViewById(R.id.recycler_view_chatroom);
        chatroomChatRecords = new ArrayList<>();
        extractChatroomChatRecords();

        // TO-DO: hardcode for now
        ChatInputAreaFragment chatInputAreaFragment = new ChatInputAreaFragment(username, userRepliedBefore);
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_input_area, chatInputAreaFragment).commit();
    }

    private void extractChatroomChatRecords() {
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, JSON_URL, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject chatroomObject = response.getJSONObject(i);
                        ChatroomChatRecord chatroomChatRecord = new ChatroomChatRecord();
                        chatroomChatRecord.setName(chatroomObject.getString("name"));
                        chatroomChatRecord.setText(chatroomObject.getString("text"));
                        chatroomChatRecord.setImage(chatroomObject.getString("image"));
                        chatroomChatRecord.setQuotedID(chatroomObject.getString("quoted_id"));
                        chatroomChatRecord.setTime(chatroomObject.getString("time"));
                        chatroomChatRecord.setUser(chatroomObject.getBoolean("is_user"));
                        chatroomChatRecords.add(chatroomChatRecord);
                        recyclerView.setLayoutManager(new LinearLayoutManager(ChatroomChatActivity.this));
                        adapter = new ChatroomChatRecyclerAdapter(ChatroomChatActivity.this, chatroomChatRecords);
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
        } else {
            tvQuotedImage.setVisibility(View.VISIBLE);
            tvQuotedText.setText("");
        }

        ImageButton ibQuotedCancel = llQuoteArea.findViewById(R.id.iv_chat_input_area_quote_cancel);
        ibQuotedCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                llQuoteArea.setVisibility(View.GONE);
                ;
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

    public void startPrivateMessageChat(String targetUser) {
        Intent intent = new Intent(ChatroomChatActivity.this, PrivateMessageChatActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("chatroomTitle", chatroomTitle);
        bundle.putString("username", username);
        bundle.putString("targetUsername", targetUser);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtras(bundle);
        startActivity(intent);
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

                        } else {
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
