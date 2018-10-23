package com.jscheng.scamera.view;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Created By Chengjunsen on 2018/8/24
 */
public class CameraSensor implements SensorEventListener{
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private int lastX, lastY, lastZ;
    private CameraSensorListener mCameraSensorListener;

    public CameraSensor(Context context) {
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mCameraSensorListener = null;
        reset();
    }
    /**
     * 方向改变时会调用
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor == null) {
            return;
        }
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            int x = (int) sensorEvent.values[0];
            int y = (int) sensorEvent.values[1];
            int z = (int) sensorEvent.values[2];
            int px = Math.abs(lastX - x);
            int py = Math.abs(lastY - y);
            int pz = Math.abs(lastZ - z);
            lastX = x;
            lastY = y;
            lastZ = z;

            if (px > 2.5 || py > 2.5 || pz > 2.5) {
                if (mCameraSensorListener != null) {
                    mCameraSensorListener.onRock();
                }
            }
        }
    }

    /**
     * 精度改变会调用
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void start() {
        // samplingPeriodUs：指定获取传感器频率, SensorManager.SENSOR_DELAY_NORMAL：正常频率
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        reset();
    }

    public void stop() {
        mSensorManager.unregisterListener(this, mSensor);
    }

    private void reset() {
        lastX = 0;
        lastY = 0;
        lastZ = 0;
    }

    public void setCameraSensorListener(CameraSensorListener listener) {
        mCameraSensorListener = listener;
    }

    public int getX() {
        return lastX;
    }

    public int getY() {
        return lastY;
    }

    public int getZ() {
        return lastZ;
    }

    public interface CameraSensorListener {
        void onRock();
    }
}
