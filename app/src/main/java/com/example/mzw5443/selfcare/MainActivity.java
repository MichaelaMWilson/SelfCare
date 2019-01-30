package com.example.mzw5443.selfcare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

/**
 * This class displays the main activity to the user, where they can select which
 * activity to visit next.
 **/

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    SharedPreferences sharedPref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set the colors based on the selected theme
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        int theme = sharedPref.getInt("theme", R.style.AppTheme);
        setTheme(theme);

        setContentView(R.layout.activity_main);
        findViewById(R.id.btnNotes).setOnClickListener(this);
    }


    //btnReminders, btnNotes, btnPillLookup, btnSettings onClick are set to this
    public void onClick(View v)
    {
        Class className;

        //Get and load the specified activity
        switch(v.getId()) {
            case (R.id.btnReminders):
                className = ReminderActivity.class;
                break;
            case (R.id.btnNotes):
                className = NoteActivity.class;
                break;
            case (R.id.btnPillLookup):
                className = MedLookupActivity.class;
                break;
            case (R.id.btnSettings):
                className = SettingsActivity.class;
                break;
            default:
                className = MainActivity.class;
        }
        Intent intent = new Intent(getApplicationContext(), className);
        startActivity(intent);

        //When settings is clicked, finish MainActivity, to prepare for
        //a relaunch in case the user changes the theme.
        if(className == SettingsActivity.class) finish();
    }
}


