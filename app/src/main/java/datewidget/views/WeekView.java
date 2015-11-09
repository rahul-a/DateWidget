package datewidget.views;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.sample.datewidget.R;

import org.joda.time.DateTime;

import java.security.InvalidParameterException;
import java.util.Calendar;
import java.util.HashMap;

import datewidget.controllers.DatePickerController;
import timber.log.Timber;

/**
 * Created by priyabratapatnaik on 03/11/15.
 */
public abstract class WeekView extends View {

    private static final String TAG = WeekView.class.getSimpleName();

    /**
     * These params can be passed into the view to control how it appears.
     * {@link #VIEW_PARAMS_WEEK} is the only required field, though the default
     * values are unlikely to fit most layouts correctly.
     */
    /**
     * This sets the height of this week in pixels
     */
    public static final String VIEW_PARAMS_HEIGHT = "height";

    /**
     * Specifies the month to be shown.
     */
    public static final String VIEW_PARAMS_MONTH = "month";

    /**
     * Specifies the current year to be shown.
     */
    public static final String VIEW_PARAMS_YEAR = "year";

    /**
     * Specifies the position week to be shown depending on the date.
     */
    public static final String VIEW_PARAMS_DATE = "date";

    /**
     * This sets one of the days in this view as selected {@link Calendar#SUNDAY}
     * through {@link Calendar#SATURDAY}.
     */
    public static final String VIEW_PARAMS_SELECTED_DAY = "selected_day";
    /**
     * Which day the week should start on. {@link Calendar#SUNDAY} through
     * {@link Calendar#SATURDAY}.
     */
    public static final String VIEW_PARAMS_WEEK_START = "week_start";

    protected static int MINI_DAY_NUMBER_TEXT_SIZE;
    private static final int SELECTED_CIRCLE_ALPHA = 255;

    protected static int MONTH_DAY_LABEL_TEXT_SIZE;

    protected static int DAY_SEPARATOR_WIDTH = 1;
    protected static int DEFAULT_HEIGHT = 32;
    protected static int MONTH_HEADER_SIZE;
    protected static final int MAX_NUM_ROWS = 1;
    protected static int MIN_HEIGHT = 10;
    protected static final int DEFAULT_SELECTED_DAY = -1;
    protected static final int DEFAULT_WEEK_START = Calendar.SUNDAY;
    protected static final int DEFAULT_NUM_DAYS = 7;
    protected static int DAY_SELECTED_CIRCLE_SIZE;

    protected static final int DEFAULT_NUM_ROWS = 1;

    private int mDayOfWeekStart = 0;

    // Quick reference to the width of this view, matches parent
    protected int mWidth;

    // The height this view should draw at in pixels, set by height param
    protected int mDayNumFrameHeight = DEFAULT_HEIGHT;

    protected DatePickerController mController;

    // affects the padding on the sides of this view
    protected int mEdgePadding = 0;

    protected int mMonth;

    protected int mYear;

    // If this view contains the today
    protected boolean mHasToday = false;
    // Which day is selected [0-6] or -1 if no day is selected
    protected Day mSelectedDay;
    // Which day is today [0-6] or -1 if no day is today
    protected int mToday = DEFAULT_SELECTED_DAY;
    // Which day of the week to start on [0-6]
    protected int mWeekStart = DEFAULT_WEEK_START;
    // How many days to display
    protected int mNumDays = DEFAULT_NUM_DAYS;
    // The number of days + a spot for week number if it is displayed
    protected int mNumCells = mNumDays;

    protected Paint mMonthDayLabelPaint;
    protected Paint mSelectedCirclePaint;
    protected Paint mMonthNumPaint;

    protected int mDayTextColor;
    protected int mSelectedDayTextColor;
    protected int mMonthDayTextColor;
    protected int mTodayNumberColor;
    protected int mHighlightedDayTextColor;
    protected int mDisabledDayTextColor;

    protected Day[] mDays = new Day[mNumDays];

    public WeekView(Context context) {
        this(context, null, null);
    }

    public WeekView(Context context, AttributeSet attrs) {
        this(context, attrs, null);
    }

