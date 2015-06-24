package com.rickendirk.rsgwijzigingen;


import android.app.backup.BackupManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;



public class MainActivity extends AppCompatActivity {
    @SuppressWarnings("unchecked")
    public ArrayList<String> wijzigingenList = new ArrayList<>();
    ProgressDialog progressDialog;
    private ZoekReceiver receiver;

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

        progressDialog = new ProgressDialog(MainActivity.this);
        //Checkt of 1e keer starten is voor wizard
        check1ekeer();


        //Dag en datum bovenaan aanpassen
        String dagEnDatum = sp.getString("dagEnDatum", "geenWaarde");
        dagEnDatumUpdater(dagEnDatum);

        //Receiver regelen vor intentservice van zoeken
        IntentFilter filter = new IntentFilter(ZoekReceiver.ACTION_RESP);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new ZoekReceiver();
        registerReceiver(receiver, filter);
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

        progressDialog.setTitle("Aan het laden");
        progressDialog.setMessage("De roosterwijzigingentabel wordt geladen en doorzocht");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
        Intent zoekIntent = new Intent(this, ZoekService.class);
        startService(zoekIntent);
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

    public class ZoekReceiver extends BroadcastReceiver {

        public static final String ACTION_RESP =
                "com.rickendirk.intent.action.MESSAGE_PROCESSED";
        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList wijzigingen = intent.getParcelableArrayListExtra("wijzigingen");
            boolean clusters_enabled = intent.getBooleanExtra("clustersAan", false);
            naZoeken(wijzigingen);
        }
    }

    private void naZoeken(ArrayList wijzigingen) {
        progressDialog.dismiss();
        //Kopieren naar wijzigingenList zodat listView bijgewerkt kan worden, eerst resultaten vorige weghalen
        wijzigingenList.clear();
        wijzigingenList.addAll(wijzigingen);

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
            //Onderstaande case kan alleen bij clusterzoeken
            case "geenClusters":
                geenClusterAlert();
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
