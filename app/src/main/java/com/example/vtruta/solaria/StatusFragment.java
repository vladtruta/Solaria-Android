package com.example.vtruta.solaria;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DecimalFormat;


public class StatusFragment extends Fragment implements SystemDataRepo.OnDatabaseUpdateListener{

    private static final String TAG = "StatusFragment";

    private TextView mAirTempTV;
    private TextView mWaterTempTV;
    private TextView mWaterLevelTV;
    private TextView mLightLevelTV;
    private TextView mHumidityTV;
    private TextView mPhTV;
    private SystemData currentSystem;
    private DecimalFormat decFormat;
    private MainActivity mainActivity;

    public StatusFragment() { }

    public static StatusFragment newInstance() {
        StatusFragment fragment = new StatusFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_status, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        decFormat = new DecimalFormat("#");

        mAirTempTV = view.findViewById(R.id.air_temp_tv);
        mWaterTempTV = view.findViewById(R.id.water_temp_tv);
        mWaterLevelTV = view.findViewById(R.id.water_level_tv);
        mLightLevelTV = view.findViewById(R.id.light_level_tv);
        mHumidityTV = view.findViewById(R.id.humidity_tv);
        mPhTV = view.findViewById(R.id.ph_tv);

        mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.getRepoInstance().addOnDatabaseUpdateListener(this);
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
        Double airTemp = currentSystem.getAirTemp();
        if (airTemp != null) {
            mAirTempTV.setText(String.valueOf(decFormat.format(airTemp)) + "°C");
        } else {
            mAirTempTV.setText("...");
        }
        Double waterTemp = currentSystem.getWaterTemp();
        if (waterTemp != null) {
            mWaterTempTV.setText(String.valueOf(decFormat.format(waterTemp)) + "°C");
        } else {
            mWaterTempTV.setText("...");
        }
        String lightStatus = currentSystem.getLightStatus();
        if (lightStatus != null) {
            mLightLevelTV.setText(lightStatus);
        } else {
            mLightLevelTV.setText("...");
        }
        Double waterLevel = currentSystem.getWaterLevel();
        if (waterLevel != null) {
            mWaterLevelTV.setText(String.valueOf(decFormat.format(waterLevel)) + "%");
        } else {
            mWaterLevelTV.setText("...");
        }
        Double pH = currentSystem.getpH();
        if (pH != null) {
            mPhTV.setText(String.valueOf(decFormat.format(pH)));
        } else {
            mPhTV.setText("...");
        }
        Double humidity = currentSystem.getHumidity();
        if (humidity != null) {
            mHumidityTV.setText(String.valueOf(decFormat.format(humidity)) + "%");
        } else {
            mHumidityTV.setText("...");
        }
    }
}
