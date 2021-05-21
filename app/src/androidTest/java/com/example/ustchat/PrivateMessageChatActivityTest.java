package com.example.ustchat;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasType;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class PrivateMessageChatActivityTest {
    @Rule
    public ActivityTestRule<PrivateMessageChatActivity> mActivityTestRule = new ActivityTestRule<>(PrivateMessageChatActivity.class);

    @Before
    public void setUp() throws Exception {
        Intents.init();
    }

    @After
    public void tearDown() throws Exception {
        Intents.release();
    }

    @Test
    public void privateMessageChatActivityTest() {
        onView(withId(R.id.tv_pm_chat_title)).check(matches(isDisplayed()));
        onView(withId(R.id.tv_pm_chat_name)).check(matches(isDisplayed()));
        onView(withId(R.id.recycler_view_chatroom)).check(matches(isDisplayed()));
        onView(withId(R.id.fl_input_area)).check(matches(isDisplayed()));
        onView(withId(R.id.ll_chat_input_area_quote)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }

    @Test
    public void privateMessageChatActivityTestInputArea() {
        onView(withId(R.id.iv_input_area_submit)).check(matches(not(isEnabled())));
        onView(withId(R.id.et_input_area_reply)).perform(typeText("hi there"), closeSoftKeyboard());
        onView(withId(R.id.iv_input_area_submit)).check(matches(isEnabled()));
        onView(withId(R.id.et_input_area_reply)).perform(replaceText(""), closeSoftKeyboard());
        onView(withId(R.id.iv_input_area_submit)).check(matches(not(isEnabled())));
        onView(withId(R.id.ib_input_area_album)).perform(click());
        intended(allOf(hasAction(equalTo(Intent.ACTION_CHOOSER)), hasExtra(equalTo(Intent.EXTRA_INTENT),
                allOf(hasAction(equalTo(Intent.ACTION_GET_CONTENT)), hasType(is("image/*"))))));
    }

    @Test
    public void courseActivityReplyHandler() {
        try {
            Thread.sleep(1000);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(allOf(withId(R.id.cv_chat_bubble), childAtPosition(childAtPosition(withId(R.id.recycler_view_pm_chat), 0), 0), isDisplayed())).perform(longClick());
        onView(withId(R.id.dialog_reply_handler)).check(matches(isDisplayed()));
        onView(withText("Reply")).perform(click());
        onView(withId(R.id.ll_chat_input_area_quote)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }


}
