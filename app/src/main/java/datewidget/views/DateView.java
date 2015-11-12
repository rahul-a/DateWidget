package datewidget.views;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sample.datewidget.R;
import com.sample.datewidget.activities.DateRecycler;
import com.sample.datewidget.activities.WeekDayLabelDecoration;

import datewidget.adapters.WeekAdapter;
import datewidget.utils.Utils;

/**
 * TODO add logic to save its state on config changed
 * Created by priyabratapatnaik on 12/11/15.
 */
public class DateView extends LinearLayout {

    public interface OnWeekChangedListener {
        void onWeekChanged(int currentWeekOfWeekYear);
    }

    public DateView(Context context) {
        super(context);
        init(context, null);
    }

    public DateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public DateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    DateRecycler dateRecycler;
    TextView dateSelected;

    private void init(Context context, AttributeSet attrs) {
        LayoutParams params;
        setOrientation(VERTICAL);
        dateSelected = new TextView(context);
        params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int padding = getResources().getDimensionPixelSize(R.dimen.std_padding);
        params.setMargins(padding, 0, padding, padding);
        dateSelected.setLayoutParams(params);
        dateSelected.setPadding(0, 0, padding, padding);
        dateSelected.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimension(R.dimen.large_text_size));
        dateSelected.setTypeface(null, Typeface.BOLD);
        dateSelected.setTextColor(Utils.getAccentColorFromThemeIfAvailable(context));
        dateSelected.setId(R.id.date_selected_text);
        addView(dateSelected);

        dateRecycler = new DateRecycler(context, attrs);
        dateRecycler.setId(R.id.date_recycler_view);
        dateRecycler.setHorizontalScrollBarEnabled(false);
        dateRecycler.setHasFixedSize(true);
        params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                getResources().getDimensionPixelSize(R.dimen.calendar_frame_height));
        dateRecycler.setLayoutParams(params);
        dateRecycler.addItemDecoration(new WeekDayLabelDecoration(context, RecyclerView.VERTICAL));
        addView(dateRecycler);
    }

    public void setAdapter(RecyclerView.Adapter adapter) {
        if (!(adapter instanceof WeekAdapter)) {
            throw new IllegalArgumentException("Adapter should extend WeekAdapter");
        }
        dateRecycler.setAdapter(adapter);
    }

    public void setOnWeekChangedListener(OnWeekChangedListener listener) {
        dateRecycler.setOnWeekChangedListener(listener);
    }

    public void scrollToPresent() {
        dateRecycler.scrollToPresent();
    }

    public void scrollToDay(WeekView.Day day) {
        dateRecycler.scrollToDay(day);
    }

    public View getSelectedDateView() {
        return dateSelected;
    }
}
