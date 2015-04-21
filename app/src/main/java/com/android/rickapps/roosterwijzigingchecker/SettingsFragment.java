package com.android.rickapps.roosterwijzigingchecker;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Instellingen laden
        addPreferencesFromResource(R.xml.preferences);}
}
