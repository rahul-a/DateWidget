package datewidget.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.Calendar;
import java.util.TimeZone;

import datewidget.holders.WeekViewHolder;
import timber.log.Timber;

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
}
