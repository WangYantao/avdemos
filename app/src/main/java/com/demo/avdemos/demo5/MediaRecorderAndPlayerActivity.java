package com.demo.avdemos.demo5;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.demo.avdemos.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

public class MediaRecorderAndPlayerActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MediaRecorderAndPlayerA";

    private static final int AUDIO_SAMPLE_RATE = 44100;
    private static final int AUDIO_BIT_RATE = AUDIO_SAMPLE_RATE * 8;

    private static final int VIDEO_FRAME_RATE = 30;

    Button btnRecord, btnPlay;
    SurfaceView sfvPreview;

    CameraManager cameraManager;
    CameraCharacteristics characteristics;
    String cameraId;
    Size outputSize;
    CameraDevice cameraDevice;
    CameraCaptureSession cameraCaptureSession;

    String mp4FilePath;

    MediaRecorder mediaRecorder;

    boolean isRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_recorder_and_player);

        initData();

        initView();
    }

    private void initData() {
        mp4FilePath = Environment.getExternalStorageDirectory() + "/avdemos/004.mp4";
    }

    private void initView() {
        btnRecord = findViewById(R.id.btnRecord);
        btnRecord.setOnClickListener(this);

        btnPlay = findViewById(R.id.btnPlay);
        btnPlay.setOnClickListener(this);

        sfvPreview = findViewById(R.id.sfvPreview);
        sfvPreview.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (holder != null) {
                    openCamera();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnRecord:
                isRecording = !isRecording;
                if (isRecording) {
                    startRecord();
                } else {
                    stopRecord();
                }
                btnRecord.setText(isRecording ? "停止录制" : "开始录制");
                break;
            case R.id.btnPlay:

                break;
        }
    }

    private void openCamera() {
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        setCameraIdAndCharacs();

        setOutputSize();

        setRecorder();

        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

            try {
                cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
                    @Override
                    public void onOpened(@NonNull final CameraDevice camera) {
                        if (camera == null) {
                            return;
                        }

                        cameraDevice = camera;

                        List<Surface> surfaces = new ArrayList<>();
                        surfaces.add(sfvPreview.getHolder().getSurface());
                        surfaces.add(mediaRecorder.getSurface());

                        createCameraSession(surfaces);
                    }

                    @Override
                    public void onDisconnected(@NonNull CameraDevice camera) {
                        Log.e(TAG, "___________onDisconnected: ");
                    }

                    @Override
                    public void onError(@NonNull CameraDevice camera, int error) {
                        Log.e(TAG, "_______________onError: ");
                    }
                }, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "请打开相机权限", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    private void createCameraSession(final List<Surface> surfaces){
        try {
            cameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {

                    cameraCaptureSession = session;


                    try {
                        CaptureRequest.Builder builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                        for (Surface surface : surfaces){
                            builder.addTarget(surface);
                        }
                        CaptureRequest request = builder.build();

                        cameraCaptureSession.setRepeatingRequest(request, new CameraCaptureSession.CaptureCallback() {
                            @Override
                            public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
                                super.onCaptureProgressed(session, request, partialResult);
                            }

                            @Override
                            public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                                super.onCaptureCompleted(session, request, result);
                            }
                        }, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void setCameraIdAndCharacs() {
        try {
            for (String cid : cameraManager.getCameraIdList()) {
                characteristics = cameraManager.getCameraCharacteristics(cid);
                int cameraFacing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (cameraFacing == CameraCharacteristics.LENS_FACING_BACK) {
                    cameraId = cid;
                    break;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void setOutputSize() {
        StreamConfigurationMap configs = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        List<Size> sizes = Arrays.asList(configs.getOutputSizes(SurfaceTexture.class));
        Collections.sort(sizes, new Comparator<Size>() {
            @Override
            public int compare(Size o1, Size o2) {
                return o2.getWidth() * o2.getHeight() - o1.getWidth() * o1.getHeight();
            }
        });
        outputSize = sizes.get(7);
        for (Size size : sizes) {
            if (size.getWidth() < 2000 && size.getWidth() > 1000 && size.getHeight() < 2000 && size.getHeight() > 1000) {
                outputSize = size;
                Log.e(TAG, "__________OutputSize: w = " + size.getWidth() + "; h = " + size.getHeight());
                break;
            }
        }
    }

    private void setRecorder() {
        mediaRecorder = new MediaRecorder();

        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);

        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setOutputFile(mp4FilePath);

        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setAudioEncodingBitRate(AUDIO_BIT_RATE);

        mediaRecorder.setVideoSize(outputSize.getWidth(), outputSize.getHeight());
        mediaRecorder.setVideoFrameRate(VIDEO_FRAME_RATE);
        mediaRecorder.setVideoEncodingBitRate(outputSize.getWidth() * outputSize.getHeight() * VIDEO_FRAME_RATE * 5);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startRecord() {
        if (mediaRecorder == null){
            setRecorder();

            List<Surface> surfaces = new ArrayList<>();
            surfaces.add(sfvPreview.getHolder().getSurface());
            surfaces.add(mediaRecorder.getSurface());

            createCameraSession(surfaces);
        }

        mediaRecorder.start();
    }

    private void stopRecord() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;

            List<Surface> surfaces = new ArrayList<>();
            surfaces.add(sfvPreview.getHolder().getSurface());

            createCameraSession(surfaces);
        }
    }

    @Override
    protected void onDestroy() {
        if (mediaRecorder != null) {
            mediaRecorder.release();
        }
        super.onDestroy();
    }
}
