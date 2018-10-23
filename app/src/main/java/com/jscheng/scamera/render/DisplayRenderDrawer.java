package com.jscheng.scamera.render;

import android.opengl.GLES30;

import com.jscheng.scamera.render.BaseRenderDrawer;

/**
 * Created By Chengjunsen on 2018/8/27
 */
public class DisplayRenderDrawer extends BaseRenderDrawer {
    private int av_Position;
    private int af_Position;
    private int s_Texture;
    private int mTextureId;

    @Override
    protected void onCreated() {
    }

    @Override
    protected void onChanged(int width, int height) {
        av_Position = GLES30.glGetAttribLocation(mProgram, "av_Position");
        af_Position = GLES30.glGetAttribLocation(mProgram, "af_Position");
        s_Texture = GLES30.glGetUniformLocation(mProgram, "s_Texture");
    }

    @Override
    protected void onDraw() {
        GLES30.glEnableVertexAttribArray(av_Position);
        GLES30.glEnableVertexAttribArray(af_Position);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVertexBufferId);
        GLES30.glVertexAttribPointer(av_Position, CoordsPerVertexCount, GLES30.GL_FLOAT, false, 0, 0);
//        GLES30.glVertexAttribPointer(av_Position, CoordsPerVertexCount, GLES30.GL_FLOAT, false, VertexStride, mVertexBuffer);
        // 用GPU中的缓冲数据，不再RAM中取数据，所以后2个参数为0
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mDisplayTextureBufferId);
        GLES30.glVertexAttribPointer(af_Position, CoordsPerTextureCount, GLES30.GL_FLOAT, false, 0, 0);
//        GLES30.glVertexAttribPointer(af_Position, CoordsPerTextureCount, GLES30.GL_FLOAT, false, TextureStride, mDisplayTextureBuffer);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mTextureId);
        GLES30.glUniform1i(s_Texture, 0);
        // 绘制 GLES30.GL_TRIANGLE_STRIP:复用坐标
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, VertexCount);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
        GLES30.glDisableVertexAttribArray(af_Position);
        GLES30.glDisableVertexAttribArray(av_Position);
    }

    private void bind2DTexture(int textureId, int textureType) {
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0 + textureType);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId);
        GLES30.glUniform1i(s_Texture, textureType);
    }

    private void unBind2DTexure() {
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
    }

    @Override
    public void setInputTextureId(int textureId) {
        this.mTextureId = textureId;
    }

    @Override
    public int getOutputTextureId() {
        return mTextureId;
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
                "   float color = tc.r * 0.3 + tc.g * 0.59 + tc.b * 0.11;\n" +
                "   gl_FragColor = texture2D(s_Texture, v_texPo);\n" +
                //"    gl_FragColor = vec4(color, color, color, 1);\n" +
                "}";
        return source;
    }
}
