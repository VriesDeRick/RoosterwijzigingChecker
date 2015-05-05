package com.android.rickapps.roosterwijzigingchecker;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import org.codepond.wizardroid.WizardStep;
import org.codepond.wizardroid.persistence.ContextVariable;

/**
 * Created by Rick on 5-5-2015.
 */
public class TutorialStap1 extends WizardStep{
    /**
     * Tell WizarDroid that these are context variables.
     * These values will be automatically bound to any field annotated with {@link ContextVariable}.
     * NOTE: Context Variable names are unique and therefore must
     * have the same name wherever you wish to use them.
     */
    private Boolean clusters;
    @ContextVariable
    private String klas;


    EditText klasET;
    CheckBox clustersCB;

    //You must have an empty constructor for every step
    public TutorialStap1() {
    }

    //Set your layout here
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tutorial_stap1, container, false);
        //Get reference to the textboxes
        EditText klasET = (EditText) v.findViewById(R.id.editTextKlas);
        CheckBox clustersCB = (CheckBox) v.findViewById(R.id.checkBoxClusters);

        //and set default values by using Context Variables
        //klasET.setText(firstname);
        //lastnameEt.setText(lastname);

        return v;
    }

    /**
     * Called whenever the wizard proceeds to the next step or goes back to the previous step
     */

    @Override
    public void onExit(int exitCode) {
        switch (exitCode) {
            case WizardStep.EXIT_NEXT:
                //bindDataFields();
                saveDataFields();
                break;
            case WizardStep.EXIT_PREVIOUS:
                //Do nothing...

                break;
        }
    }

    public void saveDataFields(){
        View v = getView();
        EditText klasET = (EditText) v.findViewById(R.id.editTextKlas);
        CheckBox clustersCB = (CheckBox) v.findViewById(R.id.checkBoxClusters);
        String klas = klasET.getText().toString();
        Boolean clusters = clustersCB.isChecked();
        SharedPreferences.Editor spEditor = PreferenceManager
                .getDefaultSharedPreferences(getActivity()).edit();
        spEditor.putString("pref_klas", klas);
        spEditor.putBoolean("pref_cluster_enabled", clusters);
        spEditor.commit();
    }

    //Onderstaande code (nog) niet nodig, laten staan voor geval dat
/*
private void bindDataFields() {
//Do some work
//...
//The values of these fields will be automatically stored in the wizard context
//and will be populated in the next steps only if the same field names are used.
firstname = firstnameEt.getText().toString();
lastname = lastnameEt.getText().toString();
}
*/
}
