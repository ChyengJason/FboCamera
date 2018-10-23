package com.jscheng.scamera.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;

/**
 * Created By Chengjunsen on 2018/8/24
 */
public class CameraSwitchView extends android.support.v7.widget.AppCompatImageView {
    private static final int ORIENTATION_UP = 0;
    private static final int ORIENTATION_BOTTOM = 180;
    private static final int ORIENTATION_LEFT = 90;
    private static final int ORIENTATION_RIGHT = 270;
    private Context mContext;
    public CameraSwitchView(Context context) {
        super(context);
        init(context);
    }

    public CameraSwitchView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CameraSwitchView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
    }

    public void setOrientation(int x, int y, int z) {
        int type = getOrientationPosition(x, y, z);
        this.setRotation(type);
    }

    private int getOrientationPosition(int x, int y, int z) {
        if (Math.abs(x) > Math.abs(y)) { //横屏倾斜
            if (x > 4) { //左边倾斜
                return ORIENTATION_LEFT;
            } else if (x < -4) { //右边倾斜
                return ORIENTATION_RIGHT;
            } else {
                return ORIENTATION_UP;
            }
        } else {
            if (y > 7) { // 左边倾斜
                return ORIENTATION_UP;
            } else if (y < -7) { //右边倾斜
                return ORIENTATION_BOTTOM;
            } else {
                return ORIENTATION_UP;
            }
        }
    }
}
