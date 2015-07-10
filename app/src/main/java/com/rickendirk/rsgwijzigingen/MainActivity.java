package com.rickendirk.rsgwijzigingen;



import android.animation.Animator;
import android.content.Intent;

import android.content.SharedPreferences;
import android.os.Bundle;

import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.List;


public class MainActivity extends AppCompatActivity {
    ViewPager viewPager;
    int animDuration;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        setupViewPager();

        animDuration = getResources().getInteger(android.R.integer.config_longAnimTime);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        setTitle("Roosterwijzigingen");

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        final ViewPagerAdapter adapter = (ViewPagerAdapter) viewPager.getAdapter();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int itemID = viewPager.getCurrentItem();

                if (itemID == 0) {
                    //Persoonlijke pagina, dus checken
                    MainFragment mainFragment = (MainFragment) adapter.getItem(itemID);
                    mainFragment.checker();
                }
                if (itemID == 1) {
                    //Algemeen, dus pagina vernieuwen
                    WebFragment webFragment = (WebFragment) adapter.getItem(itemID);
                    webFragment.refresh();
                }

            }
        });
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                viewPager.setCurrentItem(position);
                if (position == 1) {//webview fragment
                    WebFragment webFragment = (WebFragment) adapter.getItem(position);
                    boolean isFinished = webFragment.isFinished();
                    boolean isLoading = webFragment.isLoading();
                    if (!isLoading && !isFinished) {
                        webFragment.refresh();
                    }
                    fab.setVisibility(View.GONE);
                    fadeOut(fab);
                } else{
                    //Gewone fragment, dus toolbar moet weer bovenaan gaan staan
                    expandToolbar();
                    fadeIn(fab);
                }
            }


            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        check1ekeer();
    }
    private void setupViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        FragmentManager fmg = getSupportFragmentManager();
        List<Fragment> list = fmg.getFragments();
        if (list == null){
            //Geen bestaande fragments, dus nieuwe fragments toevoegen en listeners neerzetten
            adapter.addFrag(new MainFragment(), "Persoonlijk");
            adapter.addFrag(new WebFragment(), "Algemeen");
        }
        else {
            //Al wel bestaande fragments, dus die weer aan de adapter koppelen
            adapter.addFrag(list.get(0), "Persoonlijk");
            adapter.addFrag(list.get(1), "Algemeen");
        }
        viewPager.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 0) { //MainFragment, app aflsuiten
            super.onBackPressed();
        } else{ //Naar vorig item gaan
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id){
            case R.id.action_settings:
                openSettings();
                break;
            case R.id.action_refresh:
                int fragmentID = viewPager.getCurrentItem();
                List<Fragment> list = getSupportFragmentManager().getFragments();
                if (fragmentID == 0){
                    MainFragment mainFragment = (MainFragment) list.get(0);
                    mainFragment.checker();
                } else {
                    WebFragment webFragment = (WebFragment) list.get(1);
                    webFragment.refresh();
                }
                break;
        }

        if (id == R.id.action_settings) {
            openSettings();
        }
        return super.onOptionsItemSelected(item);
    }

    private void openSettings() {
        Intent settingsIntent = new Intent(getApplicationContext(),
                SettingsActivity.class);
        startActivityForResult(settingsIntent, 1874);

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
    public void expandToolbar(){
        //  Oplossing afkomstig van http://stackoverflow.com/questions/30655939/
        //  android-programmatically-collapse-or-expand-collapsingtoolbarlayout
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appBarLayout);
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams)
                appBarLayout.getLayoutParams();
        AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) params.getBehavior();
        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.main_coordinatorlayout);
        if (behavior != null){
            behavior.setTopAndBottomOffset(0);
            behavior.onNestedPreScroll(coordinatorLayout, appBarLayout, null, 0, 1, new int[2]);
        }
    }
    //Onderstaand deels afkomstig van http://developer.android.com/training/animation/crossfade.html
    private void fadeOut(final FloatingActionButton fab){
        fab.animate()
                .alpha(0f)
                .setDuration(animDuration)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        fab.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
    }
    private void fadeIn(final FloatingActionButton fab){
        fab.animate()
                .alpha(1f)
                .setDuration(animDuration)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {
                        fab.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {

                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
    }
}
