package com.example.puff.kimaiaphotolabler;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


/*
 * DBHelper
 * this is an intermediate class used to communicate with the MySQL database and commit queries
 * */
public class DBHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private final String TAG = "DBHelper"; //for logging

    //constructor - db name is fixed.
    public DBHelper(Context context) {
        super(context, "kimaiaDB", null, DATABASE_VERSION);
        Log.d(TAG, "constructor");
    }
    //creating the data table i use to save the list of photo paths, tags etc..
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate");
        String CREATE_TABLE = "CREATE TABLE " + "labeledPhotos" + "("
                + "photoUID" + " TEXT PRIMARY KEY, " + "Path" + " TEXT, " + "Tags" + " TEXT, "
                + "dateTaken" + " TEXT" + ");";
        db.execSQL(CREATE_TABLE);
    }
    //creating the table from scratch on update
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade");
        db.execSQL("DROP TABLE IF EXISTS " + com.example.puff.kimaiaphotolabler.LabeledPhoto.TABLE_NAME);
        onCreate(db);
    }
}
