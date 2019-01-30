package com.example.mzw5443.selfcare;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;
import java.util.Calendar;

/**
 * This class sets up the DatePickerFragment to allow the user to set the date using Android's
 * DatePickerDialog.
 *
 * Resources referenced:
 *       TimePicker & DatePicker - https://developer.android.com/guide/topics/ui/controls/pickers.html
 *       Callbacks - Lecture 9 Notes: More Callbacks and Beyond Wiring up the UI via XML
 **/


public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener{


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }


    public interface Listener {
        void setDate(int day, int month, int year);
    }


    private DatePickerFragment.Listener mListener;


    public void setListener(DatePickerFragment.Listener listener){
        mListener = listener;
    }


    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        if(mListener != null) mListener.setDate(day, month, year);
    }
}
