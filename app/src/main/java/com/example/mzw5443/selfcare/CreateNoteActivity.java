package com.example.mzw5443.selfcare;

import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 * This class displays the create note activity for the user to write a new note
 * and save it to the database of notes.
 *
 * Resources referenced:
 *       Callbacks - Lecture 9 Notes: More Callbacks and Beyond Wiring up the UI via XML
 *       Database - Lecture 17 Notes: ListViews Backed By SQLite Database
 **/

public class CreateNoteActivity extends AppCompatActivity implements DeleteDialog.DialogListener {

    EditText etTitle, etBody;
    SQLiteDatabase theDB;
    String date;
    long noteRow;
    SharedPreferences sharedPref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set the colors based on current theme
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        int theme = sharedPref.getInt("theme", R.style.AppTheme);
        setTheme(theme);

        setContentView(R.layout.activity_create_note);

        etTitle = findViewById(R.id.etTitle);
        etBody = findViewById(R.id.etBody);

        date = getIntent().getStringExtra("NOTE_ID");
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Get a writable database
        NoteDB.getInstance(this).getWritableDatabase(new NoteDB.OnDBReadyListener() {
            @Override
            public void onDBReady(SQLiteDatabase db) {
                theDB = db;
                loadNote();
            }
        });
    }


    //Save a new note to the database or update an existing one.
    //btnSave onClick is set to this.
    public void onSaveClick(View v){
        //The database is not finished loading yet
        if (theDB == null) {
            Toast.makeText(this, "Try again in a few seconds.", Toast.LENGTH_SHORT).show();
        }
        //The database is ready to have values added/updated
        else {
            ContentValues values = new ContentValues();
            values.put("title", etTitle.getText().toString());
            values.put("body", etBody.getText().toString());

            if (date == null) {
                //create new row in database
                values.put("date", Long.toString(System.currentTimeMillis()));
                theDB.insert("notes", null, values);

                Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show();
                finish();
            }
            else {
                //update the database
                values.put("date", date);
                String selection = "date = " + date;
                theDB.update("notes", values, selection, null);

                //Remove the loaded note
                date = null;

                Toast.makeText(this, "Note updated", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }


    //Display delete dialog. btnDelete onClick is set to this.
    public void confirmDelete(View view) {
        DialogFragment dialogFragment = new DeleteDialog();
        dialogFragment.show(getFragmentManager(), "eraseDialog");
    }


    //Callback when used presses "DELETE"
    public void onPositiveClick(){
        deleteNote();
    }


    //Delete the note from the database
    public void deleteNote(){
        //The database is not finished loading yet
        if (theDB == null) {
            Toast.makeText(this, "Try again in a few seconds.", Toast.LENGTH_SHORT).show();
        }
        //This is a new note that has never been saved
        else if(date == null){
            Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show();
            finish();
        }
        //This is a loaded note
        else{
            String selection = "date = " + date;
            theDB.delete("notes", selection, null);

            //Remove loaded note
            date = null;

            Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    //Load an old note into the layout, if the user pressed on it
    public void loadNote(){
        if(date != null && !date.isEmpty()){
            String[] columns = {"_id", "title", "body", "date"};
            String selection = "date = " + date;

            Cursor c = theDB.query("notes", columns, selection, null, null, null, null);
            if(c.moveToFirst()){
                noteRow = c.getLong(c.getColumnIndexOrThrow("_id"));
                etTitle.setText(c.getString(c.getColumnIndexOrThrow("title")));
                etBody.setText(c.getString(c.getColumnIndexOrThrow("body")));
            }
            c.close();
        }
    }
}
