package com.example.ustchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Collections;

public class SettingActivity extends AppCompatActivity {
    private String userID;
    private String ITSCAccount;
    Toolbar toolbar;
    BottomNavigationView bottomNavigationView;

    TextView tvUserID, tvITSC;
    MaterialCardView cvAboutUs, cvContactUs, cvFeedback, cvLogout;
    Switch switchNightMode, switchEnableNotification;
    FirebaseAuth mAuth;
    DatabaseReference mDatabaseRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        setContentView(R.layout.activity_setting);

        toolbar = findViewById(R.id.toolbar_center_title);
        ((TextView) toolbar.findViewById(R.id.tv_toolbar_center_title)).setText("Setting");
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setSelectedItemId(R.id.setting);
        BadgeDrawable badge = bottomNavigationView.getOrCreateBadge(R.id.private_message);
        //TO-DO : hardcode for now
        badge.setNumber(0);
//        if(mAuth.getCurrentUser()!=null){
//            unReadPMCount();
//        }

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.chatroom:
                        startActivity(new Intent(getApplicationContext(), CourseActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.private_message:
                        startActivity(new Intent(getApplicationContext(), PrivateMessageActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.setting:
                        return true;
                }
                return false;
            }
        });

        tvUserID = findViewById(R.id.tv_setting_user_id);
        tvITSC = findViewById(R.id.tv_setting_itsc);
        tvUserID.setText(mAuth.getCurrentUser().getUid());
        tvITSC.setText(mAuth.getCurrentUser().getEmail().split("@")[0]);

        cvAboutUs = findViewById(R.id.cv_setting_about_us);
        cvAboutUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        cvContactUs = findViewById(R.id.cv_setting_contact_us);
        cvContactUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        cvFeedback = findViewById(R.id.cv_setting_feedback);
        cvFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        cvLogout = findViewById(R.id.cv_setting_logout);
        cvLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                startActivity(new Intent(getApplicationContext(), LoginRegisterActivity.class));
            }
        });

        switchNightMode = findViewById(R.id.switch_setting_night_mode);
        switchEnableNotification = findViewById(R.id.switch_setting_enable_notification);

    }


//    private void unReadPMCount(){
//        Query query = mDatabaseRef.child("users/"+mAuth.getCurrentUser().getUid()+"/privateChat/");
//        query.addValueEventListener(new ValueEventListener()
//        {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                int sum = 0;
//                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
//                    Log.d("Setting", postSnapshot.getValue(String.class));
//                    int count  = postSnapshot.getValue(Integer.class);
//                    sum+=count;
//                }
//                Log.d("Setting", ""+sum);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError)
//            {
//                Log.d("Setting", "cancel"+databaseError);
//            }
//        });
//    }
}