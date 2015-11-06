package datewidget.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.sample.datewidget.R;

import datewidget.views.SimpleWeekView;
import datewidget.views.WeekView;

/**
 * Created by priyabratapatnaik on 03/11/15.
 */
public class WeekViewHolder extends RecyclerView.ViewHolder {
    WeekView mWeekView;

    public WeekViewHolder(View view) {
        super(view);
        mWeekView = (SimpleWeekView) view.findViewById(R.id.week_view);
    }

    public WeekView getWeekView() {
        return mWeekView;
    }
}
