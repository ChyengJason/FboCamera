package com.jscheng.scamera.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES11Ext;
import android.opengl.GLES30;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import android.util.Log;

import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;

import static com.jscheng.scamera.util.LogUtil.TAG;

/**
 * Created By Chengjunsen on 2018/8/29
 */
public class GlesUtil {

    public static int createProgram(String vertexSource, String fragmentSource) {
        int mVertexShader = loadShader(GLES30.GL_VERTEX_SHADER, vertexSource);
        int mFragmentShader = loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentSource);
        int program = GLES30.glCreateProgram();
        GLES30.glAttachShader(program, mVertexShader);
        GLES30.glAttachShader(program, mFragmentShader);
        GLES30.glLinkProgram(program);
        int [] status = new int[1];
        GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, status, 0);
        if (status[0] != GLES30.GL_TRUE) {
            Log.e(TAG, "createProgam: link error");
            Log.e(TAG, "createProgam: " + GLES30.glGetProgramInfoLog(program));
            GLES30.glDeleteProgram(program);
            return 0;
        }
        GLES30.glDeleteShader(mVertexShader);
        GLES30.glDeleteShader(mFragmentShader);
        return program;
    }

    public static int loadShader(int shaderType, String shaderSource) {
        int shader = GLES30.glCreateShader(shaderType);
        GLES30.glShaderSource(shader, shaderSource);
        GLES30.glCompileShader(shader);
        int status[] = new int[1];
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, status, 0);
        if (status[0] == 0) {
            Log.e(TAG, "loadShader: compiler error");
            Log.e(TAG, "loadShader: " + GLES30.glGetShaderInfoLog(shader) );
            GLES30.glDeleteShader(shader);
            return 0;
        }
        return shader;
    }

    public static void checkFrameBufferError() {
        int status= GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER);
        if(status !=GLES30.GL_FRAMEBUFFER_COMPLETE) {
            Log.e(TAG, "checkFrameBuffer error: " + status);
            throw new RuntimeException("status:" + status + ", hex:" + Integer.toHexString(status));
        }
    }

    public static void checkError() {
        if (GLES30.glGetError() != GLES30.GL_NO_ERROR) {
            Log.e(TAG, "createOutputTexture: " + GLES30.glGetError() );
        }
    }

    public static int createPixelsBuffer(int width, int height) {
        int[] buffers = new int[1];
        GLES30.glGenBuffers(1, buffers, 0);
        checkError();
        return buffers[0];
    }

    public static void createPixelsBuffers(int[] buffers, int width, int height) {
        GLES30.glGenBuffers(buffers.length, buffers, 0);
        for (int i = 0; i < buffers.length; i++) {
            GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, buffers[i]);
            GLES30.glBufferData(GLES30.GL_PIXEL_PACK_BUFFER, width * height * 4, null, GLES30.GL_DYNAMIC_READ);
        }
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, 0);
    }

    public static int createFrameBuffer() {
        int[] buffers = new int[1];
        GLES30.glGenFramebuffers(1, buffers, 0);
        checkError();
        return buffers[0];
    }

    public static int createRenderBuffer() {
        int[] render = new int[1];
        GLES30.glGenRenderbuffers(1, render, 0);
        checkError();
        return render[0];
    }

    public static int createCameraTexture() {
        int[] texture = new int[1];
        GLES30.glGenTextures(1, texture, 0);
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
        GLES30.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        GLES30.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        return texture[0];
    }

    public static int createFrameTexture(int width, int height) {
        if (width <= 0 || height <= 0) {
            Log.e(TAG, "createOutputTexture: width or height is 0");
            return -1;
        }
        int[] textures = new int[1];
        GLES30.glGenTextures(1, textures, 0);
        if (textures[0] == 0) {
            Log.e(TAG, "createFrameTexture: glGenTextures is 0");
            return -1;
        }
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textures[0]);
        GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, width, height, 0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, null);
        //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
        //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
        //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST);
        //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_NEAREST);
        GlesUtil.checkError();
        return textures[0];
    }

    public static int loadBitmapTexture(Bitmap bitmap) {
        int[] textureIds = new int[1];
        GLES30.glGenTextures(1, textureIds, 0);
        if (textureIds[0] == 0) {
            Log.e(TAG, "loadBitmapTexture: glGenTextures is 0");
            return -1;
        }
        //绑定纹理
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureIds[0]);
        //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER,GLES30.GL_NEAREST);
        //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,GLES30.GL_TEXTURE_MAG_FILTER,GLES30.GL_LINEAR);
        //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S,GLES30.GL_CLAMP_TO_EDGE);
        //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T,GLES30.GL_CLAMP_TO_EDGE);
        //根据以上指定的参数，生成一个2D纹理
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0);
        GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
        return textureIds[0];
    }

    public static int loadBitmapTexture(Context context, int resourceId) {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId);
        if (bitmap == null) {
            Log.e(TAG, "loadBitmapTexture:bitmap is null");
            return -1;
        }
        int textureId = loadBitmapTexture(bitmap);
        bitmap.recycle();
        return textureId;
    }

    public static void bindFrameTexture(int frameBufferId, int textureId){
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, frameBufferId);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId);
        GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D, textureId, 0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
        GlesUtil.checkError();
    }

    public static void bindFrameRender(int frameBufferId, int renderId, int width, int height) {
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, frameBufferId);
        GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, renderId);
        GLES30.glRenderbufferStorage(GLES30.GL_RENDERBUFFER, GLES30.GL_DEPTH_COMPONENT16, width, height);
        GLES30.glFramebufferRenderbuffer(GLES30.GL_FRAMEBUFFER, GLES30.GL_DEPTH_ATTACHMENT, GLES30.GL_RENDERBUFFER, renderId);
        GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, 0);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
    }
}
