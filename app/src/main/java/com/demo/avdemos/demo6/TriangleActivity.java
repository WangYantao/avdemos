package com.demo.avdemos.demo6;

import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.demo.avdemos.R;

public class TriangleActivity extends GLSVActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public GLSurfaceView.Renderer getRender() {
        return new TriangleRender();
    }
}
