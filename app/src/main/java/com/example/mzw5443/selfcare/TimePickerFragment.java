package com.example.mzw5443.selfcare;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;
import java.util.Calendar;

/**
 * This class sets up the TimePickerFragment to allow the user to set the time using Android's
 * TimePickerDialog.
 *
 * Resources referenced:
 *       TimePicker & DatePicker - https://developer.android.com/guide/topics/ui/controls/pickers.html
 *       Callbacks - Lecture 9 Notes: More Callbacks and Beyond Wiring up the UI via XML
 **/


public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
    }


    public interface Listener {
        void setTime(int hourOfDay, int minute);
    }


    private Listener mListener;


    public void setListener(Listener listener){
        mListener = listener;
    }


    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        if(mListener != null) mListener.setTime(hourOfDay, minute);
    }
}
