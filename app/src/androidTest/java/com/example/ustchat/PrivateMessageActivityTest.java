package com.example.ustchat;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.TextView;

import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.intent.Intents;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
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
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class PrivateMessageActivityTest {

    @Rule
    public ActivityTestRule<PrivateMessageActivity> mActivityTestRule = new ActivityTestRule<>(PrivateMessageActivity.class);

    @Before
    public void setUp() throws Exception {
        Intents.init();
    }

    @After
    public void tearDown() throws Exception {
        Intents.release();
    }

    @Test
    public void privateMessageActivityTest() {
        onView(withId(R.id.tv_toolbar_center_title)).check(matches(withText("Private Message")));
    }

    @Test
    public void privateMessageActivityTestNavToCourseActivity() {
        onView(withId(R.id.chatroom)).perform(click());
        intended(hasComponent(CourseActivity.class.getName()));
    }

    @Test
    public void privateMessageActivityTestNavToSettingActivity() {
        onView(withId(R.id.setting)).perform(click());
        intended(hasComponent(SettingActivity.class.getName()));
    }

    @Test
    public void courseActivityBackButton() {
        try {
            Thread.sleep(1000);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            onView(allOf(withId(R.id.cv_pm), childAtPosition(childAtPosition(withId(R.id.recycler_view_pm), 0), 0), isDisplayed())).perform(click());
            intended(hasComponent(PrivateMessageChatActivity.class.getName()));
            onView(Matchers.allOf(withContentDescription("Navigate up"), isDisplayed())).perform(click());
            onView(withId(R.id.tv_toolbar_center_title)).check(matches(withText("Private Message")));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
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
