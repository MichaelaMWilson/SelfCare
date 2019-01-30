package com.example.mzw5443.selfcare;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

/**
 * This class creates the dialog for deleting an item
 *
 * Resources referenced:
 *          Dialogs - Lecture 8 Notes: Notifications and Confirmations
 **/


public class DeleteDialog extends DialogFragment{

    //Interface for the callback
    public interface DialogListener{
        void onPositiveClick();
    }

    DialogListener mListener;


    @Override
    public android.app.Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage("Delete this entry?")
               .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mListener.onPositiveClick();
                    }
               })
               .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
               });
        return builder.create();
    }


    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        mListener = (DialogListener) activity;
    }
}
