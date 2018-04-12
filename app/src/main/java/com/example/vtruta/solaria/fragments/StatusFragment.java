package com.example.vtruta.solaria.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.vtruta.solaria.R;
import com.example.vtruta.solaria.database.SystemData;
import com.example.vtruta.solaria.database.SystemDataRepo;
import com.example.vtruta.solaria.activities.MainActivity;

import java.text.DecimalFormat;


public class StatusFragment extends Fragment {

    private static final String TAG = "StatusFragment";

    private SystemDataRepo.OnDatabaseUpdateListener databaseUpdateListener;

    private TextView mAirTempTV;
    private TextView mWaterTempTV;
    private TextView mWaterLevelTV;
    private TextView mLightLevelTV;
    private TextView mHumidityTV;
    private TextView mPhTV;

    private SystemData currentSystem;
    private DecimalFormat decimalFormat;
    private MainActivity mainActivity;
    private SystemDataRepo systemDataRepo;

    public StatusFragment() {
    }

    public static StatusFragment newInstance() {
        return new StatusFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_status, container, false);
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
        mAirTempTV = view.findViewById(R.id.air_temp_tv);
        mWaterTempTV = view.findViewById(R.id.water_temp_tv);
        mWaterLevelTV = view.findViewById(R.id.water_level_tv);
        mLightLevelTV = view.findViewById(R.id.light_level_tv);
        mHumidityTV = view.findViewById(R.id.humidity_tv);
        mPhTV = view.findViewById(R.id.ph_tv);
        mainActivity = (MainActivity) getActivity();
        decimalFormat = new DecimalFormat("#");
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
                try {
                    Double airTemp = currentSystem.getAirTemp();
                    mAirTempTV.setText(String.valueOf(decimalFormat.format(airTemp)) + "°C");
                } catch (Exception exc) {
                    mAirTempTV.setText("...");
                }
                try {
                    Double waterTemp = currentSystem.getWaterTemp();
                    mWaterTempTV.setText(String.valueOf(decimalFormat.format(waterTemp)) + "°C");
                } catch (Exception exc) {
                    mWaterTempTV.setText("...");
                }
                try {
                    String lightStatus = currentSystem.getLightStatus();
                    mLightLevelTV.setText(lightStatus);
                } catch (Exception exc) {
                    mLightLevelTV.setText("...");
                }
                try {
                    Double waterLevel = currentSystem.getWaterLevel();
                    mWaterLevelTV.setText(String.valueOf(decimalFormat.format(waterLevel)) + "%");
                } catch (Exception exc) {
                    mWaterLevelTV.setText("...");
                }
                try {
                    Double pH = currentSystem.getpH();
                    mPhTV.setText(String.valueOf(decimalFormat.format(pH)));
                } catch (Exception exc) {
                    mPhTV.setText("...");
                }
                try {
                    Double humidity = currentSystem.getHumidity();
                    mHumidityTV.setText(String.valueOf(decimalFormat.format(humidity)) + "%");
                } catch (Exception exc) {
                    mHumidityTV.setText("...");
                }
            }
        };
        systemDataRepo.addOnDatabaseUpdateListener(databaseUpdateListener);
    }

    private void disableAll() {
        mAirTempTV.setText("...");
        mWaterTempTV.setText("...");
        mLightLevelTV.setText("...");
        mWaterLevelTV.setText("...");
        mPhTV.setText("...");
        mHumidityTV.setText("...");
    }
}
