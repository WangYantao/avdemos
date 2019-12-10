package com.demo.avdemos.demo6;

import com.demo.avdemos.R;
import com.demo.avdemos.utils.GLUtil;

import java.nio.FloatBuffer;

import static android.opengl.GLES30.*;

/**
 * Created by wangyt on 2019/12/10
 */
public class TextureRender extends BaseRender{

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

    @Override
    public void prepareDatas() {
        vertexAttBuffer = GLUtil.floatArray2buffer(VERTEX_ATT);
        texCoordBuffer = GLUtil.floatArray2buffer(TEX_COORD);
    }

    @Override
    public int getVertexShaderResId() {
        return R.raw.shader_vertex_tex;
    }

    @Override
    public int getFragmentShaderResId() {
        return R.raw.shader_fragment_tex;
    }

    @Override
    public String getVertexUniMatrixName() {
        return UNIFORM_DES_MATRIX;
    }

    @Override
    public void doDraw() {
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
