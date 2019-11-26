package com.demo.avdemos.image;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.ImageView;

import com.demo.avdemos.R;

import java.io.File;

public class ShowImage extends AppCompatActivity {

    ImageView imageView;
    SurfaceView surfaceView;
    CustomView customView;

    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);

        prepareBitmap();

        showImgInImageView();

        showImgInSurfaceView();

        showImgInCustomView();
    }

    private void prepareBitmap(){
        String imgPath = Environment.getExternalStorageDirectory() + "/avdemos/001.png";
        bitmap = BitmapFactory.decodeFile(imgPath);
    }

    private void showImgInImageView(){
        imageView = findViewById(R.id.imageView);
        imageView.setImageBitmap(bitmap);
    }

    private void showImgInSurfaceView(){
        surfaceView = findViewById(R.id.surfaceView);
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (holder == null){
                    return;
                }
                Paint paint = new Paint();
                paint.setAntiAlias(true);
                paint.setStyle(Paint.Style.STROKE);
                Canvas canvas = holder.lockCanvas();
                canvas.drawBitmap(bitmap, 0, 0, paint);
                holder.unlockCanvasAndPost(canvas);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
    }

    private void showImgInCustomView(){
        customView = findViewById(R.id.customView);
        customView.setImage(bitmap);
    }
}
