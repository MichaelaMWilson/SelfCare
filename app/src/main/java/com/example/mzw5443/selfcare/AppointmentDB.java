package com.example.mzw5443.selfcare;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;

/**
 * This class creates the database for the appointment reminders
 *
 * Resources referenced:
 *       Database - Lecture 17 Notes: ListViews Backed By SQLite Database
 **/


public class AppointmentDB extends SQLiteOpenHelper{

    interface OnDBReadyListener {
        void onDBReady(SQLiteDatabase db);
    }

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "appointment.db";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE appointments (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "title TEXT, " +
                    "time TEXT, " +
                    "date TEXT);";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS appointments";


    private static AppointmentDB theDb;

    private AppointmentDB(Context context) {
        super(context.getApplicationContext(),DATABASE_NAME,null,DATABASE_VERSION);
    }

    static synchronized AppointmentDB getInstance(Context context) {

        if (theDb == null) {
            theDb = new AppointmentDB(context);
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

    void getWritableDatabase(OnDBReadyListener listener) {
        new OpenDbAsyncTask().execute(listener);
    }

    private static class OpenDbAsyncTask extends AsyncTask<OnDBReadyListener,Void,SQLiteDatabase> {
        OnDBReadyListener listener;

        @Override
        protected SQLiteDatabase doInBackground(OnDBReadyListener... params){
            listener = params[0];
            return AppointmentDB.theDb.getWritableDatabase();
        }

        @Override
        protected void onPostExecute(SQLiteDatabase db) {
            listener.onDBReady(db);
        }
    }
}