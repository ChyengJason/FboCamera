package com.jscheng.scamera.render;

import android.opengl.GLES30;

import com.jscheng.scamera.util.GlesUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created By Chengjunsen on 2018/8/27
 */
public abstract class BaseRenderDrawer {
    protected int width;

    protected int height;

    protected int mProgram;

    //顶点坐标 Buffer
    private FloatBuffer mVertexBuffer;
    protected int mVertexBufferId;

    //纹理坐标 Buffer
    private FloatBuffer mFrontTextureBuffer;
    protected int mFrontTextureBufferId;

    //纹理坐标 Buffer
    private FloatBuffer mBackTextureBuffer;
    protected int mBackTextureBufferId;

    private FloatBuffer mDisplayTextureBuffer;
    protected int mDisplayTextureBufferId;

    private FloatBuffer mFrameTextureBuffer;
    protected int mFrameTextureBufferId;

    protected float vertexData[] = {
            -1f, -1f,// 左下角
            1f, -1f, // 右下角
            -1f, 1f, // 左上角
            1f, 1f,  // 右上角
    };

    protected float frontTextureData[] = {
            1f, 1f, // 右上角
            1f, 0f, // 右下角
            0f, 1f, // 左上角
            0f, 0f //  左下角
    };

    protected float backTextureData[] = {
            0f, 1f, // 左上角
            0f, 0f, //  左下角
            1f, 1f, // 右上角
            1f, 0f  // 右上角
    };

    protected float displayTextureData[] = {
            0f, 1f,
            1f, 1f,
            0f, 0f,
            1f, 0f,
    };

    protected float frameBufferData[] = {
            0f, 0f,
            1f, 0f,
            0f, 1f,
            1f, 1f
    };

    protected final int CoordsPerVertexCount = 2;

    protected final int VertexCount = vertexData.length / CoordsPerVertexCount;

    protected final int VertexStride = CoordsPerVertexCount * 4;

    protected final int CoordsPerTextureCount = 2;

    protected final int TextureStride = CoordsPerTextureCount * 4;

    public BaseRenderDrawer() {

    }

    public void create() {
        mProgram = GlesUtil.createProgram(getVertexSource(), getFragmentSource());
        initVertexBufferObjects();
        onCreated();
    }

    public void surfaceChangedSize(int width, int height) {
        this.width = width;
        this.height = height;
        onChanged(width, height);
    }

    public void draw(long timestamp, float[] transformMatrix){
        clear();
        useProgram();
        viewPort(0, 0, width, height);
        onDraw();
    }

    protected void clear(){
        GLES30.glClearColor(1.0f, 1.0f, 1.0f, 0.0f);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
    }

    protected void initVertexBufferObjects() {
        int[] vbo = new int[5];
        GLES30.glGenBuffers(5, vbo, 0);

        mVertexBuffer = ByteBuffer.allocateDirect(vertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData);
        mVertexBuffer.position(0);
        mVertexBufferId = vbo[0];
        // ARRAY_BUFFER 将使用 Float*Array 而 ELEMENT_ARRAY_BUFFER 必须使用 Uint*Array
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVertexBufferId);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, vertexData.length * 4, mVertexBuffer, GLES30.GL_STATIC_DRAW);


        mBackTextureBuffer = ByteBuffer.allocateDirect(backTextureData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(backTextureData);
        mBackTextureBuffer.position(0);
        mBackTextureBufferId = vbo[1];
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mBackTextureBufferId);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, backTextureData.length * 4, mBackTextureBuffer, GLES30.GL_STATIC_DRAW);

        mFrontTextureBuffer = ByteBuffer.allocateDirect(frontTextureData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(frontTextureData);
        mFrontTextureBuffer.position(0);
        mFrontTextureBufferId = vbo[2];
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mFrontTextureBufferId);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, frontTextureData.length * 4, mFrontTextureBuffer, GLES30.GL_STATIC_DRAW);

        mDisplayTextureBuffer = ByteBuffer.allocateDirect(displayTextureData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(displayTextureData);
        mDisplayTextureBuffer.position(0);
        mDisplayTextureBufferId = vbo[3];
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mDisplayTextureBufferId);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, displayTextureData.length * 4, mDisplayTextureBuffer, GLES30.GL_STATIC_DRAW);

        mFrameTextureBuffer = ByteBuffer.allocateDirect(frameBufferData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(frameBufferData);
        mFrameTextureBuffer.position(0);
        mFrameTextureBufferId = vbo[4];
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mFrameTextureBufferId);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, frameBufferData.length * 4, mFrameTextureBuffer, GLES30.GL_STATIC_DRAW);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER,0);
    }

    protected void useProgram(){
        GLES30.glUseProgram(mProgram);
    }

    protected void viewPort(int x, int y, int width, int height) {
        GLES30.glViewport(x, y, width,  height);
    }

    public abstract void setInputTextureId(int textureId);

    public abstract int getOutputTextureId();

    protected abstract String getVertexSource();

    protected abstract String getFragmentSource();

    protected abstract void onCreated();

    protected abstract void onChanged(int width, int height);

    protected abstract void onDraw();

}
