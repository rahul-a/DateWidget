package com.sample.datewidget.activities;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.sample.datewidget.R;

import org.joda.time.Days;

import datewidget.adapters.WeekAdapter;
import datewidget.holders.WeekViewHolder;
import datewidget.views.WeekView;
import timber.log.Timber;

/**
 * Created by priyabratapatnaik on 10/11/15.
 */
public class DateRecycler extends RecyclerView {

    private static final float DEFAULT_FLING_FRICTION = 0.7f;

    private float mFlingFriction;
    private int mPositionBeforeDragging;
    private int mSmoothScrollTargetPosition;
    private int mCurrentPosition;
    private boolean mNeedAdjustAfterScrollStopped;

    private View mCurrentChildView;
    private RecyclerViewUtils mRecyclerViewUtils;
    private boolean mHasUpdatedSnappyRecyclerViewHelper;

    private RecyclerViewUtils.OnPageChangedListener mOnPageChangedListener;

    private Paint mMonthDayLabelPaint = new Paint();

    public DateRecycler(Context context) {
        super(context);
        init(context);
    }

    public DateRecycler(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mRecyclerViewUtils = new RecyclerViewUtils(this);
        CustomLayoutManager layoutManager = new CustomLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        setLayoutManager(layoutManager);
        mFlingFriction = (1.0f - DEFAULT_FLING_FRICTION);
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);

        mMonthDayLabelPaint.setAntiAlias(true);
        mMonthDayLabelPaint.setTextSize(context.getResources().getDimensionPixelSize(R.dimen.mdtp_month_day_label_text_size));
        mMonthDayLabelPaint.setColor(context.getResources().getColor(R.color.mdtp_red));
        mMonthDayLabelPaint.setStyle(Paint.Style.FILL);
        mMonthDayLabelPaint.setTextAlign(Paint.Align.CENTER);
        mMonthDayLabelPaint.setFakeBoldText(true);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (!mHasUpdatedSnappyRecyclerViewHelper) {
            mHasUpdatedSnappyRecyclerViewHelper = true;
            mRecyclerViewUtils.updateConfiguration();
        }
    }

    @Override
    public boolean fling(int velocityX, int velocityY) {
        boolean isFlinging = super.fling((int) (velocityX * mFlingFriction), (int) (velocityY * mFlingFriction));
        if (isFlinging) {
            adjustPositionWithVelocity((int) (velocityX * mFlingFriction), (int) (velocityY * mFlingFriction));
        }
        return isFlinging;
    }

    @Override
    public void smoothScrollToPosition(int position) {
        if (mOnPageChangedListener != null && mCurrentPosition != NO_POSITION && mSmoothScrollTargetPosition != position) {
            mOnPageChangedListener.onPageChanged(position);
        }
        mCurrentPosition = mSmoothScrollTargetPosition = position;
        super.smoothScrollToPosition(position);
    }

    // 1.SCROLL_STATE_DRAGGING -> SCROLL_STATE_IDLE (When the user let go, the view did not scroll)
    // 2.SCROLL_STATE_DRAGGING -> SCROLL_STATE_SETTLING (It will trigger onFling method) ->
    // SCROLL_STATE_IDLE (When the user let go, the view is still scrolling)
    //
    // After triggered onFling methods will be conducted in the method ChildView position offset.
    // And will trigger SCROLL_STATE_SETTLING state - so once entered SCROLL_STATE_SETTLING state
    //
    // It indicates that the ChildView has shifted a good position, then SCROLL_STATE_IDLE time, it does not deal with the position shifted
    // smoothScrollToPosition Triggers SCROLL_STATE_SETTLING state
    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
        switch (state) {
            case SCROLL_STATE_DRAGGING:
                mCurrentChildView = mRecyclerViewUtils.getCurrentFirstVisibleChild();
                mCurrentPosition = getChildAdapterPosition(mCurrentChildView);
                if (mCurrentChildView != null) {
                    mPositionBeforeDragging = mRecyclerViewUtils.getChildCurrentPosition(mCurrentChildView);
                }
                mNeedAdjustAfterScrollStopped = true;
                break;
            case SCROLL_STATE_SETTLING:
                mNeedAdjustAfterScrollStopped = false;
                break;
            case SCROLL_STATE_IDLE:
                if (mNeedAdjustAfterScrollStopped) {
                    if (mCurrentChildView != null) {
                        float draggingDistance = mRecyclerViewUtils.getChildCurrentPosition(mCurrentChildView) - mPositionBeforeDragging;
                        if (mRecyclerViewUtils.isRightScrollTriggered(draggingDistance)) {
                            mCurrentPosition--;
                        } else if (mRecyclerViewUtils.isLeftScrollTriggered(draggingDistance)) {
                            mCurrentPosition++;
                        }

                        int safeTargetPosition = mRecyclerViewUtils.getTargetPositionSafely(mCurrentPosition, getAdapter().getItemCount());
                        smoothScrollToPosition(safeTargetPosition);
                    }
                }
                break;
        }
    }

    private void adjustPositionWithVelocity(int velocityX, int velocityY) {
        if (getChildCount() > 0) {
            int flingCount = mRecyclerViewUtils.getFlingCountWithVelocity(velocityX, velocityY);
            int safeTargetPosition = mRecyclerViewUtils.getTargetPositionSafely(mCurrentPosition + flingCount, getAdapter().getItemCount());
            smoothScrollToPosition(safeTargetPosition);
        }
    }

    public void setOnPageChangedListener(RecyclerViewUtils.OnPageChangedListener listener) {
        mOnPageChangedListener = listener;
    }

    public void scrollToPresent() {
        scrollToDay(new WeekView.Day());
    }

    public void scrollToDay(final WeekView.Day day) {
        if (day == null) {
            return;
        }
        final int position = day.toDateTime().getWeekOfWeekyear() - 1;
        scrollToPosition(position);
        post(new Runnable() {
            @Override
            public void run() {
                WeekViewHolder holder = (WeekViewHolder) findViewHolderForAdapterPosition(position);
                if (holder != null) {
                    holder.getWeekView().onDayClick(day);
                } else {
                    Timber.v("holder is null");
                }
            }
        });
    }
}
