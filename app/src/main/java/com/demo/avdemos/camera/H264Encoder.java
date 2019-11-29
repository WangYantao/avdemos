package com.demo.avdemos.camera;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.media.Image;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by wangyt on 2019/11/28
 */
public class H264Encoder {

    //另一种方案是使用 Surface 作为 input，需要结合 opengles EGL 使用，参考：https://bigflake.com/mediacodec/
    //Image MediaCodec YUV_420_888 COLOR_FormatYUV420Flexible 等知识，参考：https://www.polarxiong.com/category/Android/

//    I420: YYYYYYYY UU VV    =>YUV420P
//    YV12: YYYYYYYY VV UU    =>YUV420P
//    NV12: YYYYYYYY UVUV     =>YUV420SP
//    NV21: YYYYYYYY VUVU     =>YUV420SP

    private static final String TAG = "H264Encoder";

    public static final int COLOR_FormatI420 = 1;
    public static final int COLOR_FormatNV21 = 2;

    private MediaCodec mediaCodec;
    private MediaFormat mediaFormat;
    byte[] configData;

    private BufferedOutputStream outputStream;
    private ArrayBlockingQueue<byte[]> yuv420queue = new ArrayBlockingQueue<>(10);
    private int width, height, framerate;
    private String mp4FilePath;

    public H264Encoder(int width, int height, int framerate) {
        this.width = width;
        this.height = height;
        this.framerate = framerate;

        mp4FilePath = Environment.getExternalStorageDirectory() + "/avdemos/002.mp4";

        mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, width * height * framerate * 5);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, framerate);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2);
    }

    public void putYuv420Data(byte[] data) {
        if (yuv420queue.size() >= 10) {
            yuv420queue.poll();
        }
        yuv420queue.add(data);
    }

    public void startEncoder() {
        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(mp4FilePath));

            mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
            mediaCodec.setCallback(new MediaCodec.Callback() {
                @Override
                public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {
                    ByteBuffer inBuffer = codec.getInputBuffer(index);
                    inBuffer.clear();
                    byte[] data = new byte[0];
                    if (yuv420queue.size() > 0) {
                        data = yuv420queue.poll();
                    }
                    inBuffer.put(data);
                    Log.e(TAG, "-----------onInputBufferAvailable: data length = " + data.length);
                    codec.queueInputBuffer(index, 0, data.length, System.nanoTime() / 1000, 0);
                }

                @Override
                public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {
                    Log.e(TAG, "-----------onOutputBufferAvailable: offset = " + info.offset + "; size = " + info.size);
                    try {
                        ByteBuffer outBuffer = codec.getOutputBuffer(index);
                        byte[] outData = new byte[info.size];
                        outBuffer.get(outData);

                        if (info.flags == MediaCodec.BUFFER_FLAG_CODEC_CONFIG) {
                            configData = new byte[info.size];
                            configData = outData;
                        } else if (info.flags == MediaCodec.BUFFER_FLAG_KEY_FRAME) {
                            byte[] keyFrameData = new byte[info.size + configData.length];
                            System.arraycopy(configData, 0, keyFrameData, 0, configData.length);
                            System.arraycopy(outData, 0, keyFrameData, configData.length, outData.length);
                            outputStream.write(keyFrameData, 0, keyFrameData.length);

                        } else {
                            outputStream.write(outData, 0, outData.length);
                        }

                        codec.releaseOutputBuffer(index, false);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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
            mediaCodec.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopEncoder() {
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

    public static byte[] getDataFromImage(Image image, int colorFormat) {
        Rect crop = image.getCropRect();
        int format = image.getFormat();
        int width = crop.width();
        int height = crop.height();
        Image.Plane[] planes = image.getPlanes();
        byte[] data = new byte[width * height * ImageFormat.getBitsPerPixel(format) / 8];
        byte[] rowData = new byte[planes[0].getRowStride()];
        int channelOffset = 0;
        int outputStride = 1;
        for (int i = 0; i < planes.length; i++) {
            switch (i) {
                case 0:
                    channelOffset = 0;
                    outputStride = 1;
                    break;
                case 1:
                    if (colorFormat == COLOR_FormatI420) {
                        channelOffset = width * height;
                        outputStride = 1;
                    } else if (colorFormat == COLOR_FormatNV21) {
                        channelOffset = width * height + 1;
                        outputStride = 2;
                    }
                    break;
                case 2:
                    if (colorFormat == COLOR_FormatI420) {
                        channelOffset = (int) (width * height * 1.25);
                        outputStride = 1;
                    } else if (colorFormat == COLOR_FormatNV21) {
                        channelOffset = width * height;
                        outputStride = 2;
                    }
                    break;
            }
            ByteBuffer buffer = planes[i].getBuffer();
            int rowStride = planes[i].getRowStride();
            int pixelStride = planes[i].getPixelStride();

            int shift = (i == 0) ? 0 : 1;
            int w = width >> shift;
            int h = height >> shift;
            buffer.position(rowStride * (crop.top >> shift) + pixelStride * (crop.left >> shift));
            for (int row = 0; row < h; row++) {
                int length;
                if (pixelStride == 1 && outputStride == 1) {
                    length = w;
                    buffer.get(data, channelOffset, length);
                    channelOffset += length;
                } else {
                    length = (w - 1) * pixelStride + 1;
                    buffer.get(rowData, 0, length);
                    for (int col = 0; col < w; col++) {
                        data[channelOffset] = rowData[col * pixelStride];
                        channelOffset += outputStride;
                    }
                }
                if (row < h - 1) {
                    buffer.position(buffer.position() + rowStride - length);
                }
            }
        }
        return data;
    }
}
