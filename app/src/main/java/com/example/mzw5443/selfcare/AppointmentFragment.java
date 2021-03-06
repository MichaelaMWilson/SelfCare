package com.example.mzw5443.selfcare;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class creates the appointment listview fragment to be displayed inside ReminderActivity.
 *
 * Resources referenced:
 *       Fragments - https://developer.android.com/training/basics/fragments/creating.html
 *       Database - Lecture 17 Notes: ListViews Backed By SQLite Database
 **/

public class AppointmentFragment extends Fragment {

    ListView lvAppointment;
    TextView tvNoAppointments;
    SQLiteDatabase theDB;
    String apptId;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_reminder, container, false);

        lvAppointment = rootView.findViewById(R.id.lvReminderView);
        tvNoAppointments = rootView.findViewById(R.id.tvNoReminders);

        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();
        // Get a writable database
        AppointmentDB.getInstance(this.getContext()).getWritableDatabase(new AppointmentDB.OnDBReadyListener() {
            @Override
            public void onDBReady(SQLiteDatabase db) {
                theDB = db;
                populateListView();
            }
        });
    }

    //Query the database and load all appointments into a listview to show to the user
    @SuppressLint("StaticFieldLeak")
    private void populateListView(){
        new AsyncTask<Boolean,Void, Cursor>() {
            protected Cursor doInBackground(Boolean... params) {
                return theDB.rawQuery("SELECT * FROM " + "appointments", null);
            }

            public void onPostExecute(Cursor data)
            {
                //Create an array list of a list of strings in order to
                //store each row of the database into a list item
                final ArrayList<List<String>> appointmentList = new ArrayList<>();
                while (data.moveToNext()) {
                    ArrayList<String> row = new ArrayList<>();
                    row.add(data.getString(0));
                    row.add(data.getString(1));
                    row.add(data.getString(2));
                    row.add(data.getString(3));
                    appointmentList.add(row);
                }
                data.close();

                //Order the list from most recent to oldest
                Collections.reverse(appointmentList);

                if (appointmentList.size() == 0) {
                    //If there are no appointments, display text to let the user know what to do
                    tvNoAppointments.setVisibility(View.VISIBLE);
                } else {
                    tvNoAppointments.setVisibility(View.INVISIBLE);
                }

                //Create an adapter for the appointment listview
                AppointmentAdapter adapter = new AppointmentAdapter(AppointmentFragment.this.getContext(), R.layout.item_appointment, appointmentList);
                lvAppointment.setAdapter(adapter);

                lvAppointment.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        //Get the id of the appointment for searching the database,
                        //because it is guaranteed to be unique
                        List<String> rowNum = appointmentList.get(i);
                        apptId = rowNum.get(0);

                        //on item click, launch the create appointment activity
                        Intent intent = new Intent(getContext().getApplicationContext(), CreateAppointmentActivity.class);
                        intent.putExtra("APPOINTMENT_ID", apptId);
                        startActivity(intent);
                    }
                });
            }
        }.execute();
    }
}