    public WeekView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, null);
    }

    public WeekView(Context context, AttributeSet attrs, DatePickerController controller) {
        super(context, attrs);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.weekView);

        mDayTextColor = typedArray.getColor(R.styleable.weekView_day_text_color,
                ContextCompat.getColor(context, R.color.mdtp_date_picker_text_normal));

        mMonthDayTextColor = typedArray.getColor(R.styleable.weekView_week_label_color,
                ContextCompat.getColor(context, R.color.mdtp_date_picker_month_day_dark_theme));

        mDisabledDayTextColor = typedArray.getColor(R.styleable.weekView_muted_day_color,
                ContextCompat.getColor(context, R.color.mdtp_date_picker_text_disabled_dark_theme));

        mHighlightedDayTextColor = typedArray.getColor(R.styleable.weekView_selected_day_color,
                ContextCompat.getColor(context, R.color.mdtp_date_picker_text_highlighted_dark_theme));

        mSelectedDayTextColor = typedArray.getColor(R.styleable.weekView_selected_day_color,
                ContextCompat.getColor(context, R.color.mdtp_white));

        mTodayNumberColor = typedArray.getColor(R.styleable.weekView_today_color,
                ContextCompat.getColor(context, R.color.mdtp_accent_color));

        mController = controller;
        Resources res = context.getResources();

        MINI_DAY_NUMBER_TEXT_SIZE = res.getDimensionPixelSize(R.dimen.mdtp_day_number_size);
        MONTH_DAY_LABEL_TEXT_SIZE = res.getDimensionPixelSize(R.dimen.mdtp_month_day_label_text_size);
        MONTH_HEADER_SIZE = res.getDimensionPixelOffset(R.dimen.mdtp_month_list_item_header_height);
        DAY_SELECTED_CIRCLE_SIZE = res.getDimensionPixelSize(R.dimen.mdtp_day_number_select_circle_radius);
        DAY_SEPARATOR_WIDTH = res.getDimensionPixelSize(R.dimen.day_separator);

        mDayNumFrameHeight = (res.getDimensionPixelOffset(R.dimen.mdtp_month_row_height)) / MAX_NUM_ROWS;

        // Sets up any standard paints that will be used
        initView();
        getDayNumbers();
    }

    /**
     * Draws the week and month day numbers for this week. Override this method
     * if you need different placement.
     *
     * @param canvas The canvas to draw on
     */
    protected void drawWeek(Canvas canvas) {
        int y = getWeekHeaderSize() + mDayNumFrameHeight / 2;
        final float dayWidthHalf = (mWidth - mEdgePadding * 2) / (mNumDays * 2.0f);
        int j = 0;
        for (int dayNumber = 0; dayNumber < mNumCells; dayNumber++) {
            final int x = (int)((2 * j + 1) * dayWidthHalf + mEdgePadding);

            int yRelativeToDay = y;
            final int startX = (int)(x - dayWidthHalf);
            final int stopX = (int)(x + dayWidthHalf);
            final int startY = (y - yRelativeToDay);
            final int stopY = (startY + mDayNumFrameHeight);

            drawWeekDate(canvas, mDays[dayNumber], x, y, startX, stopX, startY, stopY);

            j++;
        }
    }

    private int[] getDayNumbers() {
        int[] days = new int[7];
        DateTime currentDateTime;
        if (mToday == 0 || mMonth == 0 || mYear == 0) {
            currentDateTime = new DateTime();
        } else {
            currentDateTime = new DateTime(mYear, mMonth, mToday, 0, 0, 0);
        }
        DateTime startDate = new DateTime(currentDateTime.getYear(), currentDateTime.getMonthOfYear(),
                currentDateTime.dayOfWeek().withMinimumValue().getDayOfMonth(), 0, 0, 0);

        DateTime endDate = startDate.plusWeeks(1);

        int i = 0;
        while (startDate.isBefore(endDate)) {
            if (i < 7) {
                mDays[i] = new Day(startDate);
                // Timber.v("Day of week: %s,  %s", i, mDays[i]);
                days[i++] = startDate.getDayOfMonth();
            }
            // Timber.v("Week of week year: %s", startDate.getWeekOfWeekyear());
            startDate = startDate.plusDays(1);
        }

        return days;
    }

    /**
     * This method should draw the week date.  Implemented by sub-classes to allow customization.
     *
     * @param canvas  The canvas to draw on
     * @param x  The default x position to draw the day number
     * @param y  The default y position to draw the day number
     * @param startX  The left boundary of the day number rect
     * @param stopX  The right boundary of the day number rect
     * @param startY  The top boundary of the day number rect
     * @param stopY  The bottom boundary of the day number rect
     */
    public abstract void drawWeekDate(Canvas canvas, Day day, int x, int y, int startX, int stopX, int startY, int stopY);

    @Override
    protected void onDraw(Canvas canvas) {
        drawWeekDayLabels(canvas);
        drawWeek(canvas);
    }

    /**
     * Calculates the day that the given x position is in, accounting for week
     * number. Returns the day or -1 if the position wasn't in a day.
     *
     * @param x The x position of the touch event
     * @return The day number, or -1 if the position wasn't in a day
     */
    public Day getDayFromLocation(float x, float y) {
        final Day day = getInternalDayFromLocation(x, y);
        return day;
    }

    /**
     * Calculates the day that the given x position is in, accounting for week
     * number.
     *
     * @param x The x position of the touch event
     * @return The day number
     */
    protected Day getInternalDayFromLocation(float x, float y) {
        int dayStart = mEdgePadding;
        if (x < dayStart || x > mWidth - mEdgePadding) {
            return null;
        }
        int column = (int) ((x - dayStart) * mNumDays / (mWidth - (2 * mEdgePadding)));
        if (column < 0 || column > mNumCells) {
            return null;
        }
        return mDays[column];
    }

    /**
     * Called when the user clicks on a day. Handles callbacks to the
     * {@link DatePickerController#onDayOfMonthSelected(View, Day)} if one is set.
     * <p/>
     * If the day is out of the range set by minDate and/or maxDate, this is a no-op.
     *
     * @param day The day that was clicked
     */
    private void onDayClick(Day day) {
        // If the min / max date are set, only process the click if it's a valid selection.
        if (mController.isOutOfRange(day)) {
            return;
        }

        mSelectedDay = day;
        invalidate();

        if (mController != null) {
            mController.onDayOfMonthSelected(this, day);
        }
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                final Day day = getDayFromLocation(event.getX(), event.getY());
                if (day != null) {
                    onDayClick(day);
                }
                break;
        }
        return true;
    }

    /**
     * Sets up the text and style properties for painting. Override this if you
     * want to use a different paint.
     */
    protected void initView() {
        mSelectedCirclePaint = new Paint();
        mSelectedCirclePaint.setFakeBoldText(true);
        mSelectedCirclePaint.setAntiAlias(true);
        mSelectedCirclePaint.setColor(mTodayNumberColor);
        mSelectedCirclePaint.setTextAlign(Paint.Align.CENTER);
        mSelectedCirclePaint.setStyle(Paint.Style.FILL);
        mSelectedCirclePaint.setAlpha(SELECTED_CIRCLE_ALPHA);

        mMonthDayLabelPaint = new Paint();
        mMonthDayLabelPaint.setAntiAlias(true);
        mMonthDayLabelPaint.setTextSize(MONTH_DAY_LABEL_TEXT_SIZE);
        mMonthDayLabelPaint.setColor(mMonthDayTextColor);
        mMonthDayLabelPaint.setStyle(Paint.Style.FILL);
        mMonthDayLabelPaint.setTextAlign(Paint.Align.CENTER);
        mMonthDayLabelPaint.setFakeBoldText(true);

        mMonthNumPaint = new Paint();
        mMonthNumPaint.setAntiAlias(true);
        mMonthNumPaint.setTextSize(MINI_DAY_NUMBER_TEXT_SIZE);
        mMonthNumPaint.setStyle(Paint.Style.FILL);
        mMonthNumPaint.setTextAlign(Paint.Align.CENTER);
        mMonthNumPaint.setFakeBoldText(false);
    }

    /**
     * Sets all the parameters for displaying this week. The only required
     * parameter is the week number. Other parameters have a default value and
     * will only update if a new value is included, except for focus month,
     * which will always default to no focus month if no value is passed in. See
     * {@link #VIEW_PARAMS_HEIGHT} for more info on parameters.
     *
     * @param params A map of the new parameters, see
     *            {@link #VIEW_PARAMS_HEIGHT}
     */
    public void setMonthParams(HashMap<String, Integer> params) {
        if (!params.containsKey(VIEW_PARAMS_MONTH) && !params.containsKey(VIEW_PARAMS_YEAR)) {
            throw new InvalidParameterException("You must specify month and year for this view");
        }
        setTag(params);
        // We keep the current value for any params not present
        if (params.containsKey(VIEW_PARAMS_HEIGHT)) {
            mDayNumFrameHeight = params.get(VIEW_PARAMS_HEIGHT);
            if (mDayNumFrameHeight < MIN_HEIGHT) {
                mDayNumFrameHeight = MIN_HEIGHT;
            }
        }

        // Allocate space for caching the day numbers and focus values
        mMonth = params.containsKey(VIEW_PARAMS_MONTH) ? params.get(VIEW_PARAMS_MONTH) : 0;
        mYear = params.containsKey(VIEW_PARAMS_YEAR) ? params.get(VIEW_PARAMS_YEAR) : 0;
        mToday = params.containsKey(VIEW_PARAMS_DATE)? params.get(VIEW_PARAMS_DATE) : 0;

        if (mController != null) {
            Day selectedDay = mController.getSelectedDay();
            if (mController.isSelectable(selectedDay)) {
                mSelectedDay = selectedDay;
            } else {
                mSelectedDay = null;
            }
        }

        mHasToday = false;

        getDayNumbers();
        mNumCells = 7;

        Day currentDay = mController == null ? new Day(mToday, mMonth, mYear) : mController.getToday();

        for (int i = 0; i < mNumCells; i++) {
            Day tempDay = mDays[i];
            if (tempDay.equals(currentDay)) {
                mHasToday = true;
                Timber.v("Found today: %s", mToday);
            }
        }
    }

    /**
     * @param day
     * @return true if the given date should be highlighted
     */
    protected boolean isHighlighted(Day day) {
        Day[] highlightedDays = mController != null ? mController.getHighlightedDays() : null;
        if (highlightedDays == null) {
            return false;
        }
        for (Day tempDay : highlightedDays) {
            if (tempDay.equals(day)) {
                return true;
            }
        }
        Timber.v(String.format("NOT Highlighted:: day: %s, month: %s, year: %s", day.getDate(), day.getMonth(), day.getYear()));
        return false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), (getWeekHeaderSize() + mDayNumFrameHeight));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWidth = w;
        Timber.v("Width: #onSizeChanged --> %s", mWidth);
    }

    /**
     * A wrapper to the MonthHeaderSize to allow override it in children
     */
    protected int getWeekHeaderSize() {
        return MONTH_HEADER_SIZE;
    }

    @Deprecated
    // Todo take this logic into ItemDecoration for RecyclerView
    protected void drawWeekDayLabels(Canvas canvas) {
        int y = getWeekHeaderSize() - (MONTH_DAY_LABEL_TEXT_SIZE) / 2;
        int dayWidthHalf = (mWidth - mEdgePadding * 2) / (mNumDays * 2);

        for (int i = 0; i < mNumDays; i++) {
            int x = (2 * i + 1) * dayWidthHalf + mEdgePadding;
            String weekString = mDays[i].getDay().toUpperCase().substring(0, 3);
            canvas.drawText(weekString, x, y, mMonthDayLabelPaint);
        }
    }

    public static class Day {
        int year;
        int month;
        String day;
        int date;

        public Day(DateTime dateTime) {
            year = dateTime.getYear();
            month = dateTime.getMonthOfYear();
            day = dateTime.dayOfWeek().getAsShortText();
            date = dateTime.getDayOfMonth();
        }

        public Day(int date, int month, int year) {
            this.date = date;
            this.month = month;
            this.year = year;
        }

        public Day() {
            DateTime dateTime = new DateTime();
            year = dateTime.getYear();
            month = dateTime.getMonthOfYear();
            day = dateTime.dayOfWeek().getAsShortText();
            date = dateTime.getDayOfMonth();
        }

        public int getYear() {
            return year;
        }

        public int getMonth() {
            return month;
        }

        public String getDay() {
            return day;
        }

        public int getDate() {
            return date;
        }

        @Override
        public String toString() {
            return String.format("Date: %s, Day: %s, Month: %s, Year: %s", date, day, month, year);
        }

        @Override
        public boolean equals(Object object) {
            if (this == null || object == null || !(object instanceof Day)) {
                return false;
            }

            Day other = (Day) object;
            if (year == other.getYear()) {
                if (month == other.getMonth()) {
                    if (date == other.getDate()) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public Day[] getDaysInWeek() {
        return mDays;
    }

    public void setSelectedDay(Day selectedDay) {
        mSelectedDay = selectedDay;
    }

    public void setController(DatePickerController controller) {
        mController = controller;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        mWidth = right - left;
        Timber.v("Width:#onLayout: %s", mWidth);
    }
}