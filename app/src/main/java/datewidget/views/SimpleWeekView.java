package datewidget.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.util.AttributeSet;

import datewidget.controllers.DatePickerController;

/**
 * Created by priyabratapatnaik on 03/11/15.
 */
public class SimpleWeekView extends WeekView {

    public SimpleWeekView(Context context, AttributeSet attr, DatePickerController controller) {
        super(context, attr, controller);
    }

    public SimpleWeekView(Context context, AttributeSet attrs) {
        super(context, attrs, null);
    }

    public SimpleWeekView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void drawWeekDate(Canvas canvas, Day day,
                             int x, int y, int startX, int stopX, int startY, int stopY) {
        WeekView.Day selectedDay = mController.getSelectedDay();

        if (selectedDay != null && selectedDay.equals(day)) {
            canvas.drawCircle(x , y - (MINI_DAY_NUMBER_TEXT_SIZE / 3), DAY_SELECTED_CIRCLE_SIZE,
                    mSelectedCirclePaint);
        }

        if (isHighlighted(day)) {
            mMonthNumPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        } else {
            mMonthNumPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        }

        // If we have a mindate or maxdate, gray out the day number if it's outside the range.
        if (mController != null && mController.isOutOfRange(day)) {
            mMonthNumPaint.setColor(mDisabledDayTextColor);
        } else if (selectedDay != null && selectedDay.equals(day)) {
            mMonthNumPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            mMonthNumPaint.setColor(mSelectedDayTextColor);
        } else if (mHasToday && mToday == day.getDate()) {
            mMonthNumPaint.setColor(mTodayNumberColor);
        } else if (mWeekStartDay != null && mWeekStartDay.equals(day)) {
            mMonthNumPaint.setColor(mTodayNumberColor);
        } else {
            mMonthNumPaint.setColor(isHighlighted(day) ? mHighlightedDayTextColor : mDayTextColor);
        }

        canvas.drawText(String.format("%d", (day == null ? -1 : day.getDate())), x, y, mMonthNumPaint);
    }
}
