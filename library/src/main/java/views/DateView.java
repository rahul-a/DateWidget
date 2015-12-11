package views;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.library.R;

import org.joda.time.DateTime;

import controllers.DatePickerController;
import timber.log.Timber;
import utils.Utils;

/**
 * Created by priyabratapatnaik on 12/11/15.
 */
public class DateView extends LinearLayout {

    public interface OnWeekChangedListener {
        void onWeekChanged(int currentWeekOfWeekYear);
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
            return mSelectedDay;
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
            return mToday;
        }

        @Override
        public Day getStartDate() {
            return mToday;
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
            Timber.v("Setting mode");
            weekAdapter.setMode(mode);
            weekAdapter.notifyDataSetChanged();
            dateRecycler.scrollToPresent();
        } else {
            throw new IllegalStateException("Adapter should be set before setting view mode");
        }
    }

    private void init(Context context, AttributeSet attrs) {
        LayoutParams params;
        setOrientation(VERTICAL);
        dateSelected = new TextView(context);
        params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int padding = getResources().getDimensionPixelSize(R.dimen.std_padding);
        params.setMargins(padding, 0, padding, padding);
        dateSelected.setLayoutParams(params);
        dateSelected.setPadding(0, 0, padding, padding);
        dateSelected.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimension(R.dimen.large_text_size));
        dateSelected.setTypeface(null, Typeface.BOLD);
        dateSelected.setTextColor(Utils.getAccentColorFromThemeIfAvailable(context));
        dateSelected.setId(R.id.date_selected_text);
        addView(dateSelected);

        dateRecycler = new DateRecycler(context, attrs);
        dateRecycler.setId(R.id.date_recycler_view);
        dateRecycler.setHorizontalScrollBarEnabled(false);
        dateRecycler.setHasFixedSize(true);
        params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                getResources().getDimensionPixelSize(R.dimen.calendar_frame_height));
        dateRecycler.setLayoutParams(params);
        dateRecycler.addItemDecoration(new DateRecycler.WeekDayLabelDecoration(context, RecyclerView.VERTICAL));
        addView(dateRecycler);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int padding = getResources().getDimensionPixelSize(R.dimen.std_padding);
        params.setMargins((w / 14) - (2 * padding), 0, padding, padding);
        dateSelected.setLayoutParams(params);
    }

    public void setAdapter(RecyclerView.Adapter adapter) {
        if (!(adapter instanceof WeekAdapter)) {
            throw new IllegalArgumentException("Adapter should extend WeekAdapter");
        }
        dateRecycler.setAdapter(adapter);
        dateRecycler.scrollToPresent();
    }

    public void setOnWeekChangedListener(OnWeekChangedListener listener) {
        dateRecycler.setOnWeekChangedListener(listener);
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
            dateSelected.setText(selectedDay.toFormattedString());
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
            DateTime dateTime = mDateTime.withYear(actualYear).withWeekyear(actualYear).withWeekOfWeekyear(weekPos).withDayOfWeek(1);
            Timber.v("Pos %s, Year %s, week %s", position, mDateTime.getYear(), mDateTime.withYear(actualYear).withWeekyear(actualYear).withWeekOfWeekyear(weekPos).withDayOfWeek(1));
            weekView.setStartDate(dateTime);
        }

        private int getTranslatedWeekPosition(int position, int year) {
            int maxWeeks;
            int translatedPos = position;
            if (mMode == MODE_YEAR) {
                maxWeeks = mDateTime.withYear(year).weekOfWeekyear().withMaximumValue().getWeekOfWeekyear();
                translatedPos = (position + 1) % maxWeeks;
                if (translatedPos == 0) {
                    translatedPos += maxWeeks;
                }
            } else if (mMode == MODE_MONTH) {
                translatedPos += firstWeek;
            }
            // Timber.v("Translated pos %s", translatedPos);
            return translatedPos;
        }

        private int getYearForWeekPosition(int position) {
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
            }
            // Timber.v("Returning year %s", actualYear);
            return actualYear;
        }

        public int getWeekPositionInAdapter(int weekOfWeekYear, int year) {
            int startYear = mDateTime.getYear();
            while (year != startYear) {
                weekOfWeekYear += mDateTime.withYear(startYear).weekOfWeekyear().withMaximumValue().getWeekOfWeekyear();
                startYear += 1;
            }
            int weekPosition = 0;
            if (mMode == MODE_YEAR) {
                weekPosition = weekOfWeekYear - 1;
            } else if (mMode == MODE_MONTH) {
                weekPosition = weekOfWeekYear - firstWeek;
            }
            if (weekPosition > 0 && weekPosition < mWeekCount) {
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
