package com.sample.datewidget;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.flipboard.bottomsheet.BottomSheetLayout;

import org.joda.time.DateTime;

import datewidget.controllers.DatePickerController;
import datewidget.views.DateView;
import datewidget.views.WeekView;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    DatePickerController mDatePickerController = new DatePickerController() {

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
            TextView daySelectedText = (TextView) findViewById(R.id.date_selected_text);
            if (daySelectedText != null) {
                daySelectedText.setText(day.toFormattedString());
            }

            BottomSheetLayout bottomSheet = (BottomSheetLayout) findViewById(R.id.bottomsheet);
            // bottomSheet.showWithSheetView(LayoutInflater.from(view.getContext()).inflate(R.layout.activity_main, bottomSheet, false));
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

        @Override
        public WeekView.Day getStartDate() {
            return new WeekView.Day(new DateTime(2015, 2, 14, 0, 0, 0));
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

        DateView dateView = (DateView) findViewById(R.id.date_view);
        if (dateView != null) {
            dateView.setDateController(mDatePickerController);
            dateView.setOnWeekChangedListener(new DateView.OnWeekChangedListener() {
                @Override
                public void onWeekChanged(int currentWeekOfWeekYear) {
                    Timber.v("Position showing: %s", currentWeekOfWeekYear);
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

    public WeekView.Day getSelectedDay() {
        return mSelectedDay;
    }
}
