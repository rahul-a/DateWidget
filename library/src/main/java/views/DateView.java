package views;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.library.R;

import org.joda.time.DateTime;

import controllers.DatePickerController;
import timber.log.Timber;

/**
 * Created by priyabratapatnaik on 12/11/15.
 */
public class DateView extends RelativeLayout implements View.OnClickListener {

    public interface OnWeekChangedListener {
        void onWeekChanged(int currentWeekOfWeekYear, int currentYear);
    }

    public DateView(Context context) {
        super(context);
        init(context, null);
    }

    public DateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public DateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private DateRecycler dateRecycler;
    private TextView dateSelected;
    private DatePickerController mDatePickerController;
    private ImageView leftArrow, rightArrow;
    private Spinner monthSpinner, yearSpinner;

    private boolean flag = false;

    private OnWeekChangedListener mOnWeekChangedListener = new OnWeekChangedListener() {
        @Override
        public void onWeekChanged(int currentWeekOfWeekYear, int currentYear) {

            DateTime dateTime = new DateTime();
            String monthName = dateTime.withWeekOfWeekyear(currentWeekOfWeekYear).withYear(currentYear).monthOfYear().getAsText();
            WeekAdapter adapter = (WeekAdapter) dateRecycler.getAdapter();

            if (currentWeekOfWeekYear == adapter.getItemCount()) {
                rightArrow.setEnabled(false);
            } else if (currentWeekOfWeekYear == 0) {
                Timber.v("Left arrow disabled.");
                leftArrow.setEnabled(false);
            } else {
                leftArrow.setEnabled(true);
                rightArrow.setEnabled(true);
            }

            if (dateSelected != null) {
                dateSelected.setText(monthName);
            }

            // Getting position for the month
            String[] monthNames = getResources().getStringArray(R.array.month_array);
            int position = 0;
            for (int i = 0; i < monthNames.length; i++) {
                if (monthName.equalsIgnoreCase(monthNames[i])) {
                    position = i;
                    break;
                }
            }

            // setSpinnerSelectionWithoutCallingListener(monthSpinner, position);
        }
    };

    private AdapterView.OnItemSelectedListener monthSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            int currentDateOfMonth = dateRecycler.getSelectedDay().getDate();
            int currentYear = dateRecycler.getSelectedDay().getYear();
            String currentMonthName = dateRecycler.getSelectedDay().getMonthName();

            DateTime dateTime = new DateTime();

            int checkRange = new DateTime().withMonthOfYear(position + 1).dayOfMonth().getMaximumValue();

            if (currentDateOfMonth > checkRange)
                currentDateOfMonth = Math.min(currentDateOfMonth, checkRange);

            DateTime temp = dateTime.withMonthOfYear(position + 1).withDayOfMonth(currentDateOfMonth).withYear(currentYear);
            Day dateToNavigate = new Day(temp);

            // Should not scroll for the first time
            if (flag) {

                if (dateToNavigate.getDate() > checkRange) {
                    dateToNavigate.setDate(Math.min(dateToNavigate.getDate(), checkRange));
                }

                scrollToDay(dateToNavigate);

            } else {
                flag = true;
            }

