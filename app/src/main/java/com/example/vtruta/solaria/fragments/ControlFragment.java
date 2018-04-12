package com.example.vtruta.solaria.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.example.vtruta.solaria.R;
import com.example.vtruta.solaria.database.SystemData;
import com.example.vtruta.solaria.database.SystemDataRepo;
import com.example.vtruta.solaria.activities.MainActivity;


public class ControlFragment extends Fragment {

    private static final String TAG = "ControlFragment";

    private CompoundButton.OnCheckedChangeListener checkedChangeListener;
    private SystemDataRepo.OnDatabaseUpdateListener databaseUpdateListener;

    private SwitchCompat mLightSwitch;
    private SwitchCompat mNutrientsSwitch;

    private SystemData currentSystem;
    private SystemDataRepo systemDataRepo;
    private MainActivity mainActivity;

    public ControlFragment() {
    }

    public static ControlFragment newInstance() {
        return new ControlFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_control, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadFields(view);
        setListeners();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        systemDataRepo.removeOnDatabaseUpdateListener(databaseUpdateListener);
    }

    private void loadFields(View view) {
        mLightSwitch = view.findViewById(R.id.light_switch);
        mNutrientsSwitch = view.findViewById(R.id.nutrients_switch);
        mainActivity = (MainActivity) getActivity();
        systemDataRepo = SystemDataRepo.getInstance();
        if (systemDataRepo.getAllSystemNames().isEmpty())
            disableAll();
    }

    private void setListeners() {
        databaseUpdateListener = new SystemDataRepo.OnDatabaseUpdateListener() {
            @Override
            public void onDatabaseUpdate() {
                currentSystem = systemDataRepo.getSystemAt(mainActivity.getCurrentSystemIndex());
                if (currentSystem == null) {
                    disableAll();
                    return;
                }
                mNutrientsSwitch.setOnCheckedChangeListener(null);
                try {
                    Integer dropNutrients = currentSystem.getDropNutrients();
                    mNutrientsSwitch.setEnabled(true);
                    mNutrientsSwitch.setChecked(dropNutrients == 1);
                } catch (Exception exc) {
                    mNutrientsSwitch.setChecked(false);
                    mNutrientsSwitch.setEnabled(false);
                }
                mNutrientsSwitch.setOnCheckedChangeListener(checkedChangeListener);
                mLightSwitch.setOnCheckedChangeListener(null);
                try {
                    Integer turnLights = currentSystem.getTurnLights();
                    mLightSwitch.setEnabled(true);
                    mLightSwitch.setChecked(turnLights == 1);
                } catch (Exception exc) {
                    mLightSwitch.setChecked(false);
                    mLightSwitch.setEnabled(false);
                }
                mLightSwitch.setOnCheckedChangeListener(checkedChangeListener);
            }
        };
        systemDataRepo.addOnDatabaseUpdateListener(databaseUpdateListener);

        checkedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int checkedInteger = isChecked ? 1 : 0;
                switch (buttonView.getId()) {
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
        };
        mLightSwitch.setOnCheckedChangeListener(checkedChangeListener);
        mNutrientsSwitch.setOnCheckedChangeListener(checkedChangeListener);
    }

    private void disableAll() {
        mNutrientsSwitch.setChecked(false);
        mNutrientsSwitch.setEnabled(false);
        mLightSwitch.setChecked(false);
        mLightSwitch.setEnabled(false);
    }
}
