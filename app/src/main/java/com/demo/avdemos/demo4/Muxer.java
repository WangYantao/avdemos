package com.demo.avdemos.demo4;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Environment;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by wangyt on 2019/12/3
 */
public class Muxer {

    private static final String TAG = "Muxer";

    public static final int TRACK_VIDEO = 100001;
    public static final int TRACK_AUDIO = 100002;

    private static final int VIDEO_FRAME_RATE = 30;

    public static final int AUDIO_SAMPLE_RATE = 44100;
    private static final int AUDIO_CHANNEL_COUNT = 1;
    private static final int AUDIO_BIT_RATE = AUDIO_SAMPLE_RATE * 8;

    private String mp4FilePath;

    private MediaMuxer mediaMuxer;

    private MediaCodec videoCodec;
    private MediaCodec audioCodec;

    private MediaFormat videoFormat;
    private MediaFormat audioFormat;

    private int videoTrackIndex = -1;
    private int audioTrackIndex = -1;

    private boolean isVideoTrackAdded = false;
    private boolean isAudioTrackAdded = false;

    private boolean isRecording = false;
    private boolean isMuxerStarted = false;

    private LinkedBlockingDeque<MuxerData> videoData;
    private LinkedBlockingDeque<MuxerData> audioData;

    public Muxer(int width, int height) {
        mp4FilePath = Environment.getExternalStorageDirectory() + "/avdemos/003.mp4";

        videoData = new LinkedBlockingDeque<>();
        audioData = new LinkedBlockingDeque<>();

        videoFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height);
        videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
        videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, width * height * VIDEO_FRAME_RATE * 5);
        videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, VIDEO_FRAME_RATE);
        videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 3);

        audioFormat = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, AUDIO_SAMPLE_RATE, AUDIO_CHANNEL_COUNT);
        audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, AUDIO_BIT_RATE);
        audioFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, AUDIO_CHANNEL_COUNT);
        audioFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, AUDIO_SAMPLE_RATE);
    }

    public void start() {
        isRecording = true;

        try {
            mediaMuxer = new MediaMuxer(mp4FilePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

            videoCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
            videoCodec.setCallback(videoCodecCallback);
            videoCodec.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

            audioCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC);
            audioCodec.setCallback(audioCodecCallback);
            audioCodec.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

            videoCodec.start();
            audioCodec.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private MediaCodec.Callback videoCodecCallback = new MediaCodec.Callback() {
        @Override
        public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {
            setInputData(TRACK_VIDEO, codec, index);
        }

        @Override
        public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {
            setOutputData(TRACK_VIDEO, codec, index, info);
        }

        @Override
        public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {

        }

        @Override
        public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {

        }
    };

    private MediaCodec.Callback audioCodecCallback = new MediaCodec.Callback() {
        @Override
        public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {
            setInputData(TRACK_AUDIO, codec, index);
        }

        @Override
        public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {
            setOutputData(TRACK_AUDIO, codec, index, info);
        }

        @Override
        public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {

        }

        @Override
        public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {

        }
    };

    private void setInputData(int trackType, MediaCodec codec, int index) {
        ByteBuffer inBuffer = codec.getInputBuffer(index);
        inBuffer.clear();

        byte[] data = null;
        MuxerData tmp = trackType == TRACK_VIDEO ? videoData.poll() : audioData.poll();
        if (tmp != null) {
            data = tmp.data;
        }

        if (data == null && isRecording) {
            codec.queueInputBuffer(index, 0, 0, System.nanoTime() / 1000, 0);
        }

        if (data != null && isRecording) {
            inBuffer.put(data);
            codec.queueInputBuffer(index, 0, data.length, System.nanoTime() / 1000, 0);
        }

        if (!isRecording) {
            codec.queueInputBuffer(index, 0, data.length, System.nanoTime() / 1000, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
        }
    }

    private void setOutputData(int trackType, MediaCodec codec, int index, MediaCodec.BufferInfo info){
        if (trackType == TRACK_VIDEO){
            if (!isVideoTrackAdded){
                MediaFormat outFormat = codec.getOutputFormat(index);
                videoTrackIndex = mediaMuxer.addTrack(outFormat);
                isVideoTrackAdded = !isVideoTrackAdded;
            }
        }else if (trackType == TRACK_AUDIO){
            if (!isAudioTrackAdded){
                MediaFormat outFormat = codec.getOutputFormat(index);
                audioTrackIndex = mediaMuxer.addTrack(outFormat);
                isAudioTrackAdded = !isAudioTrackAdded;
            }
        }

        if (isVideoTrackAdded && isVideoTrackAdded && !isMuxerStarted){
            mediaMuxer.start();
            isMuxerStarted = true;
        }

        if (isMuxerStarted) {
            int trackIndex = trackType == TRACK_VIDEO ? videoTrackIndex : audioTrackIndex;
            if (info.flags != MediaCodec.BUFFER_FLAG_CODEC_CONFIG) {
                ByteBuffer outBuffer = codec.getOutputBuffer(index);
                mediaMuxer.writeSampleData(trackIndex, outBuffer, info);
            }
        }

        codec.releaseOutputBuffer(index, false);
    }

    public void stop() {
        isRecording = false;

        isMuxerStarted = false;

        isVideoTrackAdded = false;
        isAudioTrackAdded = false;

        if (mediaMuxer != null) {
            mediaMuxer.stop();
            mediaMuxer.release();
        }

        if (videoCodec != null) {
            videoCodec.stop();
            videoCodec.release();
        }

        if (audioCodec != null) {
            audioCodec.stop();
            audioCodec.release();
        }
    }

    public void addMuxerData(MuxerData muxerData) {
        if (isRecording) {
            if (muxerData.trackType == TRACK_VIDEO) {
                if (!videoData.offer(muxerData)) {
                    videoData.poll();
                    videoData.offer(muxerData);
                }
            } else if (muxerData.trackType == TRACK_AUDIO) {
                if (!audioData.offer(muxerData)) {
                    audioData.poll();
                    audioData.offer(muxerData);
                }
            }
        }
    }

    public boolean isRecording() {
        return isRecording;
    }

    public static class MuxerData {
        int trackType;
        byte[] data;

        public MuxerData(int trackType, byte[] data) {
            this.trackType = trackType;
            this.data = data;
        }
    }
}
