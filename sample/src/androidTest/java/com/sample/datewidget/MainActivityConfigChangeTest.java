package com.sample.datewidget;

import android.support.test.rule.ActivityTestRule;

import com.sample.datewidget.viewActions.OrientationChangeAction;

import junit.framework.Assert;

import org.junit.Rule;
import org.junit.Test;

import views.Day;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by priyabratapatnaik on 15/11/15.
 */
public class MainActivityConfigChangeTest {
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void clickItem() {
        MainActivity mainActivity = mActivityRule.getActivity();

        Day selectedDay = mainActivity.getSelectedDay();

        onView(isRoot()).perform(OrientationChangeAction.orientationLandscape());
        checkDay(mainActivity, selectedDay);

        onView(isRoot()).perform(OrientationChangeAction.orientationPortrait());
        checkDay(mainActivity, selectedDay);
    }

    private void checkDay(MainActivity mainActivity, Day selectedDay) {
        Assert.assertEquals("Selected day has changed", selectedDay, mainActivity.getSelectedDay());

        onView(withId(R.id.date_selected_text))
                .check(matches(withText(selectedDay.toFormattedString())))
                .check(matches(isDisplayed()));
    }
}