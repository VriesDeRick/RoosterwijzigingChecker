package com.rickendirk.rsgwijzigingen;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import java.util.Set;

public class SettingsFragment extends PreferenceFragment implements
        SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Instellingen laden
        addPreferencesFromResource(R.xml.preferences);
        //Alle "over deze app"-kliks verwerken
        Preference about = findPreference("pref_about");
        about.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new AlertDialog.Builder(getActivity())
                    .setTitle("Over Roosterwijzigingchecker")
                    .setMessage(getString(R.string.overDezeApp))
                    .setPositiveButton("OK", null)
                    .show();
                return true;
            }
        });
        Preference goToGit = findPreference("pref_goToGit");
        goToGit.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                //Opent Github in webbrowser
                String gitURL = "https://github.com/Richyrick/RoosterwijzigingChecker";
                Intent gitInt = new Intent(Intent.ACTION_VIEW);
                gitInt.setData(Uri.parse(gitURL));
                startActivity(gitInt);
                return true;
            }
        });
        Preference email = findPreference("pref_mail");
        email.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                //Stuurt email naar ontwikkelaars' emailadres
                Intent mailIntent = new Intent(Intent.ACTION_SEND);
                mailIntent.setType("plain/text");
                mailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"wijzigingchecker@outlook.com"});
                mailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback over RSG-Wijzigingen");
                //Error als er geen mailapp geinstalleerd is
                try{
                    startActivity(mailIntent);
                } catch(android.content.ActivityNotFoundException e){
                    Toast.makeText(getActivity(), "Geen E-mailapplicaties gevonden", Toast.LENGTH_LONG).show();
                }
                return true;
            }
        });
        Preference voorwaarden = findPreference("pref_voorwaarden");
        voorwaarden.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("Voorwaarden")
                        .setMessage(getString(R.string.voorwaarden))
                        .setPositiveButton("OK", null)
                        .show();
                return true;
            }
        });


    }


    // Alle onderstaande code afkomstig van
    // http://gmariotti.blogspot.nl/2013/02/preference-summary-or-secondary-text.html
    @Override
    public void onResume() {
        super.onResume();

        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);

        initSummary();
    }

    @Override
    public void onPause() {
        super.onPause();

        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {

        //update summary
        updatePrefsSummary(sharedPreferences, findPreference(key));

    }
    /**
     * Update summary
     */
    protected void updatePrefsSummary(SharedPreferences sharedPreferences,
                                      Preference pref) {

        if (pref == null)
            return;

        if (pref instanceof ListPreference) {
            // List Preference
            ListPreference listPref = (ListPreference) pref;
            listPref.setSummary(listPref.getEntry());

        } else if (pref instanceof EditTextPreference) {
            //Mag niet updaten bij tijdprefs
            Preference tijd1 = findPreference("pref_auto_tijd1");
            Preference tijd2 = findPreference("pref_auto_tijd2");
            // EditPreference
            if (!pref.equals(tijd1) && !pref.equals(tijd2)){
            EditTextPreference editTextPref = (EditTextPreference) pref;
            editTextPref.setSummary(editTextPref.getText());
            }

        } else if (pref instanceof MultiSelectListPreference) {
            // MultiSelectList Preference
            MultiSelectListPreference mlistPref = (MultiSelectListPreference) pref;
            String summaryMListPref = "";
            String and = "";

            // Retrieve values
            Set<String> values = mlistPref.getValues();
            for (String value : values) {
                // For each value retrieve index
                int index = mlistPref.findIndexOfValue(value);
                // Retrieve entry from index
                CharSequence mEntry = index >= 0
                        && mlistPref.getEntries() != null ? mlistPref
                        .getEntries()[index] : null;
                if (mEntry != null) {
                    // add summary
                    summaryMListPref = summaryMListPref + and + mEntry;
                    and = ";";
                }
            }
            // set summary
            mlistPref.setSummary(summaryMListPref);

        }
    }
    /*
	 * Init summary
	 */
    protected void initSummary() {
        for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
            initPrefsSummary(getPreferenceManager().getSharedPreferences(),
                    getPreferenceScreen().getPreference(i));
        }
    }

    /*
     * Init single Preference
     */
    protected void initPrefsSummary(SharedPreferences sharedPreferences,
                                    Preference p) {
        if (p instanceof PreferenceCategory) {
            PreferenceCategory pCat = (PreferenceCategory) p;
            for (int i = 0; i < pCat.getPreferenceCount(); i++) {
                initPrefsSummary(sharedPreferences, pCat.getPreference(i));
            }
        } else {
            updatePrefsSummary(sharedPreferences, p);
        }
    }
}
