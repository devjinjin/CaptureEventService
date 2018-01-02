package com.devjinjin.captureeventservice;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.startButton)
    Button startBtn;
    @BindView(R.id.stopButton)
    Button stopBtn;
    @BindView(R.id.imageView)
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.startButton, R.id.stopButton})
    public void buttonClicks(View view) {
        switch (view.getId()) {
            case R.id.startButton:
                startCaptureService();
                stopBtn.setEnabled(true);
                startBtn.setEnabled(false);
                break;
            case R.id.stopButton:
                stopCaptureService();
                stopBtn.setEnabled(false);
                startBtn.setEnabled(true);
                break;
        }
    }

    private void startCaptureService() {

    }

    private void stopCaptureService() {

    }
}
