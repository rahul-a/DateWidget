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

    @Override
    public void drawWeekDate(Canvas canvas, Day day,
                             int x, int y, int startX, int stopX, int startY, int stopY) {
        if (mSelectedDay != null && mSelectedDay.equals(day)) {
            canvas.drawCircle(x , y - (MINI_DAY_NUMBER_TEXT_SIZE / 3), DAY_SELECTED_CIRCLE_SIZE,
                    mSelectedCirclePaint);
        }

        if (isHighlighted(day)) {
            mMonthNumPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        } else {
            mMonthNumPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        }

        // If we have a mindate or maxdate, gray out the day number if it's outside the range.
        if (mController.isOutOfRange(day)) {
            mMonthNumPaint.setColor(mDisabledDayTextColor);
        } else if (mSelectedDay != null && mSelectedDay.equals(day)) {
            mMonthNumPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            mMonthNumPaint.setColor(mSelectedDayTextColor);
        } else if (mHasToday && mToday == day.getDate()) {
            mMonthNumPaint.setColor(mTodayNumberColor);
        } else {
            mMonthNumPaint.setColor(isHighlighted(day) ? mHighlightedDayTextColor : mDayTextColor);
        }

        canvas.drawText(String.format("%d", (day == null ? -1 : day.getDate())), x, y, mMonthNumPaint);
    }
}
