package controllers;

import android.view.View;

import views.Day;

/**
 * Created by priyabratapatnaik on 03/11/15.
 */
public interface DatePickerController {

    void onDayOfMonthSelected(View view, Day day);

    void onCheckinDaySelected(View view, Day day);

    void onCheckoutDaySelected(View view, Day day);

    Day getSelectedDay();

    int getAccentColor();

    Day[] getHighlightedDays();

    Day[] getSelectableDays();

    int getMinYear();

    int getMaxYear();

    boolean isOutOfRange(Day day);

    void tryVibrate();

    boolean isSelectable(Day day);

    Day getToday();

    Day getStartDate();

    Day getCheckinDay();

    Day getCheckoutDay();
}
