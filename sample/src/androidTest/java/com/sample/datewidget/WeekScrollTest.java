package com.sample.datewidget;

import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.RecyclerView;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by aritraroy on 11/27/15.
 */

@RunWith(AndroidJUnit4.class)
public class WeekScrollTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void testSelectedMonthName(){

        MainActivity mainActivity = mActivityRule.getActivity();
        RecyclerView recyclerView = (RecyclerView) mainActivity.findViewById(R.id.date_recycler_view);
        int totalCount = recyclerView.getAdapter().getItemCount();

        onView(withId(R.id.date_recycler_view)).perform(RecyclerViewActions.scrollToPosition(0));

        for(int i = 0; i<totalCount; i++) {

            onView(withId(R.id.date_arrow_right)).perform(click());
            String monthName = mActivityRule.getActivity().getMonthName();
            onView(withId(R.id.date_selected_text)).check(matches(withText(monthName)));

            /*
            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(2));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/

            onView(withId(R.id.date_arrow_right)).check(matches(isClickable()));


        }

    }


}
