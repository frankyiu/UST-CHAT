package com.example.ustchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

public class SettingActivity extends AppCompatActivity {
    private String userID;
    private String ITSCAccount;
    Toolbar toolbar;
    BottomNavigationView bottomNavigationView;

    TextView tvUserID, tvITSC;
    MaterialCardView cvAboutUs, cvContactUs, cvFeedback, cvLogout;
    Switch switchNightMode, switchEnableNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        toolbar = findViewById(R.id.toolbar_center_title);
        ((TextView) toolbar.findViewById(R.id.tv_toolbar_center_title)).setText("Setting");
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setSelectedItemId(R.id.setting);
        BadgeDrawable badge = bottomNavigationView.getOrCreateBadge(R.id.private_message);
        //TO-DO : hardcode for now
        badge.setNumber(1);

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
                startActivity(new Intent(getApplicationContext(), LoginRegisterActivity.class));
            }
        });

        switchNightMode = findViewById(R.id.switch_setting_night_mode);
        switchEnableNotification = findViewById(R.id.switch_setting_enable_notification);

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

        }
        else {

        }
    }

}