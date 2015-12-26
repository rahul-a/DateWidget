package com.sample.datewidget;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import org.joda.time.DateTime;

import controllers.DatePickerController;
import timber.log.Timber;
import views.DateView;
import views.Day;
import views.month.MonthDateView;

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
        public Day[] getSelectableDays() {
            return null;
        }

        /**
         * @return The list of dates, as Calendar Objects, which should be highlighted. null is no dates should be highlighted
         */
        @Override
        public Day[] getHighlightedDays() {
            return new Day[] { new Day(2, 2, 2015), new Day(3, 2, 2015), new Day(4, 2, 2015), new Day(5, 2, 2015), new Day(6, 2, 2015)};
        }

        /**
         * @return true if the specified year/month/day are within the selectable days or the range set by minDate and maxDate.
         * If one or either have not been set, they are considered as Integer.MIN_VALUE and
         * Integer.MAX_VALUE.
         */
        @Override
        public boolean isOutOfRange(Day day) {
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
        public void onDayOfMonthSelected(View view, Day day) {
            Timber.v("controller:: Day tapped: %s", day);
            mCheckoutDay = day;
            mSelectedDay = day;
            TextView daySelectedText = (TextView) view.findViewById(R.id.date_selected_text);
            if (daySelectedText != null) {
                daySelectedText.setText(mSelectedDay.getMonthName());
            }


            Spinner monthSpinner = (Spinner) view.findViewById(R.id.month_spinner);
            if (monthSpinner != null) {
                //setSpinnerSelectionWithoutCallingListener(monthSpinner, mSelectedDay.getMonth() - 1);
                monthSpinner.setSelection(mSelectedDay.getMonth()-1);
            }


            // BottomSheetLayout bottomSheet = (BottomSheetLayout) findViewById(R.id.bottomsheet);
            // bottomSheet.showWithSheetView(LayoutInflater.from(view.getContext()).inflate(R.layout.activity_main, bottomSheet, false));
        }

        @Override
        public void onCheckinDaySelected(View view, Day day) {
            mCheckinDay = day;
        }

        @Override
        public void onCheckoutDaySelected(View view, Day day) {
            mCheckoutDay = day;
        }


        @Override
        public Day getSelectedDay() {
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
        public boolean isSelectable(Day day) {
            if (selectableDays != null) {
                for (Day tempDay : selectableDays) {
                    if (tempDay.equals(day)) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public Day getToday() {
            return new Day(new DateTime());
        }

        @Override
        public Day getStartDate() {
            return new Day(new DateTime(2015, 2, 14, 0, 0, 0));
        }

        @Override
        public Day getCheckinDay() {
            return mCheckinDay;
        }

        @Override
        public Day getCheckoutDay() {
            return mCheckoutDay;
        }

        /**
         * Sets a Spinner selection without firing its listener
         *
         * @param spinner The spinner whose selection needs to be changed
         * @param selection The item that needs to be selected
         */
        private void setSpinnerSelectionWithoutCallingListener(final Spinner spinner, final int selection) {
            final AdapterView.OnItemSelectedListener l = spinner.getOnItemSelectedListener();
            spinner.setOnItemSelectedListener(null);
            spinner.post(new Runnable() {
                @Override
                public void run() {
                    spinner.setSelection(selection);
                    spinner.post(new Runnable() {
                        @Override
                        public void run() {
                            spinner.setOnItemSelectedListener(l);
                        }
                    });
                }
            });
        }

    };

    private static final int DEFAULT_START_YEAR = 1900;
    private static final int DEFAULT_END_YEAR = 2100;

    private Day mSelectedDay;
    private Day mCheckoutDay;
    private Day mCheckinDay;

    private int mMinYear = DEFAULT_START_YEAR;
    private int mMaxYear = DEFAULT_END_YEAR;
    private String mTitle;
    private DateTime mMinDate;
    private DateTime mMaxDate;
    private Day[] highlightedDays;
    private Day[] selectableDays = null;
    private int mAccentColor = -1;

    public String monthName;
    public int currentWeek;
    public int currentYear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_main);

        DateView dateView = (DateView) findViewById(R.id.date_view);
        if (dateView != null) {
            dateView.setDateController(mDatePickerController);
            dateView.setViewMode(DateView.WeekAdapter.MODE_YEAR);
            dateView.addOnWeekChangedListener(new DateView.OnWeekChangedListener() {
                @Override
                public void onWeekChanged(int currentWeekOfWeekYear, int currentY) {
                    DateTime dateTime = new DateTime();
                    monthName = dateTime.withWeekOfWeekyear(currentWeekOfWeekYear).withYear(currentY).monthOfYear().getAsText();
                    currentWeek = currentWeekOfWeekYear;
                    currentYear = currentY;
                }
            });
        }

        MonthDateView monthView = (MonthDateView) findViewById(R.id.month_view);
        monthView.setDateController(mDatePickerController);
    }

    public int getCurrentWeek() {
        return currentWeek;
    }

    public int getCurrentYear() {
        return currentYear;
    }

    public String getMonthName() {
        return monthName;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private boolean isAfterMax(Day day) {
        if (day == null || mMaxDate == null) {
            return false;
        }
        return day.toDateTime().withTimeAtStartOfDay().isAfter(mMaxDate);
    }

    private boolean isBeforeMin(Day day) {
        if (day == null || mMinDate == null) {
            return false;
        }
        return day.toDateTime().withTimeAtStartOfDay().isBefore(mMinDate);
    }

    public Day getSelectedDay() {
        return mCheckoutDay;
    }
}
