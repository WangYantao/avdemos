package com.demo.avdemos.audio;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.demo.avdemos.R;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class RecordAndPlayPCMActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnStart, btnStop, btnAddHeader, btnPlayInStaticMode, btnPlayInStreamMode;

    AudioRecord audioRecord = null;
    int bufferSize = 0;
    boolean isRecording = false;

    private AudioTrack audioTrack = null;

    String pcmFilePath = null;
    String wavFilePath = null;

    private static final int AUDIO_SAMPLE_RATE = 44100;
    private static final int AUDIO_CHANNEL_IN = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_CHANNEL_OUT = AudioFormat.CHANNEL_OUT_MONO;
    private static final int AUDIO_FORAMT = AudioFormat.ENCODING_PCM_16BIT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_and_play_pcm);

        initData();

        initRecoder();

        btnStart = findViewById(R.id.btnStart);
        btnStart.setOnClickListener(this);

        btnStop = findViewById(R.id.btnStop);
        btnStop.setOnClickListener(this);

        btnAddHeader = findViewById(R.id.btnAddHeader);
        btnAddHeader.setOnClickListener(this);

        btnPlayInStaticMode = findViewById(R.id.btnPlayInStaticMode);
        btnPlayInStaticMode.setOnClickListener(this);

        btnPlayInStreamMode = findViewById(R.id.btnPlayInStreamMode);
        btnPlayInStreamMode.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnStart:
                doStart();
                break;

            case R.id.btnStop:
                doStop();
                break;

            case R.id.btnAddHeader:
                addHeaderToPCM();
                break;

            case R.id.btnPlayInStaticMode:
                playInStaticMode();
                break;

            case R.id.btnPlayInStreamMode:
                playInStreamMode();
                break;
        }
    }

    private void initData(){
        pcmFilePath = Environment.getExternalStorageDirectory() + "/avdemos/001.pcm";
        wavFilePath = Environment.getExternalStorageDirectory() + "/avdemos/001.wav";

        bufferSize = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE, AUDIO_CHANNEL_IN, AUDIO_FORAMT);
    }

    private void initRecoder() {
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, AUDIO_SAMPLE_RATE, AUDIO_CHANNEL_IN, AUDIO_FORAMT, bufferSize);
    }

    private void doStart() {
        audioRecord.startRecording();
        isRecording = true;
        startRecordThread();
    }

    private void doStop() {
        isRecording = false;
        audioRecord.stop();
    }

    private void startRecordThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] data = new byte[bufferSize];
                FileOutputStream os = null;
                try {
                    os = new FileOutputStream(pcmFilePath);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                if (os != null) {
                    while (isRecording) {
                        int ret = audioRecord.read(data, 0, bufferSize);
                        if (ret != AudioRecord.ERROR_INVALID_OPERATION) {
                            try {
                                os.write(data);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    try {
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        if (audioRecord != null) {
            if (isRecording) {
                doStop();
            }
            audioRecord.release();
            audioRecord = null;
        }
        super.onDestroy();
    }

    private void addHeaderToPCM() {
        if (isRecording) {
            Toast.makeText(this, "正在录制中，请先停止录制再添加文件头", Toast.LENGTH_SHORT).show();
            return;
        }

        FileInputStream is;
        FileOutputStream os;
        long audioLen, dataLen;
        int channelNum = (AUDIO_CHANNEL_IN == AudioFormat.CHANNEL_IN_MONO ? 1 : 2);
        long byteRate = 16 * AUDIO_SAMPLE_RATE * channelNum / 8;
        byte[] data = new byte[bufferSize];

        try {
            is = new FileInputStream(pcmFilePath);
            os = new FileOutputStream(wavFilePath);
            audioLen = is.getChannel().size();
            dataLen = audioLen + 36;
            writeWaveFileHeader(os, audioLen, dataLen, AUDIO_SAMPLE_RATE, AUDIO_CHANNEL_IN, byteRate);
            while (is.read(data) != -1) {
                os.write(data);
            }
            is.close();
            os.close();
        } catch (FileNotFoundException e) {
            Toast.makeText(this, "请先录制音频再添加文件头", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeWaveFileHeader(FileOutputStream out, long totalAudioLen, long totalDataLen,
                                     long longSampleRate, int channels, long byteRate) throws IOException {
        byte[] header = new byte[44];
        // RIFF/WAVE header
        header[0] = 'R';
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        //WAVE
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        // 'fmt ' chunk
        header[12] = 'f';
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        // 4 bytes: size of 'fmt ' chunk
        header[16] = 16;
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        // format = 1
        header[20] = 1;
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        // block align
        header[32] = (byte) (2 * 16 / 8);
        header[33] = 0;
        // bits per sample
        header[34] = 16;
        header[35] = 0;
        //data
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        out.write(header, 0, 44);
    }

    private void playInStaticMode() {
        releaseAudioTrack();

        startStaticPlayThread();
    }

    private void startStaticPlayThread(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] audioData = null;
                byte[] data = new byte[bufferSize];
                FileInputStream fis;
                try {
                    fis = new FileInputStream(wavFilePath);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    while(fis.read(data) != -1){
                        bos.write(data);
                    }
                    audioData = bos.toByteArray();
                    fis.close();

                    audioTrack = new AudioTrack.Builder()
                            .setAudioAttributes(new AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_MEDIA)
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                                    .build())
                            .setAudioFormat(new AudioFormat.Builder()
                                    .setEncoding(AUDIO_FORAMT)
                                    .setSampleRate(AUDIO_SAMPLE_RATE)
                                    .setChannelMask(AUDIO_CHANNEL_OUT)
                                    .build())
                            .setTransferMode(AudioTrack.MODE_STATIC)
                            .setBufferSizeInBytes(audioData.length)
                            .build();
                    audioTrack.write(audioData, 0, audioData.length);
                    audioTrack.play();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void playInStreamMode(){
        releaseAudioTrack();

        startStreamPlayThread();
    }

    private void startStreamPlayThread(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                audioTrack = new AudioTrack.Builder()
                        .setAudioAttributes(new AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                                .build())
                        .setAudioFormat(new AudioFormat.Builder()
                                .setEncoding(AUDIO_FORAMT)
                                .setSampleRate(AUDIO_SAMPLE_RATE)
                                .setChannelMask(AUDIO_CHANNEL_OUT)
                                .build())
                        .setTransferMode(AudioTrack.MODE_STREAM)
                        .setBufferSizeInBytes(bufferSize)
                        .build();
                audioTrack.play();

                byte[] data = new byte[bufferSize];
                FileInputStream fis;
                try {
                    fis = new FileInputStream(wavFilePath);
                    int readCount = 0;
                    while((readCount = fis.read(data)) != -1){
                        audioTrack.write(data, 0, readCount);
                    }
                    fis.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    protected void onPause() {
        releaseAudioTrack();
        super.onPause();
    }

    private void releaseAudioTrack() {
        if (audioTrack != null) {
            if (audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING
                    || audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                audioTrack.stop();
            }
            audioTrack.release();
        }
    }
}
