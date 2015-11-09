package datewidget.controllers;

import android.view.View;

import com.sample.datewidget.fragments.DatePickerFragment;

import java.util.Calendar;

import datewidget.views.WeekView;

/**
 * Created by priyabratapatnaik on 03/11/15.
 */
public interface DatePickerController {
    void onYearSelected(int year);

    void onDayOfMonthSelected(View view, WeekView.Day day);

    void registerOnDateChangedListener(DatePickerFragment.OnDateChangedListener listener);

    void unregisterOnDateChangedListener(DatePickerFragment.OnDateChangedListener listener);

    WeekView.Day getSelectedDay();

    boolean isThemeDark();

    int getAccentColor();

    WeekView.Day[] getHighlightedDays();

    Calendar[] getSelectableDays();

    int getFirstDayOfWeek();

    int getMinYear();

    int getMaxYear();

    boolean isOutOfRange(WeekView.Day day);

    void tryVibrate();

    boolean isSelectable(WeekView.Day day);

    WeekView.Day getToday();
}
