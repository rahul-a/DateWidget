package views.month;

import android.content.Context;
import android.util.AttributeSet;

import controllers.DatePickerController;
import timber.log.Timber;
import views.DateRecycler;
import views.DateView;
import views.Day;

/**
 * Created by rahul on 04/12/15.
 */
public class MonthRecycler extends DateRecycler {

    private MonthDateView.OnMonthChangedListener mOnMonthChangedListener;

    public MonthRecycler(Context context) {
        super(context);
    }

    public MonthRecycler(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void scrollToDay(final Day day) {
        if (getAdapter() == null || !(getAdapter() instanceof MonthDateView.MonthAdapter)) {
            throw new IllegalStateException("Must call setAdapter() before scrolling to a day");
        }
        if (day == null) {
            return;
        }
        final MonthDateView.MonthAdapter monthAdapter = (MonthDateView.MonthAdapter) getAdapter();
        int monthOfYear = day.getMonth();
        final int actualPosition = monthAdapter.getMonthPositionInAdapter(monthOfYear, day.getYear());
        if (actualPosition >= 0 && actualPosition < getAdapter().getItemCount()) {
            scrollToPosition(actualPosition);
            post(new Runnable() {
                @Override
                public void run() {
                    MonthDateView.MonthAdapter.MonthViewHolder holder = (MonthDateView.MonthAdapter.MonthViewHolder)
                            findViewHolderForAdapterPosition(actualPosition);
                    if (holder != null) {
                        holder.getMonthView().onDayClick(day);
                    }
                }
            });
        } else {
            // If present date isn't found in the adapter then scroll to the start date
            Timber.e("Couldn't scroll to position: %s", actualPosition);
            scrollToDay(monthAdapter.getStartDay());
        }
    }

    @Override
    public Day getSelectedDay() {
        if (getAdapter() == null || !(getAdapter() instanceof MonthDateView.MonthAdapter)) {
            throw new IllegalStateException("Must call setAdapter() before scrolling to a day");
        }
        Day selectedDay = null;
        MonthDateView.MonthAdapter monthAdapter = (MonthDateView.MonthAdapter) getAdapter();
        DatePickerController controller = monthAdapter.getDateController();
        if (controller != null) {
            selectedDay = controller.getSelectedDay();
        }
        return selectedDay;
    }

    public void setMode(MonthView.Mode mode) {
        if (getAdapter() == null || !(getAdapter() instanceof MonthDateView.MonthAdapter)) {
            throw new IllegalStateException("Must call setAdapter() before scrolling to a day");
        }
        MonthDateView.MonthAdapter monthAdapter = (MonthDateView.MonthAdapter) getAdapter();
        monthAdapter.setMode(mode);
    }

    public MonthView.Mode getMode() {
        if (getAdapter() == null || !(getAdapter() instanceof MonthDateView.MonthAdapter)) {
            throw new IllegalStateException("Must call setAdapter() before scrolling to a day");
        }
        MonthDateView.MonthAdapter monthAdapter = (MonthDateView.MonthAdapter) getAdapter();
        return monthAdapter.getMode();
    }

    public void setOnWeekChangedListener(DateView.OnWeekChangedListener listener) {
        throw new IllegalAccessError("Set OnMonthChangeListener instead");
    }

    public void setOnMonthChangedListener(MonthDateView.OnMonthChangedListener listener) {
        mOnMonthChangedListener = listener;
    }
}
