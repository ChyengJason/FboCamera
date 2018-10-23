package com.jscheng.scamera.view;

import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.jscheng.scamera.BaseActivity;
import com.jscheng.scamera.R;

/**
 * Created By Chengjunsen on 2018/8/22
 */
public class MainActivity extends BaseActivity {
    private CameraFragment mCameraFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN, WindowManager.LayoutParams. FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        mCameraFragment = new CameraFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.main_container, mCameraFragment);
        transaction.commit();
    }
}
