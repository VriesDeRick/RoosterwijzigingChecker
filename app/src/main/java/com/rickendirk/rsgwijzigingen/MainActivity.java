package com.rickendirk.rsgwijzigingen;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.app.ProgressDialog;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.rickendirk.rsgwijzigingen.R;

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
    String geenKlas = null;
    String verbindfoutStr = null;
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
        geenKlas = getString(R.string.geenKlas);
        verbindfoutStr = getString(R.string.internet_error);
        geenWijziging = getString(R.string.geenWijzigingen);

        progressDialog = new ProgressDialog(MainActivity.this);

        //Checkt of 1e keer starten is voor wizard
        check1ekeer();


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
            checker(findViewById(R.id.button));
        }


        return super.onOptionsItemSelected(item);
    }

    //Onderstaand puur tijdelijk voor testdoeleinden
    private void openSettings() {
        Intent settingsIntent = new Intent(getApplicationContext(),
                SettingsActivity.class);
        startActivityForResult(settingsIntent, 1874);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1874){
            button_bold();
        }
    }

    private void button_bold() {
        Button button = (Button) findViewById(R.id.button);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.getBoolean("pref_but_bold", false)){
            button.setTypeface(null, Typeface.BOLD);
        } else button.setTypeface(null, Typeface.NORMAL);


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
        new MaterialDialog.Builder(this)
                .title("Geen klas ingevoerd")
                .content("Er is geen klas ingevoerd in het instellingenscherm")
                .positiveText("Stel een klas in")
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        openSettings();
                    }
                })
                .show();
    }

    public void verbindfoutAlert(){
        new MaterialDialog.Builder(this)
                .title("Verbindingsfout")
                .content("Er was een verbindingsfout. Controleer je internetverbinding of probeer het later opnieuw.")
                .positiveText("OK")
                .show();
    }
    public void geenClusterAlert(){
        new MaterialDialog.Builder(this)
                .title("Geen clusters")
                .content("Clusterfiltering is ingeschakeld, maar er zijn geen clusters ingevuld. " +
                        "Vul clusters in of schakel clusterfiltering uit.")
                .positiveText("Ga naar het instellingenscherm")
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        openSettings();
                    }
                })
                .show();
    }
    public void sPsaver(ArrayList wijzigingenList, String standZin){
        SharedPreferences.Editor spEditor = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext()).edit();
        Set<String> wijzigingenSet = new HashSet<>();
            wijzigingenSet.addAll(wijzigingenList);
        spEditor.putStringSet("last_wijzigingenList", wijzigingenSet);
        spEditor.putString("stand", standZin);
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
                tempList.add(geenKlas);
                return tempList;
            }
            //String opsplitsen in 2 delen, om naar hoofdletters te converteren
            char charcijfer = klasTextS.charAt(0);
            String klascijfer = String.valueOf(charcijfer);
            char charafdeling = klasTextS.charAt(1);
            String klasafdelingBig = String.valueOf(charafdeling).toUpperCase();
            //Sommige klassen hebben 2 delen, andere 3, andere 4
            String klasCorrect = "";
            //Onderstaand bij 3-delige klas, laatste deel moet kleine letter zijn.
            if(klasTextS.length() == 3){
                char klasabc = klasTextS.charAt(2);
                String klasabcSmall = String.valueOf(klasabc).toLowerCase();
                klasCorrect = klascijfer + klasafdelingBig + klasabcSmall;
            }
            if (klasTextS.length() == 4){
                char klasafdeling2 = klasTextS.charAt(2);
                String klasafdeling2Big = String.valueOf(klasafdeling2).toUpperCase();
                char klasabc = klasTextS.charAt(3);
                String klasabcSmall = String.valueOf(klasabc).toLowerCase();

                klasCorrect = klascijfer + klasafdelingBig + klasafdeling2Big + klasabcSmall;
            }
            //Onderstaand bij 2-delige klas
            if (klasTextS.length() == 2){
                klasCorrect = klascijfer + klasafdelingBig;
            }

            //Try en catch in het geval dat de internetverbinding mist
            try {
                String url = "http://www.rsgtrompmeesters.nl/roosters/roosterwijzigingen/Lijsterbesstraat/subst_001.htm";
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
                                if (!Jsoup.parse(cols.get(9).toString()).text().equals("\u00a0")){
                                    vervangingsTekst = "(" + Jsoup.parse(cols.get(9).toString()).text() + ")";
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
                    tempList.add(verbindfoutStr);
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

            boolean isVerbindingsfout = false;
            boolean geenKlasBool = false;
            int listLaatste = wijzigingenList.size() - 1;
            String list0 = wijzigingenList.get(listLaatste).toString();
            if (list0.equals(geenKlas)){
                geenKlasAlert();
                geenKlasBool = true;
            }
            if (list0.equals(verbindfoutStr)){
                verbindfoutAlert();
                isVerbindingsfout = true;
            }
            if (!geenKlasBool && !isVerbindingsfout){
                //Er is dus geen verbindfout en klasfout, list0 bevat stand
                String standZin = "Stand van" + list0;
                wijzigingenList.remove(listLaatste);
                sPsaver(wijzigingenList, standZin);

                ListView listView = (ListView) findViewById(R.id.wijzigingenList);
                listView.invalidateViews();

                TextView textStandView = (TextView) findViewById(R.id.textStand);
                textStandView.setText(standZin);
                //Mag toast met vernieuwd niet bij verbindingsfout etc
                vernieuwdToast();
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
                tempList.add(getString(R.string.geenClusters));
                return tempList;
            }
            //Checken of klas niet leeg is
            if (klasTextS.equals("")){
                tempList.add("Voer een klas in in het instellingenscherm.");
                return tempList;
            }
            //String opsplitsen in 2 delen, om naar hoofdletters te converteren
            char charcijfer = klasTextS.charAt(0);
            String klascijfer = String.valueOf(charcijfer);
            char charafdeling = klasTextS.charAt(1);
            String klasafdelingBig = String.valueOf(charafdeling).toUpperCase();
            //Sommige klassen hebben 2 delen, andere 3, andere 4
            String klasCorrect = null;
            //Onderstaand bij 3-delige klas, laatste deel moet kleine letter zijn.
            if(klasTextS.length() == 3){
                char klasabc = klasTextS.charAt(2);
                String klasabcSmall = String.valueOf(klasabc).toLowerCase();
                klasCorrect = klascijfer + klasafdelingBig + klasabcSmall;
            }
            if (klasTextS.length() == 4){
                char klasafdeling2 = klasTextS.charAt(2);
                String klasafdeling2Big = String.valueOf(klasafdeling2).toUpperCase();
                char klasabc = klasTextS.charAt(3);
                String klasabcSmall = String.valueOf(klasabc).toLowerCase();

                klasCorrect = klascijfer + klasafdelingBig + klasafdeling2Big + klasabcSmall;
            }
            //Onderstaand bij 2-delige klas
            if (klasTextS.length() == 2){
                klasCorrect = klascijfer + klasafdelingBig;
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
                            if (!Jsoup.parse(cols.get(9).toString()).text().equals("\u00a0")){
                                vervangingsTekst = "(" + Jsoup.parse(cols.get(9).toString()).text() + ")";
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
                tempList.add(verbindfoutStr);
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

            boolean isVerbindingsfout = false;
            boolean geenKlasBool = false;
            boolean geenClusters = false;
            int listLaatste = wijzigingenList.size() - 1;
            String listlaatst = wijzigingenList.get(listLaatste);
            if (listlaatst.equals(geenKlas)){
                geenKlasAlert();
                geenKlasBool = true;
            }
            if (listlaatst.equals(getString(R.string.geenClusters))){
                geenClusters = true;
                geenClusterAlert();
            }
            if (listlaatst.equals(verbindfoutStr)){
                verbindfoutAlert();
                isVerbindingsfout = true;
            }
            if (!geenKlasBool && !isVerbindingsfout && !geenClusters){
                //Er is dus geen verbindfout en klasfout/clusterfout, list0 bevat dus stand
                String standZin = "Stand van" + listlaatst;
                wijzigingenList.remove(listLaatste);
                sPsaver(wijzigingenList, standZin);

                ListView listView = (ListView) findViewById(R.id.wijzigingenList);
                listView.invalidateViews();

                TextView textStandView = (TextView) findViewById(R.id.textStand);
                textStandView.setText(standZin);
                //Mag toast met vernieuwd niet bij verbindingsfout etc
                vernieuwdToast();
            }


        }

    }

}
