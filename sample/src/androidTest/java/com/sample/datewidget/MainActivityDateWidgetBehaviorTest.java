package com.sample.datewidget;

import android.content.res.Resources;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.action.CoordinatesProvider;
import android.support.test.espresso.action.GeneralClickAction;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Tap;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import junit.framework.Assert;

import org.joda.time.DateTime;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import views.Day;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by priyabratapatnaik on 15/11/15.
 */
@RunWith(AndroidJUnit4.class)
public class MainActivityDateWidgetBehaviorTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void clickItem() {
        MainActivity mainActivity = mActivityRule.getActivity();
        final RecyclerView view = (RecyclerView) mainActivity.findViewById(R.id.date_recycler_view);
        int count = view.getAdapter().getItemCount();
        int viewWidth = view.getMeasuredWidth();
        int[] xPos = new int[7];
        int dayWidthHalf = viewWidth / 14;

        Resources res = mainActivity.getResources();
        int weekHeaderSize = res.getDimensionPixelOffset(R.dimen.month_list_item_header_height);
        int dayLabelTextSize = res.getDimensionPixelSize(R.dimen.date_number_size);
        int y = weekHeaderSize - (dayLabelTextSize) / 2;

        Day selectedDay;
        for (int j = 0; j < count; j++) {
            onView(withId(R.id.date_recycler_view)).perform(RecyclerViewActions.scrollToPosition(j));

            for (int i = 0; i < 7; i++) {
                xPos[i] = (2 * i + 1) * dayWidthHalf;
                onView(withId(R.id.date_recycler_view))
                        .perform(RecyclerViewActions.actionOnItemAtPosition(j, clickXY(xPos[i], y)));

                selectedDay = mainActivity.getSelectedDay();

                DateTime dateTime = new DateTime(selectedDay.getYear(), selectedDay.getMonth(), selectedDay.getDate(), 0, 0, 0);
                String dayOfWeek = dateTime.dayOfWeek().getAsShortText();

                Assert.assertEquals("Day of week is not same", dayOfWeek, selectedDay.getDay());

                onView(withId(R.id.date_selected_text))
                        .check(matches(withText(selectedDay.getMonthName())))
                        .check(matches(isDisplayed()));
            }
        }
    }

    public static ViewAction clickXY(final int x, final int y){
        return new GeneralClickAction(
                Tap.SINGLE,
                new CoordinatesProvider() {
                    @Override
                    public float[] calculateCoordinates(View view) {

                        final int[] screenPos = new int[2];
                        view.getLocationOnScreen(screenPos);

                        final float screenX = screenPos[0] + x;
                        final float screenY = screenPos[1] + y;
                        float[] coordinates = {screenX, screenY};

                        return coordinates;
                    }
                },
                Press.FINGER);
    }
}