package com.rickendirk.rsgwijzigingen;

import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;
import android.content.Context;

public class BackupAgent extends BackupAgentHelper {
    @Override
    public void onCreate() {
        Context context = getApplicationContext();
        String SPNaam = context.getPackageName() + "_preferences";
        String BackupKey = "defaultPrefs";
        SharedPreferencesBackupHelper helper = new SharedPreferencesBackupHelper(context, SPNaam);
        addHelper(BackupKey, helper);
    }

}
