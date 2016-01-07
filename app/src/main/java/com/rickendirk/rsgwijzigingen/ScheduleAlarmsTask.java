package com.rickendirk.rsgwijzigingen;

import android.content.Context;

import java.util.TimerTask;

/**
 * Created by Rick on 7-1-2016.
 */
public class ScheduleAlarmsTask extends TimerTask {
    Context context;
    int welke;
    public ScheduleAlarmsTask(Context context, int welke) {
        this.context = context;
        this.welke = welke;
    }

    @Override
    public void run() {
        OwnAlarmsManager.setupJobSschedulerAlarm(context, welke);
    }
}
