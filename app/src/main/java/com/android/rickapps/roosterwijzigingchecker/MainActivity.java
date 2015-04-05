package com.android.rickapps.roosterwijzigingchecker;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;



public class MainActivity extends ActionBarActivity {
    ArrayList<String> wijzigingenList = new ArrayList<>();

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
        //Klas ophalen van SP
        EditText klasText = (EditText) findViewById(R.id.klasText);
        //EMPTY is als er geen value is opgehaald, maw bij de 1e keer gebruik
        String SPKlas = getPreferences(Context.MODE_PRIVATE).getString("KLAS", "EMPTY");
        //In geval van 1e keer is SP leeg: in dat geval textView aanpassen
        if (!SPKlas.equals("EMPTY")){
            klasText.setText(SPKlas);
        }


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
            return true;
        }
        switch (item.getItemId()) {
            case R.id.action_refresh:
                checker2(findViewById(R.id.button));
        }


        return super.onOptionsItemSelected(item);
    }

    public void checker2(View view){
        new CheckerClass().execute();
        }
    class CheckerClass extends AsyncTask<Void, Void, ArrayList> {

        @Override
        public ArrayList doInBackground(Void... params) {
            EditText klasText = (EditText) findViewById(R.id.klasText);
            String klasTextS = klasText.getText().toString();
            String url = "http://www.rsgtrompmeesters.nl/roosters/roosterwijzigingen/Lijsterbesstraat/subst_001.htm";
            //String opsplitsen in 2 delen, om naar hoofdletters te converteren
            char charcijfer = klasTextS.charAt(0);
            String klascijfer = String.valueOf(charcijfer);
            char charafdeling = klasTextS.charAt(1);
            String klasafdelingBig = String.valueOf(charafdeling).toUpperCase();
            boolean langeklas = false;
            //Sommige klassen hebben 2 delen, andere 3
            if (klasTextS.length() == 3){
                 langeklas = true;
            }
            String klasCorrect;
            //Onderstaand bij 3-delige klas, laatste deel moet kleine letter zijn.
            if(langeklas){
                char klasabc = klasTextS.charAt(2);
                String klasabcSmall = String.valueOf(klasabc).toLowerCase();
                klasCorrect = klascijfer + klasafdelingBig + klasabcSmall;
            }
            //Onderstaand bij 2-delige klas
            else {
                klasCorrect = klascijfer + klasafdelingBig;
            }
            //Try en catch in het geval dat de internetverbinding mist
            try {
                    Document doc = Jsoup.connect(url).get();
                    Element table = doc.select("table").get(1);
                    Elements rows = table.select("tr");
                    wijzigingenList.clear();

                    //Loop genereren, voor elke row kijken of het de goede tekst bevat
                    //Beginnen bij 4e, bovenstaande is niet belangrijk
                    for (int i = 2; i < rows.size(); i++) {
                        Element row = rows.get(i);
                        Elements cols = row.select("td");


                        if (cols.get(0).text().contains(klasCorrect)) {
                            String wijziging =
                                    // Voegt alle kolommen samen tot 1 string
                                    // .text() zorgt voor leesbare text
                                    // Spaties voor leesbaarheid
                                    Jsoup.parse(cols.get(1).toString()).text() + "e uur " +
                                    Jsoup.parse(cols.get(2).toString()).text() + " " +
                                    Jsoup.parse(cols.get(3).toString()).text() + " wordt " +
                                    Jsoup.parse(cols.get(4).toString()).text() + " " +
                                    Jsoup.parse(cols.get(5).toString()).text() + " in " +
                                    Jsoup.parse(cols.get(6).toString()).text() + " " +
                                    Jsoup.parse(cols.get(7).toString()).text() + " (" +
                                    Jsoup.parse(cols.get(8).toString()).text() + ")";
                            wijzigingenList.add(wijziging);

                        }
                        //Geen wijzigingen pas bij laatste rij
                        if (i == rows.size() - 1){
                                  //Checken of wijzigingenList leeg is, zo ja 1 ding toevoegen
                                  if (wijzigingenList.isEmpty()){
                                      wijzigingenList.add("Er zijn geen wijzigingen.");
                                  }
                            wijzigingenList.add(klasCorrect);
                            return wijzigingenList;
                        }


                   }
            }
            catch(java.io.IOException e) {
                    //Error toevoegen aan wijzigingenList, dat wordt weergegeven in messagebox
                    wijzigingenList.clear();
                    wijzigingenList.add("Er was een verbindingsfout");
                    return wijzigingenList;
            }
            //AS wilt graag een return statment: here you go
            return null;
        }
        public void onPostExecute(ArrayList wijzigingenList){
            //int voor klasindex opvragen, index is 1 lager dan size, want hij is
            //als laatste toegevoegd
            int klasIndex = wijzigingenList.size() -1;
            //Klas opslaan voor later gebruik
            String klas = wijzigingenList.get(klasIndex).toString();
            SharedPreferences.Editor SPEditor = getPreferences(Context.MODE_PRIVATE).edit();
            SPEditor.putString("KLAS", klas);
            SPEditor.commit();
            //Listview updaten, wel eerst klas eruit weghalen
            wijzigingenList.remove(klasIndex);
            ListView listView = (ListView) findViewById(R.id.wijzigingenList);
                listView.invalidateViews();
        }

    }
}
