package com.example.mediarecorder;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;



public class SectionsPagerAdapter extends FragmentPagerAdapter {

    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        if (position == 0)
            return TravelFragment.newInstance();
        else if (position == 1)
            return SettingsFragment.newInstance();
        else if (position == 2)
            return VideoFragment.newInstance();

        else return TravelFragment.newInstance();
    }

    @Override
    public int getCount() {

        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Путешествия";
            case 1:
                return "ОПЦИИ";
            case 2:
                return "ВИДЕО";
        }
        return null;
    }
}