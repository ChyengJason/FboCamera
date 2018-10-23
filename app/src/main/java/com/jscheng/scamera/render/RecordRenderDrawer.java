package com.jscheng.scamera.render;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.EGL14;
import android.opengl.EGLContext;
import android.opengl.EGLSurface;
import android.opengl.GLES30;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.jscheng.scamera.R;
import com.jscheng.scamera.record.VideoEncoder;
import com.jscheng.scamera.util.EGLHelper;
import com.jscheng.scamera.util.GlesUtil;
import com.jscheng.scamera.util.StorageUtil;
import java.io.File;
import java.io.IOException;

import static com.jscheng.scamera.util.LogUtil.TAG;

/**
 * Created By Chengjunsen on 2018/9/21
 */
public class RecordRenderDrawer extends BaseRenderDrawer implements Runnable{
    // 绘制的纹理 ID
    private int mTextureId;
    private VideoEncoder mVideoEncoder;
    private String mVideoPath;
    private Handler mMsgHandler;
    private EGLHelper mEglHelper;
    private EGLSurface mEglSurface;
    private boolean isRecording;
    private EGLContext mEglContext;

    private int av_Position;
    private int af_Position;
    private int s_Texture;


    public RecordRenderDrawer(Context context) {
        this.mVideoEncoder = null;
        this.mEglHelper = null;
        this.mTextureId = 0;
        this.isRecording = false;
        new Thread(this).start();
    }

    @Override
    public void setInputTextureId(int textureId) {
        this.mTextureId = textureId;
        Log.d(TAG, "setInputTextureId: " + textureId);
    }

    @Override
    public int getOutputTextureId() {
        return mTextureId;
    }

    @Override
    public void create() {
        mEglContext = EGL14.eglGetCurrentContext();
    }

    public void startRecord() {
        Log.d(TAG, "startRecord context : " + mEglContext.toString());
        Message msg = mMsgHandler.obtainMessage(MsgHandler.MSG_START_RECORD, width, height, mEglContext);
        mMsgHandler.sendMessage(msg);
        isRecording = true;
    }

    public void stopRecord() {
        Log.d(TAG, "stopRecord");
        isRecording = false;
        mMsgHandler.sendMessage(mMsgHandler.obtainMessage(MsgHandler.MSG_STOP_RECORD));
    }

    public void quit() {
        mMsgHandler.sendMessage(mMsgHandler.obtainMessage(MsgHandler.MSG_QUIT));
    }

    @Override
    public void surfaceChangedSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void draw(long timestamp, float[] transformMatrix) {
        if (isRecording) {
            Log.d(TAG, "draw: ");
            Message msg = mMsgHandler.obtainMessage(MsgHandler.MSG_FRAME, timestamp);
            mMsgHandler.sendMessage(msg);
        }
    }

    @Override
    public void run() {
        Looper.prepare();
        mMsgHandler = new MsgHandler();
        Looper.loop();
    }

    private class MsgHandler extends Handler {
        public static final int MSG_START_RECORD = 1;
        public static final int MSG_STOP_RECORD = 2;
        public static final int MSG_UPDATE_CONTEXT = 3;
        public static final int MSG_UPDATE_SIZE = 4;
        public static final int MSG_FRAME = 5;
        public static final int MSG_QUIT = 6;

