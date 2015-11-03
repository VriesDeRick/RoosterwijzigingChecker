package com.rickendirk.rsgwijzigingen;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class TimeZoneChangedReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        OwnAlarmsManager.cancelAlarms(context);
        OwnAlarmsManager.setupAlarms(context);
    }
}
