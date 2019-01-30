package com.example.mzw5443.selfcare;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

 /**
 * This class creates a custom adapter for the note items
 *
 * Resources referenced:
 *       How to create a custom adapter - https://stackoverflow.com/questions/8166497/custom-adapter-for-list-view
 **/

public class NoteAdapter extends ArrayAdapter<List<String>>{


    NoteAdapter(Context context, int resource, ArrayList<List<String>> items) {
        super(context, resource, items);
    }


    @NonNull
    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent){

        //Inflate the layout based on item_note.xml
        if(convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_note, null);

        List<String> note = getItem(position);

        if(note != null){
            //Set note title, date, and body to note items
            TextView tvTitle = convertView.findViewById(R.id.tvNoteTitle);
            TextView tvDate = convertView.findViewById(R.id.tvNoteDate);
            TextView tvBody = convertView.findViewById(R.id.tvNoteBody);

            tvTitle.setText(note.get(0));
            tvBody.setText(note.get(1));

            //Format the date so it's comprehensible
            SimpleDateFormat fmt = new SimpleDateFormat("EEE, MMM d, yyyy h:mm a",
                    getContext().getResources().getConfiguration().locale);
            fmt.setTimeZone(TimeZone.getDefault());

            String date = fmt.format(new Date(Long.parseLong(note.get(2))));
            String displayDate = getContext().getText(R.string.created) + " " + date;

            tvDate.setText(displayDate);

            //When the user doesn't put in a note subject or body, adjust the padding
            //accordingly to avoid whitespace. This allows the user to put blank
            //subjects and/or bodies
            if(!tvTitle.getText().equals("")){
                tvTitle.setVisibility(View.VISIBLE);
                tvDate.setPadding(0, 0, 0, tvDate.getPaddingBottom());
            }
            else{
                tvTitle.setVisibility(View.GONE);
                tvDate.setPadding(0, tvDate.getPaddingBottom(), 0, tvDate.getPaddingBottom());
            }
            if(!tvBody.getText().equals("")){
                tvBody.setVisibility(View.VISIBLE);
            }
            else{
                tvBody.setVisibility(View.GONE);
            }

        }
        return convertView;
    }
}
