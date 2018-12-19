package com.example.sound_recorder;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import static android.content.Context.BIND_AUTO_CREATE;
import static com.example.sound_recorder.FragmentSaved.Mhandler;
import static com.example.sound_recorder.FragmentSaved.binder;
import static com.example.sound_recorder.MainActivity.getCtx;
import static com.example.sound_recorder.MainActivity.mySql;

public class DialogManager extends AppCompatActivity {
    private Dialog mDialog, mDialogAudio;
    private TextView mDialogContent;
    private Button mDialogOkBtn;
    private Button mDialogCancelBtn;
    private Button mDialogPlayCancelBtn;
    private Button mDialogPlayBtn;
    private Button mDialogPauseBtn;
    private Button mDialogShareBtn;
    private Context mDialogContext;
    private SQLiteDatabase db;
    private String GetName,GetAddress;
    private Intent intent;
    public static Handler Playhandler;
    public DialogManager(Context context){
        mDialogContext=context;
    }
    public boolean showDeleteDialog(final String getdata, final String getAddress){
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();
        mDialog=new Dialog(mDialogContext, R.style.Theme_AudioDialog);
        LayoutInflater inflater=LayoutInflater.from(mDialogContext);
        View view=inflater.inflate(R.layout.dialog_delete,null);
        GetName=getdata.trim();
        GetAddress=getAddress.trim();
        mDialog.setContentView(view);
        mDialogOkBtn=(Button)mDialog.findViewById(R.id.DialogDelete_Ok);
        mDialogCancelBtn=(Button)mDialog.findViewById(R.id.DialogDelete_Cancel);
        mDialogShareBtn=(Button)mDialog.findViewById(R.id.DialogShare_Ok);
        mDialogOkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db=mySql.getWritableDatabase();
                db.execSQL("delete from RTable where RName="+"'"+GetName+"';",new String[]{});
                db.close();
                Message msg=new Message();
                msg.what=1;
                mDialog.dismiss();
                Mhandler.sendMessage(msg);
            }
        });
        mDialogCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
        mDialogShareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareFileDialog(GetAddress);
            }
        });
        mDialog.show();
        return false;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public String getAddress(String getName){
        db=mySql.getWritableDatabase();
        //    db.execSQL("select RAddress from RTable where RName="+"'"+getName+"';",new String[]{});
        final Cursor cursor=db.query("RTable", new String[]{ "RAddress" },
                "RName=?",new String[]{getName},null,null,null);
        cursor.moveToFirst();
        db.close();
        return cursor.getString(0);
    }

    public void showPlayDialog(final String getName){
//        db=mySql.getWritableDatabase();
//    //    db.execSQL("select RAddress from RTable where RName="+"'"+getName+"';",new String[]{});
//        final Cursor cursor=db.query("RTable", new String[]{ "RAddress" },
//                "RName=?",new String[]{getName},null,null,null);
//        cursor.moveToFirst();
//        db.close();
        mDialogAudio =new Dialog(mDialogContext, R.style.Theme_AudioDialog);
        LayoutInflater inflater=LayoutInflater.from(mDialogContext);
        View view=inflater.inflate(R.layout.dialog_play,null);
        mDialogAudio.setContentView(view);
        mDialogPlayBtn =(Button) mDialogAudio.findViewById(R.id.Dialog_play);
        mDialogPauseBtn=(Button)mDialogAudio.findViewById(R.id.Dialog_pause);
        mDialogPlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    binder.play(getName);
                    mDialogAudio.dismiss();
                    Playhandler = new Handler() {
                    public void handleMessage(android.os.Message message) {
                        switch (message.what) {
                            case 1:
                                mDialogAudio.dismiss();
                                break;

                        }
                    };
                };
            }
        });
        mDialogPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binder.pause();
            }
        });
        mDialogAudio.show();
    }

    public void shareFileDialog(String mAddress) {
        // String path = dbHelper.getItemAt(position).getFilePath();
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(mAddress)));
        shareIntent.setType("audio/mp3");
        startActivity(Intent.createChooser(shareIntent, "发送"));
    }

    /*
    将文件保存至网盘，自动下载
     */
    /*
     * 分享系统自带的功能
     * @param position
     */
    public static void saveToDrive(Context context,String mAddress) {

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        //   shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(getItem(position).getFilePath())));
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(mAddress)));
        try {
            shareIntent.setPackage("com.baidu.netdisk");
            shareIntent.setClassName("com.baidu.netdisk", "com.baidu.netdisk.ui.EnterShareFileActivity");
            shareIntent.setType("audio/mp3");
            context.startActivity(shareIntent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "请先安装百度网盘APP", Toast.LENGTH_LONG).show();
            launchAppDetail( context,"com.baidu.netdisk","");
        }
    }

    //自动跳转到应用商店 百度网盘APP详情页面
    public static void launchAppDetail(Context context, String appPkg, String marketPkg) {
        try {
            if (TextUtils.isEmpty(appPkg)) return;
            Uri uri = Uri.parse("market://details?id=" + appPkg);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            if (!TextUtils.isEmpty(marketPkg)) {
                intent.setPackage(marketPkg);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

