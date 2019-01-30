package com.example.mzw5443.selfcare;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

/**
 * This class displays the settings activity where the user can select
 * a color scheme for the app.
 *
 * Resources referenced:
 *          SharedPreferences - Lecture 15 Notes: Data Persistence Across Application Runs
 **/

public class SettingsActivity extends AppCompatActivity {
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set the colors based on the current theme
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        int theme = sharedPref.getInt("theme", R.style.AppTheme);
        setTheme(theme);

        setContentView(R.layout.activity_settings);

        //Set checked RadioButton based on last choice
        String sort = sharedPref.getString("sort", "NEW");
        if(sort.equals("OLD"))
            ((RadioButton) findViewById(R.id.rbOldest)).setChecked(true);
    }


    //btnPurple, btnBlue, btnGreen, btnRed onClick are set to this
    @SuppressLint("ApplySharedPref")
    public void onThemeClick(View v){
        editor = sharedPref.edit();
        int THEME;

        switch (v.getId()){
            case R.id.btnPurple:
                THEME = R.style.AppTheme;
                break;
            case R.id.btnBlue:
                THEME = R.style.AppThemeBlue;
                break;
            case R.id.btnGreen:
                THEME = R.style.AppThemeGreen;
                break;
            case R.id.btnRed:
                THEME = R.style.AppThemeRed;
                break;
            default:
                THEME = R.style.AppTheme;
        }

        //Store the theme color
        editor.putInt("theme", THEME);
        editor.commit();
        Toast.makeText(this, "Theme set", Toast.LENGTH_SHORT).show();
        //Refresh the activity to show the new theme
        recreate();
    }

    public void onSortClick(View v){
        editor = sharedPref.edit();
        String SORT;

        switch(v.getId()) {
            case R.id.rbNewest:
                SORT = "NEW";
                break;
            case R.id.rbOldest:
                SORT = "OLD";
                break;
            default:
                SORT = "NEW";
        }

        editor.putString("sort", SORT);
        editor.apply();
        Toast.makeText(this, "Sorting set", Toast.LENGTH_SHORT).show();
    }

    //When the activity is finished, relaunch MainActivity, so that onCreate is called
    //again, and any new theme is set.
    @Override
    public void onBackPressed(){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }
}
