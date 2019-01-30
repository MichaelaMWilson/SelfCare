package com.example.mzw5443.selfcare;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
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
 * This class creates the medication listview fragment to be displayed inside ReminderActivity.
 *
 * Resources referenced:
 *       Fragments - https://developer.android.com/training/basics/fragments/creating.html
 *
 *       Database - Lecture 17 Notes: ListViews Backed By SQLite Database
 **/

public class MedicationFragment extends Fragment {

    ListView lvMedication;
    TextView tvNoMedications;
    SQLiteDatabase theDB;
    String rowId;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_reminder, container, false);

        lvMedication = rootView.findViewById(R.id.lvReminderView);
        tvNoMedications = rootView.findViewById(R.id.tvNoReminders);

        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();
        // Get a writable database
        MedicationDB.getInstance(this.getContext()).getWritableDatabase(new MedicationDB.OnDBReadyListener() {
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
                return theDB.rawQuery("SELECT * FROM " + "medications", null);
            }

            public void onPostExecute(Cursor data)
            {
                //Create an array list of a list of strings in order to
                //store each row of the database into a list item
                final ArrayList<List<String>> medicationList = new ArrayList<>();
                while (data.moveToNext()) {
                    ArrayList<String> row = new ArrayList<>();
                    row.add(data.getString(0));
                    row.add(data.getString(1));
                    row.add(data.getString(2));
                    row.add(data.getString(3));
                    medicationList.add(row);
                }
                data.close();

                //Order the list from most recent to oldest
                Collections.reverse(medicationList);

                if (medicationList.size() == 0) {
                    //If there are no medications, display text to let the user know what to do
                    tvNoMedications.setVisibility(View.VISIBLE);
                } else {
                    tvNoMedications.setVisibility(View.INVISIBLE);
                }

                //Create an adapter for the medication listview
                MedicationAdapter adapter = new MedicationAdapter(MedicationFragment.this.getContext(), R.layout.item_medication, medicationList);
                lvMedication.setAdapter(adapter);

                lvMedication.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        //Get the id of the medication for searching the database,
                        //because it is guaranteed to be unique
                        List<String> rowNum = medicationList.get(i);
                        rowId = rowNum.get(0);

                        //On an item click, launch the create medication activity
                        Intent intent = new Intent(getContext().getApplicationContext(), CreateMedicationActivity.class);
                        intent.putExtra("MEDICATION_ID", rowId);
                        startActivity(intent);
                    }
                });
            }
        }.execute();
    }

}
