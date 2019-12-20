package com.demo.avdemos.demo6.egl;

import android.opengl.EGLContext;
import android.opengl.Matrix;

import com.demo.avdemos.R;
import com.demo.avdemos.utils.GLUtil;

import java.nio.FloatBuffer;

import static android.opengl.GLES30.*;

/**
 * Created by wangyt on 2019/12/20
 */
public class TextureEGLRender extends BaseEGLRender{
    private static final String ATT_DES_VERTEX_ATT = "aPosition";
    private static final String ATT_DES_TEX_COORD = "aTexCoord";
    private static final String UNIFORM_DES_MATRIX = "uMatrix";
    private static final String UNIFORM_DES_TEX = "uS2dTex";

    private static final int ATT_BUFFER_SIZE_VERTEX_ATT = 3;
    private static final int ATT_BUFFER_SIZE_TEX_COORD = 2;

    public static final float[] VERTEX_ATT ={
            -1f,1f,0.0f,
            -1f,-1f,0.0f,
            1f,-1f,0.0f,
            1f,1f,0.0f
    };

    //纹理坐标，（s,t），t坐标方向和顶点y坐标反着
    public static final float[] TEX_COORD = {
            0.0f,0.0f,
            0.0f,1.0f,
            1.0f,1.0f,
            1.0f,0.0f
    };

    private FloatBuffer vertexAttBuffer;
    private FloatBuffer texCoordBuffer;

    public int program;
    public float[] finalMatrix = new float[16];

    public TextureEGLRender(String name) {
        super(name);
    }

    public TextureEGLRender(String name, EGLContext eglContext) {
        super(name, eglContext);
    }

    @Override
    public void onSurfaceCreated() {
        program = GLUtil.createProgram(R.raw.shader_vertex_tex, R.raw.shader_fragment_tex);
        glClearColor(0.0f, 0.5f, 0.5f, 1.0f);

        vertexAttBuffer = GLUtil.floatArray2buffer(VERTEX_ATT);
        texCoordBuffer = GLUtil.floatArray2buffer(TEX_COORD);
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        glViewport(0, 0, width, height);

        float[] projecMatrix = GLUtil.getOrthoMatrix(width, height);
        float[] lookAtMatrix = GLUtil.getLookAtMatrix();
        Matrix.multiplyMM(finalMatrix, 0, projecMatrix, 0, lookAtMatrix, 0);
    }

    @Override
    public void onDrawFrame() {
        glClear(GL_COLOR_BUFFER_BIT);
        glUseProgram(program);

        GLUtil.setUniformMatrix(program, UNIFORM_DES_MATRIX, finalMatrix);

        int texUnitIndex = 1;

        GLUtil.processVertexAtt(program, ATT_DES_VERTEX_ATT, vertexAttBuffer, GL_FLOAT, 0, ATT_BUFFER_SIZE_VERTEX_ATT, 0);
        GLUtil.processVertexAtt(program, ATT_DES_TEX_COORD, texCoordBuffer, GL_FLOAT, 0, ATT_BUFFER_SIZE_TEX_COORD, 0);

        GLUtil.setUniformTexture(program, UNIFORM_DES_TEX, texUnitIndex, GL_TEXTURE_2D, GLUtil.loadTextureFromRes(R.drawable.texture));

        glDrawArrays(GL_TRIANGLE_FAN, 0, VERTEX_ATT.length / 3);

        GLUtil.disableVertexAtt(program, ATT_DES_VERTEX_ATT);
        GLUtil.disableVertexAtt(program, ATT_DES_TEX_COORD);

        GLUtil.unbindTexture(GL_TEXTURE_2D);
    }
}
