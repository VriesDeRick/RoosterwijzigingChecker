package com.rickendirk.rsgwijzigingen;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Wijzigingen implements Parcelable {

    private ArrayList<String> wijzigingen;
    private String dagEnDatum;
    private String standZin;
    private String fout;
    public boolean setupComplete = false;
    public boolean zijnWijzigingen = false;

    private String message;
    public boolean hasMessage = false;

    public Wijzigingen(String dagEnDatum, String standZin) {
        initVar();
        this.dagEnDatum = dagEnDatum;
        this.standZin = standZin;
        setupComplete = true;
    }

    public Wijzigingen(boolean moetUitSP, @Nullable Context context) {
        initVar();
        if (moetUitSP) loadFromSP(context);
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
    public int getSize() {
        return wijzigingen.size();
    }

    public void setFout(String fout) {
        this.fout = fout;
    }

    public void setDagEnDatum(String dagEnDatum) {
        this.dagEnDatum = dagEnDatum;
    }

    public void setStandZin(String standZin) {
        this.standZin = standZin;
    }

    public String getFout() {
        return fout;
    }

    public void setWijzigingen(ArrayList<String> wijzigingen) {
        this.wijzigingen = wijzigingen;
    }
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
        hasMessage = true;
    }

    private void initVar() {
        wijzigingen = new ArrayList<>();
        zijnWijzigingen = false;
    }

    public void saveToSP(Context context){
        SharedPreferences.Editor spEditor = PreferenceManager
                .getDefaultSharedPreferences(context).edit();
        //Stand wordt opgeslagen als standzin
        spEditor.putString("stand", standZin);
        spEditor.putString("dagEnDatum", dagEnDatum);
        if (hasMessage) {
            spEditor.putString("message", message);
        }
        spEditor.putBoolean("hasMessage", hasMessage);

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
            zijnWijzigingen = true;
        }
        standZin = sp.getString("stand", "");
        dagEnDatum = sp.getString("dagEnDatum", "geenWaarde");
        hasMessage = sp.getBoolean("hasMessage", false);
        if (hasMessage) message = sp.getString("message", null);
        setupComplete = true;
    }
    public boolean isFoutmelding(){
        return fout != null; //Fout is null, dan geen fout, dus isFoutmelding false bij null
    }
    public boolean isVerbindfout(){
        if (isFoutmelding()) {
            return fout.equals("verbindFout");
        } else return false;
    }

    public boolean isNieuw(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String standOud = sp.getString("stand", "geenWaarde");
        if (!standOud.equals("geenWaarde")){
            return !standZin.equals(standOud); //Wel gelijk, dus niet nieuw en omgekeerd
        } else return true; //Goedkeuren als er nog geen waarde was: sowieso nieuw
    }

    public void addWijziging(String wijziging){
        wijzigingen.add(wijziging);
        zijnWijzigingen = true;
    }
    public void removeWijziging(int index){
        wijzigingen.remove(index);
        if (wijzigingen.size() == 0) zijnWijzigingen = false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this.wijzigingen);
        dest.writeString(this.dagEnDatum);
        dest.writeString(this.standZin);
        dest.writeString(this.fout);
        dest.writeByte(setupComplete ? (byte) 1 : (byte) 0);
        dest.writeByte(zijnWijzigingen ? (byte) 1 : (byte) 0);
        dest.writeString(this.message);
        dest.writeByte(hasMessage ? (byte) 1 : (byte) 0);
    }

    private Wijzigingen(Parcel in) {
        this.wijzigingen = (ArrayList<String>) in.readSerializable();
        this.dagEnDatum = in.readString();
        this.standZin = in.readString();
        this.fout = in.readString();
        this.setupComplete = in.readByte() != 0;
        this.zijnWijzigingen = in.readByte() != 0;
        this.message = in.readString();
        this.hasMessage = in.readByte() != 0;
    }

    public static final Creator<Wijzigingen> CREATOR = new Creator<Wijzigingen>() {
        public Wijzigingen createFromParcel(Parcel source) {
            return new Wijzigingen(source);
        }

        public Wijzigingen[] newArray(int size) {
            return new Wijzigingen[size];
        }
    };
}
