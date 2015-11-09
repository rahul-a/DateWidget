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
    private static int sHolderCount = 0;
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
        sHolderCount++;
        return new WeekViewHolder(view);
    }

    @Override
    public void onBindViewHolder(WeekViewHolder holder, int position) {
        Timber.v("BindView for Position: %s, count %s", position, sHolderCount);

        Object tag = holder.itemView.getTag();
        WeekView weekView = holder.getWeekView();
        weekView.setController(mController);
        weekView.setClickable(true);

        HashMap<String, Integer> drawingParams;
        if (tag == null) {
            drawingParams = new HashMap<>();
            holder.itemView.setTag(drawingParams);
        } else {
            drawingParams = (HashMap<String, Integer>) tag;
            drawingParams.clear();
        }

        DateTime dateTime = mDateTime.withWeekyear(mDateTime.getYear()).withWeekOfWeekyear(position + 1).withDayOfWeek(1);
        int year = dateTime.getYear();
        int month = dateTime.dayOfWeek().withMinimumValue().getMonthOfYear();
        int day = dateTime.dayOfWeek().withMinimumValue().getDayOfMonth();

        Timber.v("day: %s, month: %s, year: %s", day, month, year);

        /*if (mController.isSelectable(mSelectedDay)) {
            drawingParams.put(WeekView.VIEW_PARAMS_SELECTED_DAY, mSelectedDay.getDate());
        }*/

        drawingParams.put(WeekView.VIEW_PARAMS_YEAR, year);
        drawingParams.put(WeekView.VIEW_PARAMS_MONTH, month);
        drawingParams.put(WeekView.VIEW_PARAMS_DATE, day);
        drawingParams.put(WeekView.VIEW_PARAMS_WEEK_START, mController.getFirstDayOfWeek());

        weekView.setMonthParams(drawingParams);
        weekView.invalidate();
    }

    @Override
    public int getItemCount() {
        return mWeekCount;
    }
}
