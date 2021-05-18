package com.example.ustchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

public class CourseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, DrawerLayout.DrawerListener{
    String category;
    Toolbar toolbar;
    BottomNavigationView bottomNavigationView;
    DrawerLayout drawerLayout;

    // data for the three level expandable list view
    List<String> catNames;
    List<List<String>> secondLevel = new ArrayList<>();
    List<LinkedHashMap<String, List<String>>> data = new ArrayList<>();
    List<String> courseCodes; // used in searching

    NavigationView navigationView;
    private ExpandableListView expandableListView;
    EditText etSearch;
    ListView listView;
    PopupWindow popupWindow;
    ArrayAdapter<String> adapter;
    ThreeLevelListAdapter threeLevelListAdapterAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);

        populateChatroomList();
        setUpExpandableListViewAdapter();

        toolbar = findViewById(R.id.toolbar);
        bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setSelectedItemId(R.id.chatroom);
        BadgeDrawable badge = bottomNavigationView.getOrCreateBadge(R.id.private_message);
        //TO-DO : hardcode for now
        badge.setNumber(1);

        setSupportActionBar(toolbar);

        listView = new ListView(getApplicationContext());
        adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, courseCodes);
        listView.setAdapter(adapter);

        navigationView = findViewById(R.id.side_menu);
        navigationView.setNavigationItemSelectedListener(this);
        etSearch = findViewById(R.id.et_course_list_search);

        popupWindow = new PopupWindow(getApplication());
        popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.popup_window_course_list_search));
        popupWindow.setContentView(listView);
        popupWindow.setHeight(700);
        ((ListView) popupWindow.getContentView()).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                closeDrawer();
                switchChatroomFragment(listView.getItemAtPosition(position).toString());
            }
        });

        etSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    popupWindow.setWidth(etSearch.getMeasuredWidth());
                    popupWindow.showAsDropDown(view, 0, 0);
                }
                else {
                    popupWindow.dismiss();
                }
            }
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }
        });

        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, 0, 0) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        drawerLayout.addDrawerListener(toggle);
        drawerLayout.addDrawerListener(this);
        toggle.syncState();

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            // TO-DO remain the previous state
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.chatroom:
                        return true;
                    case R.id.private_message:
                        startActivity(new Intent(getApplicationContext(), PrivateMessageActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.setting:
                        startActivity(new Intent(getApplicationContext(), SettingActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                }
                return false;
            }
        });

        category = "General Chatroom";
        switchChatroomFragment(category);
    }

    public void closeDrawer() {
        navigationView.getMenu().findItem(R.id.my_bookmarks).setChecked(false);
        navigationView.getMenu().findItem(R.id.general_chatroom).setChecked(false);

        if (popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
        expandableListView.setAdapter(threeLevelListAdapterAdapter);    //collapse the list view
        etSearch.getText().clear();
        drawerLayout.closeDrawer(GravityCompat.START);
    }


    public void populateChatroomList() {
        catNames = new ArrayList<>();
        data = new ArrayList<>();
        courseCodes = new ArrayList<>();

        try {
            InputStream is = getAssets().open("school2code.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            JSONObject school2majorJson = new JSONObject(new String(buffer, "UTF-8"));

            Iterator<String> school2majorkeys = school2majorJson.keys();
            while(school2majorkeys.hasNext()){
                String schoolKey = school2majorkeys.next();
                catNames.add(schoolKey);

                List<String> majorList = new ArrayList<>();
                LinkedHashMap<String, List<String>> major2CourseDict = new LinkedHashMap<>();

                JSONObject major2courseJson = school2majorJson.getJSONObject(schoolKey);
                Iterator<String> major2courseKeys = major2courseJson.keys();
                while(major2courseKeys.hasNext()) {
                    String majorKey = major2courseKeys.next();
                    majorList.add(majorKey);
                    JSONArray courseJsonArr = major2courseJson.getJSONArray(majorKey);
                    List<String> courseList = new ArrayList<>();
                    for (int i = 0; i < courseJsonArr.length(); i++) {
                        String courseCode = courseJsonArr.getString(i);
                        courseList.add(courseCode);
                        courseCodes.add(courseCode);
                    }
                    major2CourseDict.put(majorKey, courseList);
                }

                secondLevel.add(majorList);
                data.add(major2CourseDict);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else {
            closeDrawer();
        }
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.search) {
            openSearchChatroomDialog();
        }
        else if (id == R.id.create_chatroom) {
            //TO-DO: check if log-in
            //check if segment is not my bookmark
            if (!toolbar.getTitle().equals("My Bookmarks")) {
                openCreateChatroomDialog();
            }
        }
        return true;
    }

    public void openCreateChatroomDialog() {
        CreateChatroomDialog dialogCreateChatroom = new CreateChatroomDialog(this, category);
        dialogCreateChatroom.show();
    }

    public void openSearchChatroomDialog() {
        SearchChatroomDialog dialogSearchChatroom = new SearchChatroomDialog(this, category);
        dialogSearchChatroom.show();
    }

    private void setUpExpandableListViewAdapter() {
        expandableListView = (ExpandableListView) findViewById(R.id.expandible_listview);
        threeLevelListAdapterAdapter = new ThreeLevelListAdapter(this, catNames, secondLevel, data);
        expandableListView.setAdapter(threeLevelListAdapterAdapter);

        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                if (data.get(groupPosition).isEmpty()) {
                    closeDrawer();
                    switchChatroomFragment(catNames.get(groupPosition));
                }
                return false;
            }
        });
    }

    void switchChatroomFragment(String category) {
        this.category = category;
        toolbar.setTitle(category);
        CourseFragment courseFragment = CourseFragment.newInstance(category);
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, courseFragment).commit();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.my_bookmarks) {
            closeDrawer();
            switchChatroomFragment("My Bookmarks");
        }
        else if (id == R.id.general_chatroom) {
            closeDrawer();
            switchChatroomFragment("General Chatroom");
        }
        return true;
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getX(), (int)event.getY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }

    @Override
    public void onDrawerSlide(@NonNull View drawerView, float slideOffset) { }

    @Override
    public void onDrawerOpened(@NonNull View drawerView) { }

    @Override
    public void onDrawerClosed(@NonNull View drawerView) {
        closeDrawer();
    }

    @Override
    public void onDrawerStateChanged(int newState) { }
}


