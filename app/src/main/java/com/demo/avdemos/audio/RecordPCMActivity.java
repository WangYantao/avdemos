package com.demo.avdemos.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.demo.avdemos.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class RecordPCMActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnStart, btnStop;

    AudioRecord audioRecord = null;
    int bufferSize = 0;
    boolean isRecording = false;

    private static final int AUDIO_SAMPLE_RATE = 44100;
    private static final int AUDIO_CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORAMT = AudioFormat.ENCODING_PCM_16BIT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_pcm);

        initRecoder();

        btnStart = findViewById(R.id.btnStart);
        btnStart.setOnClickListener(this);

        btnStop = findViewById(R.id.btnStop);
        btnStop.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnStart:
                doStart();
                break;

            case R.id.btnStop:
                doStop();
                break;
        }
    }

    private void initRecoder(){
        bufferSize = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE, AUDIO_CHANNEL, AUDIO_FORAMT);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, AUDIO_SAMPLE_RATE, AUDIO_CHANNEL, AUDIO_FORAMT, bufferSize);
    }

    private void doStart(){
        audioRecord.startRecording();
        isRecording = true;
        startRecordThread();
    }

    private void doStop(){
        isRecording = false;
        audioRecord.stop();
    }

    private void startRecordThread(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String filePath = Environment.getExternalStorageDirectory() + "/avdemos/001.pcm";
                byte[] data = new byte[bufferSize];
                FileOutputStream os = null;
                try {
                    os = new FileOutputStream(filePath);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                if (os !=null){
                    while (isRecording){
                        int ret = audioRecord.read(data, 0, bufferSize);
                        if (ret != AudioRecord.ERROR_INVALID_OPERATION){
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
        if (audioRecord != null){
            if (isRecording){
                doStop();
            }
            audioRecord.release();
            audioRecord = null;
        }
        super.onDestroy();
    }
}
