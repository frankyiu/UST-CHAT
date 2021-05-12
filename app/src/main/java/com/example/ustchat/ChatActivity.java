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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    Toolbar toolbar;
    EditText etReply;
    EditText etName;

    RecyclerView recyclerView;
    ChatroomChatRecyclerAdapter adapter;
    List<ChatroomChatRecord> chatroomChatRecords;

    LinearLayout llQuoteArea;

    Dialog dialog;

    private static String JSON_URL = "https://jsonkeeper.com/b/QZRG";

    static String userName = "CPEG guy";
    static boolean userRepliedBefore = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Bundle bundle = getIntent().getExtras();
        String chatroomTitle = ""; // or other values
        if(bundle != null) {
            chatroomTitle = bundle.getString("chatroomTitle");
        }

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

        llQuoteArea = findViewById(R.id.ll_chat_input_area_quote);

        recyclerView = findViewById(R.id.recyclerView);
        chatroomChatRecords = new ArrayList<>();
        extractChatroomChatRecords();
    }

    private void extractChatroomChatRecords() {
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, JSON_URL, null,new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject chatroomObject = response.getJSONObject(i);
                        ChatroomChatRecord chatroomChatRecord = new ChatroomChatRecord();
                        chatroomChatRecord.setName(chatroomObject.getString("name"));
                        chatroomChatRecord.setContent(chatroomObject.getString("content"));
                        chatroomChatRecord.setQuotedContent(chatroomObject.getString("quoted_content"));
                        chatroomChatRecord.setTime(chatroomObject.getString("time"));
                        chatroomChatRecord.setUser(chatroomObject.getBoolean("is_user"));
                        chatroomChatRecords.add(chatroomChatRecord);
                        recyclerView.setLayoutManager(new LinearLayoutManager(ChatActivity.this));
                        adapter = new ChatroomChatRecyclerAdapter(ChatActivity.this, chatroomChatRecords);
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