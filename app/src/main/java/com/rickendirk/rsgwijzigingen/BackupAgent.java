package com.rickendirk.rsgwijzigingen;

import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;

public class BackupAgent extends BackupAgentHelper {
    @Override
    public void onCreate() {
        String SPNaam = this.getPackageName() + "_preferences";
        String BackupKey = "defaultPrefs";
        SharedPreferencesBackupHelper helper = new SharedPreferencesBackupHelper(this, SPNaam);
        addHelper(BackupKey, helper);
    }

}
