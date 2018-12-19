package com.example.sound_recorder;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;

import android.graphics.Typeface;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import android.support.v4.app.FragmentTransaction;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;

import android.widget.TextView;


public class MainActivity extends AppCompatActivity {
    private boolean permissionToRecordAccepted = false;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private String [] permissions = { Manifest.permission.RECORD_AUDIO};
    private String mFileGetAbsolutePath;
    public static Typeface tp;
    TextView time=null;

    TextView RECORDER=null;
    TextView SAVE=null;


    private FragmentManager fragmentManager;

    private static Context ctx;
    public static MySql mySql;

    public static Context getCtx() {
        return ctx;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();

    }

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();


        tp=Typeface.createFromAsset(this.getAssets(),"fonttype.ttf");
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        ctx=MainActivity.this;
        mySql = new MySql(getCtx(), 1);
        mFileGetAbsolutePath = getExternalCacheDir().getAbsolutePath();
//        mFileName += DateFormat.format("yyyyMMdd_HHmmss", Calendar.getInstance(Locale.CHINA))+".3gp";
        RFragment(mFileGetAbsolutePath);

        RECORDER=(TextView) findViewById(R.id.tv_record);
        SAVE=(TextView)findViewById(R.id.tv_save);
        RECORDER.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RFragment(mFileGetAbsolutePath);
            }
        });
        SAVE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new FragmentSaved());
            }
        });

    }

    private void RFragment(String FileGetAbsolutePath){
        fragmentManager=getSupportFragmentManager();
        FragmentTransaction transaction=fragmentManager.beginTransaction();
        final FragmentRecoder fragmentRecoder=new FragmentRecoder();
        Bundle bundle=new Bundle();
        bundle.putString("FileGetAbsolutePath",FileGetAbsolutePath);
        fragmentRecoder.setArguments(bundle);
        transaction.add(R.id.mfrag,fragmentRecoder);
        transaction.replace(R.id.mfrag,fragmentRecoder);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void replaceFragment(Fragment fragment){
        FragmentManager Manager=getSupportFragmentManager();
        FragmentTransaction transaction=Manager.beginTransaction();
        transaction.replace(R.id.mfrag,fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }


}
