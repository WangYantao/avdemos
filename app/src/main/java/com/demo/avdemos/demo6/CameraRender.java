package com.demo.avdemos.demo6;

import android.graphics.SurfaceTexture;
import android.util.Size;

import com.demo.avdemos.R;
import com.demo.avdemos.utils.GLUtil;

import java.nio.FloatBuffer;

import static android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
import static android.opengl.GLES30.*;

/**
 * Created by wangyt on 2019/12/10
 */
public class CameraRender extends BaseRender {

    public interface SurfaceStatedListener{
        void onSurfaceCreated();
    }

    private static final String ATT_DES_VERTEX_ATT = "aPosition";
    private static final String ATT_DES_TEX_COORD = "aTexCoord";
    private static final String UNIFORM_DES_MATRIX = "uMatrix";
    private static final String UNIFORM_DES_TEX = "uSeoesTex";

    private static final int ATT_BUFFER_SIZE_VERTEX_ATT = 3;
    private static final int ATT_BUFFER_SIZE_TEX_COORD = 2;

    public static float[] VERTEX_ATT ={
            -1f,1f,0.0f,
            -1f,-1f,0.0f,
            1f,-1f,0.0f,
            1f,1f,0.0f
    };

    //纹理坐标，（s,t），t坐标方向和顶点y坐标反着
    public static float[] TEX_COORD = {
            //第一步
//            0.0f,1.0f,
//            0.0f,0.0f,
//            1.0f,0.0f,
//            1.0f,1.0f
           //第二步：t方向翻转
//            0.0f,0.0f,
//            0.0f,1.0f,
//            1.0f,1.0f,
//            1.0f,0.0f
            //第三步：这里使用后置相机，需要顺时针旋转90度
            0.0f,1.0f,
            1.0f,1.0f,
            1.0f,0.0f,
            0.0f,0.0f
    };

    private FloatBuffer vertexAttBuffer;
    private FloatBuffer texCoordBuffer;

    private SurfaceStatedListener surfaceStatedListener;
    private int inputTexture;
    private SurfaceTexture inputSurfaceTexture;

    private Size textureSize;

    public SurfaceTexture getInputSurfaceTexture(){
        return inputSurfaceTexture;
    }

    public void setSurfaceStatedListener(SurfaceStatedListener listener){
        this.surfaceStatedListener = listener;
    }

    public void setTextureSize(Size textureSize) {
        this.textureSize = textureSize;
    }

    private void fixTexCoordWithTextureSize(){
        if (textureSize == null){
            return;
        }

        float ratio = (float) textureSize.getHeight() / (float) textureSize.getWidth();

        TEX_COORD[2] = ratio;
        TEX_COORD[4] = ratio;

        vertexAttBuffer = GLUtil.floatArray2buffer(VERTEX_ATT);
        texCoordBuffer = GLUtil.floatArray2buffer(TEX_COORD);
    }

    @Override
    public void prepareDatas() {

    }

    @Override
    public int getVertexShaderResId() {
        return R.raw.shader_vertex_tex;
    }

    @Override
    public int getFragmentShaderResId() {
        return R.raw.shader_fragment_tex_camera;
    }

    @Override
    public void doOnSurfaceCreated() {
        super.doOnSurfaceCreated();
        inputTexture = GLUtil.createTexture();
        GLUtil.setTexParams(GL_TEXTURE_EXTERNAL_OES, inputTexture);
        inputSurfaceTexture = new SurfaceTexture(inputTexture);

        if (surfaceStatedListener != null){
            surfaceStatedListener.onSurfaceCreated();
        }
    }

    @Override
    public void doOnSurfaceChanged(int width, int height) {
        super.doOnSurfaceChanged(width, height);

        fixTexCoordWithTextureSize();
    }

    @Override
    public String getVertexUniMatrixName() {
        return UNIFORM_DES_MATRIX;
    }

    @Override
    public void doDraw() {
        if (inputSurfaceTexture != null){
            inputSurfaceTexture.updateTexImage();
        }

        GLUtil.processVertexAtt(program, ATT_DES_VERTEX_ATT, vertexAttBuffer, GL_FLOAT, 0, ATT_BUFFER_SIZE_VERTEX_ATT, 0);
        GLUtil.processVertexAtt(program, ATT_DES_TEX_COORD, texCoordBuffer, GL_FLOAT, 0, ATT_BUFFER_SIZE_TEX_COORD, 0);

        int texUnitIndex = 1;
        GLUtil.setUniformTexture(program, UNIFORM_DES_TEX, texUnitIndex, GL_TEXTURE_EXTERNAL_OES, inputTexture);

        glDrawArrays(GL_TRIANGLE_FAN, 0, VERTEX_ATT.length / 3);

        GLUtil.disableVertexAtt(program, ATT_DES_VERTEX_ATT);
        GLUtil.disableVertexAtt(program, ATT_DES_TEX_COORD);

        GLUtil.unbindTexture(GL_TEXTURE_EXTERNAL_OES);
    }
}
