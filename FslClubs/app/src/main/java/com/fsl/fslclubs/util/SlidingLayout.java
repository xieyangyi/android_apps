package com.fsl.fslclubs.util;

import android.content.Context;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;

/**
 * Created by B47714 on 12/3/2015.
 */
public class SlidingLayout extends LinearLayout implements View.OnTouchListener {
    public final static int SNAP_VELOCITY = 400;
    private int screenWidth;
    private int leftEdge;           // the min leftMargin of left view
    private int rightEdge = 0;      // the max leftMargin of left view
    private int leftLayoutPadding;
    private float xDown;
    private float xMove;
    private float xUp;
    private boolean isLeftLayoutVisible;
    private View leftLayout;
    private View rightLayout;
    private View mBindView;
    private MarginLayoutParams leftLayoutParams;
    private MarginLayoutParams rightLayoutParams;
    private VelocityTracker mVelocityTracker;

    public SlidingLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        screenWidth = manager.getDefaultDisplay().getWidth();
    }

    public void setScrollEvent(View bindView) {
        mBindView = bindView;
        mBindView.setOnTouchListener(this);
    }

    public void scrollToLeftLayout() {
        new ScrollTask().execute(60);
    }

    public void scrollToRightlayout() {
        new ScrollTask().execute(-60);
    }

    public boolean isLeftLayoutVisible() {
        return isLeftLayoutVisible;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed) {
            leftLayout = getChildAt(0);
            leftLayoutParams = (MarginLayoutParams) leftLayout.getLayoutParams();
            leftLayoutPadding = (int) (0.4 * screenWidth);
            leftLayoutParams.width = screenWidth - leftLayoutPadding;
            leftEdge = -leftLayoutParams.width;
            leftLayoutParams.leftMargin = leftEdge;
            leftLayout.setLayoutParams(leftLayoutParams);

            rightLayout = getChildAt(1);
            rightLayoutParams = (MarginLayoutParams) rightLayout.getLayoutParams();
            rightLayoutParams.width = screenWidth;
            rightLayout.setLayoutParams(rightLayoutParams);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        createVelocityTracker(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                xDown = event.getRawX();
                break;
            case MotionEvent.ACTION_MOVE:
                xMove = event.getRawX();
                int distanceX = (int) (xMove - xDown);
                if (isLeftLayoutVisible) {
                    leftLayoutParams.leftMargin = distanceX;
                } else {
                    leftLayoutParams.leftMargin = leftEdge + distanceX;
                }
                if (leftLayoutParams.leftMargin < leftEdge) {
                    leftLayoutParams.leftMargin = leftEdge;
                } else if (leftLayoutParams.leftMargin > rightEdge) {
                    leftLayoutParams.leftMargin = rightEdge;
                }

                if (Math.abs(distanceX) > 0.1 * screenWidth) {
                    leftLayout.setLayoutParams(leftLayoutParams);
                }
                break;
            case MotionEvent.ACTION_UP:
                xUp = event.getRawX();
                if (wantToShowLeftLayout()) {
                    if (shouldScrollToLeftLayout()) {
                        scrollToLeftLayout();
                    } else {
                        scrollToRightlayout();
                    }
                } else if (wantToShowRightlayout()) {
                    if (shouldScrollToConnect()) {
                        scrollToRightlayout();
                    } else {
                        scrollToLeftLayout();
                    }
                }
                recycleVelocityTracker();
                break;
        }

        return isBindBasicLayout();
    }

    private boolean wantToShowLeftLayout() {
        return (xUp - xDown > 0) && !isLeftLayoutVisible;
    }

    private boolean wantToShowRightlayout() {
        return (xUp - xDown < 0) && isLeftLayoutVisible;
    }

    private boolean shouldScrollToLeftLayout() {
        return (xUp - xDown > screenWidth / 2) || (getScrollVelocity() > SNAP_VELOCITY);
    }

    private boolean shouldScrollToConnect() {
        return (xDown - xUp + leftLayoutPadding > screenWidth / 2)
                || (getScrollVelocity() > SNAP_VELOCITY);
    }

    private boolean isBindBasicLayout() {
        if (mBindView == null) {
            return false;
        }
        String viewName = mBindView.getClass().getName();
        return viewName.equals(LinearLayout.class.getName())
                || viewName.equals(RelativeLayout.class.getName())
                || viewName.equals(FrameLayout.class.getName())
                || viewName.equals(TableLayout.class.getName())
                || viewName.equals(GridLayout.class.getName());
    }

    private void createVelocityTracker(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }

        mVelocityTracker.addMovement(event);
    }

    private int getScrollVelocity() {
        mVelocityTracker.computeCurrentVelocity(1000);      // velocity in each seconod
        int velocity = (int) mVelocityTracker.getXVelocity();
        return Math.abs(velocity);
    }

    private void recycleVelocityTracker() {
        mVelocityTracker.recycle();
        mVelocityTracker = null;
    }

    class ScrollTask extends AsyncTask<Integer, Integer, Integer> {
        @Override
        protected Integer doInBackground(Integer... params) {
            int speed = params[0];
            int leftMargin = leftLayoutParams.leftMargin;
            while (true) {
                leftMargin += speed;
                if (leftMargin > rightEdge) {
                    leftMargin = rightEdge;
                    break;
                }
                if (leftMargin < leftEdge) {
                    leftMargin = leftEdge;
                    break;
                }
                publishProgress(leftMargin);
                sleep(20);
            }
            if (speed > 0) {
                isLeftLayoutVisible = true;
            } else {
                isLeftLayoutVisible = false;
            }
            return leftMargin;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            leftLayoutParams.leftMargin = values[0];
            leftLayout.setLayoutParams(leftLayoutParams);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            leftLayoutParams.leftMargin = integer;
            leftLayout.setLayoutParams(leftLayoutParams);
        }
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
