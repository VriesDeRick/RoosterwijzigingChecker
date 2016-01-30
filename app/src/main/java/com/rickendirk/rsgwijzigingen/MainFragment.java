/*
 *    This file is part of RSG-Wijzigingen.
 *
 *     RSG-Wijzigingen is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     RSG-Wijzigingen is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with RSG-Wijzigingen.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.rickendirk.rsgwijzigingen;

import android.support.v4.app.Fragment;
import android.app.backup.BackupManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;

public class MainFragment extends Fragment {
    public ArrayList<String> wijzigingenList = new ArrayList<>();
    MaterialDialog progressDialog;
    private ZoekReceiver receiver;
    View mainView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.fragment_personal, container, false);
        ListView listView = (ListView) mainView.findViewById(R.id.wijzigingenList);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                getActivity(),
                android.R.layout.simple_list_item_1,
                wijzigingenList);
        listView.setAdapter(arrayAdapter);

        //Stand updaten naar laatste stand
        TextView standView = (TextView) mainView.findViewById(R.id.textStand);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String standZin = sp.getString("stand", null);
        if (standZin != null) {
            standView.setText(standZin);
        }
        String dagEnDatum = sp.getString("dagEnDatum", "geenWaarde");
        dagEnDatumUpdater(dagEnDatum);
        setupMessage(null);

        setRetainInstance(true);

        return mainView;
    }

    private void setupListview(ListView listView) {
        Wijzigingen wijzigingen = new Wijzigingen(true, getActivity()); //Auomatisch uit SP
        wijzigingenList.clear();
        if (wijzigingen.getSize() != 0){
            wijzigingenList.addAll(wijzigingen.getWijzigingen());
        } else { //Geen roosterwijzigingen
            wijzigingenList.add("Er zijn geen roosterwijzigingen");
        }
        listView.invalidateViews();
    }

    private void openSettings() {
        Intent settingsIntent = new Intent(getActivity(),
                SettingsActivity.class);
        startActivityForResult(settingsIntent, 1874);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1874) {
            //Nieuwe backup van instellingen doen
            BackupManager bm = new BackupManager(getActivity());
            bm.dataChanged();
        }
    }

    public void checker() {
        progressDialog = new MaterialDialog.Builder(getActivity())
                .title("Aan het laden")
                .content("De roosterwijzigingentabel wordt geladen en doorzocht")
                .progress(true, 0)
                .show();
        Intent zoekIntent = new Intent(getActivity(), ZoekService.class);
        zoekIntent.putExtra("isAchtergrond", false);
        getActivity().startService(zoekIntent);
    }


    public void geenKlasAlert() {
        new AlertDialogWrapper.Builder(getActivity())
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

    public void verbindfoutAlert() {
        new AlertDialogWrapper.Builder(getActivity())
                .setTitle("Verbindingsfout")
                .setMessage("Er was een verbindingsfout. Controleer je internetverbinding of probeer het later opnieuw.")
                .setPositiveButton("OK", null)
                .show();
    }

    public void geenClusterAlert() {
        new AlertDialogWrapper.Builder(getActivity())
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

    public void vernieuwdToast() {
        Toast.makeText(getActivity(), "Vernieuwd", Toast.LENGTH_LONG).show();
    }

    public void EersteTekenKlasLetter() {
        new AlertDialogWrapper.Builder(getActivity())
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

    public void klasCorrector() {
        final String klasFout = PreferenceManager.getDefaultSharedPreferences(getActivity())
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
                .getDefaultSharedPreferences(getActivity()).edit();
        spEditor.putString("pref_klas", klasGoed);
        spEditor.commit();
        Snackbar
                .make(getActivity().findViewById(R.id.main_coordinatorlayout), "Gecorrigeerde klas is " + klasGoed, Snackbar.LENGTH_LONG)
                .setAction("Ongedaan maken", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        spEditor.putString("pref_klas", klasFout);
                        spEditor.commit();
                    }
                }).show();
    }

    public void dagEnDatumUpdater(String dagEnDatum) {
        TextView dagDatumView = (TextView) mainView.findViewById(R.id.wijzigingenBanner);
        if (!dagEnDatum.equals("geenWaarde")) {
            dagDatumView.setText("Wijzigingen voor " + dagEnDatum);
        }

    }

    public void klasMeerDan4Tekens() {
        new AlertDialogWrapper.Builder(getActivity())
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
                "com.rickendirk.intent.action.MESSAGE_PROCESSED"; //Nodig voor intentfilter

        @Override
        public void onReceive(Context context, Intent intent) {
            Wijzigingen wijzigingen = intent.getExtras().getParcelable("wijzigingen");
            //Boolean wordt nog niet gebruikt, maar laten staan voor het geval dat
            boolean clusters_enabled = intent.getBooleanExtra("clustersAan", false);
            naZoeken(wijzigingen);
        }
    }

    public void registerReceiver() {
        IntentFilter filter = new IntentFilter(ZoekReceiver.ACTION_RESP);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new ZoekReceiver();
        getActivity().registerReceiver(receiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(receiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver();
        ListView listView = (ListView) mainView.findViewById(R.id.wijzigingenList);
        setupListview(listView);
    }

    private void naZoeken(Wijzigingen wijzigingen) {
        progressDialog.dismiss();

        if (wijzigingen.isFoutmelding()) {

            switch (wijzigingen.getFout()) {
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
                case "geenTabel":
                    geenTabelAlert();
                    break;
                case "andereFout":
                    andereFoutAlert();
                    break;
                //Onderstaande case kan alleen bij clusterzoeken
                case "geenClusters":
                    geenClusterAlert();
                    break;
            }
        } else {
            //Kopieren naar wijzigingenList zodat listView bijgewerkt kan worden, eerst resultaten vorige weghalen
            wijzigingenList.clear();
            //Moet toch iets in lijst worden weergegeven
            if (!wijzigingen.zijnWijzigingen) wijzigingen.addWijziging("Er zijn geen roosterwijzigingen");
            wijzigingenList.addAll(wijzigingen.getWijzigingen());

            String standZin = wijzigingen.getStandZin();
            String dagEnDatum = wijzigingen.getDagEnDatum();

            ListView listView = (ListView) mainView.findViewById(R.id.wijzigingenList);
            listView.invalidateViews();

            TextView textStandView = (TextView) mainView.findViewById(R.id.textStand);
            textStandView.setText(standZin);

            //Dag en datum updaten
            dagEnDatumUpdater(dagEnDatum);

            //Mag toast met vernieuwd niet bij verbindingsfout etc
            vernieuwdToast();
            setupMessage(wijzigingen);
        }

    }

    private void setupMessage(@Nullable Wijzigingen wijzigingen) {
        if (wijzigingen == null){
            wijzigingen = new Wijzigingen(true, getActivity());
        }
        boolean hasMessage = wijzigingen.hasMessage;
        if (hasMessage) {
            setMessage(wijzigingen);
        } else hideMessage();
    }

    private void setMessage(Wijzigingen wijzigingen) {
        TextView messageTV = (TextView) mainView.findViewById(R.id.messageTV);
        messageTV.setText(wijzigingen.getMessage());
        messageTV.setVisibility(View.VISIBLE);
    }

    private void hideMessage(){
        TextView messageTV = (TextView) mainView.findViewById(R.id.messageTV);
        messageTV.setVisibility(View.GONE);
    }

    private void andereFoutAlert() {
        new AlertDialogWrapper.Builder(getActivity())
                .setTitle("Er ging iets fout")
                .setMessage("Er iets fout gegaan. Meld dit aan de ontwikkelaar.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void geenTabelAlert() {
        new AlertDialogWrapper.Builder(getActivity())
                .setTitle("Geen tabel gevonden")
                .setMessage("Er was geen geschikte roostertabel gevonden. Mogelijkheden zijn dat het" +
                        " vakantie of toetsweek is. Probeer het later opnieuw.")
                .setPositiveButton("OK", null)
                .show();
    }
}
