package com.gradestat;


import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;


public class Settings extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {

            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.settings);
        }

        this.findPreference("dark").setOnPreferenceChangeListener((preference, newValue) -> {
            getActivity().recreate();
            // could close settings after theme change, but it's weird.
            // getFragmentManager().popBackStackImmediate();
            return true;
        });

        checkInvertSwitch(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("sorting", getString(R.string.sort_by_custom)));

        this.findPreference("sorting").setOnPreferenceChangeListener((preference, newValue) -> {
            checkInvertSwitch(newValue);
            return true;
        });

    }

    private void checkInvertSwitch(Object newValue) {
        Preference invertSwitch = this.findPreference("sorting_invert");
        if (newValue.equals(getString(R.string.sort_by_custom))) {
            invertSwitch.setEnabled(false);
        } else {
            invertSwitch.setEnabled(true);
        }
    }
}