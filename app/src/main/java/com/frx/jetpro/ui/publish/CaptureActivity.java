package com.frx.jetpro.ui.publish;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Size;
import android.view.Surface;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.core.VideoCapture;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.frx.jetpro.R;
import com.frx.jetpro.databinding.ActivityLayoutCaptureBinding;
import com.frx.jetpro.view.RecordView;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class CaptureActivity extends AppCompatActivity {
    public static final int REQ_CAPTURE = 10001;

    private static final double RATIO_4_3_VALUE = 4.0 / 3.0;
    private static final double RATIO_16_9_VALUE = 16.0 / 9.0;

    private static final String[] PERMISSIONS = new String[]{
            Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO};
    private static final int PERMISSION_CODE = 1000;
    //记录未授权权限
    private ArrayList<String> deniedPermission = new ArrayList<>();

    private ActivityLayoutCaptureBinding dataBinding;

    private int lensFacing = CameraSelector.LENS_FACING_BACK;
    private Size resolution = new Size(1280, 720);

    private ImageCapture imageCapture;
    private VideoCapture videoCapture;

    private ProcessCameraProvider processCameraProvider;
    private Preview preview;
    private CameraSelector cameraSelector;
    private Camera camera;

    public static final String RESULT_FILE_PATH = "file_path";
    public static final String RESULT_FILE_WIDTH = "file_width";
    public static final String RESULT_FILE_HEIGHT = "file_height";
    public static final String RESULT_FILE_TYPE = "file_type";

    private boolean takingPicture;
    private String outputFilePath;

    public static void startActivity(Activity activity) {
        Intent intent = new Intent(activity, CaptureActivity.class);
        activity.startActivityForResult(intent, REQ_CAPTURE);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_layout_capture);
        //申请权限1
        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_CODE);

        dataBinding.recordView.setOnRecordListener(new RecordView.OnRecordListener() {
            @Override
            public void onClick() {
                takingPicture = true;
                dataBinding.captureTips.setVisibility(View.INVISIBLE);

                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                        System.currentTimeMillis() + ".jpeg");

                ImageCapture.OutputFileOptions outputFileOptions =
                        new ImageCapture.OutputFileOptions.Builder(file).build();

                imageCapture.takePicture(outputFileOptions,
                        Executors.newSingleThreadExecutor(),
                        new ImageCapture.OnImageSavedCallback() {
                            @SuppressLint("RestrictedApi")
                            @Override
                            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                                ArchTaskExecutor.getMainThreadExecutor().execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        onFileSaved(file);
                                    }
                                });
                            }

                            @Override
                            public void onError(@NonNull ImageCaptureException exception) {
                                if (file != null && file.exists()) {
                                    file.delete();
                                }
                            }
                        });
            }

            @SuppressLint("RestrictedApi")
            @Override
            public void onLongClick() {
                takingPicture = false;
                dataBinding.captureTips.setVisibility(View.INVISIBLE);

                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                        System.currentTimeMillis() + ".mp4");
                VideoCapture.OutputFileOptions outputFileOptions
                        = new VideoCapture.OutputFileOptions.Builder(file).build();

                videoCapture.startRecording(outputFileOptions,
                        Executors.newSingleThreadExecutor(),
                        new VideoCapture.OnVideoSavedCallback() {
                            @Override
                            public void onVideoSaved(@NonNull VideoCapture.OutputFileResults outputFileResults) {
                                onFileSaved(file);
                            }

                            @Override
                            public void onError(int videoCaptureError, @NonNull String message, @Nullable Throwable cause) {
                                if (file != null && file.exists()) {
                                    file.delete();
                                }
                            }
                        });
            }

            @SuppressLint("RestrictedApi")
            @Override
            public void onFinish() {
                videoCapture.stopRecording();
            }
        });
    }

    private void onFileSaved(File file) {
        outputFilePath = file.getAbsolutePath();
        String mimeType = takingPicture ? "image/jpeg" : "video/mp4";
        // 保存到本地相册
        MediaScannerConnection.scanFile(this, new String[]{outputFilePath}, new String[]{mimeType}, null);
        PreviewActivity.startActivityForResult(this, outputFilePath, !takingPicture, "完成");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PreviewActivity.REQ_PREVIEW && resultCode == RESULT_OK) {
            Intent intent = new Intent();
            intent.putExtra(RESULT_FILE_PATH, outputFilePath);
            //当设备处于竖屏情况时，宽高的值 需要互换，横屏不需要
            intent.putExtra(RESULT_FILE_WIDTH, resolution.getHeight());
            intent.putExtra(RESULT_FILE_HEIGHT, resolution.getWidth());
            intent.putExtra(RESULT_FILE_TYPE, !takingPicture);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            deniedPermission.clear();
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int result = grantResults[i];
                if (result != PackageManager.PERMISSION_GRANTED) {
                    deniedPermission.add(permission);
                }
            }

            if (deniedPermission.isEmpty()) {
                //权限申请完成
                bindCameraX();
            } else {
                AlertDialog alertDialog = new AlertDialog.Builder(this)
                        .setMessage(getString(R.string.capture_permission_message))
                        .setNegativeButton("不授权", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                bindCameraX();
                            }
                        }).setPositiveButton("授权", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                String[] denied = new String[deniedPermission.size()];
                                ActivityCompat.requestPermissions(CaptureActivity.this, deniedPermission.toArray(denied), PERMISSION_CODE);
                            }
                        }).create();
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.setCancelable(false);
                alertDialog.show();
            }
        }
    }

    @SuppressLint("RestrictedApi")
    private void bindCameraX() {
        ListenableFuture cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    //获取屏幕的分辨率
                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    dataBinding.previewView.getDisplay().getRealMetrics(displayMetrics);
                    //获取宽高比
                    int screenAspectRatio = aspectRatio(displayMetrics.widthPixels, displayMetrics.heightPixels);
                    int rotation = dataBinding.previewView.getDisplay().getRotation();

                    // 现在保证提供摄像头
                    processCameraProvider = (ProcessCameraProvider) cameraProviderFuture.get();
                    // 设置取景器用例以显示相机预览
                    preview = new Preview.Builder()
                            //设置宽高比
                            .setTargetAspectRatio(screenAspectRatio)
                            //设置当前屏幕的旋转
                            .setTargetRotation(rotation)
                            .build();

                    // 设置允许用户拍照
                    imageCapture = new ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                            .setTargetRotation(Surface.ROTATION_0)
                            .setTargetAspectRatio(screenAspectRatio)
                            .build();

                    // 设置允许用户录像
                    videoCapture = new VideoCapture.Builder()
                            .setTargetRotation(Surface.ROTATION_0)
                            .setTargetAspectRatio(screenAspectRatio)
                            .setVideoFrameRate(30)
                            .setBitRate(3 * 1024 * 1024)
                            .build();

                    // 通过要求镜头朝向来选择相机
                    cameraSelector = new CameraSelector.Builder()
                            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                            .build();

                    // 绑定预览界面
                    preview.setSurfaceProvider(dataBinding.previewView.getSurfaceProvider());

                    //重新绑定之前必须先取消绑定
                    processCameraProvider.unbindAll();
                    // 绑定相机生命周期，获取相机实例
                    camera = processCameraProvider.bindToLifecycle(
                            CaptureActivity.this,
                            cameraSelector,
                            preview,
                            imageCapture,
                            videoCapture
                    );

                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private int aspectRatio(int widthPixels, int heightPixels) {
        double previewRatio = (double) Math.max(widthPixels, heightPixels) / (double) Math.min(widthPixels, heightPixels);
        if (Math.abs(previewRatio - RATIO_4_3_VALUE) <= Math.abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3;
        }
        return AspectRatio.RATIO_16_9;
    }
}
