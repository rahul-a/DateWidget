package views.month;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

import com.example.library.R;

import org.joda.time.DateTime;

import controllers.DatePickerController;
import views.Day;
import views.WeekView;

/**
 * Created by rahul on 03/12/15.
 */
public abstract class MonthView extends WeekView {

    public interface OnDayClickListener {
        void onDayClick(Mode mode, Day day);
    }

    private static final String TAG = MonthView.class.getSimpleName();

    protected static final int SELECTED_CIRCLE_ALPHA = 255;

    public enum Mode {
        CHECK_IN(0), CHECK_OUT(1);

        private final int code;

        Mode(int i) {
            this.code = i;
        }
    }
    private Mode mMode = Mode.CHECK_IN;

    protected int mHighlightEndCircleSize;
    protected Paint mHighlightEndCirclePaint;
    private OnDayClickListener mOnDayClickListener;

    public MonthView(Context context) {
        this(context, null, null);
    }

    public MonthView(Context context, AttributeSet attrs) {
        this(context, attrs, null);
    }

    public MonthView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, null);
    }

    public MonthView(Context context, AttributeSet attrs, DatePickerController controller) {
        super(context, attrs);

        Resources res = context.getResources();

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.weekView);

        mHighlightEndCircleSize = typedArray.getDimensionPixelSize(R.styleable.weekView_selected_circle_size,
                res.getDimensionPixelSize(R.dimen.day_number_highlight_end_select_circle_size));

        mMaxRows = 6;

        mDayNumFrameHeight = (res.getDimensionPixelOffset(R.dimen.month_row_height)) * mMaxRows;
    }

    /**
     * Draws the week and month day numbers for this week. Override this method
     * if you need different placement.
     *
     * @param canvas The canvas to draw on
     */
    protected void drawMonth(Canvas canvas) {
        int y = getWeekHeaderSize() + mDayNumFrameHeight / mMaxRows;
        final float dayWidthHalf = (mWidth - mEdgePadding * 2) / (mNumDays * 2.0f);
        int offset = getOffset();
            int j = offset;
            for (int dayNumber = 0; dayNumber < mDays.length; dayNumber++) {
                final int x = (int)((2 * j + 1) * dayWidthHalf + mEdgePadding);
                int yRelativeToDay = mDateTextSize / 3;
                final int startX = (int)(x - dayWidthHalf - 1);
                final int stopX = (int)(x + dayWidthHalf);
                final int startY = (y - yRelativeToDay  - mSelectedDayCircleSize);
                final int stopY = (y - yRelativeToDay + mSelectedDayCircleSize);
                drawDayNum(canvas, mDays[dayNumber], x, y, startX, stopX, startY, stopY);
                j++;
                if (j == 7) {
                    j = 0;
                    y += mDayNumFrameHeight / mMaxRows + mDaySeparatorWidth;
                }
            }
    }

    @Override
    protected void drawWeekDate(Canvas canvas, Day day, int x, int y, int startX, int stopX, int startY, int stopY) {
        try {
            throw new IllegalAccessException("MonthView shouldn't attempt to draw week dates");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    protected int getOffset() {
        if (mStartDate == null) {
            mStartDate = new DateTime();
        }
        return mStartDate.withDayOfMonth(1).getDayOfWeek() - mStartDate.dayOfWeek().getMinimumValue();
    }

    @Override
    protected int[] getDayNumbers() {
        DateTime startDate;
        if (mStartDate != null) {
            startDate = mStartDate;
        } else {
            startDate = new DateTime().withDayOfMonth(1);
        }
        int maxDays = startDate.dayOfMonth().withMaximumValue().getDayOfMonth();
        int[] days = new int[maxDays];

        mDays = new Day[maxDays];

        DateTime endDate = startDate.plusMonths(1);

        int i = 0;
        while (startDate.isBefore(endDate)) {
            if (i < maxDays) {
                mDays[i] = new Day(startDate);
                days[i++] = startDate.getDayOfMonth();
            }
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
    public abstract void drawDayNum(Canvas canvas, Day day, int x, int y, int startX, int stopX, int startY, int stopY);

    @Override
    protected void onDraw(Canvas canvas) {
        // drawWeekDayLabels(canvas);
        drawMonth(canvas);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Day getInternalDayFromLocation(float x, float y) {
        int dayStart = mEdgePadding;
        int rowHeight = mDayNumFrameHeight / mMaxRows;
        if (x < dayStart || x > mWidth - mEdgePadding) {
            return null;
        }

        int column = (int) ((x - dayStart) * mNumDays / (mWidth - (2 * mEdgePadding)));
        int row = (int) (y - getWeekHeaderSize()) / rowHeight - 1;
        if (column < 0 || column > mNumCells) {
            return null;
        }
        int dayNumber = column - getOffset();
        dayNumber += row * mNumCells;
        if (dayNumber < 0 || dayNumber >= mDays.length) {
            return null;
        }
        return mDays[dayNumber];
    }

    /**
     * Called when the user clicks on a day. Handles callbacks to the
     * {@link DatePickerController#onDayOfMonthSelected(View, Day)} if one is set.
     * <p/>
     * If the day is out of the range set by minDate and/or maxDate, this is a no-op.
     *
     * @param day The day that was clicked
     */
    @Override
    public void onDayClick(Day day) {
        if (mController == null) {
            return;
        }

        // If the min / max date are set, only process the click if it's a valid selection.
        if (mController.isOutOfRange(day)) {
            return;
        }

        mSelectedDay = day;
        invalidate();

        if (mController != null) {
            if (mMode == Mode.CHECK_IN) {
                mController.onCheckinDaySelected(this, day);
            } else if (mMode == Mode.CHECK_OUT){
                mController.onCheckoutDaySelected(this, day);
            }
        }

        if (mOnDayClickListener != null) {
            mOnDayClickListener.onDayClick(mMode, day);
        }
    }

    /**
     * Sets up the text and style properties for painting. Override this if you
     * want to use a different paint.
     */
    @Override
    protected void initView() {
        super.initView();

        mHighlightEndCirclePaint = new Paint();
        mHighlightEndCirclePaint.setAntiAlias(true);
        mHighlightEndCirclePaint.setFakeBoldText(true);
        mHighlightEndCirclePaint.setAntiAlias(true);
        mHighlightEndCirclePaint.setColor(mSelectedDayTextColor);
        mHighlightEndCirclePaint.setTextAlign(Paint.Align.CENTER);
        mHighlightEndCirclePaint.setStyle(Paint.Style.FILL);
        mHighlightEndCirclePaint.setAlpha(SELECTED_CIRCLE_ALPHA);
    }

    /**
     * @param day
     * @return true if the given date should be highlighted
     */
    @Override
    protected boolean isHighlighted(@NonNull Day day) {
        if (mController == null) {
            return false;
        }

        Day checkoutDay = mController.getCheckoutDay();
        Day checkinDay = mController.getCheckinDay();

        if (checkinDay != null && checkoutDay != null) {
            DateTime checkinTime = checkinDay.toDateTime().withTimeAtStartOfDay();
            DateTime checkoutTime = checkoutDay.toDateTime().withTimeAtStartOfDay();
            DateTime curTime = day.toDateTime().withTimeAtStartOfDay();
            if (curTime.isAfter(checkinTime) && curTime.isBefore(checkoutTime)) {
                return true;
            }
        }
        return false;
    }

    protected boolean isCheckoutDay(Day day) {
        Day checkoutDay = mController != null ? mController.getCheckoutDay() : null;
        if (checkoutDay == null) {
            return false;
        }
        return checkoutDay.equals(day);
    }

    protected boolean isCheckinDay(Day day) {
        Day checkinDay = mController != null ? mController.getCheckinDay() : null;
        if (checkinDay == null) {
            return false;
        }

        return checkinDay.equals(day);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), (getWeekHeaderSize() + mDayNumFrameHeight));
    }

    @Override
    public void setStartDate(DateTime dateTime) {
        mStartDate = dateTime.withDayOfMonth(1);
        getDayNumbers();
        findToday();
        invalidate();
    }

    public Mode getMode() {
        return mMode;
    }

    public void setMode(Mode mode) {
        this.mMode = mode;
    }

    public void setOnDayClickListener(OnDayClickListener listener) {
        mOnDayClickListener = listener;
    }
}