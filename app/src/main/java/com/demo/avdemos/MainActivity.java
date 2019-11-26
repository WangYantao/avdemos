package com.demo.avdemos;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.demo.avdemos.audio.RecordPCMActivity;
import com.demo.avdemos.image.ShowImageActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int PERMISSION_REQUEST_CODE= 100001;

    Button btnShowImage, btnRecorderPCM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        getPermissions();
    }

    private void initView(){
        btnShowImage = findViewById(R.id.btnShowImage);
        btnShowImage.setOnClickListener(this);

        btnRecorderPCM = findViewById(R.id.btnRecorderPCM);
        btnRecorderPCM.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnShowImage:
                toActivity(ShowImageActivity.class);
                break;
            case R.id.btnRecorderPCM:
                toActivity(RecordPCMActivity.class);
                break;
        }
    }

    private void toActivity(Class cls){
        startActivity(new Intent(MainActivity.this, cls));
    }

    private void getPermissions(){
        checkAndRequestPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        checkAndRequestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        checkAndRequestPermission(Manifest.permission.RECORD_AUDIO);
    }

    private void checkAndRequestPermission(String permission){
        if(checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{permission}, PERMISSION_REQUEST_CODE);
        }
    }
}
