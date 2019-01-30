package com.example.mzw5443.selfcare;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;

/**
 * This class creates the database for the medication reminders
 *
 * Resources referenced:
 *       Database - Lecture 17 Notes: ListViews Backed By SQLite Database
 **/


public class MedicationDB extends SQLiteOpenHelper{

    interface OnDBReadyListener {
        void onDBReady(SQLiteDatabase db);
    }

    private static final int DATABASE_VERSION = 4;
    private static final String DATABASE_NAME = "medication.db";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE medications (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "title TEXT, " +
                    "time TEXT, " +
                    "repeat TEXT);";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS medications";


    private static MedicationDB theDb;

    private MedicationDB(Context context) {
        super(context.getApplicationContext(),DATABASE_NAME,null,DATABASE_VERSION);
    }

    public static synchronized MedicationDB getInstance(Context context) {
        if (theDb == null) {
            theDb = new MedicationDB(context);
        }
        return theDb;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void getWritableDatabase(OnDBReadyListener listener) {
        new OpenDbAsyncTask().execute(listener);
    }

    private static class OpenDbAsyncTask extends AsyncTask<OnDBReadyListener,Void,SQLiteDatabase> {
        OnDBReadyListener listener;

        @Override
        protected SQLiteDatabase doInBackground(OnDBReadyListener... params){
            listener = params[0];
            return MedicationDB.theDb.getWritableDatabase();
        }

        @Override
        protected void onPostExecute(SQLiteDatabase db) {
            listener.onDBReady(db);
        }
    }
}