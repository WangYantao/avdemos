package com.demo.avdemos.demo6.glsv;

import android.opengl.GLSurfaceView;
import android.os.Bundle;

public class TextureActivity extends GLSVActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public GLSurfaceView.Renderer getRender() {
        return new TextureRender();
    }
}
