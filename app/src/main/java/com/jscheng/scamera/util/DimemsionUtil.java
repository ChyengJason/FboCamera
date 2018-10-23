package com.jscheng.scamera.util;

import android.content.Context;

/**
 * Created By Chengjunsen on 2018/8/24
 */
public class DimemsionUtil {
    public static int dp2px(Context context, float dpValue){
        float scale=context.getResources().getDisplayMetrics().density;
        return (int)(dpValue*scale + 0.5f);
    }

    public static int px2dp(Context context,float pxValue){
        float scale=context.getResources().getDisplayMetrics().density;
        return (int)(pxValue/scale + 0.5f);
    }

    public static int sp2px(Context context,float spValue){
        float fontScale=context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue*fontScale + 0.5f);
    }

    public static int px2sp(Context context,float pxValue){
        float fontScale=context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue/fontScale + 0.5f);
    }
}
