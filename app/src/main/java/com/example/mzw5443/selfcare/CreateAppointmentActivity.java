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
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.util.Calendar;
import static android.app.PendingIntent.FLAG_CANCEL_CURRENT;

/**
 * This class displays the create appointment activity for the user to select
 * a date and time for an appointment, and set an alarm to go off via a notification.
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


public class CreateAppointmentActivity extends AppCompatActivity implements DeleteDialog.DialogListener,
                                                                            TimePickerFragment.Listener,
                                                                            DatePickerFragment.Listener{

    EditText etTitle;
    Button btnDate, btnTime;
    SQLiteDatabase theDB;
    String apptId, meridiem;
    SharedPreferences sharedPref;
    int hourOfDay, minute, day, year, month;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set the colors based on the app theme
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        int theme = sharedPref.getInt("theme", R.style.AppTheme);
        setTheme(theme);

        setContentView(R.layout.activity_create_appointment);

        etTitle = findViewById(R.id.etApptTitle);
        btnTime = findViewById(R.id.btnTimePicker);
        btnDate = findViewById(R.id.btnDatePicker);

        apptId = getIntent().getStringExtra("APPOINTMENT_ID");
    }


    //Load the database and appointment
    @Override
    protected void onResume() {
        super.onResume();
        // Get a writable database
        AppointmentDB.getInstance(this).getWritableDatabase(new AppointmentDB.OnDBReadyListener() {
            @Override
            public void onDBReady(SQLiteDatabase db) {
                theDB = db;
                loadAppointment();
            }
        });
    }


    //Save a new or existing appointment. btnSave onClick is set to this.
    public void onSaveClick(View v){
        //The database hasn't finished being loaded yet
        if (theDB == null) {
            Toast.makeText(this, "Try again in a few seconds.", Toast.LENGTH_SHORT).show();
        }
        //Ensure the user has not left any fields blank
        else if(etTitle.getText().toString().trim().length() == 0 ||
                btnTime.getText().toString().equals("Select time")||
                btnDate.getText().toString().equals("Select date")){

            if(etTitle.getText().toString().trim().length() == 0)
                Toast.makeText(this, "Appointment name cannot be blank", Toast.LENGTH_SHORT).show();

            if(btnTime.getText().toString().equals("Select time"))
                Toast.makeText(this, "A time must be selected", Toast.LENGTH_SHORT).show();

            if(btnDate.getText().toString().equals("Select date"))
                Toast.makeText(this, "A date must be selected", Toast.LENGTH_SHORT).show();
        }
        else {
            ContentValues values = new ContentValues();
            long currentTime = System.currentTimeMillis();

            values.put("title", etTitle.getText().toString());
            values.put("time", btnTime.getText().toString());
            values.put("date", btnDate.getText().toString());

            //This is a new appointment
            if (apptId == null) {
                //Create new row in database
                long row = theDB.insert("appointments", null, values);
                apptId = Long.toString(row);

                Toast.makeText(this, "Appointment reminder created", Toast.LENGTH_SHORT).show();
                finish();
            }
            //This is a loaded appointment
            else {
                //Update the database
                String selection = "_id = " + apptId;
                theDB.update("appointments", values, selection, null);

                Toast.makeText(this, "Appointment reminder updated", Toast.LENGTH_SHORT).show();
                finish();
            }

            createAlarm(currentTime, apptId);
        }
    }

    //Display delete dialog. btnDelete onClick is set to this
    public void confirmDelete(View view) {
        DialogFragment dialogFragment = new DeleteDialog();
        dialogFragment.show(getFragmentManager(), "eraseDialog");
    }


    //Callback when used presses "DELETE"
    public void onPositiveClick(){
        deleteAppointment();
    }


    //Delete an appointment
    public void deleteAppointment(){
        //The database hasn't finished being loaded yet
        if (theDB == null) {
            Toast.makeText(this, "Try again in a few seconds.", Toast.LENGTH_SHORT).show();
        }
        //There is not a saved appointment to delete
        else if(apptId == null){
            Toast.makeText(this, "Appointment reminder deleted", Toast.LENGTH_SHORT).show();
            finish();
        }
        //There is a saved appointment to delete
        else{
            String selection = "_id = " + apptId;
            theDB.delete("appointments", selection, null);

            //Remove loaded appointment
            apptId = null;

            Toast.makeText(this, "Appointment reminder deleted", Toast.LENGTH_SHORT).show();
            finish();
        }
    }


    //Load an established appointment into the layout if the user clicks on a list item.
    public void loadAppointment(){
        if(apptId!= null && !apptId.isEmpty()){
            String[] columns = {"_id", "title", "time", "date"};
            String selection = "_id = " + apptId;

            Cursor c = theDB.query("appointments", columns, selection, null, null, null, null);
            if(c.moveToFirst()){
                etTitle.setText(c.getString(c.getColumnIndexOrThrow("title")));
                btnTime.setText(c.getString(c.getColumnIndexOrThrow("time")));
                btnDate.setText(c.getString(c.getColumnIndexOrThrow("date")));
            }
            c.close();


            //In case the user does not select a new date or time,
            //store the loaded date and time in their respective variables
            String[] dateString = btnDate.getText().toString().split("/");
            month = Integer.valueOf(dateString[0]) - 1;
            day = Integer.valueOf(dateString[1]);
            year = Integer.valueOf(dateString[2]);

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


    //Show the time picker dialog. btnTimePicker has onClick set to this.
    public void showTimePickerDialog(View v) {
        TimePickerFragment timeFragment = new TimePickerFragment();
        timeFragment.show(getFragmentManager(), "timePicker");
        timeFragment.setListener(this);
    }


    //Show the date picker dialog. btnDatePicker has onClick set to this.
    public void showDatePickerDialog(View v){
        DatePickerFragment dateFragment = new DatePickerFragment();
        dateFragment.show(getFragmentManager(), "datePicker");
        dateFragment.setListener(this);
    }


    //Set the text on the time button, format time
    //12hr instead of 24hr, set time variables
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


    //Set the text on the date button, set date variables
    public void setDate(int day, int month, int year){
        this.day = day;
        this.month = month;
        this.year = year;
        month = month + 1;

        String dateString = month  + "/" + day + "/" + year;
        btnDate.setText(dateString);
    }


    //This function creates and sets an alarm.
    public void createAlarm(long currentTime, String apptId){
        Context context = getApplicationContext();
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);

        //All intents must be before declaring alarmIntent
        intent.putExtra("REMINDER_ID", apptId);
        intent.putExtra("ALARM_TEXT", etTitle.getText().toString());

        //Appointments do not have a repeating option
        intent.putExtra("REPEAT", "APPOINTMENT");

        //Request code for this intent is negative version of rowId in order
        //to not duplicate any medication request codes
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, -1* Integer.valueOf(apptId), intent, FLAG_CANCEL_CURRENT);

        //Set the time for the alarm to go off
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentTime);
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.YEAR, year);

        if(alarmMgr != null)
            alarmMgr.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(), alarmIntent);
    }
 }
