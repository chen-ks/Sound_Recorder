package com.example.sound_recorder;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class MySql extends SQLiteOpenHelper {

    private  Context mContext;

    private  static  final  String  DATABASE = "recorder.db";

    private  static  final  String TABLE_NAME = "RTable";

    private  static  final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "(id Integer PRIMARY KEY AUTOINCREMENT,RName text,RLength VARCHAR(20),RAddress text,RDate text)";
    public MySql(Context context,  int version) {
        super(context, DATABASE, null, version);
        mContext=context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
        Toast.makeText(mContext,"success",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
