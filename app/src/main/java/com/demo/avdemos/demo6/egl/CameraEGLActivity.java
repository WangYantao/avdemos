package com.demo.avdemos.demo6.egl;

import android.graphics.SurfaceTexture;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.demo.avdemos.R;
import com.demo.avdemos.utils.CameraHelper;

import java.util.ArrayList;
import java.util.List;

public class CameraEGLActivity extends AppCompatActivity {

    SurfaceView surfaceView;
    CameraEGLRender cameraEGLRender;
    CameraHelper cameraHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_egl);

        initCamera();

        initRender();

        initView();
    }

    public void initRender() {
        cameraEGLRender = CameraEGLRender.getInstance("texturerender");
        cameraEGLRender.setRenderStateListener(new BaseEGLRender.RenderStateListener() {
            @Override
            public void onSurfaceCreated() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SurfaceTexture surfaceTexture = (cameraEGLRender).getSurfaceTexture();
                        surfaceTexture.setDefaultBufferSize(cameraHelper.getOutputSize().getWidth(), cameraHelper.getOutputSize().getHeight());
                        surfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
                            @Override
                            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                                cameraEGLRender.drawFrame();
                            }
                        });
                        List<Surface> surfaces = new ArrayList<>();
                        surfaces.add(new Surface(surfaceTexture));
                        cameraHelper.setSurfaces(surfaces);
                        cameraHelper.openCamera();
                    }
                });
            }

            @Override
            public void onSurfaceChanged(int width, int height) {

            }

            @Override
            public void onBeiginDrawFrame() {

            }
        });
        cameraEGLRender.prepareEglEnvironment();
    }

    private void initView() {
        surfaceView = findViewById(R.id.surfaceView);
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (holder == null) return;

                cameraEGLRender.createSurface(holder.getSurface());
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                cameraEGLRender.changeSurface(width, height);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
    }

    private void initCamera(){
        cameraHelper = new CameraHelper(this);
    }

    @Override
    protected void onDestroy() {
        cameraEGLRender.quitSafely();

        super.onDestroy();
    }
}
