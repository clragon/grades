package com.gradestat;


import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;


public class Settings extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.settings);
        }

        this.findPreference("dark").setOnPreferenceChangeListener((preference, newValue) -> {
            ((MainActivity) getActivity()).changeTheme((Boolean) newValue);
            return true;
        });

        this.findPreference("colorRings").setOnPreferenceChangeListener((preference, newValue) -> {
            getActivity().recreate();
            return true;
        });

    }
}