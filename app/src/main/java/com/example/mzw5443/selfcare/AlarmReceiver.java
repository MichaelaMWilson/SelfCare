package com.example.mzw5443.selfcare;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

/**
 * This class sets up the alarm receiver to display a notification at the specified time/date.
 *
 * Resources referenced:
 *       PendingIntent - https://developer.android.com/reference/android/app/PendingIntent.html
 *       Create a Notification - https://developer.android.com/training/notify-user/build-notification.html
 *       Callbacks - Lecture 9 Notes: More Callbacks and Beyond Wiring up the UI via XML
 **/


public class AlarmReceiver extends BroadcastReceiver {

    SQLiteDatabase theDB;


    @Override
    public void onReceive(Context context, Intent intent) {

        Intent newIntent = new Intent(context, ReminderActivity.class);
        newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, newIntent, 0);

        String text = intent.getStringExtra("ALARM_TEXT").trim();
        final String remId = intent.getStringExtra("REMINDER_ID");

        //Build the notification
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, "Notifications")
                .setSmallIcon(R.drawable.face_black)
                .setContentTitle("SELFCARE REMINDER")
                .setContentText("Reminder: " + text + ". Tap to open SelfCare")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Reminder: " + text + ". Tap to open SelfCare"))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        //Needed for newer SDK
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            CharSequence name = "Notifications";
            String description = "All app notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("Notifications", name, importance);
            channel.setDescription(description);
            // Register the channel with the system
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if(notificationManager != null)
                notificationManager.createNotificationChannel(channel);
        }

        //Send the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(Integer.valueOf(remId), mBuilder.build());

        //Delete list item if non-repeating medication or appointment
        if(intent.getStringExtra("REPEAT").equals("N")) {
            MedicationDB.getInstance(context).getWritableDatabase(new MedicationDB.OnDBReadyListener() {
                @Override
                public void onDBReady(SQLiteDatabase db) {
                    theDB = db;
                    String selection = "_id = " + remId;
                    theDB.delete("medications", selection, null);
                }
            });
        }
        if(intent.getStringExtra("REPEAT").equals("APPOINTMENT")){
            AppointmentDB.getInstance(context).getWritableDatabase(new AppointmentDB.OnDBReadyListener() {
                @Override
                public void onDBReady(SQLiteDatabase db) {
                    theDB = db;
                    String selection = "_id = " + remId;
                    theDB.delete("appointments", selection, null);
                }
            });
        }
    }
}