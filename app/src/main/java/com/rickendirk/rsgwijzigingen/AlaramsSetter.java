package com.rickendirk.rsgwijzigingen;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

public class AlaramsSetter {

    public AlaramsSetter() {
    }
    
    public static void cancelAlarms(Context context){
        Intent zoekIntent = new Intent(context, ZoekService.class);
        PendingIntent alarmIntent1 = PendingIntent.getService(context, 1,
                zoekIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent alarmIntent2 = PendingIntent.getService(context, 2,
                zoekIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        manager.cancel(alarmIntent1);
        manager.cancel(alarmIntent2);
    }

    public static void setupAlarms(Context context){
        setupAlarm(1, context);
        setupAlarm(2, context);
    }


    public static void setupAlarm(int welke, Context context) {
        Intent zoekIntent = new Intent(context, ZoekService.class);
        zoekIntent.putExtra("isAchtergrond", true);
        PendingIntent alarmIntent1 = PendingIntent.getService(context, 1,
                zoekIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent alarmIntent2 = PendingIntent.getService(context, 2,
                zoekIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (welke == 1){
            Long timeMs1 = PreferenceManager.getDefaultSharedPreferences(context).getLong("timeMs1", 0);
            manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, timeMs1,
                    AlarmManager.INTERVAL_DAY, alarmIntent1);
        } else {
            Long timeMs2 = PreferenceManager.getDefaultSharedPreferences(context).getLong("timeMs2", 0);
            manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, timeMs2,
                    AlarmManager.INTERVAL_DAY, alarmIntent2);
        }
    }
}
