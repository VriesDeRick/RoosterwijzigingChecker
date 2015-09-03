package com.rickendirk.rsgwijzigingen;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.afollestad.materialdialogs.MaterialDialog;

public class ClustersActivity extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clusters);
        setSupportActionBar((Toolbar) findViewById(R.id.clustersTB));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
        new MaterialDialog.Builder(this)
                .title("Hulp voor instellingen")
                .customView(R.layout.help_dialog, true)
                .positiveText("OK")
                .show();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }
}
