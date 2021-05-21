package com.example.ustchat;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.test.espresso.intent.Intents;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class SettingActivityTest {

    @Rule
    public ActivityTestRule<SettingActivity> mActivityTestRule = new ActivityTestRule<>(SettingActivity.class);

    @Before
    public void setUp() throws Exception {
        Intents.init();
    }

    @After
    public void tearDown() throws Exception {
        Intents.release();
    }

    @Test
    public void settingActivityTest() {
        onView(withId(R.id.tv_toolbar_center_title)).check(matches(withText("Setting")));
        onView(withId(R.id.tv_setting_user_id)).check(matches(isDisplayed()));
        onView(withId(R.id.tv_setting_itsc)).check(matches(isDisplayed()));
        onView(withId(R.id.switch_setting_night_mode)).check(matches(isDisplayed()));
        onView(withId(R.id.switch_setting_enable_notification)).check(matches(isDisplayed()));
        onView(withText("User ID")).check(matches(isDisplayed()));
        onView(withText("ITSC Account")).check(matches(isDisplayed()));
        onView(withText("About us")).check(matches(isDisplayed()));
        onView(withText("Contact us")).check(matches(isDisplayed()));
        onView(withText("Night Mode")).check(matches(isDisplayed()));
        onView(withText("Enable Notification")).check(matches(isDisplayed()));
        onView(withText("Logout")).check(matches(isDisplayed()));
    }

    @Test
    public void settingActivityTestNightMode() {
        if (Utility.isNightModeOn(mActivityTestRule.getActivity())) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        assert !Utility.isNightModeOn(mActivityTestRule.getActivity());
        onView(withId(R.id.switch_setting_night_mode)).perform(click());
        assert Utility.isNightModeOn(mActivityTestRule.getActivity());
        onView(withId(R.id.switch_setting_night_mode)).perform(click());
        assert !Utility.isNightModeOn(mActivityTestRule.getActivity());

    }

    @Test
    public void settingActivityTestEnableNotification() {
        assert Utility.enableNotification;
        onView(withId(R.id.switch_setting_enable_notification)).perform(click());
        assert !Utility.enableNotification;
        onView(withId(R.id.switch_setting_enable_notification)).perform(click());
        assert Utility.enableNotification;
    }

    @Test
    public void settingActivityTestNavToCourseActivity() {
        onView(withId(R.id.chatroom)).perform(click());
        intended(hasComponent(CourseActivity.class.getName()));
    }

    @Test
    public void settingActivityTestNavToPMActivity() {
        onView(withId(R.id.private_message)).perform(click());
        intended(hasComponent(PrivateMessageActivity.class.getName()));
    }

    @Test
    public void settingActivityTestLogout() {
        onView(withId(R.id.cv_setting_logout)).perform(click());
        intended(hasComponent(LoginRegisterActivity.class.getName()));
    }

}