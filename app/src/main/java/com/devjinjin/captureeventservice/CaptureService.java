package com.devjinjin.captureeventservice;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

/**
 * Created by jylee on 2018-01-02.
 */

public class CaptureService extends Service {
    public static final String TAG = CaptureService.class.getSimpleName();
    ServiceHandler serviceHandler = new ServiceHandler();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private ScreenShotContentObserver screenShotContentObserver;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //메모리 process 재시작인 경우 intent == null
        //flags는 서비스의 요청에 대한 추가 정보
        //서비스 요청에 대한 고유 식별자
        if (intent != null) {
            //일반 시작
            Message msg = new Message();
            msg.what = 1;
            serviceHandler.sendMessage(msg);
        } else {
            //메모리 초기화후 다시 시작
            Message msg = new Message();
            msg.what = 2;
            serviceHandler.sendMessage(msg);
        }


        screenShotContentObserver = new ScreenShotContentObserver(serviceHandler, this) {
            @Override
            protected void onScreenShot(String path, String fileName) {
                File file = new File(path); //this is the file of screenshot image
                if (file.exists()) {
                    Log.e(TAG, "exists");
                }
                if (file.canRead()) {
                    Log.e(TAG, "canRead");
                }
                if (serviceHandler != null) {
                    Message msg = new Message();
                    msg.what = 3;
                    msg.obj = path;
                    serviceHandler.sendMessage(msg);
                }
            }
        };

        getContentResolver().registerContentObserver(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                true,
                screenShotContentObserver
        );

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Message msg = new Message();
        msg.what = 0;
        serviceHandler.sendMessage(msg);

        try {
            getContentResolver().unregisterContentObserver(screenShotContentObserver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class ServiceHandler extends Handler {
        public ServiceHandler() {

        }

        @Override
        public void handleMessage(Message msg) {
            int event = msg.what;
            switch (event) {
                case 0:
                    Toast.makeText(CaptureService.this, "stop", Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    Toast.makeText(CaptureService.this, "start", Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    Toast.makeText(CaptureService.this, "restart", Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    String message = (String) msg.obj;
                    Toast.makeText(CaptureService.this, message, Toast.LENGTH_SHORT).show();
                    CapturePropertyManager manager = new CapturePropertyManager(CaptureService.this);
                    manager.setSaveCaptureList(message);
                    break;
            }
        }
    }
}
