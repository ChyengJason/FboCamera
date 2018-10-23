package com.jscheng.scamera.util;


import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import static com.jscheng.scamera.util.LogUtil.TAG;

/**
 * Created By Chengjunsen on 2018/8/23
 */
public class CameraUtil {
    private static Camera mCamera = null;
    private static int mCameraID = Camera.CameraInfo.CAMERA_FACING_BACK;

    /**
     * 检查camera硬件
     * @param context
     * @return
     */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            return true;
        } else {
            return false;
        }
    }

    public static void openCamera() {
        mCamera = Camera.open(mCameraID);
        if (mCamera == null) {
            throw new RuntimeException("Unable to open camera");
        }
    }

    public static Camera getCamera() {
        return mCamera;
    }

    public static void releaseCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    public static void switchCameraId() {
        mCameraID = isBackCamera() ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK;
    }

    public static boolean isBackCamera() {
        return mCameraID == Camera.CameraInfo.CAMERA_FACING_BACK;
    }

    public static void setDisplay(SurfaceTexture surfaceTexture) {
        try {
            if (mCamera != null) {
                mCamera.setPreviewTexture(surfaceTexture);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void startPreview(Activity activity, int width, int height) {
        if (mCamera != null) {
            int mOrientation = getCameraPreviewOrientation(activity, mCameraID);
            mCamera.setDisplayOrientation(mOrientation);

            Camera.Parameters parameters = mCamera.getParameters();
            Camera.Size bestPreviewSize = getOptimalSize(parameters.getSupportedPreviewSizes(), width, height);
            parameters.setPreviewSize(bestPreviewSize.width, bestPreviewSize.height);
            Camera.Size bestPictureSize = getOptimalSize(parameters.getSupportedPictureSizes(), width, height);
            parameters.setPictureSize(bestPictureSize.width, bestPictureSize.height);
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            mCamera.setParameters(parameters);
            mCamera.startPreview();
            Log.e(TAG, "camera startPreview: (" + width + " x " + height +")");
        }
    }

    /**
     * 获取最合适的尺寸
     * @param supportList
     * @param width
     * @param height
     * @return
     */
    private static Camera.Size getOptimalSize(List<Camera.Size> supportList, int width, int height) {
        // camera的宽度是大于高度的，这里要保证expectWidth > expectHeight
        int expectWidth = Math.max(width, height);
        int expectHeight = Math.min(width, height);
        // 根据宽度进行排序
        Collections.sort(supportList, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size pre, Camera.Size after) {
                if (pre.width > after.width) {
                    return 1;
                } else if (pre.width < after.width) {
                    return -1;
                }
                return 0;
            }
        });

        Camera.Size result = supportList.get(0);
        boolean widthOrHeight = false; // 判断存在宽或高相等的Size
        // 辗转计算宽高最接近的值
        for (Camera.Size size: supportList) {
            // 如果宽高相等，则直接返回
            if (size.width == expectWidth && size.height == expectHeight) {
                result = size;
                break;
            }
            // 仅仅是宽度相等，计算高度最接近的size
            if (size.width == expectWidth) {
                widthOrHeight = true;
                if (Math.abs(result.height - expectHeight)
                        > Math.abs(size.height - expectHeight)) {
                    result = size;
                }
            }
            // 高度相等，则计算宽度最接近的Size
            else if (size.height == expectHeight) {
                widthOrHeight = true;
                if (Math.abs(result.width - expectWidth)
                        > Math.abs(size.width - expectWidth)) {
                    result = size;
                }
            }
            // 如果之前的查找不存在宽或高相等的情况，则计算宽度和高度都最接近的期望值的Size
            else if (!widthOrHeight) {
                if (Math.abs(result.width - expectWidth)
                        > Math.abs(size.width - expectWidth)
                        && Math.abs(result.height - expectHeight)
                        > Math.abs(size.height - expectHeight)) {
                    result = size;
                }
            }
        }
        return result;
    }

    public static int getCameraPreviewOrientation(Activity activity, int cameraId) {
        if (mCamera == null) {
            throw new RuntimeException("mCamera is null");
        }
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int result;
        int degrees = getRotation(activity);
        //前置
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        }
        //后置
        else {
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }

    /**
     * 对焦
     * @param focusPoint 焦点位置
     * @param screenSize 屏幕尺寸
     * @param callback 对焦成功或失败的callback
     * @return
     */
    public static boolean newCameraFocus(Point focusPoint, Size screenSize, Camera.AutoFocusCallback callback) {
        if (mCamera == null) {
            throw new RuntimeException("mCamera is null");
        }
        Point cameraFoucusPoint = convertToCameraPoint(screenSize, focusPoint);
        Rect cameraFoucusRect = convertToCameraRect(cameraFoucusPoint, 100);
        Camera.Parameters parameters = mCamera.getParameters();
        if (Build.VERSION.SDK_INT > 14) {
            if (parameters.getMaxNumFocusAreas() <= 0) {
                return focus(callback);
            }
            clearCameraFocus();
            List<Camera.Area> focusAreas = new ArrayList<Camera.Area>();
            // 100是权重
            focusAreas.add(new Camera.Area(cameraFoucusRect, 100));
            parameters.setFocusAreas(focusAreas);
            // 设置感光区域
            parameters.setMeteringAreas(focusAreas);
            try {
                mCamera.setParameters(parameters);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            } 
        }
        return focus(callback);
    }

    private static boolean focus(Camera.AutoFocusCallback callback) {
        if (mCamera == null) {
            return false;
        }
        mCamera.cancelAutoFocus();
        mCamera.autoFocus(callback);
        return true;
    }

    /**
     * 清除焦点
     */
    public static void clearCameraFocus() {
        if (mCamera == null) {
            throw new RuntimeException("mCamera is null");
        }
        mCamera.cancelAutoFocus();
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setFocusAreas(null);
        parameters.setMeteringAreas(null);
        try {
            mCamera.setParameters(parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 将屏幕坐标转换成camera坐标
     * @param screenSize
     * @param focusPoint
     * @return cameraPoint
     */
    private static Point convertToCameraPoint(Size screenSize, Point focusPoint){
        int newX = focusPoint.y * 2000/screenSize.getHeight() - 1000;
        int newY = -focusPoint.x * 2000/screenSize.getWidth() + 1000;
        return new Point(newX, newY);
    }

    private static Rect convertToCameraRect(Point centerPoint, int radius) {
        int left = limit(centerPoint.x - radius, 1000, -1000);
        int right = limit(centerPoint.x + radius, 1000, -1000);
        int top = limit(centerPoint.y - radius, 1000, -1000);
        int bottom = limit(centerPoint.y + radius, 1000, -1000);
        return new Rect(left, top, right, bottom);
    }

    private static int limit(int s, int max, int min) {
        if (s > max) { return max; }
        if (s < min) { return min; }
        return s;
    }

    public static int getRotation(Activity activity) {
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        return degrees;
    }
}
