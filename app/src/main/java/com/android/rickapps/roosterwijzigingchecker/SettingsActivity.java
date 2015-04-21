package com.android.rickapps.roosterwijzigingchecker;

import android.app.ActionBar;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {
    //TODO: Text in Actionbar uitvogelen
    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //ActionBar actionBar = getActionBar();
        //actionBar.setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_settings);

        // Define the xml file used for preferences
        addPreferencesFromResource(R.xml.preferences);
    }

}
