package com.sample.datewidget.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.sample.datewidget.R;
import com.sample.datewidget.fragments.DatePickerFragment;

import org.joda.time.DateTime;

import datewidget.adapters.WeekAdapter;
import datewidget.controllers.DatePickerController;
import datewidget.utils.Utils;
import datewidget.views.SimpleWeekView;
import datewidget.views.WeekView;
import timber.log.Timber;

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
        public WeekView.Day[] getSelectableDays() {
            return null;
        }

        /**
         * @return The list of dates, as Calendar Objects, which should be highlighted. null is no dates should be highlighted
         */
        @Override
        public WeekView.Day[] getHighlightedDays() {
            return null;
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
        public void onDayOfMonthSelected(View view, WeekView.Day day) {
            Timber.v("controller:: Day tapped: %s", day);
            mSelectedDay = day;
            TextView daySelectedText = (TextView) findViewById(R.id.dummy_text);
            if (daySelectedText != null) {
                daySelectedText.setText(String.format("%s %s, %s", mSelectedDay.getMonthName(), mSelectedDay.getDate(), mSelectedDay.getYear()));
            }
        }


        @Override
        public WeekView.Day getSelectedDay() {
            return mSelectedDay;
        }

        @Override
        public int getMinYear() {
            if (selectableDays != null) {
                return selectableDays[0].getYear();
            }
            // Ensure no years can be selected outside of the given minimum date
            return mMinDate != null && mMinDate.getYear() > mMinYear ? mMinDate.getYear() : mMinYear;
        }

        @Override
        public int getMaxYear() {
            if (selectableDays != null) {
                return selectableDays[selectableDays.length - 1].getYear();
            }
            // Ensure no years can be selected outside of the given maximum date
            return mMaxDate != null && mMaxDate.getYear() < mMaxYear ? mMaxDate.getYear() : mMaxYear;
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

        @Override
        public WeekView.Day getToday() {
            return new WeekView.Day(new DateTime());
        }
    };

    private static final int DEFAULT_START_YEAR = 1900;
    private static final int DEFAULT_END_YEAR = 2100;

    private WeekView.Day mSelectedDay;

    private int mMinYear = DEFAULT_START_YEAR;
    private int mMaxYear = DEFAULT_END_YEAR;
    private String mTitle;
    private DateTime mMinDate;
    private DateTime mMaxDate;
    private WeekView.Day[] highlightedDays;
    private WeekView.Day[] selectableDays = null;
    private int mAccentColor = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_week);

        Timber.v("Selected Day --> " + (mSelectedDay == null ? null : mSelectedDay.getDate()));

        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.date_frame);
        if (frameLayout != null) {
            DateTime dateTime = new DateTime();
            WeekView weekView = new SimpleWeekView(this, null, mDatePickerController);
            mSelectedDay = mDatePickerController.getSelectedDay();
            mAccentColor = Utils.getAccentColorFromThemeIfAvailable(this);
            frameLayout.addView(weekView);
            weekView.setStartDate(dateTime);
        }

        DateRecycler recyclerView = (DateRecycler) findViewById(R.id.recycler_view);
        if (recyclerView != null) {
            WeekAdapter weekAdapter = new WeekAdapter(mDatePickerController);
            recyclerView.setHasFixedSize(true);
            recyclerView.setAdapter(weekAdapter);
            recyclerView.scrollToPresent();
            recyclerView.addItemDecoration(new WeekDayLabelDecoration(this, RecyclerView.VERTICAL));
            recyclerView.setOnPageChangedListener(new RecyclerViewUtils.OnPageChangedListener() {
                @Override
                public void onPageChanged(int currentPosition) {
                    Timber.v("Position showing: %s", currentPosition);
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private boolean isAfterMax(WeekView.Day day) {
        if (day == null || mMaxDate == null) {
            return false;
        }
        return day.toDateTime().withTimeAtStartOfDay().isAfter(mMaxDate);
    }

    private boolean isBeforeMin(WeekView.Day day) {
        if (day == null || mMinDate == null) {
            return false;
        }
        return day.toDateTime().withTimeAtStartOfDay().isBefore(mMinDate);
    }
}