            if (dateSelected != null) {
                dateSelected.setText(currentMonthName);
            }

        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private DatePickerController mInternalDateController = new DatePickerController() {
        @Override
        public void onDayOfMonthSelected(View view, Day day) {
            TextView daySelectedText = (TextView) findViewById(R.id.date_selected_text);
            if (daySelectedText != null) {
                daySelectedText.setText(day.toFormattedString());
            }
            if (mDatePickerController != null) {
                mDatePickerController.onDayOfMonthSelected(view, day);
            }
            mSelectedDay = day;
        }

        @Override
        public void onCheckinDaySelected(View view, Day day) {
            if (mDatePickerController != null) {
                mDatePickerController.onCheckinDaySelected(view, day);
            }
        }

        @Override
        public void onCheckoutDaySelected(View view, Day day) {
            if (mDatePickerController != null) {
                mDatePickerController.onCheckoutDaySelected(view, day);
            }
        }

        @Override
        public Day getSelectedDay() {
            return mDatePickerController != null ? mDatePickerController.getSelectedDay() : mSelectedDay;
        }

        @Override
        public int getAccentColor() {
            return 0;
        }

        @Override
        public Day[] getHighlightedDays() {
            return new Day[0];
        }

        @Override
        public Day[] getSelectableDays() {
            return new Day[0];
        }

        @Override
        public int getMinYear() {
            return 0;
        }

        @Override
        public int getMaxYear() {
            return 0;
        }

        @Override
        public boolean isOutOfRange(Day day) {
            if (mDatePickerController != null) {
                return mDatePickerController.isOutOfRange(day);
            }
            return false;
        }

        @Override
        public void tryVibrate() {

        }

        @Override
        public boolean isSelectable(Day day) {
            return false;
        }

        @Override
        public Day getToday() {
            return mDatePickerController != null ? mDatePickerController.getStartDate() : mToday;
        }

        @Override
        public Day getStartDate() {
            return mDatePickerController != null ? mDatePickerController.getStartDate() : mToday;
        }

        @Override
        public Day getCheckinDay() {
            return null;
        }

        @Override
        public Day getCheckoutDay() {
            return null;
        }
    };

    private Day mToday = new Day();
    private Day mSelectedDay = mToday;

    public void setDateController(DatePickerController controller) {
        mDatePickerController = controller;
        WeekAdapter weekAdapter = new DateView.WeekAdapter(mInternalDateController);
        setAdapter(weekAdapter);
    }

    public void setViewMode(int mode) {
        RecyclerView.Adapter adapter = dateRecycler.getAdapter();
        if (adapter != null && adapter instanceof RecyclerView.Adapter) {
            WeekAdapter weekAdapter = (WeekAdapter) adapter;
            weekAdapter.setMode(mode);
            weekAdapter.notifyDataSetChanged();
            dateRecycler.scrollToPresent();
        } else {
            throw new IllegalStateException("Adapter should be set before setting view mode");
        }
    }

