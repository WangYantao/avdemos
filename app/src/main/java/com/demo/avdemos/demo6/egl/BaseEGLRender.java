package com.demo.avdemos.demo6.egl;

import android.opengl.EGLContext;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.view.Surface;

import com.demo.avdemos.utils.EGLHelper;
import com.demo.avdemos.utils.LogUtil;

/**
 * Created by wangyt on 2019/12/19
 */
public abstract class BaseEGLRender extends HandlerThread {

    public interface RenderStateListener {
        void onSurfaceCreated();

        void onSurfaceChanged(int width, int height);

        void onBeiginDrawFrame();
    }

    public static final String BUNDLE_NAME_VIEW_PORT_SURFACE = "BUNDLE_NAME_VIEW_PORT_SURFACE";
    public static final String BUNDLE_NAME_VIEW_PORT_WIDTH = "BUNDLE_NAME_VIEW_PORT_WIDTH";
    public static final String BUNDLE_NAME_VIEW_PORT_HEIGHT = "BUNDLE_NAME_VIEW_PORT_HEIGHT";

    public static final int MESSAGE_PREPARE_EGL_ENVIRONMENT = 100000;
    public static final int MESSAGE_CREATE_SURFACE = 100001;
    public static final int MESSAGE_CHANGE_SURFACE = 100002;
    public static final int MESSAGE_DRAW_FRAME = 100003;

    private RenderStateListener renderStateListener;

    private EGLHelper eglHelper;
    private Handler handler;
    private EGLContext eglContextTmp;

    public EGLHelper getEglHelper() {
        return eglHelper;
    }

    public Handler getHandler() {
        return handler;
    }

    public void setRenderStateListener(RenderStateListener renderStateListener) {
        this.renderStateListener = renderStateListener;
    }

    public BaseEGLRender(String name) {
        this(name, null);
    }

    public BaseEGLRender(String name, EGLContext eglContext) {
        super(name);

        this.eglContextTmp = eglContext;
    }

    public void initHandler() {
        handler = new Handler(getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case MESSAGE_PREPARE_EGL_ENVIRONMENT:
                        initEGL(eglContextTmp);
                        break;
                    case MESSAGE_CREATE_SURFACE:
                        doCreateSurface((Surface) msg.getData().getParcelable(BUNDLE_NAME_VIEW_PORT_SURFACE));
                        break;
                    case MESSAGE_CHANGE_SURFACE:
                        doChangeSurface(msg.getData().getInt(BUNDLE_NAME_VIEW_PORT_WIDTH), msg.getData().getInt(BUNDLE_NAME_VIEW_PORT_HEIGHT));
                        break;
                    case MESSAGE_DRAW_FRAME:
                        doDrawFrame();
                        break;
                }
            }
        };
    }

    private void initEGL(EGLContext eglContext) {
        eglHelper = new EGLHelper();
        eglHelper.prepareEGLEnvrionment(eglContext);
    }

    public abstract void onSurfaceCreated();

    private void doCreateSurface(Surface surface) {
        eglHelper.setEGLSurface(surface);
        eglHelper.makeCurrent();

        onSurfaceCreated();

        if(renderStateListener != null){
            renderStateListener.onSurfaceCreated();
        }
    }

    public abstract void onSurfaceChanged(int width, int height);

    private void doChangeSurface(int width, int height) {
        onSurfaceChanged(width, height);

        if(renderStateListener != null){
            renderStateListener.onSurfaceChanged(width, height);
        }
    }

    public abstract void onDrawFrame();

    private void doDrawFrame() {
        if(renderStateListener != null){
            renderStateListener.onBeiginDrawFrame();
        }

        onDrawFrame();

        eglHelper.swapBuffers();
    }

    @Override
    public boolean quitSafely() {
        eglHelper.destroyEGL();

        return super.quitSafely();
    }
}
