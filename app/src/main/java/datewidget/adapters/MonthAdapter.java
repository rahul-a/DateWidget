package datewidget.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ViewGroup;

import datewidget.holders.WeekViewHolder;

import java.util.Calendar;

/**
 * Created by priyabratapatnaik on 03/11/15.
 */
public class MonthAdapter extends RecyclerView.Adapter<WeekViewHolder> {

    private static final String TAG = MonthAdapter.class.getSimpleName();

    @Override
    public WeekViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(WeekViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    /**
     * A convenience class to represent a specific date.
     */
    public static class CalendarDay {
        private Calendar calendar;
        int year;
        int month;
        int day;

        public CalendarDay() {
            setTime(System.currentTimeMillis());
        }

        public CalendarDay(long timeInMillis) {
            setTime(timeInMillis);
        }

        public CalendarDay(Calendar calendar) {
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH);
            day = calendar.get(Calendar.DAY_OF_MONTH);
            Log.v(TAG, String.format("Month: %s, Day: %s, Year: %s", month, day, year));
        }

        public CalendarDay(int year, int month, int day) {
            setDay(year, month, day);
        }

        public void set(CalendarDay date) {
            year = date.year;
            month = date.month;
            day = date.day;
        }

        public void setDay(int year, int month, int day) {
            this.year = year;
            this.month = month;
            this.day = day;
        }

        private void setTime(long timeInMillis) {
            if (calendar == null) {
                calendar = Calendar.getInstance();
            }
            calendar.setTimeInMillis(timeInMillis);
            month = calendar.get(Calendar.MONTH);
            year = calendar.get(Calendar.YEAR);
            day = calendar.get(Calendar.DAY_OF_MONTH);
        }

        public int getYear() {
            return year;
        }

        public int getMonth() {
            return month;
        }

        public int getDay() {
            return day;
        }
    }
}
