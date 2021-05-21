package com.example.ustchat;


import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import com.google.firebase.auth.FirebaseAuth;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class LoginRegisterActivityTest {

    private FirebaseAuth mAuth;
    @Rule
    public ActivityTestRule<LoginRegisterActivity> mActivityTestRule = new ActivityTestRule<>(LoginRegisterActivity.class);

    @Before
    public void setUp() throws Exception {
        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser()!=null){
            mAuth.signOut();
        }
        Intents.init();
    }

    @After
    public void tearDown() throws Exception {
        if(mAuth.getCurrentUser()!=null){
            mAuth.signOut();
        }
        Intents.release();
    }

    @Test
    public void loginRegisterActivityTest() {
        onView(withId(R.id.tv_toolbar_center_title)).check(matches(withText("Login")));
        onView(withId(R.id.tv_login_logo)).check(matches(withText("USTChat")));
        onView(withId(R.id.et_login_itsc)).check(matches(isDisplayed()));
        onView(withId(R.id.et_login_password)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_login_login)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_login_sign_up)).check(matches(isDisplayed()));
        onView(withId(R.id.tv_login_forgot_password)).check(matches(isDisplayed()));
        onView(withId(R.id.tv_login_continue_as_a_visitor)).check(matches(isDisplayed()));
        onView(withId(R.id.tv_login_warning_itsc)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(R.id.tv_login_warning_password)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(R.id.tv_login_warning_login_failure)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }

    @Test
    public void loginRegisterActivityTestEmptyInput() {
        onView(withId(R.id.btn_login_login)).perform(click());
        onView(withId(R.id.tv_login_warning_itsc)).check(matches(withText("ITSC account required.")));
        onView(withId(R.id.tv_login_warning_password)).check(matches(withText("password required.")));
    }

    @Test
    public void loginRegisterActivityTestEmptyInput2() {
        onView(withId(R.id.et_login_itsc)).perform(typeText("ccyeungaf"), closeSoftKeyboard());
        onView(withId(R.id.btn_login_login)).perform(click(), closeSoftKeyboard());
        onView(withId(R.id.tv_login_warning_itsc)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(R.id.tv_login_warning_password)).check(matches(withText("password required.")));
    }

    @Test
    public void loginRegisterActivityTestLogin() {
        onView(withId(R.id.et_login_itsc)).perform(typeText("ktyiuaa@connect.ust.hk"), closeSoftKeyboard());
        onView(withId(R.id.et_login_password)).perform(typeText("123123"), closeSoftKeyboard());
        onView(withId(R.id.btn_login_login)).perform(click());
        try {
            Thread.sleep(1000);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        intended(hasComponent(CourseActivity.class.getName()));
    }

    @Test
    public void loginRegisterActivityTestCreateAccount() {
        onView(withId(R.id.btn_login_sign_up)).perform(click());
        onView(withId(R.id.dialog_register)).check(matches(isDisplayed()));
        onView(withId(R.id.ib_create_account_submit)).perform(click());
        onView(withId(R.id.tv_create_account_warning_itsc_account)).check(matches(withText("ITSC account required.")));
        onView(withId(R.id.tv_create_account_warning_pw)).check(matches(withText("password required.")));
        onView(withId(R.id.et_create_account_itsc_account)).perform(typeText("1234"), closeSoftKeyboard());
        onView(withId(R.id.et_create_account_pw)).perform(typeText("1234"), closeSoftKeyboard());
        onView(withId(R.id.ib_create_account_submit)).perform(click());
//        onView(withId(R.id.tv_create_account_warning_itsc_account)).check(matches(withText("invalid ITSC account.")));
        onView(withId(R.id.tv_create_account_warning_pw)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }

    @Test
    public void loginRegisterActivityTestContinueAsVisitor() {
        onView(withId(R.id.tv_login_continue_as_a_visitor)).perform(click());
        intended(hasComponent(CourseActivity.class.getName()));
    }

    @Test
    public void loginRegisterActivityTestForgotPassword() {
        onView(withId(R.id.tv_login_forgot_password)).perform(click());
        onView(withId(R.id.dialog_forgot_password)).check(matches(isDisplayed()));
        onView(withId(R.id.ib_forgot_password_submit)).perform(click());
        onView(withId(R.id.tv_forgot_password_warning_itsc_account)).check(matches(withText("ITSC account required.")));
        onView(withId(R.id.et_forgot_password_itsc_account)).perform(typeText("1234"), closeSoftKeyboard());
        onView(withId(R.id.ib_forgot_password_submit)).perform(click());
        onView(withId(R.id.tv_forgot_password_warning_itsc_account)).check(matches(withText("invalid ITSC account.")));
    }

    @Test
    public void loginRegisterActivityTestInfoDialog() {
        onView(withId(R.id.login_info)).perform(click());
        onView(withId(R.id.tv_info_title)).check(matches(withText("What is USTChat?")));
        onView(withId(R.id.btn_info_ok)).perform(click());
        onView(withId(R.id.dialog_info)).check(doesNotExist());
    }

    @Test
    public void loginRegisterActivityTestRegister() {
        onView(withId(R.id.btn_login_sign_up)).perform(click());
        onView(withId(R.id.dialog_register)).check(matches(isDisplayed()));
        onView(withId(R.id.tv_create_account_title)).check(matches(withText("Create a USTChat account")));

        onView(withId(R.id.ib_create_account_submit)).perform(click());
        onView(withId(R.id.tv_create_account_warning_itsc_account)).check(matches(withText("ITSC account required.")));
        onView(withId(R.id.tv_create_account_warning_pw)).check(matches(withText("password required.")));

        onView(withId(R.id.et_create_account_itsc_account)).perform(typeText("ccyeungaf"), closeSoftKeyboard());
        onView(withId(R.id.ib_create_account_submit)).perform(click());
        onView(withId(R.id.tv_create_account_warning_itsc_account)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(R.id.tv_create_account_warning_pw)).check(matches(withText("password required.")));
    }

}
