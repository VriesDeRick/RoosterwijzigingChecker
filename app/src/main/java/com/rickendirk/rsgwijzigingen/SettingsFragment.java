package com.rickendirk.rsgwijzigingen;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
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
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.android.datetimepicker.time.RadialPickerLayout;
import com.android.datetimepicker.time.TimePickerDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Set;

public class SettingsFragment extends PreferenceFragment implements
        SharedPreferences.OnSharedPreferenceChangeListener, TimePickerDialog.OnTimeSetListener {
    private static final String TIME_PATTERN = "HH:mm";
    private Calendar calendar1;
    private Calendar calendar2;
    private SimpleDateFormat timeFormat;
    //int om bij te houden op welke v/d 2 zoektijden is gedrukt
    int welke = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupCalendar();
        timeFormat = new SimpleDateFormat(TIME_PATTERN, Locale.getDefault());

        // Instellingen laden
        addPreferencesFromResource(R.xml.preferences);
        //Alle "over deze app"-kliks verwerken
        Preference about = findPreference("pref_about");
        about.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent overAppIntent = new Intent(getActivity(), overAppActivity.class);
                startActivity(overAppIntent);
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
        Preference time1 = findPreference("pref_auto_tijd1");
        final TimePickerDialog.OnTimeSetListener listener = this;
        time1.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                welke = 1;
                TimePickerDialog.newInstance(listener, calendar1.get(Calendar.HOUR_OF_DAY),
                        calendar1.get(Calendar.MINUTE), true).show(getFragmentManager(), "timePicker1");
                return true;
            }
        });
        Preference time2= findPreference("pref_auto_tijd2");
        time2.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                welke = 2;
                TimePickerDialog.newInstance(listener, calendar2.get(Calendar.HOUR_OF_DAY),
                        calendar2.get(Calendar.MINUTE), true).show(getFragmentManager(), "timePicker2");
                return true;
            }
        });


    }

    private void setupCalendar() {
        calendar1 = Calendar.getInstance();
        calendar2 = Calendar.getInstance();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        long timeInMsCal1 = sp.getLong("timeMs1", 0);
        long timeInMsCal2 = sp.getLong("timeMs2", 0);
        //Als er geen tijd is huidige tijd pakken
        if (timeInMsCal1 == 0){
            Calendar tijdOchtend = Calendar.getInstance();
            tijdOchtend.set(Calendar.HOUR_OF_DAY, 7);
            tijdOchtend.set(Calendar.MINUTE, 50);
            timeInMsCal1 = tijdOchtend.getTimeInMillis();
        }
        if (timeInMsCal2 == 0){
            Calendar tijdMidag = Calendar.getInstance();
            tijdMidag.set(Calendar.HOUR_OF_DAY, 14);
            tijdMidag.set(Calendar.MINUTE, 5);
            timeInMsCal2 = tijdMidag.getTimeInMillis();
        }
        calendar1.setTimeInMillis(timeInMsCal1);
        calendar2.setTimeInMillis(timeInMsCal2);
    }
    @Override
    public void onTimeSet(RadialPickerLayout radialPickerLayout, int hourOfDay, int minute) {
        if (welke == 1){
            calendar1.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar1.set(Calendar.MINUTE, minute);
            saveToSP(1);
        } else{
            calendar2.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar2.set(Calendar.MINUTE, minute);
            saveToSP(2);
        }
        setupAlarm();
        setTimeSummary();
    }

    private void setupAlarm() {
        Intent zoekIntent = new Intent(getActivity(), ZoekService.class);
        PendingIntent alarmIntent1 = PendingIntent.getBroadcast(getActivity(), 1,
                zoekIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent alarmIntent2 = PendingIntent.getBroadcast(getActivity(), 2,
                zoekIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager manager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        if (welke == 1){
            manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar1.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, alarmIntent1);
        } else {
            manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar2.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, alarmIntent2);
        }
    }

    private void saveToSP(int welke) {
        SharedPreferences.Editor spEditor = PreferenceManager
                .getDefaultSharedPreferences(getActivity()).edit();
        if (welke == 1){
            long timeMs1 = calendar1.getTimeInMillis();
            spEditor.putLong("timeMs1", timeMs1);
        } else{
            long timeMs2 = calendar2.getTimeInMillis();
            spEditor.putLong("timeMs2", timeMs2);
        }
        spEditor.commit();
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
            // EditPreference
            EditTextPreference editTextPref = (EditTextPreference) pref;
            editTextPref.setSummary(editTextPref.getText());

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
        //Beschrijvingen timepickers goedzetten
        setTimeSummary();
    }

    private void setTimeSummary() {
        Preference time1 = findPreference("pref_auto_tijd1");
        time1.setSummary(getString(R.string.timePickerSum) + ". Huidig: "
                + timeFormat.format(calendar1.getTime()));
        //Zelfde voor 2e picker
        Preference time2 = findPreference("pref_auto_tijd2");
        time2.setSummary(getString(R.string.timePickerSum) + ". Huidig: "
                + timeFormat.format(calendar2.getTime()));
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
