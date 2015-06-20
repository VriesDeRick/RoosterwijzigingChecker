package com.rickendirk.rsgwijzigingen;


import android.app.backup.BackupManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.app.ProgressDialog;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;



public class MainActivity extends AppCompatActivity {
    @SuppressWarnings("unchecked")
    public ArrayList<String> wijzigingenList = new ArrayList<>();
    String geenWijziging = null;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Listview beheren waarop wijzigingen komen te staan
        ListView listView = (ListView) findViewById(R.id.wijzigingenList);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                MainActivity.this,
                android.R.layout.simple_list_item_1,
                wijzigingenList);
        listView.setAdapter(arrayAdapter);
        //listView updaten met oude wijzigingen: eerst Set ophalen van SP
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Set<String> wijzigingenSet = sp.getStringSet("last_wijzigingenList", null);
        //nullpointer voorkomen
        if (wijzigingenSet != null){
            List<String> wijzigingenList_old = new ArrayList<>(wijzigingenSet);
            //Loop om wijzigingen van ene arrayList naar andere over te zetten
            for (int i = 0; i < wijzigingenList_old.size(); i++){
                wijzigingenList.add(wijzigingenList_old.get(i));
            }
        }
        //listView updaten om eventuele wijzigingen te laten zien
        listView.invalidateViews();

        //Stand updaten naar laatste stand
        TextView standView = (TextView) findViewById(R.id.textStand);
        String standZin = sp.getString("stand", null);
        if (standZin != null){
            standView.setText(standZin);
        }


        //Titel actionBar aanpassen
        setTitle("Roosterwijzigingen");

        //Strings aanpassen uit strings.xml
        geenWijziging = getString(R.string.geenWijzigingen);

        progressDialog = new ProgressDialog(MainActivity.this);
        //Checkt of 1e keer starten is voor wizard
        check1ekeer();


        //Dag en datum bovenaan aanpassen
        String dagEnDatum = sp.getString("dagEnDatum", "geenWaarde");
        dagEnDatumUpdater(dagEnDatum);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            openSettings();
        }
        if (id == R.id.action_refresh) {
            checker(findViewById(R.id.home));
        }


        return super.onOptionsItemSelected(item);
    }

    private void openSettings() {
        Intent settingsIntent = new Intent(getApplicationContext(),
                SettingsActivity.class);
        startActivityForResult(settingsIntent, 1874);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1874){
            //Nieuwe backup van instellingen doen
            BackupManager bm = new BackupManager(this);
            bm.dataChanged();
        }
    }
    public void checker(View view){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Boolean clusters_enabled = sp.getBoolean("pref_cluster_enabled", false);
        //Alvast progressDialog starten: hoeft het niet dubbel
        progressDialog.setTitle("Aan het laden");
        progressDialog.setMessage("De roosterwijzigingentabel wordt geladen en doorzocht");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
        if (clusters_enabled){
        new CheckerClusters().execute();}
        else new CheckerKlas().execute();
        }


    public void geenKlasAlert(){
        new AlertDialog.Builder(this)
                .setTitle("Geen klas ingevoerd")
                .setMessage("Er is geen klas ingevoerd in het instellingenscherm")
                .setPositiveButton("Stel een klas in", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        openSettings();
                    }
                })
                .show();
    }

    public void verbindfoutAlert(){
        new AlertDialog.Builder(this)
                .setTitle("Verbindingsfout")
                .setMessage("Er was een verbindingsfout. Controleer je internetverbinding of probeer het later opnieuw.")
                .setPositiveButton("OK", null)
                .show();
    }
    public void geenClusterAlert(){
        new AlertDialog.Builder(this)
                .setTitle("Geen clusters")
                .setMessage(getString(R.string.geenClusterMelding))
                .setPositiveButton("Ga naar het instellingenscherm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        openSettings();
                    }
                })
                .show();
    }
    public void sPsaver(ArrayList wijzigingenList, String standZin, String dagEnDatum){
        SharedPreferences.Editor spEditor = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext()).edit();
        Set<String> wijzigingenSet = new HashSet<>();
            wijzigingenSet.addAll(wijzigingenList);
        spEditor.putStringSet("last_wijzigingenList", wijzigingenSet);
        spEditor.putString("stand", standZin);
        spEditor.putString("dagEnDatum", dagEnDatum);
        spEditor.commit();
    }
    public void vernieuwdToast(){
        Toast.makeText(getApplicationContext(), "Vernieuwd", Toast.LENGTH_LONG).show();
    }

    public void check1ekeer(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean is1eKeer = sp.getBoolean("1ekeer", true);
        if(is1eKeer){

            //Wizard starten
            Intent wizardInt = new Intent(getApplicationContext(),
                    WizardActivity.class);
            startActivityForResult(wizardInt, 1903);
            //Mag niet volgende keer weer starten
            is1eKeer = false;
            SharedPreferences.Editor spEditor = PreferenceManager
                    .getDefaultSharedPreferences(getApplicationContext()).edit();
            spEditor.putBoolean("1ekeer", is1eKeer);
            spEditor.commit();
        }
    }
    public void EersteTekenKlasLetter(){
        new AlertDialog.Builder(this)
                .setTitle("Klas bestaat niet")
                .setMessage(R.string.verkeerdeKlas)
                .setPositiveButton("Automatisch", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        klasCorrector();
                    }
                })
                .setNeutralButton("Handmatig", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        openSettings();
                    }
                })
                .show();
    }
    public void klasCorrector(){
        final String klasFout = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("pref_klas", "");
        char[] c = klasFout.toCharArray();
        //Letter opslaan
        char letter = c[0];
        //Cijfer naar 1e plaats verplaatsen
        c[0] = c[1];
        //Letter naar 2e plaats zetten
        c[1] = letter;
        String klasGoed = new String(c);
        final SharedPreferences.Editor spEditor = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext()).edit();
        spEditor.putString("pref_klas", klasGoed);
        spEditor.commit();
        Snackbar
                .make(findViewById(R.id.coordinatorlayout), "Gecorrigeerde klas is " + klasGoed  ,Snackbar.LENGTH_LONG)
                .setAction("Ongedaan maken", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        spEditor.putString("pref_klas", klasFout);
                        spEditor.commit();
                    }
                }).show();
    }
    public void dagEnDatumUpdater(String dagEnDatum){
        TextView dagDatumView = (TextView) findViewById(R.id.wijzigingenBanner);
        if (!dagEnDatum.equals("geenWaarde")){
            dagDatumView.setText("Wijzigingen voor " + dagEnDatum);
        }

    }
    public void klasMeerDan4Tekens(){
        new AlertDialog.Builder(this)
                .setTitle("Klas meer dan 4 tekens")
                .setMessage(R.string.klasMeerDan4Tekens)
                .setPositiveButton("Naar het instellingenscherm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        openSettings();
                    }
                })
                .show();
    }
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
    }


    //Zoekalgoritme voor klassen
    class CheckerKlas extends AsyncTask<Void, Void, ArrayList> {

        @Override
        public ArrayList doInBackground(Void... params) {
            //wijzigingenList alleen bijwerken vanaf UI-Thread. tempList wordt in onPostExecute
            //gekopieerd naar wijzigingenList, zodat er geen errors komen
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
                String url = getURL();
                Document doc = Jsoup.connect(url).get();
                    Element table = doc.select("table").get(1);
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
                                //Soms veregeten ze de opmerkingen, dan krijg je size+ 9 en error
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
                                      tempList.add(geenWijziging);
                                  }
                            //Dag waarvoor wijzigingen zijn ophalen
                            Element dag = doc.getElementsByAttributeValueContaining("style", "font-size:11.5pt")
                                    .first();
                            //TODO: Hier degelijke oplossing voor vinden
                            String dagStr;
                            if (dag == null){
                                dagStr = "08-04-2015 Woensdag";
                            } else dagStr = dag.text().toLowerCase();
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
            //AS wilt graag een return statment: here you go
            return tempList;
        }
        public void onPostExecute(ArrayList tempList){
            progressDialog.dismiss();
            //Kopieren naar wijzigingenList zodat listView bijgewerkt kan worden, eerst resultaten vorige weghalen
            wijzigingenList.clear();
            wijzigingenList.addAll(tempList);

            int listLaatste = wijzigingenList.size() - 1;
            String listlaatst = wijzigingenList.get(listLaatste);
            switch (listlaatst) {
                case "geenKlas":
                    geenKlasAlert();
                    break;
                case "verbindFout":
                    verbindfoutAlert();
                    break;
                case "EersteTekenLetter":
                    EersteTekenKlasLetter();
                    break;
                case "klasMeerDan4Tekens":
                    klasMeerDan4Tekens();
                    break;
                default:
                    //Er is dus geen verbindfout en klasfout, listlaatst bevat stand
                    String standZin = "Stand van" + listlaatst;
                    //Dag met datum ophalen uit lijst
                    int dagIndex = wijzigingenList.size() - 2;
                    String dagEnDatum = wijzigingenList.get(dagIndex);

                    wijzigingenList.remove(listLaatste);
                    //Dag en datum moeten er ook uit, nu is die de laatste
                    wijzigingenList.remove(wijzigingenList.size() - 1);
                    sPsaver(wijzigingenList, standZin, dagEnDatum);

                    ListView listView = (ListView) findViewById(R.id.wijzigingenList);
                    listView.invalidateViews();

                    TextView textStandView = (TextView) findViewById(R.id.textStand);
                    textStandView.setText(standZin);

                    //Dag en datum updaten
                    dagEnDatumUpdater(dagEnDatum);

                    //Mag toast met vernieuwd niet bij verbindingsfout etc
                    vernieuwdToast();
                    break;
            }


        }


    }
    //Zoekalgoritme voor clusters
    class CheckerClusters extends AsyncTask<Void, Void, ArrayList> {

        @Override
        public ArrayList doInBackground(Void... params) {
            //wijzigingenList alleen bijwerken vanaf UI-Thread. tempList wordt in onPostExecute
            //gekopieerd naar wijzigingenList, zodat er geen errors komen
            ArrayList<String> tempList = new ArrayList<>();
            //String van klas halen uit SP
            String klasTextS = PreferenceManager.getDefaultSharedPreferences
                    (getApplicationContext()).getString("pref_klas", "");
            String url = getURL();
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
                Element table = doc.select("table").get(1);
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
                            tempList.add(geenWijziging);
                        }
                        //Dag waarvoor wijzigingen zijn ophalen
                        Element dag = doc.getElementsByAttributeValueContaining("style", "font-size:11.5pt")
                                .first();
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
            //AS wilt graag een return statment: here you go
            return tempList;
        }



        public void onPostExecute(ArrayList tempList){
            progressDialog.dismiss();
            //Kopieren naar wijzigingenList zodat listView bijgewerkt kan worden, eerst resultaten vorige weghalen
            wijzigingenList.clear();
            wijzigingenList.addAll(tempList);

            int listLaatste = wijzigingenList.size() - 1;
            String listlaatst = wijzigingenList.get(listLaatste);
            switch (listlaatst) {
                case "geenKlas":
                    geenKlasAlert();
                    break;
                case "geenClusters":
                    geenClusterAlert();
                    break;
                case "verbindFout":
                    verbindfoutAlert();
                    break;
                case "klasMeerDan4Tekens":
                    klasMeerDan4Tekens();
                    break;
                case "EersteTekenLetter":
                    EersteTekenKlasLetter();
                    break;
                default:
                    //Er is dus geen verbindfout en klasfout/clusterfout, listlaatst bevat dus stand
                    String standZin = "Stand van" + listlaatst;
                    int dagIndex = wijzigingenList.size() - 2;
                    String dagEnDatum = wijzigingenList.get(dagIndex);

                    wijzigingenList.remove(listLaatste);
                    //Dag en datum moeten er ook uit, nu is die de laatste
                    wijzigingenList.remove(wijzigingenList.size() - 1);

                    sPsaver(wijzigingenList, standZin, dagEnDatum);

                    ListView listView = (ListView) findViewById(R.id.wijzigingenList);
                    listView.invalidateViews();

                    TextView textStandView = (TextView) findViewById(R.id.textStand);
                    textStandView.setText(standZin);

                    //Dag en datum updaten
                    dagEnDatumUpdater(dagEnDatum);
                    //Mag toast met vernieuwd niet bij verbindingsfout etc
                    vernieuwdToast();
                    break;
            }


        }

    }

}
