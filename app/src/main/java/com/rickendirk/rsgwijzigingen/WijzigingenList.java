package com.rickendirk.rsgwijzigingen;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class WijzigingenList {

    private ArrayList<String> wijzigingen;
    private String dagEnDatum;
    private String standZin;
    public boolean setupComplete = false;

    public WijzigingenList(String dagEnDatum, String standZin) {
        initVar();
        this.dagEnDatum = dagEnDatum;
        this.standZin = standZin;
        setupComplete = true;
    }

    public WijzigingenList(boolean moetUitSP, Context context) {
        initVar();
        loadFromSP(context);
    }

    public String getStandZin() {
        return standZin;
    }

    public ArrayList<String> getWijzigingen() {
        return wijzigingen;
    }

    public String getDagEnDatum() {
        return dagEnDatum;
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

    public void addWijziging(String wijziging){
        wijzigingen.add(wijziging);
    }

}
