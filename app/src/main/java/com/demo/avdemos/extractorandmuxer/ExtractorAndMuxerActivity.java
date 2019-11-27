package com.demo.avdemos.extractorandmuxer;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.demo.avdemos.R;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ExtractorAndMuxerActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnExtractor;

    String mp4FilePath = null;
    String h264FilePath = null;
    String aacFilePath = null;
    String muxerMp4FilePath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extractor_and_muxer);

        initData();

        btnExtractor = findViewById(R.id.btnExtractor);
        btnExtractor.setOnClickListener(this);
    }

    private void initData(){
        mp4FilePath = Environment.getExternalStorageDirectory() + "/avdemos/001.mp4";
        h264FilePath = Environment.getExternalStorageDirectory() + "/avdemos/001-video.h264";
        aacFilePath = Environment.getExternalStorageDirectory() + "/avdemos/001-audio.aac";
        muxerMp4FilePath = Environment.getExternalStorageDirectory() + "/avdemos/001-muxer.mp4";
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnExtractor:
                extractorAndMuxer();
                break;
        }
    }

    private void extractorAndMuxer(){
        MediaExtractor mediaExtractor = new MediaExtractor();

        MediaMuxer mediaMuxer = null;
        int framebrate = 0;
        int muxerVideoTrackIndex = 0;

        FileOutputStream h264os = null;
        FileOutputStream aacos = null;
        try {
            h264os = new FileOutputStream(h264FilePath);
            aacos = new FileOutputStream(aacFilePath);

            mediaExtractor.setDataSource(mp4FilePath);

            int trackCount = mediaExtractor.getTrackCount();
            int videoTrackIndex = -1, audioTrackIndex = -1;
            for (int i = 0; i < trackCount; i++){
                MediaFormat mediaFormat = mediaExtractor.getTrackFormat(i);
                String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
                if (mime.startsWith("video/")){
                    videoTrackIndex = i;

                    framebrate = mediaFormat.getInteger(MediaFormat.KEY_FRAME_RATE);
                    mediaMuxer = new MediaMuxer(muxerMp4FilePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                    muxerVideoTrackIndex = mediaMuxer.addTrack(mediaFormat);
                    mediaMuxer.start();
                }
                if (mime.startsWith("audio/")){
                    audioTrackIndex = i;
                }
            }

            MediaCodec.BufferInfo mediaCodecBufferInfo = new MediaCodec.BufferInfo();
            mediaCodecBufferInfo.presentationTimeUs = 0;

            ByteBuffer byteBuffer = ByteBuffer.allocate(500 * 1024);
            mediaExtractor.selectTrack(videoTrackIndex);
            int sampleSize = 0;
            while ((sampleSize = mediaExtractor.readSampleData(byteBuffer, 0)) > 0){
                mediaCodecBufferInfo.offset = 0;
                mediaCodecBufferInfo.size = sampleSize;
                mediaCodecBufferInfo.flags = MediaCodec.BUFFER_FLAG_KEY_FRAME;
                mediaCodecBufferInfo.presentationTimeUs += 1000 * 1000 / framebrate;
                mediaMuxer.writeSampleData(muxerVideoTrackIndex, byteBuffer, mediaCodecBufferInfo);

                byte[] bytes = new byte[sampleSize];
                byteBuffer.get(bytes);
                h264os.write(bytes);
                byteBuffer.clear();
                mediaExtractor.advance();
            }

            mediaExtractor.selectTrack(audioTrackIndex);
            sampleSize = 0;
            while ((sampleSize = mediaExtractor.readSampleData(byteBuffer, 0)) > 0){
                byte[] bytes = new byte[sampleSize];
                byteBuffer.get(bytes);
                byte[] aacbytes = new byte[sampleSize + 7];
                addADTStoPacket(aacbytes, sampleSize + 7);
                System.arraycopy(bytes, 0, aacbytes, 7, sampleSize);
                aacos.write(aacbytes);
                byteBuffer.clear();
                mediaExtractor.advance();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            mediaMuxer.stop();
            mediaMuxer.release();

            mediaExtractor.release();
            mediaExtractor = null;
            try {
                if (h264os != null) {
                    h264os.close();
                }
                if (aacos != null){
                    aacos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void addADTStoPacket(byte[] packet, int packetLen) {
        int profile = 2; // AAC LC
        int freqIdx = getFreqIdx(44100);
        int chanCfg = 2; // CPE

        // fill in ADTS data
        packet[0] = (byte) 0xFF;
        packet[1] = (byte) 0xF9;
        packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
        packet[3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;
    }

    private static int getFreqIdx(int sampleRate) {
        int freqIdx;

        switch (sampleRate) {
            case 96000:
                freqIdx = 0;
                break;
            case 88200:
                freqIdx = 1;
                break;
            case 64000:
                freqIdx = 2;
                break;
            case 48000:
                freqIdx = 3;
                break;
            case 44100:
                freqIdx = 4;
                break;
            case 32000:
                freqIdx = 5;
                break;
            case 24000:
                freqIdx = 6;
                break;
            case 22050:
                freqIdx = 7;
                break;
            case 16000:
                freqIdx = 8;
                break;
            case 12000:
                freqIdx = 9;
                break;
            case 11025:
                freqIdx = 10;
                break;
            case 8000:
                freqIdx = 11;
                break;
            case 7350:
                freqIdx = 12;
                break;
            default:
                freqIdx = 8;
                break;
        }

        return freqIdx;
    }
}
