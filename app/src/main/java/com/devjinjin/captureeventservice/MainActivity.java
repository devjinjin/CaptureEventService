package com.devjinjin.captureeventservice;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_STORAGE = 200;
    @BindView(R.id.startButton)
    Button startBtn;
    @BindView(R.id.stopButton)
    Button stopBtn;
    @BindView(R.id.imageView)
    ImageView imageView;

    //저장된 리스트
    private Set<String> capturedList;

    //서비스 패키지명
    final String name = "com.devjinjin.captureeventservice.CaptureService";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if (isUsedService()) {
            stopBtn.setEnabled(true);
            startBtn.setEnabled(false);
        } else {
            stopBtn.setEnabled(false);
            startBtn.setEnabled(true);
        }
    }

    @OnClick({R.id.startButton, R.id.stopButton})
    public void buttonClicks(View view) {
        switch (view.getId()) {
            case R.id.startButton:
                if (isHasPermission()) {
                    startCaptureService();
                }
                break;
            case R.id.stopButton:
                stopCaptureService();
                break;
        }
    }


    private boolean isHasPermission() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            // 이 권한을 필요한 이유를 설명해야하는가?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("권한 요청");
                builder.setMessage("캡쳐 이벤트 확인을 위해서 권한이 필요합니다. 세팅화면으로 이동하시겠습니까?");
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                .setData(Uri.parse("package:" + MainActivity.this.getPackageName()));
                        MainActivity.this.startActivity(intent);
                    }
                });
                builder.setNegativeButton("종료", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        MainActivity.this.finish();
                    }
                });
                builder.show();
                return false;
                // 다이어로그같은것을 띄워서 사용자에게 해당 권한이 필요한 이유에 대해 설명합니다
                // 해당 설명이 끝난뒤 requestPermissions()함수를 호출하여 권한허가를 요청해야 합니다
            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_STORAGE);
                // 필요한 권한과 요청 코드를 넣어서 권한허가요청에 대한 결과를 받아야 합니다
                return false;
            }
        }

        return true;
    }


    /* 캡쳐 검출 서비스 동작하는지 확인 */
    private boolean isUsedService() {
        ActivityManager manager = (ActivityManager) this.getSystemService(Activity.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (name.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void startCaptureService() {
        if (!isUsedService()) {
            Intent intent = new Intent(this, CaptureService.class);
            startService(intent);
        }
        stopBtn.setEnabled(true);
        startBtn.setEnabled(false);
    }

    private void stopCaptureService() {
        if (isUsedService()) {
            Intent intent = new Intent(this, CaptureService.class);
            stopService(intent);
        }
        stopBtn.setEnabled(false);
        startBtn.setEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();

        /* 캡쳐리스트 가지고 오기 */
        if (isExistCaptureList()) {
            showCapturedAlertView();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_STORAGE:

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 권한 허가
                    startCaptureService();
                } else {
                    // 권한 거부
// 사용자가 해당권한을 거부했을때 해주어야 할 동작을 수행합니다
                    stopCaptureService();
                }
                return;
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        /* 캡쳐리스트 초기화 */
        clearCaptureList();
    }


    /* 캡쳐리스트 초기화 */
    private void clearCaptureList() {
        if (capturedList != null) {
            capturedList.clear();
            capturedList = null;
        }
        CapturePropertyManager manager = new CapturePropertyManager(this);
        manager.clearSaveCaptureList();
    }

    /* 캡쳐리스트 가지고 오기 */
    private boolean isExistCaptureList() {
        CapturePropertyManager manager = new CapturePropertyManager(this);
        capturedList = manager.getSaveCaptureList();
        if (capturedList != null && !capturedList.isEmpty()) {
            return true;
        }
        return false;
    }

    /*Capture list 발견시 출력 다이얼로그*/
    private void showCapturedAlertView() {
        if (capturedList != null) {
            /* 캡쳐 리스트 가지고 오기 */
            Object[] capturedArray = capturedList.toArray();
            StringBuffer buffer = new StringBuffer();

            //리스트가 한개일때
            if (capturedArray.length > 1) {
                buffer.append(capturedArray);
            } else { //리스트가 여러개일때
                for (Object path : capturedArray) {
                    buffer.append("\n");
                    buffer.append(path);
                    buffer.append("\n");
                }
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("캡쳐 확인");
            builder.setMessage("캡쳐된 내용이 있습니다. " + buffer.toString() +
                    " 해당 이미지를 사용하시겠습니까?");
            builder.setPositiveButton("사용", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //캡쳐리스트에서 path 값 가져오기
                    Object[] capturedArray = capturedList.toArray();
                    if (capturedArray != null) {
                        String path = (String) capturedArray[0];
                        if (path != null && path.length() > 0) {
                            Bitmap bitmap = BitmapFactory.decodeFile(path);
                            if (bitmap != null) {
                                //이미지에 출력
                                imageView.setImageBitmap(bitmap);
                            }
                        }
                    }
                }
            });
            builder.setNegativeButton("사용안함", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //기존 저장한 리스트 초기화
                    clearCaptureList();
                    dialog.dismiss();
                }
            });
            builder.show();
        }
    }
}
