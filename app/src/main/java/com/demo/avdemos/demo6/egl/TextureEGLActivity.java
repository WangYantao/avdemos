package com.demo.avdemos.demo6.egl;

import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.demo.avdemos.R;

public class TextureEGLActivity extends AppCompatActivity {

    SurfaceView surfaceView;
    BaseEGLRender textureEGLRender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_textureegl);

        initRender();

        initView();
    }

    public void initRender() {
        textureEGLRender = TextureEGLRender.getInstance("texturerender");
        textureEGLRender.prepareEglEnvironment();
    }

    private void initView() {
        surfaceView = findViewById(R.id.surfaceView);
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (holder == null) return;

                textureEGLRender.createSurface(holder.getSurface());
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                textureEGLRender.changeSurface(width, height);

                textureEGLRender.drawFrame();
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        textureEGLRender.quitSafely();

        super.onDestroy();
    }
}
