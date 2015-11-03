package datewidget.controllers;

import com.sample.datewidget.fragments.DatePickerFragment;

import java.util.Calendar;

import datewidget.adapters.MonthAdapter;

/**
 * Created by priyabratapatnaik on 03/11/15.
 */
public interface DatePickerController {
    void onYearSelected(int year);

    void onDayOfMonthSelected(int year, int month, int day);

    void registerOnDateChangedListener(DatePickerFragment.OnDateChangedListener listener);

    void unregisterOnDateChangedListener(DatePickerFragment.OnDateChangedListener listener);

    MonthAdapter.CalendarDay getSelectedDay();

    boolean isThemeDark();

    int getAccentColor();

    Calendar[] getHighlightedDays();

    Calendar[] getSelectableDays();

    int getFirstDayOfWeek();

    int getMinYear();

    int getMaxYear();

    boolean isOutOfRange(int year, int month, int day);

    void tryVibrate();
}
