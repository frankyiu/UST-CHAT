package com.example.ustchat;


import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.TextView;

import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
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
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerMatchers.isClosed;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class CourseActivityTest {

    @Rule
    public ActivityTestRule<CourseActivity> mActivityTestRule = new ActivityTestRule<>(CourseActivity.class);

    @Before
    public void setUp() throws Exception {
        Intents.init();
    }

    @After
    public void tearDown() throws Exception {
        Intents.release();
    }

    @Test
    public void courseActivityTest() {
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar)))).check(matches(withText("General Chatroom")));
    }

    @Test
    public void courseActivityTestCreateChatroom() {
        onView(withId(R.id.create_chatroom)).check(matches(isDisplayed()));
        onView(withId(R.id.create_chatroom)).perform(click());
        onView(withId(R.id.dialog_create_chatroom)).check(matches(isDisplayed()));
        onView(withId(R.id.tv_create_chatroom)).check(matches(withText("Create a chatroom")));
        onView(withId(R.id.vf_create_chatroom_chip_general)).check(matches(isDisplayed()));

        onView(withId(R.id.ib_create_chatroom_submit)).perform(click());
        onView(withId(R.id.tv_create_chatroom_warning_invalid_title)).check(matches(withText("title can't be empty.")));
        onView(withId(R.id.tv_create_chatroom_warning_invalid_name)).check(matches(withText("name can't be empty.")));

        onView(withId(R.id.et_create_chatroom_title)).perform(typeText("hi there"), closeSoftKeyboard());
        onView(withId(R.id.et_create_chatroom_name)).perform(replaceText(""), closeSoftKeyboard());
        onView(withId(R.id.ib_create_chatroom_submit)).perform(click());
        onView(withId(R.id.tv_create_chatroom_warning_invalid_title)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(R.id.tv_create_chatroom_warning_invalid_name)).check(matches(withText("name can't be empty.")));

    }

    @Test
    public void courseActivityTestSelectChatroom() {
        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT))).perform(DrawerActions.open());
        onView(withId(R.id.side_menu)).check(matches(isDisplayed()));
        onView(withId(R.id.my_bookmarks)).perform(click());
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar)))).check(matches(withText("My Bookmarks")));
        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT))).perform(DrawerActions.open());
        onView(withId(R.id.general_chatroom)).perform(click());
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar)))).check(matches(withText("General Chatroom")));
    }

    @Test
    public void courseActivityTestSelectChatroom2() {
        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT))).perform(DrawerActions.open());
        onView(withId(R.id.side_menu)).check(matches(isDisplayed()));
        onView(withText("Engineering School")).perform(click());
        onView(withText("COMP")).perform(click());
        onView(withText("COMP4521")).perform(ViewActions.scrollTo()).perform(click());
        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT)));
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar)))).check(matches(withText("COMP4521")));
    }

    @Test
    public void courseActivityTestSelectChatroom3() {
        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT))).perform(DrawerActions.open());
        onView(withId(R.id.side_menu)).check(matches(isDisplayed()));
        onView(withId(R.id.et_course_list_search)).perform(typeText("comp4521"));
        onView(withText("COMP4521")).inRoot(RootMatchers.isPlatformPopup()).perform(click());
        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT)));
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar)))).check(matches(withText("COMP4521")));
    }

    @Test
    public void settingActivityTestNavToPMActivity() {
        onView(withId(R.id.private_message)).perform(click());
        intended(hasComponent(PrivateMessageActivity.class.getName()));
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
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(allOf(withId(R.id.cv_chatroom), childAtPosition(childAtPosition(withId(R.id.recycler_view_chatroom), 0), 0), isDisplayed())).perform(click());
        intended(hasComponent(ChatroomChatActivity.class.getName()));
        onView(Matchers.allOf(withContentDescription("Navigate up"), isDisplayed())).perform(click());
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar)))).check(matches(withText("General Chatroom")));
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
