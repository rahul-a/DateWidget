package views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.example.library.R;

import java.util.ArrayList;

import controllers.DatePickerController;
import timber.log.Timber;
import utils.RecyclerViewUtils;
import utils.SmoothLinearLayoutManager;

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

    private ArrayList<DateView.OnWeekChangedListener> mOnWeekChangedListeners = new ArrayList<>();

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
        SmoothLinearLayoutManager layoutManager = new SmoothLinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        setLayoutManager(layoutManager);
        mFlingFriction = (1.0f - DEFAULT_FLING_FRICTION);
        setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);

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

        // Dispatching events to multiple listeners
        if (mOnWeekChangedListeners != null && mCurrentPosition != RecyclerView.NO_POSITION && mSmoothScrollTargetPosition != position) {

            DateView.WeekAdapter weekAdapter = (DateView.WeekAdapter) getAdapter();
            int yearFromPos = weekAdapter.getYearForWeekPosition(position);
            int correctPosition = weekAdapter.getTranslatedWeekPosition(position, yearFromPos);

            for(DateView.OnWeekChangedListener listener: mOnWeekChangedListeners)
                listener.onWeekChanged(correctPosition, yearFromPos);
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
            case RecyclerView.SCROLL_STATE_DRAGGING:
                mCurrentChildView = mRecyclerViewUtils.getCurrentFirstVisibleChild();
                mCurrentPosition = getChildAdapterPosition(mCurrentChildView);
                if (mCurrentChildView != null) {
                    mPositionBeforeDragging = mRecyclerViewUtils.getChildCurrentPosition(mCurrentChildView);
                }
                mNeedAdjustAfterScrollStopped = true;
                break;
            case RecyclerView.SCROLL_STATE_SETTLING:
                mNeedAdjustAfterScrollStopped = false;
                break;
            case RecyclerView.SCROLL_STATE_IDLE:
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

    public void addOnWeekChangedListener(DateView.OnWeekChangedListener listener) {
        mOnWeekChangedListeners.add(listener);
    }

    public void scrollToPresent() {
        scrollToDay(new Day());
    }

    public void scrollToDay(final Day day) {
        if (getAdapter() == null || !(getAdapter() instanceof DateView.WeekAdapter)) {
            throw new IllegalStateException("Must call setAdapter() before scrolling to a day");
        }
        if (day == null) {
            return;
        }
        final DateView.WeekAdapter weekAdapter = (DateView.WeekAdapter) getAdapter();
        int weekOfWeekyear = day.toDateTime().getWeekOfWeekyear();
        final int actualPosition = weekAdapter.getWeekPositionInAdapter(weekOfWeekyear, day.getYear());
        if (actualPosition >= 0 && actualPosition < getAdapter().getItemCount()) {
            scrollToPosition(actualPosition);
            post(new Runnable() {
                @Override
                public void run() {
                    DateView.WeekAdapter.WeekViewHolder holder = (DateView.WeekAdapter.WeekViewHolder) findViewHolderForAdapterPosition(actualPosition);
                    if (holder != null) {
                        holder.getWeekView().onDayClick(day);
                    }
                }
            });
        } else {
            // If present date isn't found in the adapter then scroll to the start date
            Timber.e("Couldn't scroll to position: %s", actualPosition);
            scrollToDay(weekAdapter.getStartDay());
        }
    }

    public Day getSelectedDay() {
        if (getAdapter() == null || !(getAdapter() instanceof DateView.WeekAdapter)) {
            throw new IllegalStateException("Must call setAdapter() before scrolling to a day");
        }
        Day selectedDay = null;
        DateView.WeekAdapter weekAdapter = (DateView.WeekAdapter) getAdapter();
        DatePickerController controller = weekAdapter.getDateController();
        if (controller != null) {
            selectedDay = controller.getSelectedDay();
        }
        return selectedDay;
    }

    /**
     * Created by priyabratapatnaik on 10/11/15.
     */
    public static class WeekDayLabelDecoration extends ItemDecoration {

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
        public void onDrawOver(Canvas c, RecyclerView parent, State state) {
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
                Day[] days = null;
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
}
