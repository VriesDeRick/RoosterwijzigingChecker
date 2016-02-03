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


import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

public class ClustersActivity extends AppCompatActivity{
    public static final String SHOWCASE_ID = "HELPSHOWCASE";
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

    @Override
    protected void onResume() {
        //I know it's a hack, but can't get it to work
        Handler myHandler = new Handler();
        myHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showcaseViews();
            }
        }, 500);
        super.onResume();
    }

    private void showcaseViews() {
        View button = findViewById(R.id.action_help);
        new MaterialShowcaseView.Builder(this)
                .setTarget(button)
                .setDismissText("OKE")
                .setContentText("Hier vind je hulp waar je je clusters kan vinden")
                .setMaskColour(getResources().getColor(R.color.statusBarDarkerOpacity))
                .setDelay(500)
                .singleUse(SHOWCASE_ID)
                .show();
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
