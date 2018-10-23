package com.jscheng.scamera.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.jscheng.scamera.R;
import com.jscheng.scamera.util.CameraUtil;
import com.jscheng.scamera.util.PermisstionUtil;
import com.jscheng.scamera.widget.CameraFocusView;
import com.jscheng.scamera.widget.CameraGLSurfaceView;
import com.jscheng.scamera.widget.CameraProgressButton;
import com.jscheng.scamera.widget.CameraSwitchView;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.jscheng.scamera.util.LogUtil.TAG;

/**
 * Created By Chengjunsen on 2018/8/22
 */
public class CameraFragment extends Fragment implements CameraProgressButton.Listener, CameraGLSurfaceView.CameraGLSurfaceViewCallback, CameraSensor.CameraSensorListener{
    private final static int REQUEST_CODE = 1;
    private final static int MSG_START_PREVIEW = 1;
    private final static int MSG_SWITCH_CAMERA = 2;
    private final static int MSG_RELEASE_PREVIEW = 3;
    private final static int MSG_MANUAL_FOCUS = 4;
    private final static int MSG_ROCK = 5;

    private CameraGLSurfaceView mCameraView;
    private CameraSensor mCameraSensor;
    private CameraProgressButton mProgressBtn;
    private CameraFocusView mFocusView;
    private CameraSwitchView mSwitchView;
    private boolean isFocusing;
    private Size mPreviewSize;
    private Handler mCameraHanlder;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_camera, container, false);
        initCameraHandler();
        initView(contentView);
        return contentView;
    }

    private void initView(View contentView) {
        isFocusing = false;
        mPreviewSize = null;
        mCameraView = contentView.findViewById(R.id.camera_view);
        mProgressBtn = contentView.findViewById(R.id.progress_btn);
        mFocusView = contentView.findViewById(R.id.focus_view);
        mSwitchView = contentView.findViewById(R.id.switch_view);

        mCameraSensor = new CameraSensor(getContext());
        mCameraSensor.setCameraSensorListener(this);
        mProgressBtn.setProgressListener(this);

        mCameraView.setCallback(this);
        mCameraView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    focus((int)event.getX(), (int)event.getY(), false);
                    return true;
                }
                return false;
            }
        });
        mSwitchView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                mCameraHanlder.sendEmptyMessage(MSG_SWITCH_CAMERA);
            }
        });
    }

    private void initCameraHandler() {
        mCameraHanlder = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_START_PREVIEW:
                        startPreview();
                        break;
                    case MSG_RELEASE_PREVIEW:
                        releasePreview();
                        break;
                    case MSG_SWITCH_CAMERA:
                        switchCamera();
                        break;
                    case MSG_MANUAL_FOCUS:
                        manualFocus(msg.arg1, msg.arg2);
                        break;
                    case MSG_ROCK:
                        autoFocus();
                        break;
                    default:
                        break;
                }
            }
        };
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        mCameraHanlder.sendEmptyMessage(MSG_RELEASE_PREVIEW);
        super.onDetach();
    }

    @Override
    public void onSurfaceViewCreate(SurfaceTexture texture) {

    }

    @Override
    public void onSurfaceViewChange(int width, int height) {
        Log.e(TAG, "surfaceChanged: ( " + width +" x " + height +" )");
        mPreviewSize = new Size(width, height);
        mCameraHanlder.sendEmptyMessage(MSG_START_PREVIEW);
    }

    public void startPreview() {
        if (mPreviewSize != null && requestPermission() ) {
            if (CameraUtil.getCamera() == null) {
                CameraUtil.openCamera();
                Log.e(TAG, "openCamera" );
                CameraUtil.setDisplay(mCameraView.getSurfaceTexture());
            }
            CameraUtil.startPreview(getActivity(), mPreviewSize.getWidth(), mPreviewSize.getHeight());
            mCameraSensor.start();
            mSwitchView.setOrientation(mCameraSensor.getX(), mCameraSensor.getY(), mCameraSensor.getZ());
        }
    }

    public void releasePreview() {
        CameraUtil.releaseCamera();
        mCameraSensor.stop();
        mFocusView.cancelFocus();
        Log.e(TAG, "releasePreview releaseCamera" );
    }

    public void switchCamera() {
        mFocusView.cancelFocus();
        if (CameraUtil.getCamera() != null && mPreviewSize != null) {
            mCameraView.releaseSurfaceTexture();
            CameraUtil.releaseCamera();
            CameraUtil.switchCameraId();
            CameraUtil.openCamera();
            mCameraView.resumeSurfaceTexture();
            CameraUtil.setDisplay(mCameraView.getSurfaceTexture());
            CameraUtil.startPreview(getActivity(), mPreviewSize.getWidth(), mPreviewSize.getHeight());
        }
    }

    public void autoFocus() {
        if (CameraUtil.isBackCamera() && CameraUtil.getCamera() != null) {
            focus(mCameraView.getWidth() / 2, mCameraView.getHeight() / 2, true);
        }
        mSwitchView.setOrientation(mCameraSensor.getX(), mCameraSensor.getY(), mCameraSensor.getZ());
    }

    public void manualFocus(int x, int y) {
        focus(x, y, false);
    }

    private void focus(final int x, final int y, final boolean isAutoFocus) {
        if (CameraUtil.getCamera() == null || !CameraUtil.isBackCamera()) {
            return;
        }
        if (isFocusing && isAutoFocus) {
            return;
        }
        isFocusing = true;
        Point focusPoint = new Point(x, y);
        Size screenSize = new Size(mCameraView.getWidth(), mCameraView.getHeight());
        if (!isAutoFocus) {
            mFocusView.beginFocus(x, y);
        }
        CameraUtil.newCameraFocus(focusPoint, screenSize, new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                isFocusing = false;
                if (!isAutoFocus) {
                    mFocusView.endFocus(success);
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        releasePreview();
    }

    @Override
    public void onResume() {
        super.onResume();
        startPreview();
    }

    @Override
    public void onShortPress() {
        if (requestPermission()) {
            takePicture();
        }
    }

    @SuppressLint("NewApi")
    private void takePicture() {

    }

    @Override
    public void onStartLongPress() {
        if (requestPermission()) {
            mCameraView.startRecord();
        }
    }

    @Override
    public void onEndLongPress() {
        mCameraView.stopRecord();
    }

    @Override
    public void onEndMaxProgress() {
    }

    @Override
    public void onRock() {
        mCameraHanlder.sendEmptyMessage(MSG_ROCK);
    }

    private boolean requestPermission() {
        return PermisstionUtil.checkPermissionsAndRequest(getContext(), PermisstionUtil.CAMERA, REQUEST_CODE, "请求相机权限被拒绝")
                && PermisstionUtil.checkPermissionsAndRequest(getContext(), PermisstionUtil.STORAGE, REQUEST_CODE, "请求访问SD卡权限被拒绝");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == requestCode ) {
            mCameraHanlder.sendEmptyMessage(MSG_START_PREVIEW);
        }
    }
}
