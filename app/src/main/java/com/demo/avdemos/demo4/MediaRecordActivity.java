package com.demo.avdemos.demo4;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
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
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.demo.avdemos.R;
import com.demo.avdemos.utils.ImageFormatUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.demo.avdemos.demo4.Muxer.AUDIO_SAMPLE_RATE;

public class MediaRecordActivity extends AppCompatActivity {

    private static final String TAG = "MediaRecordActivity";

    private static final int AUDIO_CHANNEL_IN = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_CHANNEL_OUT = AudioFormat.CHANNEL_OUT_MONO;
    private static final int AUDIO_FORAMT = AudioFormat.ENCODING_PCM_16BIT;

    Button btnRecoderVideo;

    boolean isRecording = false;

    Muxer muxer;

    SurfaceView surfaceView;
    CameraManager cameraManager;
    CameraCharacteristics characteristics;
    String cameraId;
    ImageReader outputReader;
    Size outputSize;

    AudioRecord audioRecord;
    int audioMinbufferSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        initData();

        initView();
    }

    private void initData(){
        audioMinbufferSize = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE, AUDIO_CHANNEL_IN, AUDIO_FORAMT);
    }

    private void initView() {
        btnRecoderVideo = findViewById(R.id.btnRecoderVideo);
        btnRecoderVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (muxer == null){
                    Toast.makeText(MediaRecordActivity.this, "相关资源尚未初始化", Toast.LENGTH_SHORT).show();
                    return;
                }

                isRecording = !isRecording;
                btnRecoderVideo.setText(isRecording ? "停止录制" : "开始录制");
                if (isRecording){
                    audioRecord.startRecording();
                    startAudioRecordThread();

                    muxer.start();
                }else {
                    audioRecord.stop();

                    muxer.stop();
                }
            }
        });

        surfaceView = findViewById(R.id.surfaceView);
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (holder == null) {
                    return;
                }

                openCamera();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
    }

    private void startAudioRecordThread(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] data = new byte[audioMinbufferSize];
                while (isRecording){
                    int ret = audioRecord.read(data, 0, audioMinbufferSize);
                    if (ret != AudioRecord.ERROR_INVALID_OPERATION) {
                        muxer.addMuxerData(new Muxer.MuxerData(Muxer.TRACK_AUDIO, data));
                    }
                }
            }
        }).start();
    }

    private void openCamera() {
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        setCameraIdAndCharacs();

        setOutputSize();

        setReader();

        setMuxer();

        setAudioRecord();

        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

            try {
                cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
                    @Override
                    public void onOpened(@NonNull final CameraDevice camera) {
                        if (camera == null){
                            return;
                        }

                        final Surface previewSurface = surfaceView.getHolder().getSurface();

                        try {
                            camera.createCaptureSession(Arrays.asList(previewSurface, outputReader.getSurface()), new CameraCaptureSession.StateCallback() {
                                @Override
                                public void onConfigured(@NonNull CameraCaptureSession session) {
                                    try {
                                        CaptureRequest.Builder builder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                                        builder.addTarget(previewSurface);
                                        builder.addTarget(outputReader.getSurface());
                                        CaptureRequest request = builder.build();

                                        session.setRepeatingRequest(request, new CameraCaptureSession.CaptureCallback() {
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

                    @Override
                    public void onDisconnected(@NonNull CameraDevice camera) {

                    }

                    @Override
                    public void onError(@NonNull CameraDevice camera, int error) {

                    }
                }, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }else {
            Toast.makeText(this, "请打开相机权限", Toast.LENGTH_SHORT).show();
            return;
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
        for (Size size : sizes){
            if (size.getWidth() < 2000 && size.getWidth() > 1000 && size.getHeight() < 2000 && size.getHeight() > 1000){
                outputSize = size;
                Log.e(TAG, "__________OutputSize: w = " + size.getWidth() + "; h = " + size.getHeight());
                break;
            }
        }
    }

    private void setReader(){
        outputReader = ImageReader.newInstance(outputSize.getWidth(), outputSize.getHeight(), ImageFormat.YUV_420_888, 2);
        outputReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                Image image = reader.acquireLatestImage();
                if (image != null){
                    if (muxer != null){
                        muxer.addMuxerData(new Muxer.MuxerData(Muxer.TRACK_VIDEO, ImageFormatUtil.image2nv12(image)));
                    }
                    image.close();
                }
            }
        }, null);
    }

    private void setMuxer(){
        muxer = new Muxer(outputSize.getWidth(), outputSize.getHeight());
    }

    private void setAudioRecord(){
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, Muxer.AUDIO_SAMPLE_RATE, AUDIO_CHANNEL_IN, AUDIO_FORAMT, audioMinbufferSize);
    }

    @Override
    protected void onDestroy() {
        if (isRecording){
            audioRecord.stop();
            muxer.stop();
        }

        if (audioRecord != null){
            audioRecord.release();
            audioRecord = null;
        }
        super.onDestroy();
    }
}
