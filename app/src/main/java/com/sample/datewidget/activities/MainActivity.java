package com.sample.datewidget.activities;

import android.graphics.Canvas;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.FrameLayout;

import com.sample.datewidget.R;
import com.sample.datewidget.fragments.DatePickerFragment;

import java.util.Calendar;
import java.util.HashMap;

import datewidget.adapters.MonthAdapter;
import datewidget.controllers.DatePickerController;
import datewidget.utils.Utils;
import datewidget.views.SimpleWeekView;
import datewidget.views.WeekView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    DatePickerController mDatePickerController = new DatePickerController() {

        @Override
        public void registerOnDateChangedListener(DatePickerFragment.OnDateChangedListener listener) {

        }

        @Override
        public void unregisterOnDateChangedListener(DatePickerFragment.OnDateChangedListener listener) {

        }

        @Override
        public boolean isThemeDark() {
            return false;
        }

        /**
         * Get the accent color of this dialog
         * @return accent color
         */
        @Override
        public int getAccentColor() {
            return mAccentColor;
        }

        @Override
        public Calendar[] getSelectableDays() {
            return new Calendar[0];
        }

        /**
         * @return The list of dates, as Calendar Objects, which should be highlighted. null is no dates should be highlighted
         */
        @Override
        public Calendar[] getHighlightedDays() {
            return highlightedDays;
        }

        @Override
        public int getFirstDayOfWeek() {
            return mWeekStart;
        }

        /**
         * @return true if the specified year/month/day are within the selectable days or the range set by minDate and maxDate.
         * If one or either have not been set, they are considered as Integer.MIN_VALUE and
         * Integer.MAX_VALUE.
         */
        @Override
        public boolean isOutOfRange(int year, int month, int day) {
            if (selectableDays != null) {
                return !isSelectable(year, month, day);
            }

            if (isBeforeMin(year, month, day)) {
                return true;
            }
            else if (isAfterMax(year, month, day)) {
                return true;
            }

            return false;
        }

        @Override
        public void tryVibrate() {

        }

        @Override
        public void onYearSelected(int year) {
            mCalendar.set(Calendar.YEAR, year);
            adjustDayInMonthIfNeeded(mCalendar);
        }

        @Override
        public void onDayOfMonthSelected(int year, int month, int day) {
            mCalendar.set(Calendar.YEAR, year);
            mCalendar.set(Calendar.MONTH, month);
            mCalendar.set(Calendar.DAY_OF_MONTH, day);
        }


        @Override
        public MonthAdapter.CalendarDay getSelectedDay() {
            return new MonthAdapter.CalendarDay(mCalendar);
        }

        @Override
        public int getMinYear() {
            if(selectableDays != null) return selectableDays[0].get(Calendar.YEAR);
            // Ensure no years can be selected outside of the given minimum date
            return mMinDate != null && mMinDate.get(Calendar.YEAR) > mMinYear ? mMinDate.get(Calendar.YEAR) : mMinYear;
        }

        @Override
        public int getMaxYear() {
            if(selectableDays != null) return selectableDays[selectableDays.length-1].get(Calendar.YEAR);
            // Ensure no years can be selected outside of the given maximum date
            return mMaxDate != null && mMaxDate.get(Calendar.YEAR) < mMaxYear ? mMaxDate.get(Calendar.YEAR) : mMaxYear;
        }
    };

    private static final int DEFAULT_START_YEAR = 1900;
    private static final int DEFAULT_END_YEAR = 2100;

    protected static int WEEK_7_OVERHANG_HEIGHT = 7;
    protected static final int MONTHS_IN_YEAR = 12;

    private MonthAdapter.CalendarDay mSelectedDay;
    private final Calendar mCalendar = Calendar.getInstance();
    private int mWeekStart = mCalendar.getFirstDayOfWeek();
    private int mMinYear = DEFAULT_START_YEAR;
    private int mMaxYear = DEFAULT_END_YEAR;
    private String mTitle;
    private Calendar mMinDate;
    private Calendar mMaxDate;
    private Calendar[] highlightedDays;
    private Calendar[] selectableDays;
    private boolean mThemeDark = false;
    private int mAccentColor = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAccentColor = Utils.getAccentColorFromThemeIfAvailable(this);
        WeekView weekView = new SimpleWeekView(this, null, mDatePickerController);
        MonthAdapter.CalendarDay calendarDay = new MonthAdapter.CalendarDay();

        final int position = (calendarDay.getYear() - mDatePickerController.getMinYear())
                * MONTHS_IN_YEAR + calendarDay.getMonth();

        final int month = position % MONTHS_IN_YEAR;
        final int year = position / MONTHS_IN_YEAR + mDatePickerController.getMinYear();

        Log.v(TAG, String.format("Year: %s, minYear: %s", year, mDatePickerController.getMinYear()));

        int selectedDay = -1;
        mSelectedDay = mDatePickerController.getSelectedDay();

        if (isSelectedDayInMonth(year, month)) {
            selectedDay = mSelectedDay.getDay();
            Log.v(TAG, "Selected Day --> " + selectedDay);
        }


        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.date_frame);
        frameLayout.addView(weekView);

        HashMap<String, Integer> drawingParams = new HashMap<>();
        drawingParams.put(WeekView.VIEW_PARAMS_SELECTED_DAY, selectedDay);
        drawingParams.put(WeekView.VIEW_PARAMS_YEAR, year);
        drawingParams.put(WeekView.VIEW_PARAMS_MONTH, month);
        drawingParams.put(WeekView.VIEW_PARAMS_WEEK_START, mDatePickerController.getFirstDayOfWeek());
        weekView.setMonthParams(drawingParams);
        weekView.invalidate();
    }

    private boolean isAfterMax(int year, int month, int day) {
        if (mMaxDate == null) {
            return false;
        }

        if (year > mMaxDate.get(Calendar.YEAR)) {
            return true;
        } else if (year < mMaxDate.get(Calendar.YEAR)) {
            return false;
        }

        if (month > mMaxDate.get(Calendar.MONTH)) {
            return true;
        } else if (month < mMaxDate.get(Calendar.MONTH)) {
            return false;
        }

        if (day > mMaxDate.get(Calendar.DAY_OF_MONTH)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isSelectedDayInMonth(int year, int month) {
        return mSelectedDay.getYear() == year && mSelectedDay.getMonth() == month;
    }

    private boolean isBeforeMin(int year, int month, int day) {
        if (mMinDate == null) {
            return false;
        }

        if (year < mMinDate.get(Calendar.YEAR)) {
            return true;
        } else if (year > mMinDate.get(Calendar.YEAR)) {
            return false;
        }

        if (month < mMinDate.get(Calendar.MONTH)) {
            return true;
        } else if (month > mMinDate.get(Calendar.MONTH)) {
            return false;
        }

        if (day < mMinDate.get(Calendar.DAY_OF_MONTH)) {
            return true;
        } else {
            return false;
        }
    }

    // If the newly selected month / year does not contain the currently selected day number,
    // change the selected day number to the last day of the selected month or year.
    //      e.g. Switching from Mar to Apr when Mar 31 is selected -> Apr 30
    //      e.g. Switching from 2012 to 2013 when Feb 29, 2012 is selected -> Feb 28, 2013
    private void adjustDayInMonthIfNeeded(Calendar calendar) {
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        if (day > daysInMonth) {
            calendar.set(Calendar.DAY_OF_MONTH, daysInMonth);
        }
        setToNearestDate(calendar);
    }

    private void setToNearestDate(Calendar calendar) {
        if(selectableDays != null) {
            int distance = Integer.MAX_VALUE;
            for (Calendar c : selectableDays) {
                int newDistance = Math.abs(calendar.compareTo(c));
                if(newDistance < distance) distance = newDistance;
                else {
                    calendar.setTimeInMillis(c.getTimeInMillis());
                    break;
                }
            }
            return;
        }

        if(isBeforeMin(calendar)) {
            calendar.setTimeInMillis(mMinDate.getTimeInMillis());
            return;
        }

        if(isAfterMax(calendar)) {
            calendar.setTimeInMillis(mMaxDate.getTimeInMillis());
            return;
        }
    }

    private boolean isAfterMax(Calendar calendar) {
        return isAfterMax(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
    }

    private boolean isBeforeMin(Calendar calendar) {
        return isBeforeMin(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
    }

    private boolean isSelectable(int year, int month, int day) {
        for (Calendar c : selectableDays) {
            if(year < c.get(Calendar.YEAR)) break;
            if(year > c.get(Calendar.YEAR)) continue;
            if(month < c.get(Calendar.MONTH)) break;
            if(month > c.get(Calendar.MONTH)) continue;
            if(day < c.get(Calendar.DAY_OF_MONTH)) break;
            if(day > c.get(Calendar.DAY_OF_MONTH)) continue;
            return true;
        }
        return false;
    }
}
