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
    private SystemData mCurrentSystem;
    private DecimalFormat mDecFormat;
    private MainActivity mParentActivity;

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

        mDecFormat = new DecimalFormat("#");

        mAirTempTV = view.findViewById(R.id.air_temp_tv);
        mWaterTempTV = view.findViewById(R.id.water_temp_tv);
        mWaterLevelTV = view.findViewById(R.id.water_level_tv);
        mLightLevelTV = view.findViewById(R.id.light_level_tv);
        mHumidityTV = view.findViewById(R.id.humidity_tv);
        mPhTV = view.findViewById(R.id.ph_tv);

        mParentActivity = (MainActivity) getActivity();
        if (mParentActivity != null) {
            mParentActivity.getRepoInstance().addOnDatabaseUpdateListener(this);
            if (mParentActivity.getRepoInstance().getAllSystemNames().size() == 0)
                disableAll();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mParentActivity.getRepoInstance().removeOnDatabaseUpdateListener(this);
    }

    @Override
    public void onDatabaseUpdate() {
        mCurrentSystem = mParentActivity.getRepoInstance().getSystemAt(mParentActivity.getCurrentSystemIndex());
        if (mCurrentSystem == null) {
            disableAll();
            return;
        }
        Double airTemp = mCurrentSystem.getAirTemp();
        if (airTemp != null) {
            mAirTempTV.setText(String.valueOf(mDecFormat.format(airTemp)) + "°C");
        } else {
            mAirTempTV.setText("...");
        }
        Double waterTemp = mCurrentSystem.getWaterTemp();
        if (waterTemp != null) {
            mWaterTempTV.setText(String.valueOf(mDecFormat.format(waterTemp)) + "°C");
        } else {
            mWaterTempTV.setText("...");
        }
        String lightStatus = mCurrentSystem.getLightStatus();
        if (lightStatus != null) {
            mLightLevelTV.setText(lightStatus);
        } else {
            mLightLevelTV.setText("...");
        }
        Double waterLevel = mCurrentSystem.getWaterLevel();
        if (waterLevel != null) {
            mWaterLevelTV.setText(String.valueOf(mDecFormat.format(waterLevel)) + "%");
        } else {
            mWaterLevelTV.setText("...");
        }
        Double pH = mCurrentSystem.getpH();
        if (pH != null) {
            mPhTV.setText(String.valueOf(mDecFormat.format(pH)));
        } else {
            mPhTV.setText("...");
        }
        Double humidity = mCurrentSystem.getHumidity();
        if (humidity != null) {
            mHumidityTV.setText(String.valueOf(mDecFormat.format(humidity)) + "%");
        } else {
            mHumidityTV.setText("...");
        }
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
