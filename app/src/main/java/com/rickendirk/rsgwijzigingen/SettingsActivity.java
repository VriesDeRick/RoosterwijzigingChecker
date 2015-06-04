package com.rickendirk.rsgwijzigingen;


import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

public class SettingsActivity extends AppCompatActivity{
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //Fragment als content zetten op activity
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //Up-button (R.id.home) sluit activity af
        if (id == android.R.id.home){
            finish();
            return true;
        }
        if (id == R.id.action_help){
            helpDialog();
        }
        return super.onOptionsItemSelected(item);
    }
    public void helpDialog(){
        new AlertDialog.Builder(this)
                .setTitle("Hulp voor instellingen")
                //.setMessage(R.string.settingsHelp)
                .setView(R.layout.help_dialog)
                .setPositiveButton("OK", null)
                .show();
    }

}
