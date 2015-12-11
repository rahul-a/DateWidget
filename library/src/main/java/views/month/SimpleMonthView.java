package views.month;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.util.AttributeSet;

import controllers.DatePickerController;
import timber.log.Timber;
import views.Day;

/**
 * Created by rahul on 03/12/15.
 */
public class SimpleMonthView extends MonthView {

    public SimpleMonthView(Context context) {
        super(context);
    }

    public SimpleMonthView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SimpleMonthView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SimpleMonthView(Context context, AttributeSet attrs, DatePickerController controller) {
        super(context, attrs, controller);
    }

    @Override
    public void drawDayNum(Canvas canvas, Day day,
                           int x, int y, int startX, int stopX, int startY, int stopY) {
        Day selectedDay = mController == null ? null : mController.getSelectedDay();

        Day checkin = mController == null ? null : mController.getCheckinDay();
        Day checkout = mController == null ? null : mController.getCheckoutDay();

        if (selectedDay != null && selectedDay.equals(day)) {
            canvas.drawCircle(x , y - (mDateTextSize / 3), mSelectedDayCircleSize,
                    mSelectedCirclePaint);
        }

        boolean isCheckinEqualCheckout = checkin == checkout;

        if (isCheckinDay(day) && checkout != null && !isCheckinEqualCheckout) {
            canvas.drawCircle(x , y - (mDateTextSize / 3), mSelectedDayCircleSize,
                    mSelectedCirclePaint);
            canvas.drawRect(x, startY, stopX, stopY, mSelectedCirclePaint);
            mMonthNumPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        } else if (isCheckoutDay(day) && checkin != null && !isCheckinEqualCheckout) {
            canvas.drawRect(startX, startY, x, stopY, mSelectedCirclePaint);

            canvas.drawCircle(x , y - (mDateTextSize / 3), mSelectedDayCircleSize,
                    mSelectedCirclePaint);
            canvas.drawCircle(x, y - (mDateTextSize / 3), mHighlightEndCircleSize,
                    mHighlightEndCirclePaint);
        } else if (isHighlighted(day) && (checkin != null && checkout != null)) {
            mMonthNumPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawRect(startX, startY, stopX, stopY, mSelectedCirclePaint);
        } else {
            mMonthNumPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        }

        // If we have a mindate or maxdate, gray out the day number if it's outside the range.
        if (mController != null && mController.isOutOfRange(day)) {
            mMonthNumPaint.setColor(mDisabledDayTextColor);
        } else if (selectedDay != null && selectedDay.equals(day)) {
            mMonthNumPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            mMonthNumPaint.setColor(mSelectedDayTextColor);
            Timber.v("selected day: %s", selectedDay);
        } else if (mHasToday && mToday == day.getDate()) {
            mMonthNumPaint.setColor(mTodayNumberColor);
        } else {
            mMonthNumPaint.setColor((isHighlighted(day) || isCheckinDay(day)) ? mHighlightedDayTextColor : mDayTextColor);
            if (isCheckoutDay(day)) {
                mMonthNumPaint.setColor(mDayTextColor);
            }
        }

        canvas.drawText(String.format("%d", (day == null ? -1 : day.getDate())), x, y, mMonthNumPaint);
    }
}
