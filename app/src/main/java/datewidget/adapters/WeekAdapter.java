package datewidget.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sample.datewidget.R;

import org.joda.time.DateTime;

import datewidget.controllers.DatePickerController;
import datewidget.holders.WeekViewHolder;
import datewidget.views.WeekView;
import timber.log.Timber;

/**
 * TODO test against future dates
 * Created by priyabratapatnaik on 03/11/15.
 */
public class WeekAdapter extends RecyclerView.Adapter<WeekViewHolder> {

    private static final String TAG = WeekAdapter.class.getSimpleName();
    private DatePickerController mController;
    private DateTime mDateTime;
    private int mWeekCount;
    private int mOffset;

    public WeekAdapter(DatePickerController controller) {
        mController = controller;
        mDateTime = new DateTime();
        int presentYear = mDateTime.getYear();
        mDateTime = mController == null ? new DateTime() : mController.getStartDate().toDateTime();
        mOffset = presentYear - mDateTime.getYear();
        DateTime dateTime = mDateTime.weekOfWeekyear().setCopy(1);
        if (mOffset > 0) {
            mWeekCount += dateTime.weekOfWeekyear().withMaximumValue().getWeekOfWeekyear();
            for (int i = mOffset; i > 0; i--) {
                mWeekCount += dateTime.plusYears(i).weekOfWeekyear().withMaximumValue().getWeekOfWeekyear();
            }
            mWeekCount++;
        } else {
            mWeekCount += dateTime.weekOfWeekyear().withMaximumValue().getWeekOfWeekyear();
            for (int i = Math.abs(mOffset); i > 0; i--) {
                mWeekCount += dateTime.minusYears(i).weekOfWeekyear().withMaximumValue().getWeekOfWeekyear();
            }
        }
    }

    @Override
    public WeekViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.week_view_layout, parent, false);
        view.setLayoutParams(new RecyclerView.LayoutParams(parent.getMeasuredWidth(), RecyclerView.LayoutParams.MATCH_PARENT));
        return new WeekViewHolder(view);
    }

    @Override
    public void onBindViewHolder(WeekViewHolder holder, int position) {
        WeekView weekView = holder.getWeekView();
        weekView.setController(mController);
        weekView.setClickable(true);

        int actualYear = getYearForWeekPosition(position);
        int weekPos = getTranslatedWeekPosition(position, actualYear);
        DateTime dateTime = mDateTime.withYear(actualYear).withWeekyear(actualYear).withWeekOfWeekyear(weekPos).withDayOfWeek(1);
        Timber.v("Pos %s, Year %s, week %s", position, mDateTime.getYear(), mDateTime.withYear(actualYear).withWeekyear(actualYear).withWeekOfWeekyear(weekPos).withDayOfWeek(1));
        weekView.setStartDate(dateTime);
    }

    private int getTranslatedWeekPosition(int position, int year) {
        int maxWeeks = mDateTime.withYear(year).weekOfWeekyear().withMaximumValue().getWeekOfWeekyear();
        int translatedPos = (position + 1) % maxWeeks;
        if (translatedPos == 0) {
            translatedPos += maxWeeks;
        }
        // Timber.v("Translated pos %s", translatedPos);
        return translatedPos;
    }

    private int getYearForWeekPosition(int position) {
        int startYear = mDateTime.getYear();
        int actualYear = startYear;
        if (mOffset > 0) {
            int maxWeeks = mDateTime.weekOfWeekyear().withMaximumValue().getWeekOfWeekyear();
            if (position <= maxWeeks - 1) {
                return startYear;
            } else {
                for (int i = 1; i <= mOffset; i++) {
                    maxWeeks += mDateTime.withYear(startYear + i).weekOfWeekyear().withMaximumValue().getWeekOfWeekyear();
                    actualYear = startYear + i;
                    if (position <= maxWeeks - 1) {
                        break;
                    }
                }
            }
        }
        // Timber.v("Returning year %s", actualYear);
        return actualYear;
    }

    @Override
    public int getItemCount() {
        return mWeekCount;
    }
}