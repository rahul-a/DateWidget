package com.sample.datewidget.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.FrameLayout;

import com.sample.datewidget.R;
import com.sample.datewidget.fragments.DatePickerFragment;

import org.joda.time.DateTime;

import java.util.Calendar;
import java.util.HashMap;

import datewidget.adapters.WeekAdapter;
import datewidget.controllers.DatePickerController;
import datewidget.utils.Utils;
import datewidget.views.SimpleWeekView;
import datewidget.views.WeekView;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements WeekView.OnDayClickListener {

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
        public WeekView.Day[] getHighlightedDays() {
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
        public boolean isOutOfRange(WeekView.Day day) {
            if (selectableDays != null) {
                return !isSelectable(day);
            }

            if (isBeforeMin(day)) {
                return true;
            } else if (isAfterMax(day)) {
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
            // adjustDayInMonthIfNeeded(mCalendar);
        }

        @Override
        public void onDayOfMonthSelected(WeekView.Day day) {
            mSelectedDay = day;
        }


        @Override
        public WeekView.Day getSelectedDay() {
            return new WeekView.Day(new DateTime());
        }

        @Override
        public int getMinYear() {
            if (selectableDays != null) {
                return selectableDays[0].getYear();
            }
            // Ensure no years can be selected outside of the given minimum date
            return mMinDate != null && mMinDate.get(Calendar.YEAR) > mMinYear ? mMinDate.get(Calendar.YEAR) : mMinYear;
        }

        @Override
        public int getMaxYear() {
            if (selectableDays != null) {
                return selectableDays[selectableDays.length - 1].getYear();
            }
            // Ensure no years can be selected outside of the given maximum date
            return mMaxDate != null && mMaxDate.get(Calendar.YEAR) < mMaxYear ? mMaxDate.get(Calendar.YEAR) : mMaxYear;
        }

        @Override
        public boolean isSelectable(WeekView.Day day) {
            if (selectableDays != null) {
                for (WeekView.Day tempDay : selectableDays) {
                    if (tempDay.equals(day)) {
                        return true;
                    }
                }
            }
            return false;
        }
    };

    private static final int DEFAULT_START_YEAR = 1900;
    private static final int DEFAULT_END_YEAR = 2100;

    protected static final int MONTHS_IN_YEAR = 12;

    private WeekView.Day mSelectedDay;
    private final Calendar mCalendar = Calendar.getInstance();
    /**
     * Defines the first day of the week to be shown in labels, if {@link Calendar#getFirstDayOfWeek()} is used
     * then the First day becomes locale specific in labels
     *
     * Example SUNDAY for en_US, MONDAY for en_GB
     */
    private int mWeekStart = mCalendar.MONDAY;
    private int mMinYear = DEFAULT_START_YEAR;
    private int mMaxYear = DEFAULT_END_YEAR;
    private String mTitle;
    private Calendar mMinDate;
    private Calendar mMaxDate;
    private WeekView.Day[] highlightedDays;
    private WeekView.Day[] selectableDays = null; //new WeekView.Day[] {new WeekView.Day(4, 11, 2015), new WeekView.Day(6, 11, 2015)};
    private boolean mThemeDark = false;
    private int mAccentColor = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_week);
        mAccentColor = Utils.getAccentColorFromThemeIfAvailable(this);
        WeekView weekView = new SimpleWeekView(this, null, mDatePickerController);
        weekView.setOnDayClickListener(this);
        WeekView.Day calendarDay = new WeekView.Day();

        final int position = (calendarDay.getYear() - mDatePickerController.getMinYear())
                * MONTHS_IN_YEAR + calendarDay.getMonth();

        final int month = (position % MONTHS_IN_YEAR) != 0 ? (position % MONTHS_IN_YEAR) : 12;
        final int year = position / MONTHS_IN_YEAR + mDatePickerController.getMinYear();

        mSelectedDay = mDatePickerController.getSelectedDay();

        Timber.v("Selected Day --> " + mSelectedDay.getDate());

        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.date_frame);
        if (frameLayout != null) {
            frameLayout.addView(weekView);

            DateTime dateTime = new DateTime();

            HashMap<String, Integer> drawingParams = new HashMap<>();

            if (mDatePickerController.isSelectable(mSelectedDay)) {
                drawingParams.put(WeekView.VIEW_PARAMS_SELECTED_DAY, mSelectedDay.getDate());
            }
            drawingParams.put(WeekView.VIEW_PARAMS_YEAR, year);
            drawingParams.put(WeekView.VIEW_PARAMS_MONTH, month);
            drawingParams.put(WeekView.VIEW_PARAMS_DATE, dateTime.getDayOfMonth());
            drawingParams.put(WeekView.VIEW_PARAMS_WEEK_START, mDatePickerController.getFirstDayOfWeek());

            weekView.setMonthParams(drawingParams);
            weekView.invalidate();
        }

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        if (recyclerView != null) {
            WeekAdapter weekAdapter = new WeekAdapter(mDatePickerController);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            recyclerView.setHasFixedSize(true);
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(weekAdapter);
        }
    }

    private boolean isAfterMax(WeekView.Day day) {
        if (mMaxDate == null) {
            return false;
        }

        int year = day.getYear();
        int month = day.getMonth();
        int date = day.getDate();

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

        if (date > mMaxDate.get(Calendar.DAY_OF_MONTH)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isSelectedDayInMonth(int year, int month) {
        return mSelectedDay.getYear() == year && mSelectedDay.getMonth() == month;
    }

    private boolean isSelectedDayInWeek(int year, int month) {
        return mSelectedDay.getYear() == year && mSelectedDay.getMonth() == month;
    }

    private boolean isBeforeMin(WeekView.Day day) {
        if (mMinDate == null) {
            return false;
        }
        int year = day.getYear();
        int month = day.getMonth();
        int date = day.getDate();

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

        if (date < mMinDate.get(Calendar.DAY_OF_MONTH)) {
            return true;
        } else {
            return false;
        }
    }

    /*// If the newly selected month / year does not contain the currently selected day number,
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
    }*/

    /*private boolean isAfterMax(Calendar calendar) {
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
    }*/

    @Override
    public void onDayClick(WeekView view, WeekView.Day day) {
        if (day != null) {
            Timber.v("Day tapped: %s", day);
            mDatePickerController.onDayOfMonthSelected(day);
        }
    }
}
