package com.demo.avdemos.utils;

import android.Manifest;
import android.app.Activity;
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
import android.support.annotation.NonNull;
import android.util.Size;
import android.view.Surface;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by wangyt on 2019/12/19
 */
public class CameraHelper {

    Context context;
    CameraManager cameraManager;
    CameraCharacteristics characteristics;
    String cameraId;
    Size outputSize;

    private List<Surface> surfaces;

    public CameraHelper(Activity activity) {
        this.context = activity;
        cameraManager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        setCameraIdAndCharacs();
        setOutputSize();
    }

    public Size getOutputSize() {
        return outputSize;
    }

    public void setSurfaces(List<Surface> surfaces){
        this.surfaces = surfaces;
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
        outputSize = sizes.get(8);
//        for (Size size : sizes){
//            if (size.getWidth() < 2000 && size.getWidth() > 1000 && size.getHeight() < 2000 && size.getHeight() > 1000){
//                outputSize = size;
//                LogUtil.e("OutputSize: w = " + size.getWidth() + "; h = " + size.getHeight());
//                break;
//            }
//        }
    }

    public void openCamera() {
        if (surfaces == null || surfaces.size() == 0){
            Toast.makeText(context, "未设置相机输出surface！", Toast.LENGTH_SHORT).show();
            return;
        }

        if (context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

            try {
                cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
                    @Override
                    public void onOpened(@NonNull final CameraDevice camera) {
                        if (camera == null){
                            return;
                        }

                        try {
                            camera.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {
                                @Override
                                public void onConfigured(@NonNull CameraCaptureSession session) {
                                    try {
                                        CaptureRequest.Builder builder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                                        for (Surface surface : surfaces) {
                                            builder.addTarget(surface);
                                        }
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
            Toast.makeText(context, "请打开相机权限", Toast.LENGTH_SHORT).show();
            return;
        }
    }
}
