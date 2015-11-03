package datewidget.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.sample.datewidget.R;

import java.security.InvalidParameterException;
import java.util.Calendar;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Locale;

import datewidget.adapters.MonthAdapter;
import datewidget.controllers.DatePickerController;

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
     * This specifies the position (or weeks since the epoch) of this week.
     */
    public static final String VIEW_PARAMS_MONTH = "month";
    /**
     * This specifies the position (or weeks since the epoch) of this week.
     */
    public static final String VIEW_PARAMS_YEAR = "year";
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
    protected int mRowHeight = DEFAULT_HEIGHT;

    protected int mNumRows = DEFAULT_NUM_ROWS;

    protected DatePickerController mController;

    // affects the padding on the sides of this view
    protected int mEdgePadding = 0;

    protected int mMonth;

    protected int mYear;

    // If this view contains the today
    protected boolean mHasToday = false;
    // Which day is selected [0-6] or -1 if no day is selected
    protected int mSelectedDay = -1;
    // Which day is today [0-6] or -1 if no day is today
    protected int mToday = DEFAULT_SELECTED_DAY;
    // Which day of the week to start on [0-6]
    protected int mWeekStart = DEFAULT_WEEK_START;
    // How many days to display
    protected int mNumDays = DEFAULT_NUM_DAYS;
    // The number of days + a spot for week number if it is displayed
    protected int mNumCells = mNumDays;
    // Optional listener for handling day click actions
    protected OnDayClickListener mOnDayClickListener;

    protected Paint mMonthDayLabelPaint;
    protected Paint mSelectedCirclePaint;
    protected Paint mMonthNumPaint;

    protected int mDayTextColor;
    protected int mSelectedDayTextColor;
    protected int mMonthDayTextColor;
    protected int mTodayNumberColor;
    protected int mHighlightedDayTextColor;
    protected int mDisabledDayTextColor;

    private final Calendar mCalendar;
    protected final Calendar mDayLabelCalendar;

    /**
     * Handles callbacks when the user clicks on a time object.
     */
    public interface OnDayClickListener {
        void onDayClick(WeekView view, MonthAdapter.CalendarDay day);
    }

    public WeekView(Context context) {
        this(context, null, null);
    }

    public WeekView(Context context, AttributeSet attrs, DatePickerController controller) {
        super(context, attrs);
        mController = controller;
        Resources res = context.getResources();

        mDayLabelCalendar = Calendar.getInstance();
        mCalendar = Calendar.getInstance();

        boolean darkTheme = mController != null && mController.isThemeDark();
        if(darkTheme) {
            mDayTextColor = ContextCompat.getColor(context, R.color.mdtp_date_picker_text_normal_dark_theme);
            mMonthDayTextColor = ContextCompat.getColor(context, R.color.mdtp_date_picker_month_day_dark_theme);
            mDisabledDayTextColor = ContextCompat.getColor(context, R.color.mdtp_date_picker_text_disabled_dark_theme);
            mHighlightedDayTextColor = ContextCompat.getColor(context, R.color.mdtp_date_picker_text_highlighted_dark_theme);
        }
        else {
            mDayTextColor = ContextCompat.getColor(context, R.color.mdtp_date_picker_text_normal);
            mMonthDayTextColor = ContextCompat.getColor(context, R.color.mdtp_date_picker_month_day);
            mDisabledDayTextColor = ContextCompat.getColor(context, R.color.mdtp_date_picker_text_disabled);
            mHighlightedDayTextColor = ContextCompat.getColor(context, R.color.mdtp_date_picker_text_highlighted);
        }
        mTodayNumberColor = mController.getAccentColor();

        MINI_DAY_NUMBER_TEXT_SIZE = res.getDimensionPixelSize(R.dimen.mdtp_day_number_size);
        // MONTH_LABEL_TEXT_SIZE = res.getDimensionPixelSize(R.dimen.mdtp_month_label_size);
        MONTH_DAY_LABEL_TEXT_SIZE = res.getDimensionPixelSize(R.dimen.mdtp_month_day_label_text_size);
        // MONTH_HEADER_SIZE = res.getDimensionPixelOffset(R.dimen.mdtp_month_list_item_header_height);
        DAY_SELECTED_CIRCLE_SIZE = res.getDimensionPixelSize(R.dimen.mdtp_day_number_select_circle_radius);

        mRowHeight = (res.getDimensionPixelOffset(R.dimen.mdtp_date_picker_view_animator_height)) / MAX_NUM_ROWS;

        // Sets up any standard paints that will be used
        initView();
    }

    /**
     * Draws the week and month day numbers for this week. Override this method
     * if you need different placement.
     *
     * @param canvas The canvas to draw on
     */
    protected void drawMonthNums(Canvas canvas) {
        int y = (((mRowHeight + MINI_DAY_NUMBER_TEXT_SIZE) / 2) - DAY_SEPARATOR_WIDTH);
        final float dayWidthHalf = (mWidth - mEdgePadding * 2) / (mNumDays * 2.0f);
        int j = findDayOffset();
        for (int dayNumber = 1; dayNumber <= mNumCells; dayNumber++) {
            final int x = (int)((2 * j + 1) * dayWidthHalf + mEdgePadding);

            int yRelativeToDay = (mRowHeight + MINI_DAY_NUMBER_TEXT_SIZE) / 2 - DAY_SEPARATOR_WIDTH;

            final int startX = (int)(x - dayWidthHalf);
            final int stopX = (int)(x + dayWidthHalf);
            final int startY = (int)(y - yRelativeToDay);
            final int stopY = (int)(startY + mRowHeight);

            drawMonthDay(canvas, mYear, mMonth, dayNumber, x, y, startX, stopX, startY, stopY);

            j++;
            if (j == mNumDays) {
                j = 0;
                y += mRowHeight;
            }
        }
    }

    /**
     * This method should draw the month day.  Implemented by sub-classes to allow customization.
     *
     * @param canvas  The canvas to draw on
     * @param year  The year of this month day
     * @param month  The month of this month day
     * @param day  The day number of this month day
     * @param x  The default x position to draw the day number
     * @param y  The default y position to draw the day number
     * @param startX  The left boundary of the day number rect
     * @param stopX  The right boundary of the day number rect
     * @param startY  The top boundary of the day number rect
     * @param stopY  The bottom boundary of the day number rect
     */
    public abstract void drawMonthDay(Canvas canvas, int year, int month, int day,
                                      int x, int y, int startX, int stopX, int startY, int stopY);


    protected int findDayOffset() {
        return (mDayOfWeekStart < mWeekStart ? (mDayOfWeekStart + mNumDays) : mDayOfWeekStart)
                - mWeekStart;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawMonthNums(canvas);
    }

    /**
     * Calculates the day that the given x position is in, accounting for week
     * number. Returns the day or -1 if the position wasn't in a day.
     *
     * @param x The x position of the touch event
     * @return The day number, or -1 if the position wasn't in a day
     */
    public int getDayFromLocation(float x, float y) {
        final int day = getInternalDayFromLocation(x, y);
        if (day < 1 || day > mNumCells) {
            return -1;
        }
        return day;
    }

    /**
     * Calculates the day that the given x position is in, accounting for week
     * number.
     *
     * @param x The x position of the touch event
     * @return The day number
     */
    protected int getInternalDayFromLocation(float x, float y) {
        int dayStart = mEdgePadding;
        if (x < dayStart || x > mWidth - mEdgePadding) {
            return -1;
        }
        // Selection is (x - start) / (pixels/day) == (x -s) * day / pixels
        int row = (int) y / mRowHeight;
        int column = (int) ((x - dayStart) * mNumDays / (mWidth - dayStart - mEdgePadding));

        int day = column - findDayOffset() + 1;
        day += row * mNumDays;
        return day;
    }

    /**
     * Called when the user clicks on a day. Handles callbacks to the
     * {@link OnDayClickListener} if one is set.
     * <p/>
     * If the day is out of the range set by minDate and/or maxDate, this is a no-op.
     *
     * @param day The day that was clicked
     */
    private void onDayClick(int day) {
        // If the min / max date are set, only process the click if it's a valid selection.
        if (mController.isOutOfRange(mYear, mMonth, day)) {
            return;
        }

        if (mOnDayClickListener != null) {
            mOnDayClickListener.onDayClick(this, new MonthAdapter.CalendarDay(mYear, mMonth, day));
        }
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                final int day = getDayFromLocation(event.getX(), event.getY());
                if (day >= 0) {
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
        // mMonthDayLabelPaint.setTypeface(TypefaceHelper.get(getContext(),"Roboto-Medium"));
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
            mRowHeight = params.get(VIEW_PARAMS_HEIGHT);
            if (mRowHeight < MIN_HEIGHT) {
                mRowHeight = MIN_HEIGHT;
            }
        }
        if (params.containsKey(VIEW_PARAMS_SELECTED_DAY)) {
            mSelectedDay = params.get(VIEW_PARAMS_SELECTED_DAY);
        }

        // Allocate space for caching the day numbers and focus values
        mMonth = params.get(VIEW_PARAMS_MONTH);
        mYear = params.get(VIEW_PARAMS_YEAR);

        // Figure out what day today is
        //final Time today = new Time(Time.getCurrentTimezone());
        //today.setToNow();
        final Calendar today = Calendar.getInstance();
        mHasToday = false;
        mToday = -1;

        mCalendar.set(Calendar.MONTH, mMonth);
        mCalendar.set(Calendar.YEAR, mYear);
        mCalendar.set(Calendar.DAY_OF_MONTH, 1);
        mDayOfWeekStart = mCalendar.get(Calendar.DAY_OF_WEEK);

        if (params.containsKey(VIEW_PARAMS_WEEK_START)) {
            mWeekStart = params.get(VIEW_PARAMS_WEEK_START);
        } else {
            mWeekStart = mCalendar.getFirstDayOfWeek();
        }

        mNumCells = mCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int i = 0; i < mNumCells; i++) {
            final int day = i + 1;
            if (sameDay(day, today)) {
                mHasToday = true;
                mToday = day;
            }
        }
        mNumRows = calculateNumRows();
    }

    /**
     * @param year
     * @param month
     * @param day
     * @return true if the given date should be highlighted
     */
    protected boolean isHighlighted(int year, int month, int day) {
        Calendar[] highlightedDays = mController.getHighlightedDays();
        if(highlightedDays == null) return false;
        for (Calendar c : highlightedDays) {
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


    private int calculateNumRows() {
        int offset = findDayOffset();
        int dividend = (offset + mNumCells) / mNumDays;
        int remainder = (offset + mNumCells) % mNumDays;
        return (dividend + (remainder > 0 ? 1 : 0));
    }

    private boolean sameDay(int day, Calendar today) {
        return mYear == today.get(Calendar.YEAR) &&
                mMonth == today.get(Calendar.MONTH) &&
                day == today.get(Calendar.DAY_OF_MONTH);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), mRowHeight * mNumRows + 5);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWidth = w;
    }

    public int getMonth() {
        return mMonth;
    }

    public int getYear() {
        return mYear;
    }
}
