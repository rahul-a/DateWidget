package views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.example.library.R;

/**
 * Created by rahul on 04/12/15.
 */
public class MonthControllerStatus extends View {

    private Paint mTopLabelPaint;
    private Paint mDatePaint;
    private Paint mDayPaint;
    private Paint mMonthLabelPaint;

    private int mWidth;
    private int mControllerStatusHeight;
    private int mEdgeMargin;

    public MonthControllerStatus(Context context) {
        this(context, null);
    }

    public MonthControllerStatus(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MonthControllerStatus(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Resources res = getResources();

        mTopLabelPaint = new Paint();
        mTopLabelPaint.setTextSize(res.getDimension(R.dimen.month_controller_extra_label_size));
        mTopLabelPaint.setAntiAlias(true);
        mTopLabelPaint.setColor(res.getColor(R.color.dark_gray));

        mDatePaint = new Paint();
        mDatePaint.setTextSize(res.getDimension(R.dimen.month_controller_month_date_size));
        mDatePaint.setAntiAlias(true);
        mDatePaint.setColor(res.getColor(R.color.dark_gray));

        mDayPaint = new Paint();
        mDayPaint.setTextSize(res.getDimension(R.dimen.month_controller_month_day_size));
        mDayPaint.setAntiAlias(true);
        mDayPaint.setColor(res.getColor(R.color.dark_gray));

        mMonthLabelPaint = new Paint();
        mMonthLabelPaint.setTextSize(res.getDimension(R.dimen.month_controller_month_label_size));
        mMonthLabelPaint.setAntiAlias(true);
        mMonthLabelPaint.setColor(res.getColor(R.color.dark_gray));

        init();
    }

    private void init() {
        mEdgeMargin = (int) getResources().getDimension(R.dimen.std_padding);
        mControllerStatusHeight = (int) getResources().getDimension(R.dimen.month_controller_status_height);
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        mWidth = w;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), (mControllerStatusHeight + 2 * mEdgeMargin));
    }

    @Override
    public void onDraw(Canvas canvas) {

    }
}
