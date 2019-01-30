package com.example.mzw5443.selfcare;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * This class creates a pager to use with the ViewPager and TabHost,
 * for sliding between tabbed activities
 *
 * Resources referenced:
 *         Creating Swipe Views with Tabs -  https://developer.android.com/training/implementing-navigation/lateral.html
 *         ViewPager - https://developer.android.com/reference/android/support/v4/view/ViewPager.html
 */


public class Pager extends FragmentStatePagerAdapter {

    private String[] tabTitles = new String[]{"Appointments", "Medications"};

    Pager(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new AppointmentFragment();
            case 1:
                return new MedicationFragment();
            default:
                return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position){
        return tabTitles[position];
    }

    @Override
    public int getCount() {
        return tabTitles.length;
    }
}
