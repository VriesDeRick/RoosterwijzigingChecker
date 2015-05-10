package com.rickendirk.rsgwijzigingen;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.rickendirk.rsgwijzigingen.R;

public class SettingsActivity extends AppCompatActivity{
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
        else return super.onOptionsItemSelected(item);
    }

}
