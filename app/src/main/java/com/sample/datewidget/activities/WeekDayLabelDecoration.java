package com.sample.datewidget.activities;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.sample.datewidget.R;

import java.util.Arrays;

import datewidget.views.WeekView;
import timber.log.Timber;

/**
 * Created by priyabratapatnaik on 10/11/15.
 */
public class WeekDayLabelDecoration extends RecyclerView.ItemDecoration {

    public static final int VERTICAL_LIST = LinearLayoutManager.VERTICAL;

    private int mOrientation;
    private Paint mMonthDayLabelPaint = new Paint();

    public WeekDayLabelDecoration(Context context, int orientation) {
        setOrientation(orientation);

        mMonthDayLabelPaint.setAntiAlias(true);
        mMonthDayLabelPaint.setTextSize(context.getResources().getDimensionPixelSize(R.dimen.mdtp_month_day_label_text_size));
        mMonthDayLabelPaint.setColor(context.getResources().getColor(R.color.mdtp_red));
        mMonthDayLabelPaint.setStyle(Paint.Style.FILL);
        mMonthDayLabelPaint.setTextAlign(Paint.Align.CENTER);
        mMonthDayLabelPaint.setFakeBoldText(true);
    }

    public void setOrientation(int orientation) {
        if (orientation != VERTICAL_LIST) {
            throw new IllegalArgumentException("invalid orientation");
        }
        mOrientation = orientation;
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (mOrientation == VERTICAL_LIST) {
            drawVertical(c, parent);
        }
    }

    public void drawVertical(Canvas c, RecyclerView parent) {
        final int left = parent.getPaddingLeft();
        final int right = parent.getWidth() - parent.getPaddingRight();

        final int childCount = parent.getChildCount();
            int dayWidthHalf = ((right - left)) / (7 * 2);
        Resources res = parent.getResources();
        int weekHeaderSize = res.getDimensionPixelOffset(R.dimen.mdtp_month_list_item_header_height);
        int dayLabelTextSize = res.getDimensionPixelSize(R.dimen.mdtp_day_number_size);
        int y = weekHeaderSize - (dayLabelTextSize) / 2;

        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            View childView = child.findViewById(R.id.week_view);
            WeekView.Day[] days = null;
            if (days == null && childView instanceof WeekView) {
                WeekView weekView = (WeekView) childView;
                days = weekView.getDaysInWeek();
            }

            if (days != null) {
                for (int j = 0; j < days.length; j++) {
                    int x = (2 * j + 1) * dayWidthHalf;
                    String weekString = days[j].getDay().toUpperCase().substring(0, 3);
                    c.drawText(weekString, x, y, mMonthDayLabelPaint);
                }
            }
        }
    }
}