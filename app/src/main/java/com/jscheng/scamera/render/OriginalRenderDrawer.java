package com.jscheng.scamera.render;

import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES30;

import com.jscheng.scamera.render.BaseRenderDrawer;
import com.jscheng.scamera.util.CameraUtil;
import com.jscheng.scamera.util.GlesUtil;

/**
 * Created By Chengjunsen on 2018/8/27
 */
public class OriginalRenderDrawer extends BaseRenderDrawer {
    private int av_Position;
    private int af_Position;
    private int s_Texture;
    private int mInputTextureId;
    private int mOutputTextureId;

    @Override
    protected void onCreated() {
    }

    @Override
    protected void onChanged(int width, int height) {
        mOutputTextureId = GlesUtil.createFrameTexture(width, height);

        av_Position = GLES30.glGetAttribLocation(mProgram, "av_Position");
        af_Position = GLES30.glGetAttribLocation(mProgram, "af_Position");
        s_Texture = GLES30.glGetUniformLocation(mProgram, "s_Texture");
    }

    @Override
    protected void onDraw() {
        if (mInputTextureId == 0 || mOutputTextureId == 0) {
            return;
        }
        GLES30.glEnableVertexAttribArray(av_Position);
        GLES30.glEnableVertexAttribArray(af_Position);
        //GLES30.glVertexAttribPointer(av_Position, CoordsPerVertexCount, GLES30.GL_FLOAT, false, VertexStride, mVertexBuffer);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVertexBufferId);
        GLES30.glVertexAttribPointer(av_Position, CoordsPerVertexCount, GLES30.GL_FLOAT, false, 0, 0);
        if (CameraUtil.isBackCamera()) {
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mBackTextureBufferId);
        } else {
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mFrontTextureBufferId);
        }
        GLES30.glVertexAttribPointer(af_Position, CoordsPerTextureCount, GLES30.GL_FLOAT, false, 0, 0);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
        bindTexture(mInputTextureId);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, VertexCount);
        unBindTexure();
        GLES30.glDisableVertexAttribArray(av_Position);
        GLES30.glDisableVertexAttribArray(af_Position);
    }

    private void bindTexture(int textureId) {
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        GLES30.glUniform1i(s_Texture, 0);
    }

    private void unBindTexure() {
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
    }

    @Override
    public void setInputTextureId(int textureId) {
        mInputTextureId = textureId;
    }

    @Override
    public int getOutputTextureId() {
        return mOutputTextureId;
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
        final String source = "#extension GL_OES_EGL_image_external : require \n" +
                "precision mediump float; " +
                "varying vec2 v_texPo; " +
                "uniform samplerExternalOES s_Texture; " +
                "void main() { " +
                "   gl_FragColor = texture2D(s_Texture, v_texPo); " +
                "} ";
        return source;
    }
}
