package com.example.mzw5443.selfcare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

/**
 * This class displays the reminder activity with two tabs, which load the appointment and medication
 * fragments.
 *
 * Resources referenced:
 *       Creating Swipe Views with Tabs -  https://developer.android.com/training/implementing-navigation/lateral.html
 *       ViewPager - https://developer.android.com/reference/android/support/v4/view/ViewPager.html
 *       TabLayout - https://developer.android.com/reference/android/support/design/widget/TabLayout.html
 *       TabLayout and ViewPager in your Android App -
 *               https://android.jlelse.eu/tablayout-and-viewpager-in-your-android-app-738b8840c38a
 **/

public class ReminderActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener {

    TabLayout tabLayout;
    ViewPager viewPager;
    SharedPreferences sharedPref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set the colors based on the app theme
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        int theme = sharedPref.getInt("theme", R.style.AppTheme);
        setTheme(theme);

        setContentView(R.layout.activity_reminder_list);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        //Set up ViewPager and TabLayout together so that users can swipe the screen
        //to switch between tabs
        Pager adapter = new Pager(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }


    //Launch new create medication or create appointment activity based on the current tab
    //btnNew onClick is set to this
    public void onNewReminderClick(View v) {
        Intent intent;

        switch (viewPager.getCurrentItem()){
            case 1:
                intent = new Intent(getApplicationContext(), CreateMedicationActivity.class);
                break;
            default:
                intent = new Intent(getApplicationContext(), CreateAppointmentActivity.class);
        }

        startActivity(intent);
    }


    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        viewPager.setCurrentItem(tab.getPosition());
    }


    @Override
    public void onTabUnselected(TabLayout.Tab tab) { //Required method
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) { //Required method
    }
}
