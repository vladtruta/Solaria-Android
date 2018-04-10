package com.example.vtruta.solaria;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;


public class ControlFragment extends Fragment implements SystemDataRepo.OnDatabaseUpdateListener, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "ControlFragment";

    private SwitchCompat mLightSwitch;
    private SwitchCompat mNutrientsSwitch;
    private SystemData currentSystem;
    private MainActivity mainActivity;

    public ControlFragment() { }

    public static ControlFragment newInstance() {
        ControlFragment fragment = new ControlFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_control, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mLightSwitch = view.findViewById(R.id.light_switch);
        mNutrientsSwitch = view.findViewById(R.id.nutrients_switch);
        mLightSwitch.setOnCheckedChangeListener(this);
        mNutrientsSwitch.setOnCheckedChangeListener(this);

        mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.getRepoInstance().addOnDatabaseUpdateListener(this);
            if (mainActivity.getRepoInstance().getAllSystemNames().size() == 0)
                disableAll();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mainActivity.getRepoInstance().removeOnDatabaseUpdateListener(this);
    }

    @Override
    public void onDatabaseUpdate() {
        currentSystem = mainActivity.getRepoInstance().getSystemAt(mainActivity.getCurrentSystemIndex());
        if (currentSystem == null) {
            disableAll();
            return;
        }
        Integer dropNutrients = currentSystem.getDropNutrients();
        mNutrientsSwitch.setOnCheckedChangeListener(null);
        mLightSwitch.setOnCheckedChangeListener(null);
        if (dropNutrients != null) {
            mNutrientsSwitch.setEnabled(true);
            mNutrientsSwitch.setChecked(dropNutrients == 1);
            mNutrientsSwitch.setOnCheckedChangeListener(this);
        } else { // Small chances for the code to reach these else blocks, but still good for safety
            mNutrientsSwitch.setChecked(false);
            mNutrientsSwitch.setEnabled(false);
        }
        Integer turnLights = currentSystem.getTurnLights();
        if (turnLights != null) {
            mLightSwitch.setEnabled(true);
            mLightSwitch.setChecked(turnLights == 1);
            mLightSwitch.setOnCheckedChangeListener(this);
        } else {
            mLightSwitch.setChecked(false);
            mLightSwitch.setEnabled(false);
        }
    }

    private void disableAll() {
        mNutrientsSwitch.setChecked(false);
        mNutrientsSwitch.setEnabled(false);
        mLightSwitch.setChecked(false);
        mLightSwitch.setEnabled(false);
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int checkedInteger = isChecked ? 1 : 0;
        switch (buttonView.getId())
        {
            case R.id.light_switch:
                if (currentSystem != null)
                    currentSystem.updateTurnLights(checkedInteger);
                break;
            case R.id.nutrients_switch:
                if (currentSystem != null)
                    currentSystem.updateDropNutrients(checkedInteger);
                break;
        }
    }
}