        public MsgHandler() {

        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_START_RECORD:
                    prepareVideoEncoder((EGLContext) msg.obj, msg.arg1, msg.arg2);
                    break;
                case MSG_STOP_RECORD:
                    stopVideoEncoder();
                    break;
                case MSG_UPDATE_CONTEXT:
                    updateEglContext((EGLContext) msg.obj);
                    break;
                case MSG_UPDATE_SIZE:
                    updateChangedSize(msg.arg1, msg.arg2);
                    break;
                case MSG_FRAME:
                    drawFrame((long)msg.obj);
                    break;
                case MSG_QUIT:
                    quitLooper();
                    break;
                default:
                    break;
            }
        }
    }

    private void prepareVideoEncoder(EGLContext context, int width, int height) {
        try {
            mEglHelper = new EGLHelper();
            mEglHelper.createGL(context);
            mVideoPath = StorageUtil.getVedioPath(true) + "glvideo.mp4";
            mVideoEncoder = new VideoEncoder(width, height, new File(mVideoPath));
            mEglSurface = mEglHelper.createWindowSurface(mVideoEncoder.getInputSurface());
            boolean error = mEglHelper.makeCurrent(mEglSurface);
            if (!error) {
                Log.e(TAG, "prepareVideoEncoder: make current error");
            }
            onCreated();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopVideoEncoder() {
        mVideoEncoder.drainEncoder(true);
        if (mEglHelper != null) {
            mEglHelper.destroySurface(mEglSurface);
            mEglHelper.destroyGL();
            mEglSurface = EGL14.EGL_NO_SURFACE;
            mVideoEncoder.release();
            mEglHelper = null;
            mVideoEncoder = null;
        }
    }

    private void updateEglContext(EGLContext context) {
        mEglSurface = EGL14.EGL_NO_SURFACE;
        mEglHelper.destroyGL();
        mEglHelper.createGL(context);
        mEglSurface = mEglHelper.createWindowSurface(mVideoEncoder.getInputSurface());
        boolean error = mEglHelper.makeCurrent(mEglSurface);
        if (!error) {
            Log.e(TAG, "prepareVideoEncoder: make current error");
        }
    }

    private void drawFrame(long timeStamp) {
        Log.d(TAG, "drawFrame: " + timeStamp );
        mEglHelper.makeCurrent(mEglSurface);
        mVideoEncoder.drainEncoder(false);
        onDraw();
        mEglHelper.setPresentationTime(mEglSurface, timeStamp);
        mEglHelper.swapBuffers(mEglSurface);
    }

    private void updateChangedSize(int width, int height) {
        onChanged(width, height);
    }

    private void quitLooper() {
        Looper.myLooper().quit();
    }

    @Override
    protected void onCreated() {
        mProgram = GlesUtil.createProgram(getVertexSource(), getFragmentSource());
        initVertexBufferObjects();
        av_Position = GLES30.glGetAttribLocation(mProgram, "av_Position");
        af_Position = GLES30.glGetAttribLocation(mProgram, "af_Position");
        s_Texture = GLES30.glGetUniformLocation(mProgram, "s_Texture");
        Log.d(TAG, "onCreated: av_Position " + av_Position);
        Log.d(TAG, "onCreated: af_Position " + af_Position);
        Log.d(TAG, "onCreated: s_Texture " + s_Texture);
        Log.e(TAG, "onCreated: error " + GLES30.glGetError());
    }

    @Override
    protected void onChanged(int width, int height) {

    }

    @Override
    protected void onDraw() {
        clear();
        useProgram();
        viewPort(0, 0, width, height);

        GLES30.glEnableVertexAttribArray(av_Position);
        GLES30.glEnableVertexAttribArray(af_Position);
//        GLES30.glVertexAttribPointer(av_Position, CoordsPerVertexCount, GLES30.GL_FLOAT, false, VertexStride, mVertexBuffer);
//        GLES30.glVertexAttribPointer(af_Position, CoordsPerTextureCount, GLES30.GL_FLOAT, false, TextureStride, mDisplayTextureBuffer);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVertexBufferId);
        GLES30.glVertexAttribPointer(av_Position, CoordsPerVertexCount, GLES30.GL_FLOAT, false, 0, 0);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mDisplayTextureBufferId);
        GLES30.glVertexAttribPointer(af_Position, CoordsPerTextureCount, GLES30.GL_FLOAT, false, 0, 0);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mTextureId);
        GLES30.glUniform1i(s_Texture, 0);
        // 绘制 GLES30.GL_TRIANGLE_STRIP:复用坐标
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, VertexCount);
        GLES30.glDisableVertexAttribArray(av_Position);
        GLES30.glDisableVertexAttribArray(af_Position);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
    }

    @Override
    protected String getVertexSource() {
        final String source = "attribute vec4 av_Position; " +
                "attribute vec2 af_Position; " +
                "varying vec2 v_texPo; " +
                "void main() { " +
                "    v_texPo = af_Position; " +
                "    gl_Position = av_Position; " +
                "}";
        return source;
    }

    @Override
    protected String getFragmentSource() {
        final String source = "precision mediump float;\n" +
                "varying vec2 v_texPo;\n" +
                "uniform sampler2D s_Texture;\n" +
                "void main() {\n" +
                "   vec4 tc = texture2D(s_Texture, v_texPo);\n" +
                "   gl_FragColor = texture2D(s_Texture, v_texPo);\n" +
                "}";
        return source;
    }
}
