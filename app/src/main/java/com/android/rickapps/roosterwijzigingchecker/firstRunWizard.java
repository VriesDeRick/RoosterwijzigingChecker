package com.android.rickapps.roosterwijzigingchecker;

import org.codepond.wizardroid.WizardFlow;
import org.codepond.wizardroid.layouts.BasicWizardLayout;

public class firstRunWizard extends BasicWizardLayout {
    @Override
    public void onWizardComplete() {
        super.onWizardComplete();
        getActivity().finish();
    }

    /**
     * Note that initially BasicWizardLayout inherits from {@link android.support.v4.app.Fragment} and therefore you must have an empty constructor
     */
    public firstRunWizard() {
        super();
    }

    //You must override this method and create a wizard flow by
    //using WizardFlow.Builder as shown in this example
    @Override
    public WizardFlow onSetup() {
        // Optionally, you can set different labels for the control buttons
        setNextButtonText("Volgende");
        setBackButtonText("Terug");
        setFinishButtonText("Voltooien");

        return new WizardFlow.Builder()
                .addStep(TutorialStap1.class)           //Add your steps in the order you want them
                //.addStep(TutorialStap2.class)           to appear and eventually call create()
                .create();                              //to create the wizard flow.
    }
}
