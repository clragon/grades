package com.gradestat;


import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import java.util.Objects;


public class Settings extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        if (((AppCompatActivity) requireActivity()).getSupportActionBar() != null) {
            Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setTitle(R.string.settings);
        }

        Objects.requireNonNull((androidx.preference.Preference) this.findPreference("dark")).setOnPreferenceChangeListener((preference, newValue) -> {
            requireActivity().recreate();
            // could close settings after theme change, but it's weird.
            // getFragmentManager().popBackStackImmediate();
            return true;
        });

        checkInvertSwitch(PreferenceManager.getDefaultSharedPreferences(requireActivity()).getString("sorting", "sorting_custom"));

        Objects.requireNonNull((androidx.preference.Preference) this.findPreference("sorting")).setOnPreferenceChangeListener((preference, newValue) -> {
            checkInvertSwitch(newValue);
            return true;
        });

        checkCompensateDouble(PreferenceManager.getDefaultSharedPreferences(requireActivity()).getBoolean("compensate", true));

        Objects.requireNonNull((androidx.preference.Preference) this.findPreference("compensate")).setOnPreferenceChangeListener((preference, newValue) -> {
            checkCompensateDouble(newValue);
            return true;
        });
    }

    private void checkInvertSwitch(Object newValue) {
        Preference invertSwitch = Objects.requireNonNull(this.findPreference("sorting_invert"));
        invertSwitch.setEnabled(!newValue.equals("sorting_custom"));
        invertSwitch.setVisible(!newValue.equals("sorting_custom"));
    }

    private void checkCompensateDouble(Object newValue) {
        Preference compensateDouble = Objects.requireNonNull(this.findPreference("compensateDouble"));
        compensateDouble.setEnabled((boolean) newValue);
        compensateDouble.setVisible((boolean) newValue);
    }
}