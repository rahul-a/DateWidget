package views;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.library.R;

import org.joda.time.DateTime;

import controllers.DatePickerController;
import timber.log.Timber;
import utils.Utils;

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

    private OnWeekChangedListener mOnWeekChangedListener = new OnWeekChangedListener() {
        @Override
        public void onWeekChanged(int currentWeekOfWeekYear, int currentYear) {

            DateTime dateTime = new DateTime();
            String monthName = dateTime.withWeekOfWeekyear(currentWeekOfWeekYear).withYear(currentWeekOfWeekYear).monthOfYear().getAsText();
            dateSelected.setText(monthName);

            WeekAdapter adapter = (WeekAdapter) dateRecycler.getAdapter();

            if(currentWeekOfWeekYear == adapter.getItemCount()){
                rightArrow.setEnabled(false);
            } else if(currentWeekOfWeekYear == 0){
                leftArrow.setEnabled(false);
            }else{
                leftArrow.setEnabled(true);
                rightArrow.setEnabled(true);
            }

        }
    };

    public void setDateController(DatePickerController controller) {
        mDatePickerController = controller;
        WeekAdapter weekAdapter = new DateView.WeekAdapter(mDatePickerController);
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

        // Adding the selected month TextView
        RelativeLayout.LayoutParams params;
        dateSelected = new TextView(context);
        params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
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

        // Adding the left arrow
        leftArrow = new ImageView(context);
        leftArrow.setId(R.id.date_arrow_left);
        leftArrow.setImageResource(R.drawable.arrow_left);
        leftArrow.setAdjustViewBounds(true);
        params = new RelativeLayout.LayoutParams(getResources().getDimensionPixelSize(R.dimen.arrow_width), getResources().getDimensionPixelSize(R.dimen.arrow_height));
        params.addRule(BELOW, R.id.date_selected_text);
        params.addRule(ALIGN_TOP, R.id.date_recycler_view);
        params.addRule(ALIGN_BOTTOM, R.id.date_recycler_view);
        params.addRule(ALIGN_PARENT_LEFT);
        leftArrow.setLayoutParams(params);
        addView(leftArrow);

        // Adding the right arrow
        rightArrow = new ImageView(context);
        rightArrow.setId(R.id.date_arrow_right);
        rightArrow.setImageResource(R.drawable.arrow_right);
        rightArrow.setAdjustViewBounds(true);
        params = new RelativeLayout.LayoutParams(getResources().getDimensionPixelSize(R.dimen.arrow_width), getResources().getDimensionPixelSize(R.dimen.arrow_height));
        params.addRule(BELOW, R.id.date_selected_text);
        params.addRule(ALIGN_TOP, R.id.date_recycler_view);
        params.addRule(ALIGN_BOTTOM, R.id.date_recycler_view);
        params.addRule(ALIGN_PARENT_RIGHT);
        rightArrow.setLayoutParams(params);
        addView(rightArrow);

        // Adding the WeekView
        dateRecycler = new DateRecycler(context, attrs);
        dateRecycler.setId(R.id.date_recycler_view);
        dateRecycler.setHorizontalScrollBarEnabled(false);
        dateRecycler.setHasFixedSize(true);
        params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                getResources().getDimensionPixelSize(R.dimen.calendar_frame_height));
        params.addRule(RIGHT_OF, R.id.date_arrow_left);
        params.addRule(BELOW, R.id.date_selected_text);
        params.addRule(LEFT_OF, R.id.date_arrow_right);
        dateRecycler.setLayoutParams(params);
        dateRecycler.addItemDecoration(new DateRecycler.WeekDayLabelDecoration(context, RecyclerView.VERTICAL));
        addView(dateRecycler);

        // Settings all the necessary listeners
        leftArrow.setOnClickListener(this);
        rightArrow.setOnClickListener(this);
        addOnWeekChangedListener(mOnWeekChangedListener);

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

        RelativeLayout.LayoutParams params = new LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        int padding = getResources().getDimensionPixelSize(R.dimen.std_padding);
        params.setMargins((w / 14) - (2 * padding), 0, padding, padding);
        params.addRule(CENTER_HORIZONTAL);
        dateSelected.setLayoutParams(params);

    }

    public void setAdapter(RecyclerView.Adapter adapter) {
        if (!(adapter instanceof WeekAdapter)) {
            throw new IllegalArgumentException("Adapter should extend WeekAdapter");
        }
        dateRecycler.setAdapter(adapter);
        dateRecycler.scrollToPresent();
    }

    public void addOnWeekChangedListener(OnWeekChangedListener listener) {
        dateRecycler.addOnWeekChangedListener(listener);
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
            dateSelected.setText(selectedDay.getMonthName());
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
            if(pos > -1) {
                dateRecycler.smoothScrollToPosition(pos - 1);
            }
        } else if (v.getId() == R.id.date_arrow_right) {
            dateRecycler.smoothScrollToPosition(pos+1);
        }

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

        public static final int MODE_MONTH = 0;
        public static final int MODE_YEAR = 1;
        public static final int INVALID_POSITION = -1;

        private static final String TAG = WeekAdapter.class.getSimpleName();
        private DatePickerController mController;
        private DateTime mDateTime;
        private int mWeekCount;
        private int mOffset;
        private int mMode = MODE_YEAR;

        public WeekAdapter(DatePickerController controller) {
            mController = controller;
            mDateTime = new DateTime();
            int presentYear = mDateTime.getYear();
            mDateTime = mController == null ? new DateTime() : mController.getStartDate().toDateTime();
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
                }
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
            DateTime dateTime = mDateTime.withYear(actualYear).withWeekyear(actualYear).withWeekOfWeekyear(weekPos).withDayOfWeek(1);
            Timber.v("Pos %s, Year %s, week %s", position, mDateTime.getYear(), mDateTime.withYear(actualYear).withWeekyear(actualYear).withWeekOfWeekyear(weekPos).withDayOfWeek(1));
            weekView.setStartDate(dateTime);
        }

        public int getTranslatedWeekPosition(int position, int year) {
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

        public WeekView.Day getStartDay() {
            return new WeekView.Day(mDateTime);
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
