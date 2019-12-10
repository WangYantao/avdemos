package com.demo.avdemos.utils;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import static android.opengl.GLES30.*;
import static android.opengl.Matrix.orthoM;

/**
 * Created by wangyt on 2019/12/9
 */
public class GLUtil {
    private static final String TAG = "GLUtil";

    public static final int BYTES_PER_FLOAT = 4;
    public static final int BYTES_PER_SHORT = 2;

    public static Context context;

    public static void init(Context ctx){
        context = ctx.getApplicationContext();
    }

    public static void checkGLError(String operation){
        int error;
        while ((error = glGetError()) != GL_NO_ERROR){
            LogUtil.e("checkGLError:" + operation + "--" + error);
            throw new RuntimeException(operation + " error:" + error);
        }
    }

    public static String loadShaderRes(int resId){
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(resId)));
        String nextLine;
        try {
            while ((nextLine = reader.readLine()) != null){
                sb.append(nextLine);
                sb.append('\n');
            }
        }catch (IOException e){
            LogUtil.e("loadShaderRes error");
            e.printStackTrace();
        }

        return sb.toString();
    }

    public static int combileShader(int type, String shaderRes){
        int shader = glCreateShader(type);
        if (shader != 0){
            glShaderSource(shader, shaderRes);

            glCompileShader(shader);
            int[] status = new int[1];
            glGetShaderiv(shader, GL_COMPILE_STATUS, status, 0);
            if (status[0] == 0){
                LogUtil.e("combileShader error: type = " + type + "; info = " + glGetShaderInfoLog(shader));
                glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }

    public static int createProgram(int vertexShaderResId, int fragmentShaderResId){
        int vertexShader = combileShader(GL_VERTEX_SHADER, loadShaderRes(vertexShaderResId));
        if (vertexShader == 0){
            LogUtil.e("combileShader error: vertexShader");
            return 0;
        }
        int fragmentShader = combileShader(GL_FRAGMENT_SHADER, loadShaderRes(fragmentShaderResId));
        if (fragmentShader == 0){
            LogUtil.e("combileShader error: fragmentShader");
            return 0;
        }

        int program = glCreateProgram();
        if (program != 0){
            glAttachShader(program, vertexShader);
            checkGLError("glAttachShader:vertex shader");

            glAttachShader(program, fragmentShader);
            checkGLError("glAttachShader:fragment shader");

            glLinkProgram(program);
            int[] linkStatus = new int[1];
            glGetProgramiv(program, GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GL_TRUE){
                LogUtil.e("link program error: " + glGetProgramInfoLog(program));
                glDeleteProgram(program);
                program = 0;
            }
        }
        return program;
    }

    public static FloatBuffer floatArray2buffer(float[] array){
        FloatBuffer buffer = ByteBuffer.allocateDirect(array.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        buffer.put(array).position(0);
        return buffer;
    }

    public static ShortBuffer shotArray2buffer(short[] array){
        ShortBuffer buffer = ByteBuffer.allocateDirect(array.length * BYTES_PER_SHORT)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer();
        buffer.put(array).position(0);
        return buffer;
    }

    public static void processVertexAtt(int program,
                                     String attPosDes,
                                     FloatBuffer attBuffer,
                                     int attBufferType,
                                     int attBufferStart,
                                     int attBufferSize,
                                     int strid){
        int attPos = glGetAttribLocation(program, attPosDes);
        attBuffer.position(attBufferStart);
        glVertexAttribPointer(attPos,
                attBufferSize,
                attBufferType,
                false,
                strid,
                attBuffer);
        glEnableVertexAttribArray(attPos);
    }

    public static void disableVertexAtt(int program, String attPosDes){
        int attPos = glGetAttribLocation(program, attPosDes);
        glDisableVertexAttribArray(attPos);
    }

    public static void setUniformMatrix(int program, String unifomName, float[] matrix){
        int matrixPosition = glGetUniformLocation(program, unifomName);
        glUniformMatrix4fv(matrixPosition, 1, false, matrix, 0);
    }

    public static float[] getOrthoMatrix(int width, int height){
        float[] orthoM = new float[16];

        float aspectRatio = width > height ?
                (float)width / (float)height : (float)height / (float)width;
        if (width > height){
            orthoM(orthoM, 0, -aspectRatio, aspectRatio, -1f, 1f, -1f, 1f);
        }else {
            orthoM(orthoM, 0, -1f, 1f, -aspectRatio, aspectRatio, -1f, 1f);
        }

        return orthoM;
    }
}
