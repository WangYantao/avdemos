package com.demo.avdemos.demo6;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.demo.avdemos.R;

public class GLActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnTriangle, btnTexture, btnGLCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gl);

        initView();
    }

    private void initView(){
        btnTriangle = findViewById(R.id.btnTriangle);
        btnTriangle.setOnClickListener(this);

        btnTexture = findViewById(R.id.btnTexture);
        btnTexture.setOnClickListener(this);

        btnGLCamera = findViewById(R.id.btnGLCamera);
        btnGLCamera.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnTriangle:
                toActivity(TriangleActivity.class);
                break;
            case R.id.btnTexture:
                toActivity(TextureActivity.class);
                break;
            case R.id.btnGLCamera:
                toActivity(GLCameraActivity.class);
                break;
        }
    }

    private void toActivity(Class cls){
        startActivity(new Intent(GLActivity.this, cls));
    }
}
