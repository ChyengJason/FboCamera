package com.jscheng.scamera.render;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES30;

import com.jscheng.scamera.R;
import com.jscheng.scamera.render.BaseRenderDrawer;
import com.jscheng.scamera.util.GlesUtil;

/**
 * Created By Chengjunsen on 2018/8/29
 */
public class WaterMarkRenderDrawer extends BaseRenderDrawer{
    private int mMarkTextureId;
    private int mInputTextureId;
    private Bitmap mBitmap;
    private int avPosition;
    private int afPosition;
    private int sTexture;

    public WaterMarkRenderDrawer(Context context) {
        mBitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.watermark);
    }
    @Override
    public void setInputTextureId(int textureId) {
        this.mInputTextureId = textureId;
    }

    @Override
    public int getOutputTextureId() {
        return mInputTextureId;
    }

    @Override
    protected void onCreated() {

    }

    @Override
    protected void onChanged(int width, int height) {
        mMarkTextureId = GlesUtil.loadBitmapTexture(mBitmap);
        avPosition = GLES30.glGetAttribLocation(mProgram, "av_Position");
        afPosition = GLES30.glGetAttribLocation(mProgram, "af_Position");
        sTexture = GLES30.glGetUniformLocation(mProgram, "sTexture");
    }

    @Override
    public void draw(long timestamp, float[] transformMatrix) {
        useProgram();
        //clear();
        viewPort(40, 75, mBitmap.getWidth() * 2, mBitmap.getHeight() * 2);
        GLES30.glDisable(GLES30.GL_DEPTH_TEST);
        GLES30.glEnable(GLES30.GL_BLEND);
        GLES30.glBlendFunc(GLES30.GL_SRC_COLOR, GLES30.GL_DST_ALPHA);
        onDraw();
        GLES30.glDisable(GLES30.GL_BLEND);
    }

    @Override
    protected void onDraw() {
        GLES30.glEnableVertexAttribArray(avPosition);
        GLES30.glEnableVertexAttribArray(afPosition);
        //设置顶点位置值
        //GLES30.glVertexAttribPointer(avPosition, CoordsPerVertexCount, GLES30.GL_FLOAT, false, VertexStride, mVertexBuffer);
        //设置纹理位置值
        //GLES30.glVertexAttribPointer(afPosition, CoordsPerTextureCount, GLES30.GL_FLOAT, false, TextureStride, mFrameTextureBuffer);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVertexBufferId);
        GLES30.glVertexAttribPointer(avPosition, CoordsPerVertexCount, GLES30.GL_FLOAT, false, 0, 0);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mFrameTextureBufferId);
        GLES30.glVertexAttribPointer(afPosition, CoordsPerTextureCount, GLES30.GL_FLOAT, false, 0, 0);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mMarkTextureId);
        GLES30.glUniform1i(sTexture, 0);
        //绘制 GLES30.GL_TRIANGLE_STRIP:复用坐标
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, VertexCount);
        GLES30.glDisableVertexAttribArray(avPosition);
        GLES30.glDisableVertexAttribArray(afPosition);
    }

    @Override
    protected String getVertexSource() {
        final String source =
                "attribute vec4 av_Position; " +
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
        final String source =
                "precision mediump float; " +
                        "varying vec2 v_texPo; " +
                        "uniform sampler2D sTexture; " +
                        "void main() { " +
                        "   gl_FragColor = texture2D(sTexture, v_texPo); " +
                        "} ";
        return source;
    }
}
