package com.demo.avdemos.demo6.glsv;

import static android.opengl.GLES30.*;

import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.demo.avdemos.utils.GLUtil;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by wangyt on 2019/12/9
 */
public abstract class BaseRender implements GLSurfaceView.Renderer {

    public float[] finalMatrix = new float[16];

    public int program;

    public BaseRender() {
        prepareDatas();
    }

    public abstract void prepareDatas();

    public abstract int getVertexShaderResId();
    public abstract int getFragmentShaderResId();

    public void doOnSurfaceCreated(){

    }

    public void calFinalMatrix(int width, int height){
        float[] projecMatrix = GLUtil.getOrthoMatrix(width, height);
        float[] lookAtMatrix = GLUtil.getLookAtMatrix();
        Matrix.multiplyMM(finalMatrix, 0, projecMatrix, 0, lookAtMatrix, 0);
    }

    public void doOnSurfaceChanged(int width, int height){

    }

    public abstract String getVertexUniMatrixName();

    public abstract void doDraw();

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        program = GLUtil.createProgram(getVertexShaderResId(), getFragmentShaderResId());
        glClearColor(0.0f, 0.5f, 0.5f, 1.0f);

        doOnSurfaceCreated();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glViewport(0, 0, width, height);

        calFinalMatrix(width, height);

        doOnSurfaceChanged(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        glClear(GL_COLOR_BUFFER_BIT);
        glUseProgram(program);

        GLUtil.setUniformMatrix(program, getVertexUniMatrixName(), finalMatrix);
        doDraw();
    }
}
