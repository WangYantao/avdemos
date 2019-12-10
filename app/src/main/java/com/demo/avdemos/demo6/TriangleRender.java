package com.demo.avdemos.demo6;

import com.demo.avdemos.R;
import com.demo.avdemos.utils.GLUtil;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import static android.opengl.GLES30.*;

/**
 * Created by wangyt on 2019/12/10
 */
public class TriangleRender extends BaseRender {

    private static final String ATT_DES_POSITION = "aPosition";
    private static final String ATT_DES_COLOR = "aColor";
    private static final String UNIFORM_DES_MATRIX = "uMatrix";

    private static final float[] VERTEX_ATTRIBS = {
            //X Y Z R G B A
            0.0f, 1f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f,
            -1f, -1f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f,
            1f, -1f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f};
    private static final int ATT_BUFFER_SIZE_POSITION = 3;
    private static final int ATT_BUFFER_SIZE_COLOR = 4;
    private static final int ATT_BUFFER_STRID =
            (ATT_BUFFER_SIZE_POSITION + ATT_BUFFER_SIZE_COLOR) * GLUtil.BYTES_PER_FLOAT;


    private static final short[] VERTEX_INDEX = {0, 1, 2};


    public FloatBuffer vertexAttBuffer;
    public ShortBuffer vertexIndexBuffer;

    @Override
    public void prepareDatas() {
        vertexAttBuffer = GLUtil.floatArray2buffer(VERTEX_ATTRIBS);
        vertexIndexBuffer = GLUtil.shotArray2buffer(VERTEX_INDEX);
    }

    @Override
    public int getVertexShaderResId() {
        return R.raw.shader_vertex_triangle;
    }

    @Override
    public int getFragmentShaderResId() {
        return R.raw.shader_fragment_triangle;
    }

    @Override
    public String getVertexUniMatrixName() {
        return UNIFORM_DES_MATRIX;
    }

    @Override
    public void doDraw() {
        GLUtil.processVertexAtt(program, ATT_DES_POSITION, vertexAttBuffer, GL_FLOAT, 0, ATT_BUFFER_SIZE_POSITION, ATT_BUFFER_STRID);
        GLUtil.processVertexAtt(program, ATT_DES_COLOR, vertexAttBuffer, GL_FLOAT, ATT_BUFFER_SIZE_POSITION, ATT_BUFFER_SIZE_COLOR, ATT_BUFFER_STRID);

        glDrawElements(GL_TRIANGLES, VERTEX_INDEX.length, GL_UNSIGNED_SHORT, vertexIndexBuffer);

        GLUtil.disableVertexAtt(program, ATT_DES_POSITION);
        GLUtil.disableVertexAtt(program, ATT_DES_COLOR);
    }
}