    private void init(Context context, AttributeSet attrs) {

        RelativeLayout.LayoutParams params;
        int padding = getResources().getDimensionPixelSize(R.dimen.std_padding);

        // Adding the monthSpinner View
        /*monthSpinner = new Spinner(context);
        monthSpinner.setId(R.id.month_spinner);
        params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, padding, 0);
        monthSpinner.setLayoutParams(params);

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(context, R.array.month_array, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(spinnerAdapter);
        addView(monthSpinner);*/

        // Adding the selected month TextView
        /*dateSelected = new TextView(context);
        params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.setMargins(padding, 0, padding, padding);
        params.addRule(RIGHT_OF, R.id.month_spinner);
        params.addRule(ALIGN_TOP, R.id.month_spinner);
        dateSelected.setLayoutParams(params);
        dateSelected.setPadding(0, 0, padding, padding);
        dateSelected.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimension(R.dimen.large_text_size));
        dateSelected.setTypeface(null, Typeface.BOLD);
        dateSelected.setTextColor(Utils.getAccentColorFromThemeIfAvailable(context));
        dateSelected.setId(R.id.date_selected_text);
        addView(dateSelected);*/

        // Adding the left arrow
        leftArrow = new ImageView(context);
        leftArrow.setId(R.id.date_arrow_left);
        leftArrow.setImageResource(R.drawable.arrow_left);
        leftArrow.setAdjustViewBounds(true);
        params = new RelativeLayout.LayoutParams(getResources().getDimensionPixelSize(R.dimen.arrow_width), getResources().getDimensionPixelSize(R.dimen.arrow_height));
        params.addRule(ALIGN_PARENT_LEFT);
        params.topMargin = getResources().getDimensionPixelSize(R.dimen.top_arrow_margin);
        params.leftMargin = getResources().getDimensionPixelSize(R.dimen.left_arrow_margin);
        leftArrow.setLayoutParams(params);
        leftArrow.setPadding(padding, padding, padding, padding);
        addView(leftArrow);

        // Adding the right arrow
        rightArrow = new ImageView(context);
        rightArrow.setId(R.id.date_arrow_right);
        rightArrow.setImageResource(R.drawable.arrow_right);
        rightArrow.setAdjustViewBounds(true);
        params = new RelativeLayout.LayoutParams(getResources().getDimensionPixelSize(R.dimen.arrow_width), getResources().getDimensionPixelSize(R.dimen.arrow_height));
        params.topMargin = getResources().getDimensionPixelSize(R.dimen.top_arrow_margin);
        params.rightMargin = getResources().getDimensionPixelSize(R.dimen.left_arrow_margin);
        params.addRule(ALIGN_PARENT_RIGHT);
        rightArrow.setLayoutParams(params);
        rightArrow.setPadding(padding, padding, padding, padding);
        addView(rightArrow);

        // Adding the WeekView
        dateRecycler = new DateRecycler(context, attrs);
        dateRecycler.setId(R.id.date_recycler_view);
        dateRecycler.setHorizontalScrollBarEnabled(false);
        dateRecycler.setHasFixedSize(true);
        params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.calendar_frame_height));
        params.addRule(RIGHT_OF, R.id.date_arrow_left);
        params.addRule(BELOW, R.id.date_selected_text);
        params.addRule(LEFT_OF, R.id.date_arrow_right);
        dateRecycler.setLayoutParams(params);
        dateRecycler.addItemDecoration(new DateRecycler.WeekDayLabelDecoration(context, RecyclerView.VERTICAL));
        addView(dateRecycler);

        // Setting all the necessary listeners
        leftArrow.setOnClickListener(this);
        rightArrow.setOnClickListener(this);
        addOnWeekChangedListener(mOnWeekChangedListener);
        // monthSpinner.setOnItemSelectedListener(monthSelectedListener);
        // monthSpinner.setSelection(new DateTime().getMonthOfYear() - 1);

    }

    /**
     * Sets the height and width for the ViewGroup, so any changes to its children's layout params must be done here
     *
     * @param w
     * @param h
     * @param oldW
     * @param oldH
     */

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);

        /*RelativeLayout.LayoutParams params = new LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        int padding = getResources().getDimensionPixelSize(R.dimen.std_padding);
        params.setMargins((w / 14) - (2 * padding), 0, padding, padding);
        params.addRule(CENTER_HORIZONTAL);
        dateSelected.setLayoutParams(params);*/

    }

    /**
     * Generates an array of years starting from the baseYear towards the past
     *
     * @param baseYear The starting year for the array
     * @param count    The number of years to be generated
     * @return Array of years
     */
    public Integer[] generateYearArray(int baseYear, int count) {
        Integer years[] = new Integer[count];

        int i = count, k = 0;

        while (i > 0) {
            years[k++] = baseYear--;
            i--;
        }

        return years;
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

    public void setAdapter(RecyclerView.Adapter adapter) {
        if (!(adapter instanceof WeekAdapter)) {
            throw new IllegalArgumentException("Adapter should extend WeekAdapter");
        }
        dateRecycler.setAdapter(adapter);
        dateRecycler.scrollToDay(mDatePickerController != null ? mDatePickerController.getToday() : mToday);
    }

    public void addOnWeekChangedListener(OnWeekChangedListener listener) {
        dateRecycler.addOnWeekChangedListener(listener);
    }

    public void scrollToPresent() {
        dateRecycler.scrollToPresent();
    }

    public void scrollToDay(Day day) {
        dateRecycler.scrollToDay(day);
    }

    public View getSelectedDateView() {
        return dateSelected;
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        return new SavedState(superState, dateRecycler.getSelectedDay());
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {

        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(((SavedState) state).getSuperState());
        Day selectedDay = savedState.mSelectedDay;
        if (selectedDay != null) {
            dateRecycler.scrollToDay(selectedDay);
            if (dateSelected != null) {
                dateSelected.setText(selectedDay.getMonthName());
            }
        } else {
            Timber.v("Scrolling to present");
            dateRecycler.scrollToPresent();
        }

    }

    @Override
    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        // As we save our own instance state, ensure our children don't save and restore their state as well.
        super.dispatchFreezeSelfOnly(container);
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        /** See comment in {@link #dispatchSaveInstanceState(android.util.SparseArray)}  */
        super.dispatchThawSelfOnly(container);
    }

    @Override
    public void onClick(View v) {

        int pos = ((LinearLayoutManager) dateRecycler.getLayoutManager()).findFirstVisibleItemPosition();

        if (v.getId() == R.id.date_arrow_left) {
            // Can't scroll to negative positions
            if (pos > 0) {
                dateRecycler.smoothScrollToPosition(pos - 1);
            }
        } else if (v.getId() == R.id.date_arrow_right) {
            dateRecycler.smoothScrollToPosition(pos + 1);
        }

    }

    protected static class SavedState extends BaseSavedState {
        Day mSelectedDay;

        public SavedState(Parcel source) {
            super(source);
            mSelectedDay = source.readParcelable(Day.class.getClassLoader());
        }

        public SavedState(Parcelable superState, Day selectedDay) {
            super(superState);
            mSelectedDay = selectedDay;
        }

        @Override
        public void writeToParcel(Parcel destination, int flags) {
            super.writeToParcel(destination, flags);
            destination.writeParcelable(mSelectedDay, flags);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Creator<SavedState>() {

            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }

        };
    }

    /**
     * Created by priyabratapatnaik on 03/11/15.
     */
    public static class WeekAdapter extends RecyclerView.Adapter<WeekAdapter.WeekViewHolder> {

        public static final int MODE_MONTH = 0;
        public static final int MODE_YEAR = 1;
        public static final int INVALID_POSITION = -1;

        private static final String TAG = WeekAdapter.class.getSimpleName();
        private DatePickerController mController;
        private DateTime mDateTime;
        private int mWeekCount;
        private int mOffset;
        private int firstWeek;
        private int mMode = MODE_YEAR;

        public WeekAdapter(DatePickerController controller) {
            mController = controller;
            mDateTime = mController == null ? new DateTime() : mController.getStartDate().toDateTime();
            int presentYear = mDateTime.getYear();
            mOffset = presentYear - mDateTime.getYear();
            Timber.v("start time: %s", mDateTime);
            adjustWeekCount();
        }

        public void setMode(int mode) {
            if (mode != MODE_MONTH && mode != MODE_YEAR) {
                Timber.e("Invalid mode");
                return;
            }
            if (mMode != mode) {
                mMode = mode;
                adjustWeekCount();
                notifyDataSetChanged();
            }
        }

        @Override
        public long getItemId(int position) {
            return super.getItemId(position);
        }

        private void adjustWeekCount() {
            if (mMode == MODE_MONTH) {
                int maxDay = mDateTime.dayOfMonth().withMaximumValue().getDayOfMonth();
                firstWeek = mDateTime.withDayOfMonth(1).getWeekOfWeekyear();
                int lastWeek = mDateTime.withDayOfMonth(maxDay).getWeekOfWeekyear();
                mWeekCount = lastWeek - firstWeek + 1;
                int start = mDateTime.withWeekOfWeekyear(lastWeek).dayOfWeek().withMinimumValue().getDayOfMonth();
                int end = mDateTime.withWeekOfWeekyear(lastWeek).dayOfWeek().withMaximumValue().getDayOfMonth();
                Timber.v("Week count: %s, first week: %s, last week: %s, Last weeks dates --> %s to %s", mWeekCount, firstWeek, lastWeek, start, end);
            } else {
                if (mOffset > 0) {
                    mWeekCount += mDateTime.weekOfWeekyear().withMaximumValue().getWeekOfWeekyear();
                    for (int i = mOffset; i > 0; i--) {
                        mWeekCount += mDateTime.plusYears(i).weekOfWeekyear().withMaximumValue().getWeekOfWeekyear();
                    }
                    mWeekCount++;
                } else {
                    mWeekCount = mDateTime.weekOfWeekyear().withMaximumValue().getWeekOfWeekyear();
                    mWeekCount++;
                }
            }
        }

        @Override
        public WeekViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.week_view_layout, parent, false);
            view.setLayoutParams(new RecyclerView.LayoutParams(parent.getMeasuredWidth(), RecyclerView.LayoutParams.MATCH_PARENT));
            return new WeekViewHolder(view);
        }

        @Override
        public void onBindViewHolder(WeekViewHolder holder, int position) {
            WeekView weekView = holder.getWeekView();
            weekView.setController(mController);
            weekView.setClickable(true);

            int actualYear = getYearForWeekPosition(position);
            int weekPos = getTranslatedWeekPosition(position, actualYear);
            Timber.v("Pos: %s", position);
                DateTime dateTime;
            if (position == 0) {
                dateTime = mDateTime.withYear(actualYear).withWeekyear(actualYear).withWeekOfWeekyear(53).withDayOfWeek(1);
            } else {
                dateTime = mDateTime.withYear(actualYear).withWeekyear(actualYear).withWeekOfWeekyear(weekPos).withDayOfWeek(1);
            }
            Timber.v("Pos %s, Year %s, week %s", position, mDateTime.getYear(), mDateTime.withYear(actualYear).withWeekyear(actualYear).withWeekOfWeekyear(weekPos).withDayOfWeek(1));
            weekView.setStartDate(dateTime);
        }

        public int getTranslatedWeekPosition(int position, int year) {
            int maxWeeks;
            int translatedPos = position;
            if (mMode == MODE_YEAR) {
                maxWeeks = mDateTime.withYear(year).weekOfWeekyear().withMaximumValue().getWeekOfWeekyear();
                Timber.v("max weeks: %s", maxWeeks);
                translatedPos = (position) % maxWeeks;
                if (translatedPos == 0) {
                    translatedPos += maxWeeks;
                }
            } else if (mMode == MODE_MONTH) {
                translatedPos += firstWeek;
            }
            Timber.v("Translated pos %s", translatedPos);
            return translatedPos;
        }

        public int getYearForWeekPosition(int position) {
            int startYear = mDateTime.getYear();
            int actualYear = startYear;
            if (mOffset > 0) {
                int maxWeeks = mDateTime.weekOfWeekyear().withMaximumValue().getWeekOfWeekyear();
                if (position <= maxWeeks - 1) {
                    return startYear;
                } else {
                    for (int i = 1; i <= mOffset; i++) {
                        maxWeeks += mDateTime.withYear(startYear + i).weekOfWeekyear().withMaximumValue().getWeekOfWeekyear();
                        actualYear = startYear + i;
                        if (position <= maxWeeks - 1) {
                            break;
                        }
                    }
                }
            } else {
                if (position == 0) {
                    return actualYear - 1;
                }
            }
            // Timber.v("Returning year %s", actualYear);
            return actualYear;
        }

        public int getWeekPositionInAdapter(int weekOfWeekYear, int year) {
            int startYear = mDateTime.getYear();
            while (year != startYear) {
                if (mMode == MODE_YEAR) {
                    if (weekOfWeekYear > 51) {
                        int weekPosition = 0;
                        Timber.v("week pos: %s, weekOfWeekYear: %s", weekPosition, weekOfWeekYear);
                        return weekPosition;
                    }
                }
                weekOfWeekYear += mDateTime.withYear(startYear).weekOfWeekyear().withMaximumValue().getWeekOfWeekyear();
                startYear += 1;
            }
            int weekPosition = 0;
            if (mMode == MODE_YEAR) {
                weekPosition = weekOfWeekYear;
                Timber.v("week pos: %s, weekOfWeekYear: %s", weekPosition, weekOfWeekYear);
            } else if (mMode == MODE_MONTH) {
                weekPosition = weekOfWeekYear - firstWeek;
            }
            if (weekPosition >= 0 && weekPosition < mWeekCount) {
                Timber.v("WeekPosition: %s, WeekCount: %s", weekPosition, mWeekCount);
                return weekPosition;
            }
            Timber.e("Couldn't find position in adapter");
            return INVALID_POSITION;
        }

        @Override
        public int getItemCount() {
            return mWeekCount;
        }

        public DatePickerController getDateController() {
            return mController;
        }

        public Day getStartDay() {
            return new Day(mDateTime);
        }

        /**
         * Created by priyabratapatnaik on 03/11/15.
         */
        public static class WeekViewHolder extends RecyclerView.ViewHolder {
            WeekView mWeekView;

            public WeekViewHolder(View view) {
                super(view);
                mWeekView = (SimpleWeekView) view.findViewById(R.id.week_view);
            }

            public WeekView getWeekView() {
                return mWeekView;
            }
        }
    }
}
