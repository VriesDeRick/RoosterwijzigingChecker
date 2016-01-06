package com.rickendirk.rsgwijzigingen;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.ArraySet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class WijzigingenList {

    ArrayList<String> wijzigingen;
    String stand;
    String dagEnDatum;
    String standZin;
    boolean setupComplete = false;

    public WijzigingenList(String dagEnDatum, String stand) {
        initVar();
        this.dagEnDatum = dagEnDatum;
        this.stand = stand;
        standZin = "Stand van" + stand;
        setupComplete = true;
    }

    public WijzigingenList(boolean moetUitSP, Context context) {
        initVar();
        loadFromSP(context);
    }

    private void initVar() {
        wijzigingen = new ArrayList<>();
    }

    private void saveToSP(Context context){
        SharedPreferences.Editor spEditor = PreferenceManager
                .getDefaultSharedPreferences(context).edit();
        //Stand wordt opgeslagen als standzin
        spEditor.putString("stand", standZin);
        spEditor.putString("dagEnDatum", dagEnDatum);

        Set<String> wijzigingenSet = new HashSet<>();
        wijzigingenSet.addAll(wijzigingen);
        spEditor.putStringSet("last_wijzigingenList", wijzigingenSet);
        spEditor.commit();
    }

    private void loadFromSP(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> wijzigingenSet = sp.getStringSet("last_wijzigingenList", null);
        if (wijzigingenSet != null) {
            wijzigingen.addAll(wijzigingenSet);
        }
        standZin = sp.getString("stand", "");
        dagEnDatum = sp.getString("dagEnDatum", "geenWaarde");
        setupComplete = true;
    }

}
