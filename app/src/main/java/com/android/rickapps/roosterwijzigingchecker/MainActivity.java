package com.android.rickapps.roosterwijzigingchecker;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.Connection;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
                ArrayList<String> wijzigingenList = new ArrayList<String>();
                try {
                    Document doc = Jsoup.connect(url).get();
                    Element table = doc.select("table").get(1);
                    Elements rows = table.select("tr");

                    //Loop genereren, voor elke row kijken of het de goede tekst bevat
                    //Beginnen bij 4e, bovenstaande is niet belangrijk
                    for (int i = 3; i < rows.size(); i++) {
                        Element row = rows.get(i);
                        Elements cols = row.select("td");


                        if (cols.get(0).text().contains(klasTextS)) {
                            String wijziging = " De wijziging is: " +
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
                        else{
                              if (i == rows.size() - 1){
                                  //Checken of wijzigingenList leeg is, zo ja 1 ding toevoegen
                                  if (wijzigingenList.size() == 0){
                                      wijzigingenList.add("Er zijn geen wijzigingen.");
                                  }
                                  return wijzigingenList;
                            }

                        }
                   }
                }
                catch(java.io.IOException e) {
                    //Error toevoegen aan wijzigingenList, dat wordt weergegeven in messagebox
                    wijzigingenList.add("Er was een verbindingsfout");
                    return wijzigingenList;
                }
                return null;
            }
            public void onPostExecute(ArrayList wijzigingenList){
                //Messageboxen met wijzigingen laten zien: Net zolang doorgaan totdat alle
                //wijzigingen zijn weergegeven
                for (int i = 0; i< wijzigingenList.size(); i++){
                AlertDialog.Builder messageBox = new AlertDialog.Builder(MainActivity.this);
                messageBox.setTitle("Resultaat");
                messageBox.setMessage(wijzigingenList.get(i).toString());
                messageBox.setCancelable(false);
                messageBox.setNeutralButton("Oké!", null);
                messageBox.show();
            }}

    }


    private void messageBoxGeenWijziging() {
            AlertDialog.Builder messageBox = new AlertDialog.Builder(this);
            messageBox.setTitle("Er zijn geen wijzigingen!");
            messageBox.setMessage("Er zijn geen wijzigingen, volgende keer beter!");
            messageBox.setCancelable(false);
            messageBox.setNeutralButton("Oké!", null);
            messageBox.show();
    }

    private void messageBoxWijziging(String wijzigingen) {
        AlertDialog.Builder messageBox = new AlertDialog.Builder(this);
        messageBox.setTitle("Er zijn wijzigingen!");
        messageBox.setMessage(wijzigingen);
        messageBox.setCancelable(false);
        messageBox.setNeutralButton("Dankje!", null);
        messageBox.show();
    }
}
