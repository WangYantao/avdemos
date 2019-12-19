package com.demo.avdemos;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.demo.avdemos.demo5.MediaRecorderAndPlayerActivity;
import com.demo.avdemos.demo2.RecordAndPlayPCMActivity;
import com.demo.avdemos.demo4.MediaRecordActivity;
import com.demo.avdemos.demo3.ExtractorAndMuxerActivity;
import com.demo.avdemos.demo1.ShowImageActivity;
import com.demo.avdemos.demo6.glsv.GLActivity;
import com.demo.avdemos.utils.GLUtil;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int PERMISSION_REQUEST_CODE= 100001;

    Button btnMediaRecorderAndPlayer, btnShowImage, btnRecorderPCM, btnCamera, btnExtractorAndMuxer,
            btnOpengl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUtils();

        initView();

        getPermissions();
    }

    private void initUtils(){
        GLUtil.init(this);
    }

    private void initView(){
        btnMediaRecorderAndPlayer = findViewById(R.id.btnMediaRecorderAndPlayer);
        btnMediaRecorderAndPlayer.setOnClickListener(this);

        btnShowImage = findViewById(R.id.btnShowImage);
        btnShowImage.setOnClickListener(this);

        btnRecorderPCM = findViewById(R.id.btnRecorderPCM);
        btnRecorderPCM.setOnClickListener(this);

        btnCamera = findViewById(R.id.btnCamera);
        btnCamera.setOnClickListener(this);

        btnExtractorAndMuxer = findViewById(R.id.btnExtractorAndMuxer);
        btnExtractorAndMuxer.setOnClickListener(this);

        btnOpengl = findViewById(R.id.btnOpengl);
        btnOpengl.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnMediaRecorderAndPlayer:
                toActivity(MediaRecorderAndPlayerActivity.class);
                break;
            case R.id.btnShowImage:
                toActivity(ShowImageActivity.class);
                break;
            case R.id.btnRecorderPCM:
                toActivity(RecordAndPlayPCMActivity.class);
                break;
            case R.id.btnCamera:
                toActivity(MediaRecordActivity.class);
                break;
            case R.id.btnExtractorAndMuxer:
                toActivity(ExtractorAndMuxerActivity.class);
                break;
            case R.id.btnOpengl:
                toActivity(GLActivity.class);
                break;
        }
    }

    private void toActivity(Class cls){
        startActivity(new Intent(MainActivity.this, cls));
    }

    private void getPermissions(){
        checkAndRequestPermission(Manifest.permission.INTERNET);
        checkAndRequestPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        checkAndRequestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        checkAndRequestPermission(Manifest.permission.RECORD_AUDIO);
        checkAndRequestPermission(Manifest.permission.CAMERA);
    }

    private void checkAndRequestPermission(String permission){
        if(checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{permission}, PERMISSION_REQUEST_CODE);
        }
    }
}
