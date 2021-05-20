package com.example.ustchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
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

public class SettingActivity extends AppCompatActivity implements NavigationNotification {
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
        BadgeDrawable notificationBadge = bottomNavigationView.getOrCreateBadge(R.id.private_message);
        //TO-DO : hardcode for now

        notificationBadge.setNumber(1);
        enableNotificationBadge(Utility.enableNotification);


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
                openAboutUsDialog();
            }
        });

        cvContactUs = findViewById(R.id.cv_setting_contact_us);
        cvContactUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openContactUsDialog();
            }
        });

        cvLogout = findViewById(R.id.cv_setting_logout);
        cvLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Logout
                mAuth.signOut();
                startActivity(new Intent(getApplicationContext(), LoginRegisterActivity.class));
            }
        });

        switchNightMode = findViewById(R.id.switch_setting_night_mode);
        switchNightMode.setChecked(Utility.isNightModeOn(this));
        switchNightMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setNightMode(switchNightMode.isChecked());
            }
        });

        switchEnableNotification = findViewById(R.id.switch_setting_enable_notification);
        switchEnableNotification.setChecked(Utility.enableNotification);
        switchEnableNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setEnableNotification(switchEnableNotification.isChecked());
            }
        });

    }

    private void openAboutUsDialog() {
        InfoDialog dialogInfo = new InfoDialog(this, getResources().getString(R.string.ustchat_about_us_title),
                getResources().getString(R.string.ustchat_about_us_description));
        dialogInfo.show();
    }


    private void openContactUsDialog() {
        InfoDialog dialogInfo = new InfoDialog(this, getResources().getString(R.string.ustchat_contact_us_title),
                getResources().getString(R.string.ustchat_contact_us_description));
        dialogInfo.show();
    }

    private void setNightMode(boolean nightModeOn) {
        if (nightModeOn) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    public void enableNotificationBadge(boolean enable) {
        bottomNavigationView.getOrCreateBadge(R.id.private_message).setVisible(enable);
    }

    private void setEnableNotification(boolean enable) {
        enableNotificationBadge(enable);
        Utility.enableNotification = enable;
    }

}