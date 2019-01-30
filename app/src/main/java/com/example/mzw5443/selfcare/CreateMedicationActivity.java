package com.example.mzw5443.selfcare;

import android.app.AlarmManager;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;
import java.util.Calendar;
import static android.app.PendingIntent.FLAG_CANCEL_CURRENT;

/**
 * This class displays the create medication activity for the user to select
 * a time and frequency for a medication, and set an alarm to go off via a notification.
 *
 * Resources referenced:
 *       PendingIntents - https://developer.android.com/reference/android/app/PendingIntent.html
 *       AlarmManager - https://developer.android.com/reference/android/app/AlarmManager.html
 *       Scheduling Alarms - https://developer.android.com/training/scheduling/alarms.html
 *       TimePicker & DatePicker - https://developer.android.com/guide/topics/ui/controls/pickers.html
 *       Notifications - Lecture 8 Notes: Notifications and Confirmations
 *       Callbacks - Lecture 9 Notes: More Callbacks and Beyond Wiring up the UI via XML
 *       Database - Lecture 17 Notes: ListViews Backed By SQLite Database
 **/


public class CreateMedicationActivity extends AppCompatActivity implements DeleteDialog.DialogListener,
                                                                           TimePickerFragment.Listener{

    SharedPreferences sharedPref;
    EditText etTitle;
    Button btnTime;
    SQLiteDatabase theDB;
    String medId, meridiem;
    int hourOfDay, minute;
    RadioButton rbOneTime, rbDaily;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set the colors based on the app theme
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        int theme = sharedPref.getInt("theme", R.style.AppTheme);
        setTheme(theme);

        setContentView(R.layout.activity_create_medication);

        etTitle = findViewById(R.id.etMedTitle);
        btnTime = findViewById(R.id.btnMedTimePicker);

        rbOneTime = findViewById(R.id.rbOneTime);
        rbDaily = findViewById(R.id.rbDaily);

        medId = getIntent().getStringExtra("MEDICATION_ID");

    }
    @Override
    protected void onResume() {
        super.onResume();
        // Get a writable database
        MedicationDB.getInstance(this).getWritableDatabase(new MedicationDB.OnDBReadyListener() {
            @Override
            public void onDBReady(SQLiteDatabase db) {
                theDB = db;
                loadMedication();
            }
        });
    }

    public void onSaveClick(View v){
        if (theDB == null) {
            Toast.makeText(this, "Try again in a few seconds.", Toast.LENGTH_SHORT).show();
        }
        //check the length of each field to make sure the user put something
        else if(etTitle.getText().toString().trim().length() == 0 ||
                btnTime.getText().toString().equals("Select time")){
            if(etTitle.getText().toString().trim().length() == 0)
                Toast.makeText(this, "Medication name cannot be blank", Toast.LENGTH_SHORT).show();
            if(btnTime.getText().toString().equals("Select time"))
                Toast.makeText(this, "A time must be selected", Toast.LENGTH_SHORT).show();
        }
        else {
            ContentValues values = new ContentValues();
            long currentTime = System.currentTimeMillis();

            values.put("title", etTitle.getText().toString());
            values.put("time", btnTime.getText().toString());

            if(rbOneTime.isChecked())
                values.put("repeat", "One Time Reminder");
            else
                values.put("repeat", "Daily Reminder");

            if (medId == null) {
                //create new row in database
                long row = theDB.insert("medications", null, values);
                medId = Long.toString(row);

                Toast.makeText(this, "Medication reminder created", Toast.LENGTH_SHORT).show();
                finish();
            }
            else {
                //update the database
                String selection = "_id = " + medId;
                theDB.update("medications", values, selection, null);

                Toast.makeText(this, "Medication reminder updated", Toast.LENGTH_SHORT).show();
                finish();
            }

            createAlarm(currentTime, medId);
        }
    }


    //Display delete dialog. btnMedDelete onClick is set to this.
    public void confirmDelete(View view) {
        DialogFragment dialogFragment = new DeleteDialog();
        dialogFragment.show(getFragmentManager(), "eraseDialog");
    }


    //Callback when used presses "DELETE"
    public void onPositiveClick(){
        deleteNote();
    }



    public void deleteNote(){
        if (theDB == null) {
            Toast.makeText(this, "Try again in a few seconds.", Toast.LENGTH_SHORT).show();
        }
        else if(medId == null){
            Toast.makeText(this, "Medication reminder deleted", Toast.LENGTH_SHORT).show();
            finish();
        }
        else{
            //Cancel pending notification
            Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
            PendingIntent.getBroadcast(getApplicationContext(), Integer.valueOf(medId), intent, 0).cancel();

            //Delete from database
            String selection = "_id = " + medId;
            theDB.delete("medications", selection, null);

            //remove loaded medication
            medId = null;

            Toast.makeText(this, "Medication reminder deleted", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    //If the user pressed an old medication, load it into the layout
    public void loadMedication(){
        if(medId != null && !medId.isEmpty()){
            String[] columns = {"_id", "title", "time", "repeat"};
            String selection = "_id = " + medId;

            Cursor c = theDB.query("medications", columns, selection, null, null, null, null);
            if(c.moveToFirst()){
                etTitle.setText(c.getString(c.getColumnIndexOrThrow("title")));
                btnTime.setText(c.getString(c.getColumnIndexOrThrow("time")));
                String repeatCheck = c.getString(c.getColumnIndexOrThrow("repeat"));

                if(repeatCheck.equals("Daily Reminder"))
                    rbDaily.setChecked(true);
            }
            c.close();

            //In case the user does not select a new time,
            //store the loaded time into the time variables
            String timeString = btnTime.getText().toString();
            String timeStringMeridiem = timeString.substring(timeString.length() - 2);
            String[] timeStringArr = timeString.substring(0, timeString.length() - 3).split(":");

            hourOfDay = Integer.valueOf(timeStringArr[0]);
            minute = Integer.valueOf(timeStringArr[1]);

            if(timeStringMeridiem.equals("PM")){
                if(hourOfDay != 12)
                    hourOfDay = hourOfDay + 12;
            }
            else {
                if(hourOfDay == 12)
                    hourOfDay = 0;
            }
        }
    }

    public void showTimePickerDialog(View v) {
        TimePickerFragment timeFragment = new TimePickerFragment();
        timeFragment.show(getFragmentManager(), "timePicker");
        timeFragment.setListener(this);
    }

    public void setTime(int hourOfDay, int minute) {

        this.hourOfDay = hourOfDay;
        this.minute = minute;

        String strMin = Integer.toString(minute);
        if(hourOfDay >= 12){
            meridiem = "PM";
            if(hourOfDay != 12)
                hourOfDay = hourOfDay - 12;
        }
        else{
            meridiem = "AM";
            if(hourOfDay == 0)
                hourOfDay = 12;
        }
        if(minute < 10){
            strMin = "0" + minute;
        }

        String timeString = hourOfDay + ":" + strMin + " " + meridiem;
        btnTime.setText(timeString);
    }

    public void createAlarm(long currentTime, String medId){
        Context context = getApplicationContext();
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);

        //put all intents before declaring alarmIntent
        intent.putExtra("REMINDER_ID", medId);
        intent.putExtra("ALARM_TEXT", etTitle.getText().toString());

        if(rbOneTime.isChecked()){
            intent.putExtra("REPEAT", "N");
        }
        else{
            intent.putExtra("REPEAT", "Y");
        }

        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, Integer.valueOf(medId), intent, FLAG_CANCEL_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentTime);
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        Calendar now = Calendar.getInstance();
        now.setTimeInMillis(currentTime);

        //Check if the alarm time has already passed for the day
        boolean timePassed = false;

        if(hourOfDay <= now.get(Calendar.HOUR_OF_DAY)){
            if(hourOfDay == now.get(Calendar.HOUR_OF_DAY)){
                if(minute < now.get(Calendar.MINUTE)){
                    timePassed = true;
                }
            }
            else timePassed = true;
        }

        if(alarmMgr != null) {
            //Check if repeating and set alarms
            if (rbOneTime.isChecked()) {
                //If time has already passed, set alarm for next day
                if (!timePassed)
                    alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
                else
                    alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis()
                            + (1000 * 60 * 60 * 24), alarmIntent);
            }
            //Set a repeating alarm for every 24 hours
            else {
                //If time has already passed, set alarm to start on the next day
                if (!timePassed)
                    alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                            1000 * 60 * 60 * 24, alarmIntent);
                else
                    alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis()
                            + (1000 * 60 * 60 * 24), 1000 * 60 * 60 * 24, alarmIntent);
            }
        }
    }
}
