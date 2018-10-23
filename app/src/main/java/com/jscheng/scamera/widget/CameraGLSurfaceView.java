package com.jscheng.scamera.widget;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import com.jscheng.scamera.render.CameraSurfaceRender;
import java.nio.ByteBuffer;

/**
 * Created By Chengjunsen on 2018/8/25
 */
public class CameraGLSurfaceView extends GLSurfaceView implements CameraSurfaceRender.CameraSufaceRenderCallback{
    private CameraSurfaceRender mRender;
    private CameraGLSurfaceViewCallback mCallback;

    public CameraGLSurfaceView(Context context) {
        super(context, null);
    }

    public CameraGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        setEGLContextClientVersion(3);
        setDebugFlags(GLSurfaceView.DEBUG_CHECK_GL_ERROR);
        mRender = new CameraSurfaceRender(context);
        mRender.setCallback(this);
        this.setRenderer(mRender);
        this.setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    public SurfaceTexture getSurfaceTexture() {
        return mRender.getCameraSurfaceTexture();
    }

    @Override
    public void onRequestRender() {
        requestRender();
    }

    @Override
    public void onCreate() {
        if (mCallback != null) {
            mCallback.onSurfaceViewCreate(getSurfaceTexture());
        }
    }

    @Override
    public void onChanged(int width, int height) {
        if (mCallback != null) {
            mCallback.onSurfaceViewChange(width, height);
        }
    }

    @Override
    public void onDraw() {

    }

    public void setCallback(CameraGLSurfaceViewCallback mCallback) {
        this.mCallback = mCallback;
    }

    public void releaseSurfaceTexture() {
        mRender.releaseSurfaceTexture();
    }

    public void resumeSurfaceTexture() {
        mRender.resumeSurfaceTexture();
    }

    public void startRecord() {
        mRender.startRecord();
    }

    public void stopRecord() {
        mRender.stopRecord();
    }

    public interface CameraGLSurfaceViewCallback {
        void onSurfaceViewCreate(SurfaceTexture texture);
        void onSurfaceViewChange(int width, int height);
    }
}