class CreateChatroomDialog extends Dialog {
    private String chatroomCategory;
    private ImageButton ibCreateChatroom;
    private EditText etTitle;
    private EditText etName;
    private ChipGroup chipGroup;
    private ViewFlipper vfChipGroup;
    private TextView tvWarningInvalidTitle;
    private TextView tvWarningInvalidName;

    public CreateChatroomDialog(final Context context, String chatroomCategory) {
        super(context);
        this.chatroomCategory = chatroomCategory;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_create_chatroom);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        vfChipGroup = findViewById(R.id.vf_create_chatroom_chip_group);
        etTitle = findViewById(R.id.et_create_chatroom_title);
        etName = findViewById(R.id.et_create_chatroom_name);
        ibCreateChatroom = findViewById(R.id.btn_create_chatroom_submit);
        tvWarningInvalidTitle = findViewById(R.id.tv_create_chatroom_warning_invalid_title);
        tvWarningInvalidName = findViewById(R.id.tv_create_chatroom_warning_invalid_name);

        if (chatroomCategory.equals("General Chatroom")) {
            vfChipGroup.setDisplayedChild(vfChipGroup.indexOfChild(findViewById(R.id.vf_create_chatroom_chip_general)));
            chipGroup = findViewById(R.id.chipgroup_tag_general);
        }
        else {
            vfChipGroup.setDisplayedChild(vfChipGroup.indexOfChild(findViewById(R.id.vf_create_chatroom_chip_course)));
            chipGroup = findViewById(R.id.chipgroup_tag_course);
        }

