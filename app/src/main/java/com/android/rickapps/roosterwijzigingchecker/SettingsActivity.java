package com.android.rickapps.roosterwijzigingchecker;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

public class SettingsActivity extends ActionBarActivity{
    @SuppressWarnings("deprecation")
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
