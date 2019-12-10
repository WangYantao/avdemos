package com.demo.avdemos.demo6;

import static android.opengl.GLES30.*;
import static android.opengl.Matrix.orthoM;

import android.opengl.GLES30;
import android.opengl.GLSurfaceView;

import com.demo.avdemos.utils.GLUtil;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by wangyt on 2019/12/9
 */
public abstract class BaseRender implements GLSurfaceView.Renderer {

    public float[] finalMatrix;

    public int program;

    public BaseRender() {
        prepareDatas();
    }

    public abstract void prepareDatas();

    public abstract int getVertexShaderResId();
    public abstract int getFragmentShaderResId();

    public abstract String getVertexUniMatrixName();

    public abstract void doDraw();

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        program = GLUtil.createProgram(getVertexShaderResId(), getFragmentShaderResId());

        glClearColor(0.0f, 0.5f, 0.5f, 1.0f);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glViewport(0, 0, width, height);

        finalMatrix = GLUtil.getOrthoMatrix(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        glClear(GL_COLOR_BUFFER_BIT);
        glUseProgram(program);

        GLUtil.setUniformMatrix(program, getVertexUniMatrixName(), finalMatrix);
        doDraw();
    }
}
