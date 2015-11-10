package com.sample.datewidget.activities;

import android.content.Context;
import android.graphics.PointF;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;

/**
 * Created by priyabratapatnaik on 09/11/15.
 */
public class CustomLayoutManager extends LinearLayoutManager {
    private static final float MILLISECONDS_PER_INCH = 280f;
    private DateSmoothScroller mSmoothScroller;

    public CustomLayoutManager(Context context) {
        super(context);
        mSmoothScroller = new DateSmoothScroller(context);
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, final int position) {
        computeScrollVectorForPosition(position);
        mSmoothScroller.setTargetPosition(position);
        startSmoothScroll(mSmoothScroller);
    }

    @Override
    public void startSmoothScroll(RecyclerView.SmoothScroller smoothScroller) {
        super.startSmoothScroll(smoothScroller);
    }

    public class DateSmoothScroller extends LinearSmoothScroller {

        public DateSmoothScroller(Context context) {
            super(context);
        }

        //This controls the direction in which smoothScroll looks for your view
        @Override
        public PointF computeScrollVectorForPosition(int targetPosition) {
            return CustomLayoutManager.this.computeScrollVectorForPosition(targetPosition);
        }

        //This returns the milliseconds it takes to scroll one pixel.
        @Override
        protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
            return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
        }

        @Override
        protected int getHorizontalSnapPreference() {
            return SNAP_TO_START;
        }
    }
 }
