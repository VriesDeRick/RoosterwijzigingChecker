package com.rickendirk.rsgwijzigingen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

public class BootCompleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Boolean isAlarmsEnabled = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean("pref_auto_zoek", false);
        if (isAlarmsEnabled) {
            AlarmsSetter setter = new AlarmsSetter(context);
            setter.setupAlarms();
        }
    }
}
