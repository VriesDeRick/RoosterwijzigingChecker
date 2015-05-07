package com.android.rickapps.roosterwijzigingchecker;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.codepond.wizardroid.WizardStep;

public class TutorialStap2 extends WizardStep {
    public TutorialStap2(){

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tutorial_stap2, container, false);
        return v;

    }

    @Override
    public void onExit(int exitCode) {
        switch (exitCode) {
            case WizardStep.EXIT_NEXT:
                //bindDataFields();
                break;
            case WizardStep.EXIT_PREVIOUS:
                //Do nothing...
                break;
        }
    }
}
