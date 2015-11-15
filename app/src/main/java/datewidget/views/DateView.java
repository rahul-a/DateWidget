package datewidget.views;

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

import com.sample.datewidget.R;

import org.joda.time.DateTime;

import datewidget.controllers.DatePickerController;
import datewidget.utils.Utils;
import timber.log.Timber;

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

    public void setDateController(DatePickerController controller) {
        mDatePickerController = controller;
        WeekAdapter weekAdapter = new DateView.WeekAdapter(mDatePickerController);
        setAdapter(weekAdapter);
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

    public void scrollToDay(WeekView.Day day) {
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
        WeekView.Day selectedDay = savedState.mSelectedDay;
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
        WeekView.Day mSelectedDay;

        public SavedState(Parcel source) {
            super(source);
            mSelectedDay = source.readParcelable(WeekView.Day.class.getClassLoader());
        }

        public SavedState(Parcelable superState, WeekView.Day selectedDay) {
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

        private static final String TAG = WeekAdapter.class.getSimpleName();
        private DatePickerController mController;
        private DateTime mDateTime;
        private int mWeekCount;
        private int mOffset;

        public WeekAdapter(DatePickerController controller) {
            mController = controller;
            mDateTime = new DateTime();
            int presentYear = mDateTime.getYear();
            mDateTime = mController == null ? new DateTime() : mController.getStartDate().toDateTime();
            mOffset = presentYear - mDateTime.getYear();
            DateTime dateTime = mDateTime.weekOfWeekyear().setCopy(1);
            if (mOffset > 0) {
                mWeekCount += dateTime.weekOfWeekyear().withMaximumValue().getWeekOfWeekyear();
                for (int i = mOffset; i > 0; i--) {
                    mWeekCount += dateTime.plusYears(i).weekOfWeekyear().withMaximumValue().getWeekOfWeekyear();
                }
                mWeekCount++;
            } else {
                int maxDay = mDateTime.dayOfMonth().withMaximumValue().getDayOfMonth();
                firstWeek = mDateTime.withDayOfMonth(1).getWeekOfWeekyear();

                int lastWeek = mDateTime.withDayOfMonth(maxDay).getWeekOfWeekyear();
                mWeekCount += lastWeek - firstWeek + 1;
                int start = dateTime.withWeekOfWeekyear(lastWeek).dayOfWeek().withMinimumValue().getDayOfMonth();
                int end = dateTime.withWeekOfWeekyear(lastWeek).dayOfWeek().withMaximumValue().getDayOfMonth();
                Timber.v("Week count: %s, first week: %s, last week: %s, Last weeks dates --> %s to %s", mWeekCount, firstWeek, lastWeek, start, end);
            }
        }
        int firstWeek;
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
            DateTime dateTime = mDateTime.withYear(actualYear).withWeekyear(actualYear).withWeekOfWeekyear(position + firstWeek).withDayOfWeek(1);
            Timber.v("Pos %s, Year %s, week %s", position, mDateTime.getYear(), mDateTime.withYear(actualYear).withWeekyear(actualYear).withWeekOfWeekyear(position + firstWeek).withDayOfWeek(1));
            weekView.setStartDate(dateTime);
        }

        private int getTranslatedWeekPosition(int position, int year) {
            int maxWeeks = mDateTime.withYear(year).weekOfWeekyear().withMaximumValue().getWeekOfWeekyear();
            int translatedPos = (position + 1) % maxWeeks;
            if (translatedPos == 0) {
                translatedPos += maxWeeks;
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

        public int getWeekPositionInAdapter(int position, int year) {
            int startYear = mDateTime.getYear();
            int weekOfWeekYear = position;
            while (year != startYear) {
                weekOfWeekYear += mDateTime.withYear(startYear).weekOfWeekyear().withMaximumValue().getWeekOfWeekyear();
                startYear += 1;
            }
            return weekOfWeekYear - firstWeek;
        }

        @Override
        public int getItemCount() {
            return mWeekCount;
        }

        public DatePickerController getDateController() {
            return mController;
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
