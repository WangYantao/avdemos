package com.demo.avdemos.demo6.egl;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.demo.avdemos.R;

public class TextureEGLActivity extends AppCompatActivity {

    SurfaceView surfaceView;
    TextureEGLRender textureEGLRender;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_textureegl);

        initRenderAndHandler();

        initView();
    }

    public void initRenderAndHandler(){
        textureEGLRender = new TextureEGLRender("cameraRender");
        textureEGLRender.start();
        textureEGLRender.initHandler();
        handler = textureEGLRender.getHandler();
        Message msg = new Message();
        msg.what = BaseEGLRender.MESSAGE_PREPARE_EGL_ENVIRONMENT;
        handler.sendMessage(msg);
    }

    private void initView(){
        surfaceView = findViewById(R.id.surfaceView);
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (holder == null) return;

                Message msg = new Message();
                msg.what = BaseEGLRender.MESSAGE_CREATE_SURFACE;
                Bundle bundle = new Bundle();
                bundle.putParcelable(BaseEGLRender.BUNDLE_NAME_VIEW_PORT_SURFACE, holder.getSurface());
                msg.setData(bundle);
                handler.sendMessage(msg);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Message msg = new Message();
                msg.what = BaseEGLRender.MESSAGE_CHANGE_SURFACE;
                Bundle bundle = new Bundle();
                bundle.putInt(BaseEGLRender.BUNDLE_NAME_VIEW_PORT_WIDTH, width);
                bundle.putInt(BaseEGLRender.BUNDLE_NAME_VIEW_PORT_HEIGHT, height);
                msg.setData(bundle);
                handler.sendMessage(msg);


                Message msg2 = new Message();
                msg2.what = BaseEGLRender.MESSAGE_DRAW_FRAME;
                handler.sendMessageDelayed(msg2, 1000);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        textureEGLRender.quitSafely();

        super.onDestroy();
    }
}
