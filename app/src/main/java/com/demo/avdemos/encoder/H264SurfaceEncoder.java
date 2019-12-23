package com.demo.avdemos.encoder;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.view.Surface;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by wangyt on 2019/11/28
 */
public class H264SurfaceEncoder {

    //另一种方案是使用 Surface 作为 input，需要结合 opengles EGL 使用，参考：https://bigflake.com/mediacodec/
    //Image MediaCodec YUV_420_888 COLOR_FormatYUV420Flexible 等知识，参考：https://www.polarxiong.com/category/Android/

//    I420: YYYYYYYY UU VV    =>YUV420P
//    YV12: YYYYYYYY VV UU    =>YUV420P
//    NV12: YYYYYYYY UVUV     =>YUV420SP
//    NV21: YYYYYYYY VUVU     =>YUV420SP

    private static final String TAG = "H264BufferEncoder";

    private Surface surface;

    private String mp4FilePath;
    private BufferedOutputStream outputStream;

    private MediaCodec mediaCodec;
    private MediaFormat mediaFormat;

    private MediaMuxer mediaMuxer;
    private int muxerVideoTrackIndex;

    private int width, height, framerate;

    public Surface getSurface(){
        return surface;
    }

    public H264SurfaceEncoder(int width, int height, int framerate) {
       this.width = width;
       this.height = height;
       this.framerate = framerate;

       initEncoder();
    }

    private void initEncoder(){
        try {
            mp4FilePath = Environment.getExternalStorageDirectory() + "/avdemos/005.mp4";
            outputStream = new BufferedOutputStream(new FileOutputStream(mp4FilePath));

            mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height);
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, width * height * framerate * 5);
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, framerate);
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 3);

            mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
            mediaCodec.setCallback(new MediaCodec.Callback() {
                @Override
                public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {

                }

                @Override
                public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {
                    if (mediaMuxer == null) {
                        MediaFormat outFormat = codec.getOutputFormat(index);
                        try {
                            mediaMuxer = new MediaMuxer(mp4FilePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                            muxerVideoTrackIndex = mediaMuxer.addTrack(outFormat);
                            mediaMuxer.start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    if (info.flags != MediaCodec.BUFFER_FLAG_CODEC_CONFIG) {
                        ByteBuffer outBuffer = codec.getOutputBuffer(index);
                        mediaMuxer.writeSampleData(muxerVideoTrackIndex, outBuffer, info);
                    }

                    codec.releaseOutputBuffer(index, false);

                }

                @Override
                public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {
                    e.printStackTrace();
                }

                @Override
                public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {

                }
            });
            mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            surface = mediaCodec.createInputSurface();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startEncoder() {
        if (mediaCodec == null){
            initEncoder();
        }
        mediaCodec.start();
    }

    public void stopEncoder() {
        mediaMuxer.stop();
        mediaMuxer.release();

        mediaCodec.stop();
        mediaCodec.release();

        if (outputStream != null) {
            try {
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
