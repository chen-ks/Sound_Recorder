package com.example.sound_recorder;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

import static com.example.sound_recorder.MainActivity.getCtx;
import static com.example.sound_recorder.MainActivity.mySql;


/**
 * A simple {@link Fragment} subclass.
 */
@SuppressLint("ValidFragment")
public class FragmentRecoder extends Fragment {
    private static final String LOG_TAG = "AudioRecordTest";
    private  String mFileName = null;
    private  String mFileAddress = null;

    private static MediaRecorder mRecorder = null;


    private TextView textTime=null;
    boolean mStartRecording = true;

    int timeCount;                // 录音时长 计数
    final int TIME_COUNT = 0x101;
    public boolean isRecording; //计时状态
    private Thread timeThread;

    private int recLen=0;

    private View view;
    private SQLiteDatabase db;
    private ContentValues values;
    private String FileGetAbsolutePath;
    private String getTime;

    public void onRecord(boolean start) {
        if (start) {
            mFileName= UUID.randomUUID()+".mp3";
            mFileAddress=FileGetAbsolutePath+mFileName;
            isRecording=true;
            startRecording();
            timeThread = new Thread(new Runnable() {
                @Override
                public void run() {
                 //   handler.postDelayed(runnable, 1000);
                    countTime();
                }
            });
            timeThread.start();
        } else {
            db=mySql.getWritableDatabase();
            values=new ContentValues();
            values.put("RName",mFileName);
            values.put("RLength",getTime);
            values.put("RAddress",mFileAddress);
            values.put("RDate",DateFormat.format("yyyyMMdd_HHmmss", Calendar.getInstance(Locale.CHINA))+"");
            db.insert("RTable",null,values);
            db.close();
            isRecording=false;
            stopRecording();
        }
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileAddress);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
        try {
            mRecorder.start();
        } catch (IllegalStateException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

    }

    private void stopRecording() {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
    }

    public static String FormatMiss(int miss) {
        String hh = miss / 3600 > 9 ? miss / 3600 + "" : "0" + miss / 3600;
        String mm = (miss % 3600) / 60 > 9 ? (miss % 3600) / 60 + "" : "0" + (miss % 3600) / 60;
        String ss = (miss % 3600) % 60 > 9 ? (miss % 3600) % 60 + "" : "0" + (miss % 3600) % 60;
        return hh + ":" + mm + ":" + ss;
    }


    Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TIME_COUNT:
                    int count = (int) msg.obj;
                    getTime=FormatMiss(count);
                    textTime.setText(FormatMiss(count));
                    break;
            }
        }
    };


    private void countTime() {
        while (isRecording) {
            timeCount++;
            Message msg = Message.obtain();
            msg.what = TIME_COUNT;
            msg.obj = timeCount;
            myHandler.sendMessage(msg);
            try {
                timeThread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        timeCount = 0;
        Message msg = Message.obtain();
        msg.what = TIME_COUNT;
        msg.obj = timeCount;
        myHandler.sendMessage(msg);
    }

    Handler handler=new Handler();
    Runnable runnable=new Runnable() {
        @Override
        public void run() {
            recLen++;
            textTime.setText(FormatMiss(recLen));
            handler.postDelayed(this, 1000);

        }
    };
    @Override
    public void onStop() {
        super.onStop();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view= inflater.inflate(R.layout.fragment_fragment_recoder, container, false);
        ImageView RecodrerImageView=(ImageView) view.findViewById(R.id.recordImage);
        textTime=view.findViewById(R.id.recordTime);

        /**
         * get Message from Activity
         **/
        Bundle bundle=this.getArguments();
        FileGetAbsolutePath=bundle.getString("FileGetAbsolutePath");

        RecodrerImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRecord(mStartRecording);
                if (mStartRecording) {

                } else {
                }
                mStartRecording = !mStartRecording;
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}
