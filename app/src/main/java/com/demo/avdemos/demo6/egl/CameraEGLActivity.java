package com.demo.avdemos.demo6.egl;

import android.graphics.SurfaceTexture;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import com.demo.avdemos.R;
import com.demo.avdemos.utils.CameraHelper;

import java.util.ArrayList;
import java.util.List;

public class CameraEGLActivity extends AppCompatActivity {

    Button btnRecorder;
    boolean isRecording;

    SurfaceView surfaceView;
    CameraEGLRender cameraEGLRender;
    CameraHelper cameraHelper;

    EncorderEGLRender encoderRender;
    H264EGLEncoder h264EGLEncoder;

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

            @Override
            public void onFinishDrawFrame() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isRecording) {
                            encoderRender.drawFrame();
                        }
                    }
                });
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

        btnRecorder = findViewById(R.id.btnRecorder);
        btnRecorder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnRecorder.setText(!isRecording ? "停止录制" : "开始录制");
                if (!isRecording){
                    startRecord();
                }else {
                    stopRecord();
                }
            }
        });
    }

    private void startRecord(){
        int width = cameraEGLRender.getWidth() % 2 == 0 ? cameraEGLRender.getWidth() : cameraEGLRender.getWidth() - 1;
        int height = cameraEGLRender.getHeight() % 2 == 0 ? cameraEGLRender.getHeight() : cameraEGLRender.getHeight() - 1;
        h264EGLEncoder = new H264EGLEncoder(width, height, 30);

        encoderRender = EncorderEGLRender.getInstance("encorderRender", cameraEGLRender.getEglHelper().getEglContext());
        encoderRender.setRenderStateListener(new BaseEGLRender.RenderStateListener() {
            @Override
            public void onSurfaceCreated() {

            }

            @Override
            public void onSurfaceChanged(int width, int height) {
                isRecording = !isRecording;
                h264EGLEncoder.startEncoder();
            }

            @Override
            public void onBeiginDrawFrame() {

            }

            @Override
            public void onFinishDrawFrame() {

            }
        });
        encoderRender.prepareEglEnvironment();
        encoderRender.setTexture(cameraEGLRender.getTexture());
        encoderRender.createSurface(h264EGLEncoder.getSurface());
        encoderRender.changeSurface(cameraEGLRender.getWidth(), cameraEGLRender.getHeight());
    }

    private void stopRecord(){
        encoderRender.quitSafely();
        h264EGLEncoder.stopEncoder();
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
