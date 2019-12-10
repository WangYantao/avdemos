package com.demo.avdemos.demo6;

import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.demo.avdemos.R;

public class TriangleActivity extends AppCompatActivity {

    private static final int GL_VERSION = 3;

    GLSurfaceView glsv;
    GLSurfaceView.Renderer render;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_triangle);

        initRender();

        initView();
    }

    private void initRender(){
        render = new TriangleRender();
    }

    private void initView(){
        glsv = findViewById(R.id.glsv);
        glsv.setEGLContextClientVersion(GL_VERSION);
        glsv.setRenderer(render);
    }

    @Override
    protected void onResume() {
        super.onResume();

        glsv.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        glsv.onPause();
    }
}
