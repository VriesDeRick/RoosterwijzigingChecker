package com.rickendirk.rsgwijzigingen;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.preference.PreferenceManager;

public class OwnAlarmsManager {

    public OwnAlarmsManager() {
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
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setupJobSschedulerAlarm(Context context, int welke){
        cancelJobSschedulerAlarms(context,welke);
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (welke == 1) {
            JobInfo.Builder builder1 = new JobInfo.Builder(1,
                    new ComponentName(context.getPackageName(),
                            JobSchedulerService.class.getName()));
            builder1.setPeriodic(5000)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setPersisted(true)
                    .setRequiresCharging(false)
                    .setRequiresDeviceIdle(false);
            jobScheduler.schedule(builder1.build());
        } else {
            JobInfo.Builder builder2 = new JobInfo.Builder(2,
                    new ComponentName(context.getPackageName(),
                            JobSchedulerService.class.getName()));
            builder2.setPeriodic(5000)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setPersisted(true)
                    .setRequiresCharging(false)
                    .setRequiresDeviceIdle(false);
            jobScheduler.schedule(builder2.build());
        }
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void cancelJobSschedulerAlarms(Context context, int welke){
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (welke == 1)jobScheduler.cancel(1);
        else jobScheduler.cancel(2);
    }
}
