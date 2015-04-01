package com.android.rickapps.roosterwijzigingchecker;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
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
//Onderstaand was een experiment, is niet gebruikt, gewoon negeren dus
    public void checker(View view) throws IOException {
        //ArrayList<String> wijzigingen = new ArrayList<>();
        String url = "http://www.rsgtrompmeesters.nl/roosters/roosterwijzigingen/Lijsterbesstraat/subst_001.htm";
        Document doc = Jsoup.connect(url).get();
        Element table = doc.select("table").get(0);
        Elements rows = table.select("tr");
        //Loop genereren, voor elke row kijken of het de goede tekst bevat
        for (int i = 1; i < rows.size(); i++) {
            Element row = rows.get(i);
            Elements cols = row.select("td");

            if (cols.get(1).text().equals("5Va")) {
                String wijzigingen = cols.get(1).toString();
                TextView text = (TextView) findViewById(R.id.textView);
                text.setText(wijzigingen);

            }//text.setText(Arrays.toString(wijzigingen));
            // ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, wijzigingen);
            //ListView listView = (ListView) findViewById(R.id.listView);
            //listView.setAdapter(adapter);
        }
    }
        public void checker2(View view){
        new CheckerClass().execute();
    }
        class CheckerClass extends AsyncTask<Void, Void, String> {

            @Override
            public String doInBackground(Void... params) {
                String url = "http://www.rsgtrompmeesters.nl/roosters/roosterwijzigingen/Lijsterbesstraat/subst_001.htm";
                try {
                    Document doc = Jsoup.connect(url).get();
                    Element table = doc.select("table").get(1);
                    Elements rows = table.select("tr");

                    //Loop genereren, voor elke row kijken of het de goede tekst bevat
                    //Beginnen bij 4e, bovenstaande is niet belangrijk
                    for (int i = 3; i < rows.size(); i++) {
                        Element row = rows.get(i);
                        Elements cols = row.select("td");
                        //TODO: Proberen cols.get(1) etc, kijken of 1e klas werkt

                        if (cols.get(0).text().contains("1Va")) {
                            String wijzigingen = " De wijziging is " + cols.get(1).toString();
                            return wijzigingen;

                        }
                        else{
                              if (i == rows.size() - 1){
                                  return "Er zijn geen wijzigingen.";
                            }

                        }
                   }
                }
                catch(java.io.IOException e) {
                    return "Er was een verbindingsfout";
                }
                return null;
            }
            public void onPostExecute(String wijzigingen){
                //Messagebox met wijzigingen laten zien
                AlertDialog.Builder messageBox = new AlertDialog.Builder(MainActivity.this);
                messageBox.setTitle("Resultaat");
                messageBox.setMessage(wijzigingen);
                messageBox.setCancelable(false);
                messageBox.setNeutralButton("Oké!", null);
                messageBox.show();
            }

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