        ibCreateChatroom.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                JSONObject chatroomJson = createChatroom();
                if (chatroomJson != null) {
                    dismiss();
                }
            }
        });

        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            chip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                    if (checked) {
                        if (chipGroup.getCheckedChipIds().size() > 3) {
                            chip.setChecked(false);
                        }
                    }
                }
            });
        }

        clearWarningMessages();

        String generatedStudentName = generateStudentName();
        etName.setText(generatedStudentName);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getX(), (int)event.getY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }

    //TO-DO: generate a student name
    public String generateStudentName() {
        return "Student12345";
    }

    public boolean validateChatroomSubmission(String chatroomTitle, String chatroomName) {
        boolean success = true;

        if (chatroomTitle.isEmpty()) {
            etTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_error, 0);
            tvWarningInvalidTitle.setVisibility(View.VISIBLE);
            success = false;
        }
        else {
            etTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            tvWarningInvalidTitle.setVisibility(View.GONE);
        }

        if (chatroomName.isEmpty()) {
            etName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_error, 0);
            tvWarningInvalidName.setVisibility(View.VISIBLE);
            success = false;
        }
        else {
            etName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            tvWarningInvalidName.setVisibility(View.GONE);
        }

        return success;
    }

    public void clearWarningMessages() {
        tvWarningInvalidTitle.setVisibility(View.GONE);
        tvWarningInvalidName.setVisibility(View.GONE);
    }

    public JSONObject createChatroom() {
        String chatroomTitle = etTitle.getText().toString();
        String chatroomName = etName.getText().toString();

        if (validateChatroomSubmission(chatroomTitle, chatroomName)) {
            List<Integer> ids = chipGroup.getCheckedChipIds();
            JSONArray chatroomTags = new JSONArray();

            for (Integer id : ids) {
                Chip chip = chipGroup.findViewById(id);
                chatroomTags.put(chip.getText().toString());
            }

            JSONObject chatroom = new JSONObject();
            try {
                chatroom.put("title", chatroomTitle);
                chatroom.put("name", chatroomName);
                chatroom.put("tags", chatroomTags);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return chatroom;
        }
        return null;
    }
}

class SearchChatroomDialog extends Dialog {
    private String chatroomCategory;
    private ImageButton ibSubmit;
    private EditText etTitle;
    private ChipGroup chipGroup;
    private ViewFlipper vfChipGroup;
    private TextView tvWarningInvalidTitle;

    public SearchChatroomDialog(final Context context, String chatroomCategory) {
        super(context);
        this.chatroomCategory = chatroomCategory;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_search_chatroom);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        vfChipGroup = findViewById(R.id.vf_search_chatroom_chip_group);
        etTitle = findViewById(R.id.et_search_chatroom_title);
        ibSubmit = findViewById(R.id.btn_search_chatroom_submit);
        tvWarningInvalidTitle = findViewById(R.id.tv_search_chatroom_warning_invalid_title);

        if (chatroomCategory.equals("General Chatroom")) {
            vfChipGroup.setDisplayedChild(vfChipGroup.indexOfChild(findViewById(R.id.vf_search_chatroom_chip_general)));
            chipGroup = findViewById(R.id.chipgroup_tag_general);
        }
        else {
            vfChipGroup.setDisplayedChild(vfChipGroup.indexOfChild(findViewById(R.id.vf_search_chatroom_chip_course)));
            chipGroup = findViewById(R.id.chipgroup_tag_course);
        }

        ibSubmit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                JSONObject chatroomJson = createChatroom();
                if (chatroomJson != null) {
                    dismiss();
                }
            }
        });

        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            chip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                    if (checked) {
                        if (chipGroup.getCheckedChipIds().size() > 3) {
                            chip.setChecked(false);
                        }
                    }
                }
            });
        }

        clearWarningMessages();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getX(), (int)event.getY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }

    public boolean validateChatroomSubmission(String chatroomTitle) {
        boolean success = true;

        if (chatroomTitle.isEmpty()) {
            etTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_error, 0);
            tvWarningInvalidTitle.setVisibility(View.VISIBLE);
            success = false;
        }
        else {
            etTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            tvWarningInvalidTitle.setVisibility(View.GONE);
        }

        return success;
    }

    public void clearWarningMessages() {
        tvWarningInvalidTitle.setVisibility(View.GONE);
    }

    public JSONObject createChatroom() {
        String chatroomTitle = etTitle.getText().toString();

        if (validateChatroomSubmission(chatroomTitle)) {
            List<Integer> ids = chipGroup.getCheckedChipIds();
            JSONArray chatroomTags = new JSONArray();

            for (Integer id : ids) {
                Chip chip = chipGroup.findViewById(id);
                chatroomTags.put(chip.getText().toString());
            }

            JSONObject chatroom = new JSONObject();
            try {
                chatroom.put("title", chatroomTitle);
                chatroom.put("tags", chatroomTags);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return chatroom;
        }
        return null;
    }
}
