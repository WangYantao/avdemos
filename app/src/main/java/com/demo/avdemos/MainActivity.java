package com.demo.avdemos;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.demo.avdemos.image.ShowImage;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int PERMISSION_REQUEST_CODE= 100001;

    Button btnShowImage;

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
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnShowImage:
                toActivity(ShowImage.class);
                break;
        }
    }

    private void toActivity(Class cls){
        startActivity(new Intent(MainActivity.this, cls));
    }

    private void getPermissions(){
        checkAndRequestPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    private void checkAndRequestPermission(String permission){
        if(checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{permission}, PERMISSION_REQUEST_CODE);
        }
    }
}
