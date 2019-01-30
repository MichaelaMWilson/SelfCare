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
 * This class creates a custom adapter for the medication items
 *
 * Resources referenced:
 *       How to create a custom adapter - https://stackoverflow.com/questions/8166497/custom-adapter-for-list-view
 **/


public class MedicationAdapter extends ArrayAdapter<List<String>> {


    MedicationAdapter(Context context, int resource, ArrayList<List<String>> items) {
        super(context, resource, items);
    }


    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent){

        //Inflate the layout based on item_medication.xml
        if(convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_medication, null);

        List<String> medication = getItem(position);

        if(medication != null){
            //Set medication title, time, and repeating status to medication items
            TextView tvTitle = convertView.findViewById(R.id.tvMedTitle);
            TextView tvTime = convertView.findViewById(R.id.tvMedTime);
            TextView tvMeridian = convertView.findViewById(R.id.tvMedAMPM);
            TextView tvRepeat = convertView.findViewById(R.id.tvMedRepeat);

            tvTitle.setText(medication.get(1));

            String time = medication.get(2);
            tvTime.setText(time.substring(0, time.length() - 2));
            tvMeridian.setText(time.substring(time.length() - 2));

            tvRepeat.setText(medication.get(3));
        }
        return convertView;
    }
}
