package controllers;

import android.view.View;

import views.WeekView;

/**
 * Created by priyabratapatnaik on 03/11/15.
 */
public interface DatePickerController {

    void onDayOfMonthSelected(View view, WeekView.Day day);

    WeekView.Day getSelectedDay();

    int getAccentColor();

    WeekView.Day[] getHighlightedDays();

    WeekView.Day[] getSelectableDays();

    int getMinYear();

    int getMaxYear();

    boolean isOutOfRange(WeekView.Day day);

    void tryVibrate();

    boolean isSelectable(WeekView.Day day);

    WeekView.Day getToday();

    WeekView.Day getStartDate();
}
