package com.jscheng.scamera.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.jscheng.scamera.R;

/**
 * Created By Chengjunsen on 2018/8/22
 */
public class CameraProgressButton extends View{
    private int mMaxProgress = 10000; // 默认10s
    private Paint mBgPaint;
    private Paint mStrokePaint;
    private RectF mRectF;
    private int progress;
    private int mCircleColor;
    private int mCircleLineColor;
    private Handler mTouchHandler;
    private Listener mListener;

    public CameraProgressButton(Context context) {
        super(context);
        init(context, null);
    }

    public CameraProgressButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context,@Nullable AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.camera_progress_button);
        mCircleColor = typedArray.getColor(R.styleable.camera_progress_button_circle, Color.RED);
        mCircleLineColor = typedArray.getColor(R.styleable.camera_progress_button_circle_line, Color.BLACK);

        mStrokePaint = new Paint();
        mBgPaint = new Paint();
        mRectF = new RectF();
        progress = 0;
        mTouchHandler = new InnerTouchHandler();
        mListener = null;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = Math.min(getWidth(), getHeight());
        int radius = progress == 0 ? width/3 : width/2;
        int mStrokeWidth = width / 10;

        int centerX = width/2;
        int centerY = width/2;

        float progressSweepAngle = 0;
        if (progress > 0 && progress < mMaxProgress) {
            progressSweepAngle = ((float) progress / mMaxProgress) * 360;
        } else if (progress >= mMaxProgress) {
            progressSweepAngle = 360;
        }

        // 设置画笔相关属性
        mStrokePaint.setAntiAlias(true);
        mStrokePaint.setStrokeWidth(mStrokeWidth);
        mStrokePaint.setStyle(Paint.Style.STROKE);
        mStrokePaint.setColor(mCircleLineColor);

        // 位置
        mRectF.left = centerX - radius + mStrokeWidth/2;
        mRectF.top = centerY - radius + mStrokeWidth/2;
        mRectF.right = centerX + radius - mStrokeWidth/2;
        mRectF.bottom = centerY + radius - mStrokeWidth/2;

        // 实心圆
        mBgPaint.setAntiAlias(true);
        mBgPaint.setStrokeWidth(mStrokeWidth);
        mBgPaint.setStyle(Paint.Style.FILL);
        mBgPaint.setColor(mCircleColor);

        canvas.drawCircle(centerX, centerY, radius - mStrokeWidth, mBgPaint);
        canvas.drawArc(mRectF, -90, progressSweepAngle, false, mStrokePaint);
    }

    public void setMaxProgress(int maxProgress) {
        this.mMaxProgress = maxProgress;
    }

    public void setProgress(int progress) {
        this.progress = progress > mMaxProgress ? mMaxProgress : progress;
        invalidate();
    }

    public int getProgress() {
        return progress;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchHandler.sendEmptyMessage(InnerTouchHandler.ACTION_DOWN);
                return true;
            case MotionEvent.ACTION_UP:
                mTouchHandler.sendEmptyMessage(InnerTouchHandler.ACTION_UP);
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    public interface Listener {
        void onShortPress();
        void onStartLongPress();
        void onEndLongPress();
        void onEndMaxProgress();
    }

    public void setProgressListener(Listener mListener) {
        this.mListener = mListener;
    }

    private class InnerTouchHandler extends Handler {
        public static final int ACTION_DOWN = 1;
        public static final int ACTION_UP = 2;
        public static final int SCHEDULE_PRESSING = 3;
        public int LONG_PRESS_DURATION = 300;
        public int EACH_DURATION = 100;

        private boolean isPress = false;
        public InnerTouchHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ACTION_DOWN:
                    if (!isPress) {
                        isPress = true;
                        sendEmptyMessageDelayed(SCHEDULE_PRESSING, LONG_PRESS_DURATION);
                    }
                    break;
                case ACTION_UP:
                    if (isPress) {
                        if (mListener != null) {
                            if (getProgress() == 0) {
                                mListener.onShortPress();
                            } else {
                                mListener.onEndLongPress();
                            }
                        }
                        isPress = false;
                        setProgress(0);
                    }
                    break;
                case SCHEDULE_PRESSING:
                    if (isPress) {
                        int endProgress = getProgress() + EACH_DURATION;
                        if (mListener != null) {
                            if (getProgress() == 0) {
                                mListener.onStartLongPress();
                            } else if (endProgress >= mMaxProgress){
                                mListener.onEndMaxProgress();
                            }
                        }
                        setProgress(endProgress);
                        sendEmptyMessageDelayed(SCHEDULE_PRESSING, EACH_DURATION);
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
