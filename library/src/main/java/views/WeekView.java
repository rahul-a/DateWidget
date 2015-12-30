package views;

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

import com.example.library.R;

import org.joda.time.DateTime;

import controllers.DatePickerController;

/**
 * Created by priyabratapatnaik on 03/11/15.
 */
public abstract class WeekView extends View {

    private static final String TAG = WeekView.class.getSimpleName();

    protected int mMaxRows = 1;
    protected static final int DEFAULT_SELECTED_DAY = -1;
    protected static final int DEFAULT_NUM_DAYS = 7;

    private static final int SELECTED_CIRCLE_ALPHA = 255;

    protected int mDateTextSize;
    protected static int mDayLabelTextSize;
    protected int mDaySeparatorWidth = 1;
    protected int mDefaultHeight = 32;
    protected int mWeekDayHeaderSize;
    protected int mMinHeight = 10;
    protected DateTime mStartDate;

    protected int mSelectedDayCircleSize;

    // Quick reference to the width of this view, matches parent
    protected int mWidth;

    // The height this view should draw at in pixels, set by height param
    protected int mDayNumFrameHeight = mDefaultHeight;

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

        Resources res = context.getResources();

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
                ContextCompat.getColor(context, R.color.bumble_green));

        mDateTextSize = typedArray.getDimensionPixelSize(R.styleable.weekView_date_size,
                res.getDimensionPixelSize(R.dimen.date_number_size));

        mDayLabelTextSize = typedArray.getDimensionPixelSize(R.styleable.weekView_day_label_size,
                res.getDimensionPixelSize(R.dimen.date_number_size));

        mSelectedDayCircleSize = typedArray.getDimensionPixelSize(R.styleable.weekView_selected_circle_size,
                res.getDimensionPixelSize(R.dimen.day_select_circle_radius));

        mDaySeparatorWidth = typedArray.getDimensionPixelSize(R.styleable.weekView_day_separator_width,
                res.getDimensionPixelSize(R.dimen.day_separator));


        typedArray.recycle();
        mController = controller;

        mWeekDayHeaderSize = res.getDimensionPixelOffset(R.dimen.month_list_item_header_height);

        mDayNumFrameHeight = (res.getDimensionPixelOffset(R.dimen.month_row_height)) * mMaxRows;

        // Sets up any standard paints that will be used
        initView();
        getDayNumbers();

        findToday();

        if (mController != null) {
            Day selectedDay = mController.getSelectedDay();
            if (mController.isSelectable(selectedDay)) {
                mSelectedDay = selectedDay;
            } else {
                mSelectedDay = null;
            }
        }
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

    protected int[] getDayNumbers() {
        int[] days = new int[7];
        DateTime startDate;
        if (mStartDate != null) {
            startDate = mStartDate;
        } else {
            startDate = new DateTime();
        }

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
    protected abstract void drawWeekDate(Canvas canvas, Day day, int x, int y, int startX, int stopX, int startY, int stopY);

    @Override
    protected void onDraw(Canvas canvas) {
        // drawWeekDayLabels(canvas);
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
    public void onDayClick(Day day) {
        // If the min / max date are set, only process the click if it's a valid selection.
        if (mController.isOutOfRange(day)) {
            return;
        }

        mSelectedDay = day;
        invalidate();

        if (mController != null && isVisibleInWeek(day)) {
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
        mMonthDayLabelPaint.setTextSize(mDayLabelTextSize);
        mMonthDayLabelPaint.setColor(mMonthDayTextColor);
        mMonthDayLabelPaint.setStyle(Paint.Style.FILL);
        mMonthDayLabelPaint.setTextAlign(Paint.Align.CENTER);
        mMonthDayLabelPaint.setFakeBoldText(true);

        mMonthNumPaint = new Paint();
        mMonthNumPaint.setAntiAlias(true);
        mMonthNumPaint.setTextSize(mDateTextSize);
        mMonthNumPaint.setStyle(Paint.Style.FILL);
        mMonthNumPaint.setTextAlign(Paint.Align.CENTER);
        mMonthNumPaint.setFakeBoldText(false);
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
        return false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), (getWeekHeaderSize() + mDayNumFrameHeight));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWidth = w;
    }

    /**
     * A wrapper to the MonthHeaderSize to allow override it in children
     */
    protected int getWeekHeaderSize() {
        return mWeekDayHeaderSize;
    }

    @Deprecated
    // Todo take this logic into ItemDecoration for RecyclerView
    protected void drawWeekDayLabels(Canvas canvas) {
        int y = getWeekHeaderSize() - (mDayLabelTextSize) / 2;
        int dayWidthHalf = (mWidth - mEdgePadding * 2) / (mNumDays * 2);

        for (int i = 0; i < mNumDays; i++) {
            int x = (2 * i + 1) * dayWidthHalf + mEdgePadding;
            String weekString = mDays[i].getDay().toUpperCase().substring(0, 3);
            canvas.drawText(weekString, x, y, mMonthDayLabelPaint);
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

    public void setStartDate(DateTime dateTime) {
        mStartDate = dateTime;
        getDayNumbers();
        findToday();
        invalidate();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        mWidth = right - left;
    }

    private boolean isVisibleInWeek(Day day) {
        if (day == null || mStartDate == null) {
            return false;
        }

        DateTime dateTime = day.toDateTime().withTimeAtStartOfDay();
        if (dateTime.isAfter(mStartDate.withTimeAtStartOfDay().minusDays(1)) && dateTime.isBefore(mStartDate.withTimeAtStartOfDay().plusWeeks(1))) {
            return true;
        }

        return false;
    }

    protected void findToday() {
        mHasToday = false;
        Day currentDay = mController == null ? new Day() : mController.getToday();
        for (int i = 0; i < mDays.length; i++) {
            Day tempDay = mDays[i];
            if (tempDay.equals(currentDay)) {
                mHasToday = true;
                mToday = currentDay.getDate();
            }
        }
    }
}