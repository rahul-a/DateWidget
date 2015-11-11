package datewidget.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sample.datewidget.R;

import org.joda.time.DateTime;

import java.util.HashMap;

import datewidget.controllers.DatePickerController;
import datewidget.holders.WeekViewHolder;
import datewidget.views.WeekView;
import timber.log.Timber;

/**
 * Created by priyabratapatnaik on 03/11/15.
 */
public class WeekAdapter extends RecyclerView.Adapter<WeekViewHolder> {

    private static final String TAG = WeekAdapter.class.getSimpleName();
    private DatePickerController mController;
    private DateTime mDateTime;
    int mWeekCount;

    public WeekAdapter(DatePickerController controller) {
        mController = controller;
        mDateTime = new DateTime();
        mWeekCount = mDateTime.weekOfWeekyear().withMaximumValue().getWeekOfWeekyear();
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

        DateTime dateTime = mDateTime.withWeekyear(mDateTime.getYear()).withWeekOfWeekyear(position + 1).withDayOfWeek(1);

        weekView.setStartDate(dateTime);
    }

    @Override
    public int getItemCount() {
        return mWeekCount;
    }
}
