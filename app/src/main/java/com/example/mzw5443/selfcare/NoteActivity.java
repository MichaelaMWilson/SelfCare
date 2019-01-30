package com.example.mzw5443.selfcare;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class displays the listview of saved notes to the user.
 *
 * Resources referenced:
 *       Database - Lecture 17 Notes: ListViews Backed By SQLite Database
 */

public class NoteActivity extends AppCompatActivity{

    ListView lvNoteView;
    TextView tvNoNotes;
    SQLiteDatabase theDB;
    String date, sort;
    SharedPreferences sharedPref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set the colors based on the selected theme
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        int theme = sharedPref.getInt("theme", R.style.AppTheme);
        setTheme(theme);
        sort = sharedPref.getString("sort", "NEW");

        setContentView(R.layout.activity_note_list);

        lvNoteView = findViewById(R.id.lvNoteView);
        tvNoNotes = findViewById(R.id.tvNoNotes);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Get a writable database
        NoteDB.getInstance(this).getWritableDatabase(new NoteDB.OnDBReadyListener() {
            @Override
            public void onDBReady(SQLiteDatabase db) {
                theDB = db;
                populateListView();
            }
        });
    }


    //Launch activity to create a new note. btnNewNote onClick is set to this
    public void onNewNoteClick(View v) {
        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
        startActivity(intent);
    }


    //Populate the listview with notes from the database
    @SuppressLint("StaticFieldLeak")
    private void populateListView(){
        new AsyncTask<Boolean,Void, Cursor>() {
            protected Cursor doInBackground(Boolean... params) {
                return theDB.rawQuery("SELECT * FROM " + "notes", null);
            }

            public void onPostExecute(Cursor data)
            {
                //Create an array list of a list of strings in order to
                //store each row of the database into a list item
                final ArrayList<List<String>> noteList = new ArrayList<>();
                while (data.moveToNext()) {
                    ArrayList<String> row = new ArrayList<>();
                    row.add(data.getString(1));
                    row.add(data.getString(2));
                    row.add(data.getString(3));
                    noteList.add(row);
                }
                data.close();

                if(sort.equals("NEW")) {
                    //Order the list from most recent to oldest
                    Collections.reverse(noteList);
                }

                if (noteList.size() == 0) {
                    //If there are no notes, display text to let the user know what to do
                    tvNoNotes.setVisibility(View.VISIBLE);
                } else {
                    tvNoNotes.setVisibility(View.INVISIBLE);
                }

                NoteAdapter adapter = new NoteAdapter(NoteActivity.this, R.layout.item_note, noteList);
                lvNoteView.setAdapter(adapter);

                lvNoteView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        //Get the date of the note for searching the database,
                        //because it is guaranteed to be unique
                        List<String> rowNum = noteList.get(i);
                        date = rowNum.get(2);

                        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
                        intent.putExtra("NOTE_ID", date);
                        startActivity(intent);
                    }
                });
            }
        }.execute();
    }
}


