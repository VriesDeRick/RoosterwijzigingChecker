package com.android.rickapps.roosterwijzigingchecker;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Instellingen laden
        addPreferencesFromResource(R.xml.preferences);
        SharedPreferences sharedPref = getPreferenceScreen().getSharedPreferences();
        sharedPref.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //Veranderde pref vinden
        Preference pref = findPreference(key);
        //Huidige summary krijgen
        String summaryStr = (String)pref.getSummary();
        //Nieuwe data opvragen
        String prefixStr = sharedPreferences.getString(key, "");
        //Nieuwe data als summary neerzetten
        pref.setSummary(summaryStr.concat(": [").concat(prefixStr).concat("]"));
    }
}
