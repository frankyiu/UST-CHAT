package com.example.ustchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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

public class PrivateMessageChatActivity extends AppCompatActivity {
    Toolbar toolbar;
    private String chatroomTitle;
    private String username;
    private String targetUsername;

    TextView tvTitle;
    TextView tvTargetUsername;

    RecyclerView recyclerView;
    PrivateChatRecyclerAdapter adapter;
    List<PrivateChatRecord> privateChatRecords;

    LinearLayout llQuoteArea;
    private static String JSON_URL = "https://jsonkeeper.com/b/VKKN";

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_message_chat);

        Bundle bundle = getIntent().getExtras();
        chatroomTitle = "";
        username = "";
        targetUsername = "";
        if(bundle != null) {
            chatroomTitle = bundle.getString("chatroomTitle");
            username = bundle.getString("username");
            targetUsername = bundle.getString("targetUsername");
        }

        toolbar = findViewById(R.id.toolbar_pm_chat);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        tvTitle = toolbar.findViewById(R.id.tv_pm_chat_title);
        tvTitle.setText(chatroomTitle);
        tvTargetUsername = toolbar.findViewById(R.id.tv_pm_chat_name);
        tvTargetUsername.setText(targetUsername);

        // TO-DO: hardcode for now
        ChatInputAreaFragment chatInputAreaFragment = new ChatInputAreaFragment(username, true);
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_input_area, chatInputAreaFragment).commit();

        llQuoteArea = findViewById(R.id.ll_chat_input_area_quote);

        recyclerView = findViewById(R.id.recyclerView);
        privateChatRecords = new ArrayList<>();
        extractPrivateChatRecords();
    }

    private void extractPrivateChatRecords() {
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, JSON_URL, null,new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject privateChatJObject = response.getJSONObject(i);
                        PrivateChatRecord privateChatRecord = new PrivateChatRecord();
                        privateChatRecord.setText(privateChatJObject.getString("text"));
                        privateChatRecord.setImage(privateChatJObject.getString("image"));
                        privateChatRecord.setQuotedID(privateChatJObject.getString("quoted_id"));
                        privateChatRecord.setTime(privateChatJObject.getString("time"));
                        privateChatRecord.setUser(privateChatJObject.getBoolean("is_user"));
                        privateChatRecords.add(privateChatRecord);
                        recyclerView.setLayoutManager(new LinearLayoutManager(PrivateMessageChatActivity.this));
                        adapter = new PrivateChatRecyclerAdapter(PrivateMessageChatActivity.this, privateChatRecords);
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
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
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
                switch(position){
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

