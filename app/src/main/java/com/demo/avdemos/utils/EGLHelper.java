package com.demo.avdemos.utils;

import android.graphics.SurfaceTexture;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.util.Size;
import android.view.Surface;

import static android.opengl.EGL14.*;
import static android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES;

/**
 * Created by wangyt on 2019/12/19
 */
public class EGLHelper {
    private EGLDisplay eglDisplay;
    private EGLConfig eglConfig;
    private EGLContext eglContext;
    private EGLSurface eglSurface;

    public EGLHelper() {
    }

    public EGLContext getEglContext() {
        return eglContext;
    }

    public EGLDisplay getEglDisplay() {
        return eglDisplay;
    }

    public EGLSurface getEglSurface() {
        return eglSurface;
    }

    public void prepareEGLEnvrionment() {
        prepareEGLEnvrionment(null);
    }

    public void prepareEGLEnvrionment(EGLContext context) {
        initEglDisplay();
        initEGLConfig();
        initEGLContext(context);
    }

    private void initEglDisplay() {
        eglDisplay = eglGetDisplay(EGL_DEFAULT_DISPLAY);
        if (eglDisplay == EGL_NO_DISPLAY) {
            throw new RuntimeException("egl error:" + eglGetError());
        }

        int[] version = new int[2];
        if (!eglInitialize(eglDisplay, version, 0, version, 1)) {
            throw new RuntimeException("egl error:" + eglGetError());
        }
    }

    private void initEGLConfig() {
        int[] configAttribList = {
                EGL_BUFFER_SIZE, 32,
                EGL_ALPHA_SIZE, 8,
                EGL_BLUE_SIZE, 8,
                EGL_GREEN_SIZE, 8,
                EGL_RED_SIZE, 8,
                EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
                EGL_SURFACE_TYPE, EGL_WINDOW_BIT,
                EGL_NONE
        };

        int[] configNum = new int[1];
        EGLConfig[] configs = new EGLConfig[1];
        if (!eglChooseConfig(eglDisplay,
                configAttribList, 0,
                configs, 0, configs.length,
                configNum, 0)) {
            throw new RuntimeException("egl error:" + eglGetError());
        }
        eglConfig = configs[0];
    }

    private void initEGLContext(EGLContext context) {
        //创建ELG上下文
        int[] contextAttribList = {
                EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL_NONE
        };
        eglContext = eglCreateContext(eglDisplay, eglConfig, context == null ? EGL_NO_CONTEXT : context, contextAttribList, 0);
        if (eglContext == EGL_NO_CONTEXT) {
            throw new RuntimeException("egl error:" + eglGetError());
        }
    }

    public void setEGLSurface(Surface surface) {
        int[] surfaceAttribList = {EGL_NONE};
        eglSurface = eglCreateWindowSurface(eglDisplay, eglConfig, surface, surfaceAttribList, 0);
    }

    public void makeCurrent(){
        eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext);
    }

    public void swapBuffers(){
        eglSwapBuffers(eglDisplay, eglSurface);
    }

    public void destroyEGL() {
        eglMakeCurrent(eglDisplay, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
        eglDestroySurface(eglDisplay, eglSurface);
        eglDestroyContext(eglDisplay, eglContext);
    }
}
