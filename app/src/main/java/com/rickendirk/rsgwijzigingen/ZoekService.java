package com.rickendirk.rsgwijzigingen;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;

public class ZoekService extends IntentService{

    public static final int notifID = 3395;

    public ZoekService(){
        super("Zoekservice");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Boolean clusters_enabled = sp.getBoolean("pref_cluster_enabled", true);
        ArrayList<String> wijzigingen;
        if (clusters_enabled){
            wijzigingen = checkerClusters();
        }
        else {
            wijzigingen = checkerKlas();
        }
        //Tracken dat er is gezocht
        OwnApplication application = (OwnApplication) getApplication();
        Tracker tracker = application.getDefaultTracker();
        boolean isAchtergrond = intent.getBooleanExtra("isAchtergrond", false);
        if (isAchtergrond){
            tracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Acties")
                    .setAction("Zoeken_achtergrond")
                    .build());
            sendNotification(wijzigingen);
        } else {
            tracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Acties")
                    .setAction("Zoeken_voorgrond")
                    .build());
            broadcastResult(wijzigingen, clusters_enabled);
        }

    }

    private void sendNotification(ArrayList<String> wijzigingen) {
        boolean isFoutMelding = isFoutmelding(wijzigingen);
        boolean zijnWijzigingen = zijnWijzigingen(wijzigingen);
        ArrayList<String> schoneLijst = new ArrayList<>();
        if (!isFoutMelding){
            schoneLijst = maakLijstSchoon(wijzigingen);
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.rsg_2)
                .setContentTitle("Roosterwijzigingen");
        if (isFoutMelding){
            builder.setContentText("Er was een fout. Probeer het handmatig opnieuw");
        } else {
            if (zijnWijzigingen){
                if (schoneLijst.size() == 1){
                    builder.setContentText(schoneLijst.get(0));
                } else {
                    builder.setContentText("Er zijn " + schoneLijst.size() + " wijzigingen!");
                    NotificationCompat.InboxStyle inboxStyle =
                            new NotificationCompat.InboxStyle();
                    inboxStyle.setBigContentTitle("De roosterwijzigingen zijn:");
                    for (int i = 0; i < schoneLijst.size(); i++){
                        inboxStyle.addLine(schoneLijst.get(i));
                    }
                    builder.setStyle(inboxStyle);
                }
            } else {
                builder.setContentText("Er zijn geen roosterwijzigingen.");
            }
        }
        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent pendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        NotificationManagerCompat notifManager = NotificationManagerCompat.from(this);
        notifManager.notify(notifID, builder.build());
        vibrate();
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator.hasVibrator()){
            //Eerste waarde vertraging, 2e duur, 3e vertraging, 4e duur, etc
            long[] pattern = {0,100,400,100};
            vibrator.vibrate(pattern, - 1); // -1 betekent geen herhaling
        }
    }

    private ArrayList<String> maakLijstSchoon(ArrayList<String> wijzigingen) {
        wijzigingen.remove(wijzigingen.size() - 1);
        wijzigingen.remove(wijzigingen.size() - 1);
        return wijzigingen;
    }

    private boolean zijnWijzigingen(ArrayList<String> wijzigingen) {
        String listLaatst = wijzigingen.get(wijzigingen.size() -3); // 3 Omdat daarvoor stand/datum bevat
        if (listLaatst.equals("Er zijn geen wijzigingen.")){
            return false;
        } else return true;
    }

    private boolean isFoutmelding(ArrayList<String> wijzigingen) {
        String listLaatst = wijzigingen.get(wijzigingen.size() - 1);
        if (listLaatst.equals("geenKlas") || listLaatst.equals("verbindFout") ||
                listLaatst.equals("EersteTekenLetter") || listLaatst.equals("klasMeerDan4Tekens") ||
                listLaatst.equals("geenTabel") || listLaatst.equals("andereFout") ||
                listLaatst.equals("geenClusters")){
            return true;
        } else {
            return false;
        }
    }

    private void broadcastResult(ArrayList wijzigingen, Boolean clusters_enabled) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.setAction(MainFragment.ZoekReceiver.ACTION_RESP); //Nodig voor intentfilter
        broadcastIntent.putParcelableArrayListExtra("wijzigingen", wijzigingen);
        if (clusters_enabled){
            broadcastIntent.putExtra("clustersAan", true);
        }
        else{
            broadcastIntent.putExtra("clustersAan", false);
        }
        sendBroadcast(broadcastIntent);
    }

    /*
    Op dit moment is onderstaande code niet nodig, laat het staan voor testdoeleinden
    public String getURL(){
        int nummerOud = PreferenceManager.getDefaultSharedPreferences(this)
                .getInt("URLInt", 2);
        //Wisselen tussen URL's via even/oneven nummer: if is even, else is oneven
        String URLStr;
        if (nummerOud%2 == 0){
            URLStr = "https://raw.githubusercontent.com/Richyrick/RoosterwijzigingChecker/master/deps/example_website.htm";
        } else{
            URLStr = "https://raw.githubusercontent.com/Richyrick/RoosterwijzigingChecker/master/deps/example_website_2.htm";
        }
        int nummerNieuw = nummerOud + 1;
        //Nieuw nummer opslaan voor volgende keer
        SharedPreferences.Editor spEditor = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext()).edit();
        spEditor.putInt("URLInt", nummerNieuw);
        spEditor.commit();

        return URLStr;
    } */

    private ArrayList<String> checkerKlas() {
        ArrayList<String> tempList = new ArrayList<>();
        //String halen uit SP
        String klasTextS = PreferenceManager.getDefaultSharedPreferences
                (getApplicationContext()).getString("pref_klas", "");
        //Checken of klas niet leeg is
        if (klasTextS.equals("")){
            tempList.add("geenKlas");
            return tempList;
        }
        //Eerste teken klas mag geen letter zijn
        if(Character.isLetter(klasTextS.charAt(0))) {
            tempList.add("EersteTekenLetter");
            return tempList;
        }
        //String opsplitsen in 2 delen, om naar hoofdletters te converteren
        char charcijfer = klasTextS.charAt(0);
        String klascijfer = String.valueOf(charcijfer);
        char charafdeling = klasTextS.charAt(1);
        String klasafdelingBig = String.valueOf(charafdeling).toUpperCase();
        //Onderstaand voor hoofdlettercorrectie
        String klasCorrect; //KlasCorrect is klas na hoofdlettercorrectie
        switch (klasTextS.length()){
            case 2:
                klasCorrect = klascijfer + klasafdelingBig;
                break;
            case 3:
                char klasabc = klasTextS.charAt(2);
                String klasabcSmall = String.valueOf(klasabc).toLowerCase();
                klasCorrect = klascijfer + klasafdelingBig + klasabcSmall;
                break;
            case 4:
                char klasafdeling2 = klasTextS.charAt(2);
                String klasafdeling2Big = String.valueOf(klasafdeling2).toUpperCase();
                klasabc = klasTextS.charAt(3);
                klasabcSmall = String.valueOf(klasabc).toLowerCase();

                klasCorrect = klascijfer + klasafdelingBig + klasafdeling2Big + klasabcSmall;
                break;
            default:
                tempList.add("klasMeerDan4Tekens");
                return tempList;
        }

        //Try en catch in het geval dat de internetverbinding mist
        try {
            String url = "http://www.rsgtrompmeesters.nl/roosters/roosterwijzigingen/Lijsterbesstraat/subst_001.htm";
            Document doc = Jsoup.connect(url).get();
            Elements tables = doc.select("table");
            if (tables.size() < 2){
                //Geen geschikte tabel aanwezig
                tempList.add("geenTabel");
                return tempList;
            }
            Element table = tables.get(1);
            Elements rows = table.select("tr");
            //Loop genereren, voor elke row kijken of het de goede tekst bevat
            //Beginnen bij 4e, bovenstaande is niet belangrijk
            for (int i = 2; i < rows.size(); i++) {
                Element row = rows.get(i);
                Elements cols = row.select("td");


                if (cols.get(0).text().contains(klasCorrect)) {
                    //If in geval van uitval, else ingeval van wijziging
                    if (Jsoup.parse(cols.get(6).toString()).text().contains("--")){
                        String wijziging =
                                Jsoup.parse(cols.get(1).toString()).text() + "e uur " +
                                        Jsoup.parse(cols.get(2).toString()).text() + " valt uit.";
                        tempList.add(wijziging);
                    }
                    else {
                        String vakOud = Jsoup.parse(cols.get(2).toString()).text();
                        String docentOud = Jsoup.parse(cols.get(3).toString()).text();
                        String vakNieuw = Jsoup.parse(cols.get(4).toString()).text();
                        String docentNieuw = Jsoup.parse(cols.get(5).toString()).text();
                        String wijzigingKaal;
                        if (vakOud.equals(vakNieuw) && !docentOud.equals(docentNieuw)){
                            // Opvang door andere docent: dit staat alleen bij klas omdat
                            // dit amper gebeurt bij clusters
                            wijzigingKaal = Jsoup.parse(cols.get(1).toString()).text() + "e uur " +
                                    docentOud + " wordt opgevangen door " + docentNieuw + ".";
                        } else { //Geen opvang door andere docent, lokaalwijziging oid
                            wijzigingKaal =
                                    // Voegt alle kolommen samen tot 1 string
                                    // .text() zorgt voor leesbare text
                                    // Spaties voor leesbaarheid
                                    Jsoup.parse(cols.get(1).toString()).text() + "e uur " +
                                            Jsoup.parse(cols.get(2).toString()).text() + " " +
                                            Jsoup.parse(cols.get(3).toString()).text() + " wordt " +
                                            Jsoup.parse(cols.get(4).toString()).text() + " " +
                                            Jsoup.parse(cols.get(5).toString()).text() + " in " +
                                            Jsoup.parse(cols.get(6).toString()).text();
                        }
                        //ipv en naar bevatten een "/" ivm uren (ma 12-04 / 4)
                        String ipv = "";
                        if (Jsoup.parse(cols.get(7).toString()).text().contains("/")) {
                            ipv = "ipv " + Jsoup.parse(cols.get(7).toString()).text();
                        }
                        String naar = "";
                        if (Jsoup.parse(cols.get(8).toString()).text().contains("/")) {
                            naar = "naar " + Jsoup.parse(cols.get(8).toString()).text() + " ";
                        }
                        String vervangingsTekst = "";
                        //&nbsp; staat in lege cell, encoding enz, zie volgende link:
                        // http://stackoverflow.com/questions/26837034/how-to-tell-if-a-html-table-has-an-empty-cell-nbsp-using-jsoup
                        //Soms veregeten ze de opmerkingen, dan krijg je size = 9 en error
                        if (cols.size() > 9) {
                            if (!Jsoup.parse(cols.get(9).toString()).text().equals("\u00a0")) {
                                vervangingsTekst = "(" + Jsoup.parse(cols.get(9).toString()).text() + ")";
                            }
                        }
                        String wijziging = wijzigingKaal + " " + ipv + " " + naar + " "
                                + vervangingsTekst;
                        tempList.add(wijziging);

                    }

                }
                //Geen wijzigingen pas bij laatste rij
                if (i == rows.size() - 1){
                    //Checken of tempList leeg is, zo ja 1 ding toevoegen
                    if (tempList.isEmpty()){
                        tempList.add("Er zijn geen wijzigingen.");
                    }
                    //Dag waarvoor wijzigingen zijn ophalen
                    Element dag = doc.select("body > div > div:nth-child(2) > p > b > span").first();
                    String dagStr = dag.text().toLowerCase();
                    // Woorden staan verkeerd om: omwisselen
                    int indexVanSpatie = dagStr.indexOf(" ");
                    String datum = dagStr.substring(0, indexVanSpatie);
                    String rest = dagStr.substring(indexVanSpatie + 1);
                    String dagGoed = rest + " " + datum;
                    tempList.add(dagGoed);

                    //Stand ophalen: staat in 1e tabel van HTML
                    Element tableDate = doc.select("table").get(0);
                    String dateFullText = tableDate.getElementsContainingOwnText("Stand:").text();
                    //Deel achter "Stand:" pakken
                    String FullTextSplit[] = dateFullText.split("Stand:");
                    tempList.add(FullTextSplit[1]);
                    return tempList;
                }


            }
        }
        catch(java.io.IOException e) {
            //Error toevoegen aan tempList, dat wordt weergegeven in messagebox
            tempList.clear();
            tempList.add("verbindFout");
            return tempList;
        }
        //Zover hoort de method NOOIT te komen
        tempList.add("andereFout");
        return tempList;
    }
    private ArrayList<String> checkerClusters() {
        ArrayList<String> tempList = new ArrayList<>();
        //String van klas halen uit SP
        String klasTextS = PreferenceManager.getDefaultSharedPreferences
                (getApplicationContext()).getString("pref_klas", "");
        String url = "http://www.rsgtrompmeesters.nl/roosters/roosterwijzigingen/Lijsterbesstraat/subst_001.htm";
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        //Clusters ophalen uit SP
        ArrayList<String> clusters = new ArrayList<>();
        for (int a = 1; a < 15; a++){
            //String initializen, anders kan hij hem niet toevoegen
            String clusterLowCase = "";
            String cluster = sp.getString("pref_cluster" + a, "");
            //If om nullpointer te voorkomen
            if (!cluster.equals("")){
                clusterLowCase = cluster.substring(0, 1).toLowerCase() +
                        cluster.substring(1);
            }
            clusters.add(clusterLowCase);
        }

        //Lege clusters weghalen uit arraylist TODO: Kijken of singleton werkt/wat het is
        clusters.removeAll(Collections.singleton(""));
        //Er moeten wel clusters zijn ingevoerd: Zo nee, komt AlertDialog via onPostExecute
        if (clusters.isEmpty()){
            tempList.add("geenClusters");
            return tempList;
        }
        //Checken of klas niet leeg is
        if (klasTextS.equals("")){
            tempList.add("geenKlas");
            return tempList;
        }
        //Eerste teken klas mag geen letter zijn
        if(Character.isLetter(klasTextS.charAt(0))){
            tempList.add("EersteTekenLetter");
            return tempList;
        }
        //String opsplitsen in 2 delen, om naar hoofdletters te converteren
        char charcijfer = klasTextS.charAt(0);
        String klascijfer = String.valueOf(charcijfer);
        char charafdeling = klasTextS.charAt(1);
        String klasafdelingBig = String.valueOf(charafdeling).toUpperCase();
        //Onderstaand voor hoofdlettercorrectie
        String klasCorrect; //KlasCorrect is klas na hoofdlettercorrectie
        switch (klasTextS.length()){
            case 2:
                klasCorrect = klascijfer + klasafdelingBig;
                break;
            case 3:
                char klasabc = klasTextS.charAt(2);
                String klasabcSmall = String.valueOf(klasabc).toLowerCase();
                klasCorrect = klascijfer + klasafdelingBig + klasabcSmall;
                break;
            case 4:
                char klasafdeling2 = klasTextS.charAt(2);
                String klasafdeling2Big = String.valueOf(klasafdeling2).toUpperCase();
                klasabc = klasTextS.charAt(3);
                klasabcSmall = String.valueOf(klasabc).toLowerCase();

                klasCorrect = klascijfer + klasafdelingBig + klasafdeling2Big + klasabcSmall;
                break;
            default:
                tempList.add("klasMeerDan4Tekens");
                return tempList;
        }
        //Try en catch in het geval dat de internetverbinding mist
        try {
            Document doc = Jsoup.connect(url).get();
            Elements tables = doc.select("table");
            if (tables.size() < 1){
                //Geen geschikte tabel aanwezig
                tempList.add("geenTabel");
                return tempList;
            }
            Element table = tables.get(1);
            Elements rows = table.select("tr");
            //Eerste loop is om 2e loop te herhalen voor iedere cluster, tweede loop
            //doorzoekt dan op zowel klas als cluster
            for (int b = 0; b < clusters.size(); b++){
                for (int i = 2; i < rows.size(); i++) {
                    Element row = rows.get(i);
                    Elements cols = row.select("td");


                    if (cols.get(0).text().contains(klasCorrect)
                            && cols.get(2).text().contains(clusters.get(b))) {
                        //If in geval van uitval, else ingeval van wijziging
                        if (Jsoup.parse(cols.get(6).toString()).text().contains("--")){
                            String wijziging =
                                    Jsoup.parse(cols.get(1).toString()).text() + "e uur " +
                                            Jsoup.parse(cols.get(2).toString()).text() + " valt uit.";
                            tempList.add(wijziging);
                        }
                        else {
                            String wijzigingKaal =
                                    // Voegt alle kolommen samen tot 1 string
                                    // .text() zorgt voor leesbare text
                                    // Spaties voor leesbaarheid
                                    Jsoup.parse(cols.get(1).toString()).text() + "e uur " +
                                            Jsoup.parse(cols.get(2).toString()).text() + " " +
                                            Jsoup.parse(cols.get(3).toString()).text() + " wordt " +
                                            Jsoup.parse(cols.get(4).toString()).text() + " " +
                                            Jsoup.parse(cols.get(5).toString()).text() + " in " +
                                            Jsoup.parse(cols.get(6).toString()).text();

                            //ipv en naar bevatten een "/" ivm uren (ma 12-04 / 4)
                            String ipv = "";
                            if(Jsoup.parse(cols.get(7).toString()).text().contains("/")){
                                ipv = "ipv " + Jsoup.parse(cols.get(7).toString()).text();
                            }
                            String naar = "";
                            if (Jsoup.parse(cols.get(8).toString()).text().contains("/")){
                                naar = "naar " + Jsoup.parse(cols.get(8).toString()).text() + " ";
                            }
                            String vervangingsTekst = "";
                            //&nbsp; staat in lege cell, encoding enz, zie volgende link:
                            // http://stackoverflow.com/questions/26837034/how-to-tell-if-a-html-table-has-an-empty-cell-nbsp-using-jsoup
                            if (cols.size() > 9) {
                                if (!Jsoup.parse(cols.get(9).toString()).text().equals("\u00a0")) {
                                    vervangingsTekst = "(" + Jsoup.parse(cols.get(9).toString()).text() + ")";
                                }
                            }
                            String wijziging = wijzigingKaal + " " + ipv + " " + naar + " "
                                    + vervangingsTekst;
                            tempList.add(wijziging);
                        }

                    }
                    //Geen wijzigingen pas bij laatste rij en de laatste cluster
                    if (i == rows.size() - 1 && b == clusters.size() - 1){
                        //Checken of tempList leeg is, zo ja 1 ding toevoegen
                        if (tempList.isEmpty()){
                            tempList.add("Er zijn geen wijzigingen.");
                        }
                        //Dag waarvoor wijzigingen zijn ophalen
                        Element dag = doc.select("body > div > div:nth-child(2) > p > b > span").first();
                        String dagStr = dag.text().toLowerCase();
                        // Woorden staan verkeerd om: omwisselen
                        int indexVanSpatie = dagStr.indexOf(" ");
                        String datum = dagStr.substring(0, indexVanSpatie);
                        String rest = dagStr.substring(indexVanSpatie + 1);
                        String dagGoed = rest + " " + datum;
                        tempList.add(dagGoed);

                        //Stand ophalen: staat in 1e tabel van HTML
                        Element tableDate = doc.select("table").get(0);
                        String dateFullText = tableDate.getElementsContainingOwnText("Stand:").text();
                        //Deel achter "Stand:" pakken
                        String FullTextSplit[] = dateFullText.split("Stand:");
                        tempList.add(FullTextSplit[1]);
                        return tempList;

                    }
                } }
        }
        catch(java.io.IOException e) {
            //Error toevoegen aan tempList, dat wordt weergegeven in messagebox
            tempList.clear();
            tempList.add("verbindFout");
            return tempList;
        }
        //Zover hoort de method NOOIT te komen
        tempList.add("andereFout");
        return tempList;
    }
}

