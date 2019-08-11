package com.gradecalc;


import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

import com.afollestad.aesthetic.Aesthetic;


public class Settings extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.settings);
        }


        this.findPreference("dark").setOnPreferenceChangeListener((preference, newValue) -> {
            if ((Boolean) newValue) {
                Aesthetic.get()
                        .activityTheme(R.style.AppTheme)
                        .isDark(true)
                        .apply();
            } else {
                Aesthetic.get()
                        .activityTheme(R.style.AppThemeLight)
                        .isDark(false)
                        .apply();
            }
            return true;
        });

    }
}