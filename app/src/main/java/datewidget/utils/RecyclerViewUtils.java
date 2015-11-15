package datewidget.utils;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by priyabratapatnaik on 10/11/15.
 */
public class RecyclerViewUtils {

    public static RecyclerView.ViewHolder getViewHolderUnder(RecyclerView rv, float x, float y) {
        View childView  = rv.findChildViewUnder(x, y);
        RecyclerView.ViewHolder holder = null;
        if (childView != null) {
            holder = rv.getChildViewHolder(childView);
        }
        return holder;
    }

    private static final float DEFAULT_SLIDING_THRESHOLD = 0.22f;
    private static final int DEFAULT_VISIBLE_CHILD_COUNT = 1;

    private RecyclerView mRecyclerView;
    private float mSlidingThreshold;
    private int mVisibleChildCount;
    private int mOrientation;

    private float mHorizontalSlidingThreshold;
    private float mVerticalSlidingThreshold;

    private int mItemCenterPositionX;
    private int mItemCenterPositionY;

    private boolean mAllowFastFling = false;

    public RecyclerViewUtils(RecyclerView recyclerView) {
        mSlidingThreshold = DEFAULT_SLIDING_THRESHOLD;
        mVisibleChildCount = DEFAULT_VISIBLE_CHILD_COUNT;
        mRecyclerView = recyclerView;
    }

    private void initSlidingThreshold() {
        int itemWidth = (mRecyclerView.getWidth() - mRecyclerView.getPaddingLeft() - mRecyclerView.getPaddingRight()) / mVisibleChildCount;
        int itemHeight = (mRecyclerView.getWidth() - mRecyclerView.getPaddingLeft() - mRecyclerView.getPaddingRight()) / mVisibleChildCount;
        mHorizontalSlidingThreshold = mVisibleChildCount == 1 ? itemWidth * mSlidingThreshold : itemWidth * 0.3f;
        mVerticalSlidingThreshold = mVisibleChildCount == 1 ? itemHeight * mSlidingThreshold : itemHeight * 0.3f;
    }

    private void initCenterParentPosition() {
        mItemCenterPositionX = mRecyclerView.getLeft() + mRecyclerView.getWidth() / (mVisibleChildCount * 2);
        mItemCenterPositionY = mRecyclerView.getTop() + mRecyclerView.getHeight() / (mVisibleChildCount * 2);
    }

    public void updateConfiguration() {
        initSlidingThreshold();
        initCenterParentPosition();
    }

    public void setSlidingThreshold(float slidingThreshold) {
        mSlidingThreshold = slidingThreshold;
        initSlidingThreshold();
    }

    /**
     * Setup a display number for views to be shown
     */
    public void setVisibleChildCount(int visibleChildCount) {
        mVisibleChildCount = visibleChildCount;
        initCenterParentPosition();
    }

    public int getVisibleChildCount() {
        return mVisibleChildCount;
    }

    public void setOrientation(int orientation) {
        mOrientation = orientation;
    }

    /**
     * Get the current first visible view
     */
    public View getCurrentFirstVisibleChild() {
        int childCount = mRecyclerView.getChildCount();
        if (childCount > 0) {
            for (int i = 0; i < childCount; i++) {
                View child = mRecyclerView.getChildAt(i);
                if (isChildVisible(child)) {
                    return child;
                }
            }
        }
        return null;
    }

    private boolean isChildVisible(View child) {
        switch (mOrientation) {
            case LinearLayoutManager.HORIZONTAL:
                return child.getLeft() <= mItemCenterPositionX && child.getRight() >= mItemCenterPositionX;
            case LinearLayoutManager.VERTICAL:
                return child.getTop() <= mItemCenterPositionY && child.getBottom() >= mItemCenterPositionY;
            default:
                return false;
        }
    }

    public int getChildCurrentPosition(View child) {
        switch (mOrientation) {
            case LinearLayoutManager.HORIZONTAL:
                return child.getLeft();
            case LinearLayoutManager.VERTICAL:
                return child.getTop();
            default:
                return LinearLayoutManager.INVALID_OFFSET;
        }
    }

    public int getTargetPositionSafely(int position, int count) {
        if (position < 0) {
            return 0;
        }
        if (position >= count) {
            return count - 1;
        }
        return position;
    }

    public int getFlingCountWithVelocity(int velocityX, int velocityY) {
        switch (mOrientation) {
            case LinearLayoutManager.HORIZONTAL:
                int childWidth = (mRecyclerView.getWidth() - mRecyclerView.getPaddingLeft() - mRecyclerView.getPaddingRight()) / mVisibleChildCount;
                if (mAllowFastFling) {
                    return velocityX / childWidth;
                } else {
                    return velocityX / Math.abs(velocityX);
                }
            case LinearLayoutManager.VERTICAL:
                int childHeight = (mRecyclerView.getHeight() - mRecyclerView.getPaddingTop() - mRecyclerView.getPaddingBottom()) / mVisibleChildCount;
                if (mAllowFastFling) {
                    return velocityX / childHeight;
                } else {
                    return velocityX / Math.abs(velocityX);
                }
            default:
                return 0;
        }
    }

    public boolean isLeftScrollTriggered(float distance) {
        return mRecyclerView.canScrollHorizontally(mOrientation)
                && distance <= 0 && Math.abs(distance) >= mHorizontalSlidingThreshold;
    }

    public boolean isRightScrollTriggered(float distance) {
        return mRecyclerView.canScrollHorizontally(mOrientation)
                && distance >= 0 && Math.abs(distance) >= mHorizontalSlidingThreshold;
    }

    public void setFastFling(boolean allowFastFling) {
        mAllowFastFling = allowFastFling;
    }
}
