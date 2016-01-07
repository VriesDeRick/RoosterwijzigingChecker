package com.rickendirk.rsgwijzigingen;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class JobSchedulerService extends JobService {

    JobParameters params;
    @Override
    public boolean onStartJob(JobParameters params) {
        startZoekService();
        this.params = params;
        return true;
    }

    private void startZoekService() {
        Intent zoekIntent = new Intent(this, ZoekService.class);
        zoekIntent.putExtra("isAchtergrond", true);
        zoekIntent.putExtra("isJobService", true);
        startService(zoekIntent);
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
    public class ZoekReceiver extends BroadcastReceiver {

        public static final String ACTION_RESP =
                "com.rickendirk.intent.action.MESSAGE_PROCESSED"; //Nodig voor intentfilter

        @Override
        public void onReceive(Context context, Intent intent) {
            jobFinished(params, false);
        }
    }
}
