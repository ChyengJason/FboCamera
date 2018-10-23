package com.jscheng.scamera.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

/**
 * Created By Chengjunsen on 2018/8/22
 */
public class PermisstionUtil {
    public static final String[] CALENDAR;
    public static final String[] CAMERA;
    public static final String[] CONTACTS;
    public static final String[] LOCATION;
    public static final String[] MICROPHONE;
    public static final String[] PHONE;
    public static final String[] SENSORS;
    public static final String[] SMS;
    public static final String[] STORAGE;

    /**
     * 单个权限请求检测
     * @param context
     * @param permissionName
     * @return
     */
    public static boolean isPermissionGranted(Context context, String permissionName) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        //判断是否需要请求允许权限
        int hasPermision = context.checkSelfPermission(permissionName);
        if (hasPermision != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

    /**
     * 多个权限请求检测，返回list,如果list.size为空说明权限全部有了不需要请求，否则请求没有的
     * @param context
     * @param permArray
     * @return
     */
    public static List<String> isPermissionsAllGranted(Context context, String[] permArray) {
        List<String> list = new ArrayList<>();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return list;
        }
        for (int i = 0; permArray != null && i < permArray.length; i++) {
            //获得批量请求但被禁止的权限列表
            if (PackageManager.PERMISSION_GRANTED != context.checkSelfPermission(permArray[i])) {
                list.add(permArray[i]);
            }
        }
        return list;
    }

    /**
     * 判断是否已拒绝过权限
     * @param context
     * @param permission
     * @return
     */
    public static boolean judgePermission(Context context, String permission) {
        if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, permission)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断是否已拒绝过权限
     * @param context
     * @param permissions
     * @return
     */
    public static boolean judgePermission(Context context, String[] permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 请求权限
     * @param context
     * @param permission
     * @param requestCode
     */
    public static void requestPermission(Context context, String permission, int requestCode) {
        ActivityCompat.requestPermissions((Activity) context, new String[]{permission}, requestCode);
    }

    /**
     * 请求多个权限
     * @param context
     * @param permissions
     * @param requestCode
     */
    public static void requestPermissions(Context context, String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions((Activity) context, permissions, requestCode);
    }


    /**
     * 申请权限复合接口
     * 检查权限 -> 申请权限 -> 被拒绝则打开对话窗口
     * @param context
     * @param permissions
     * @param requestCode
     * @param hint
     * @return
     */
    public static boolean checkPermissionsAndRequest(Context context, String[] permissions, int requestCode, String hint) {
        List<String> notGrantPermissions = PermisstionUtil.isPermissionsAllGranted(context, permissions);
        if(notGrantPermissions.isEmpty()){
            return true;
        }
        if (PermisstionUtil.judgePermission(context, permissions)) {
            PermisstionUtil.showPermissionAlterDialog(context, hint);
        } else {
            PermisstionUtil.requestPermissions(context, permissions, requestCode);
        }
        return false;
    }

    public static void showPermissionAlterDialog(final Context context, String hint) {
        new AlertDialog.Builder(context)
                .setTitle("提示")
                .setMessage(hint)
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //前往应用详情界面
                        try {
                            Uri packUri = Uri.parse("package:" + context.getPackageName());
                            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packUri);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                        } catch (Exception e) {
                            Toast.makeText(context, "跳转失败", Toast.LENGTH_SHORT).show();
                        }
                        dialog.dismiss();
                    }
                }).create().show();
    }
    static {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            CALENDAR = new String[]{};
            CAMERA = new String[]{};
            CONTACTS = new String[]{};
            LOCATION = new String[]{};
            MICROPHONE = new String[]{};
            PHONE = new String[]{};
            SENSORS = new String[]{};
            SMS = new String[]{};
            STORAGE = new String[]{};
        } else {
            CALENDAR = new String[]{
                    Manifest.permission.READ_CALENDAR,
                    Manifest.permission.WRITE_CALENDAR};

            CAMERA = new String[]{
                    Manifest.permission.CAMERA};

            CONTACTS = new String[]{
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.WRITE_CONTACTS,
                    Manifest.permission.GET_ACCOUNTS};

            LOCATION = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION};

            MICROPHONE = new String[]{
                    Manifest.permission.RECORD_AUDIO};

            PHONE = new String[]{
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.READ_CALL_LOG,
                    Manifest.permission.WRITE_CALL_LOG,
                    Manifest.permission.USE_SIP,
                    Manifest.permission.PROCESS_OUTGOING_CALLS};

            SENSORS = new String[]{
                    Manifest.permission.BODY_SENSORS};

            SMS = new String[]{
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.READ_SMS,
                    Manifest.permission.RECEIVE_WAP_PUSH,
                    Manifest.permission.RECEIVE_MMS};

            STORAGE = new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE};
        }
    }

}
