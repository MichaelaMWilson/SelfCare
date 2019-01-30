package com.example.mzw5443.selfcare;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

/**
 * This class creates a custom adapter for the appointment items
 *
 * Resources referenced:
 *       How to create a custom adapter - https://stackoverflow.com/questions/8166497/custom-adapter-for-list-view
 **/


public class AppointmentAdapter extends ArrayAdapter<List<String>> {


    AppointmentAdapter(Context context, int resource, ArrayList<List<String>> items) {
        super(context, resource, items);
    }


    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent){

        //Inflate the layout based on item_appointment.xml
        if(convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_appointment, null);

        List<String> appointment = getItem(position);

        if(appointment != null){
            //Set appointment title, time, and date to appointment items
            TextView tvTitle = convertView.findViewById(R.id.tvApptTitle);
            TextView tvTime = convertView.findViewById(R.id.tvApptTime);
            TextView tvDate = convertView.findViewById(R.id.tvApptDate);
            TextView tvMeridian = convertView.findViewById(R.id.tvApptAMPM);

            tvTitle.setText(appointment.get(1));

            String time = appointment.get(2);
            tvTime.setText(time.substring(0, time.length() - 2));
            tvMeridian.setText(time.substring(time.length() - 2));

            tvDate.setText(appointment.get(3));
        }
        return convertView;
    }
}
