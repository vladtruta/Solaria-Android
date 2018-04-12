package com.example.vtruta.solaria.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vtruta.solaria.R;
import com.example.vtruta.solaria.activities.MainActivity;
import com.example.vtruta.solaria.database.SystemDataRepo;

import java.text.DecimalFormat;

public class ThresholdFragment extends Fragment {

    private static final String TAG = "ThresholdFragment";

    private SystemDataRepo.OnDatabaseUpdateListener databaseUpdateListener;

    private static final int AIR_TEMP_MIN = -5;
    private static final int AIR_TEMP_MAX = 40;
    private static final int WATER_TEMP_MIN = -5;
    private static final int WATER_TEMP_MAX = 40;
    private static final int WATER_LEVEL_MIN = 0;
    private static final int WATER_LEVEL_MAX = 50;
    private static final int HUMIDITY_MIN = 0;
    private static final int HUMIDITY_MAX = 95;
    private static final int PH_MIN = 1;
    private static final int PH_MAX = 14;

    private SeekBar mAirTempSB;
    private SeekBar mWaterTempSB;
    private SeekBar mWaterLevelSB;
    private SeekBar mHumiditySB;
    private SeekBar mPhSB;
    private TextView mAirTempValueTV;
    private TextView mWaterTempValueTV;
    private TextView mWaterLevelValueTV;
    private TextView mHumidityValueTV;
    private TextView mPhValueTV;

    private DecimalFormat decimalFormat;
    private SystemDataRepo systemDataRepo;
    private MainActivity mainActivity;

    public ThresholdFragment() {
    }

    public static ThresholdFragment newInstance() {
        return new ThresholdFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_threshold, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadFields(view);
        setListeners();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (mainActivity != null)
            mainActivity.getMenuInflater().inflate(R.menu.menu_threshold, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_save_threshold:
                updateAllThresholds();
                Toast.makeText(getActivity(), "Success: Limits saved successfully!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_revert_threshold:
                databaseUpdateListener.onDatabaseUpdate();
                Toast.makeText(getActivity(), "Revert: Reverted to original limits.", Toast.LENGTH_SHORT).show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadFields(View view) {
        mAirTempSB = view.findViewById(R.id.air_temp_sb);
        mAirTempSB.setMax(AIR_TEMP_MAX - AIR_TEMP_MIN);
        mWaterTempSB = view.findViewById(R.id.water_temp_sb);
        mWaterTempSB.setMax(WATER_TEMP_MAX - WATER_TEMP_MIN);
        mWaterLevelSB = view.findViewById(R.id.water_level_sb);
        mWaterLevelSB.setMax(WATER_LEVEL_MAX - WATER_LEVEL_MIN);
        mHumiditySB = view.findViewById(R.id.humidity_sb);
        mHumiditySB.setMax(HUMIDITY_MAX - HUMIDITY_MIN);
        mPhSB = view.findViewById(R.id.ph_sb);
        mPhSB.setMax(PH_MAX - PH_MIN);
        mAirTempValueTV = view.findViewById(R.id.threshold_air_temp);
        mWaterTempValueTV = view.findViewById(R.id.threshold_water_temp);
        mWaterLevelValueTV = view.findViewById(R.id.threshold_water_level);
        mHumidityValueTV = view.findViewById(R.id.threshold_humidity);
        mPhValueTV = view.findViewById(R.id.threshold_ph);
        decimalFormat = new DecimalFormat("#");
        mainActivity = (MainActivity) getActivity();
        systemDataRepo = SystemDataRepo.getInstance();
        if (systemDataRepo.getAllSystemNames().isEmpty())
            disableAll();
    }

    private void setListeners() {
        SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                switch (seekBar.getId()) {
                    case R.id.air_temp_sb:
                        mAirTempValueTV.setText(String.valueOf(progress + AIR_TEMP_MIN) + "°C");
                        break;
                    case R.id.water_temp_sb:
                        mWaterTempValueTV.setText(String.valueOf(progress + WATER_TEMP_MIN) + "°C");
                        break;
                    case R.id.water_level_sb:
                        mWaterLevelValueTV.setText(String.valueOf(progress + WATER_LEVEL_MIN) + "%");
                        break;
                    case R.id.humidity_sb:
                        mHumidityValueTV.setText(String.valueOf(progress + HUMIDITY_MIN) + "%");
                        break;
                    case R.id.ph_sb:
                        mPhValueTV.setText(String.valueOf(progress + PH_MIN));
                        break;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };
        mAirTempSB.setOnSeekBarChangeListener(seekBarChangeListener);
        mWaterTempSB.setOnSeekBarChangeListener(seekBarChangeListener);
        mWaterLevelSB.setOnSeekBarChangeListener(seekBarChangeListener);
        mHumiditySB.setOnSeekBarChangeListener(seekBarChangeListener);
        mPhSB.setOnSeekBarChangeListener(seekBarChangeListener);

        databaseUpdateListener = new SystemDataRepo.OnDatabaseUpdateListener() {
            @Override
            public void onDatabaseUpdate() {
                try {
                    Double airTempThreshold = systemDataRepo.getAirTempThreshold();
                    mAirTempSB.setEnabled(true);
                    mAirTempValueTV.setText(String.valueOf(decimalFormat.format(airTempThreshold)) + "°C");
                    mAirTempSB.setProgress(airTempThreshold.intValue() - AIR_TEMP_MIN);
                } catch (Exception exc) {
                    mAirTempSB.setEnabled(false);
                    mAirTempValueTV.setText("...");
                    mAirTempSB.setProgress(0);
                }
                try {
                    Double waterTempThreshold = systemDataRepo.getWaterTempThreshold();
                    mWaterTempSB.setEnabled(true);
                    mWaterTempValueTV.setText(String.valueOf(decimalFormat.format(waterTempThreshold)) + "°C");
                    mWaterTempSB.setProgress(waterTempThreshold.intValue() - WATER_TEMP_MIN);
                } catch (Exception exc) {
                    mWaterTempSB.setEnabled(false);
                    mWaterTempValueTV.setText("...");
                    mWaterTempSB.setProgress(0);
                }
                try {
                    Double waterLevelThreshold = systemDataRepo.getWaterLevelThreshold();
                    mWaterLevelSB.setEnabled(true);
                    mWaterLevelValueTV.setText(String.valueOf(decimalFormat.format(waterLevelThreshold)) + "%");
                    mWaterLevelSB.setProgress(waterLevelThreshold.intValue() - WATER_LEVEL_MIN);
                } catch (Exception exc) {
                    mWaterLevelSB.setEnabled(false);
                    mWaterLevelValueTV.setText("...");
                    mWaterLevelSB.setProgress(0);
                }
                try {
                    Double pHThreshold = systemDataRepo.getpHThreshold();
                    mPhSB.setEnabled(true);
                    mPhValueTV.setText(String.valueOf(decimalFormat.format(pHThreshold)));
                    mPhSB.setProgress(pHThreshold.intValue() - PH_MIN);
                } catch (Exception exc) {
                    mPhSB.setEnabled(true);
                    mPhValueTV.setText("...");
                    mPhSB.setProgress(0);
                }
                try {
                    Double humidityThreshold = systemDataRepo.getHumidityThreshold();
                    mHumiditySB.setEnabled(true);
                    mHumidityValueTV.setText(String.valueOf(decimalFormat.format(humidityThreshold)) + "%");
                    mHumiditySB.setProgress(humidityThreshold.intValue() - HUMIDITY_MIN);
                } catch (Exception exc) {
                    mHumiditySB.setEnabled(false);
                    mHumidityValueTV.setText("...");
                    mHumiditySB.setProgress(0);
                }
            }
        };
        systemDataRepo.addOnDatabaseUpdateListener(databaseUpdateListener);
    }

    private void updateAllThresholds() {
        systemDataRepo.removeOnDatabaseUpdateListener(databaseUpdateListener);
        systemDataRepo.updateAirTempThreshold((double) mAirTempSB.getProgress() + AIR_TEMP_MIN);
        systemDataRepo.updateWaterTempThreshold((double) mWaterTempSB.getProgress() + WATER_TEMP_MIN);
        systemDataRepo.updateWaterLevelThreshold((double) mWaterLevelSB.getProgress() + WATER_LEVEL_MIN);
        systemDataRepo.updateHumidityThreshold((double) mHumiditySB.getProgress() + HUMIDITY_MIN);
        systemDataRepo.updatepHThreshold((double) mPhSB.getProgress() + PH_MIN);
        systemDataRepo.addOnDatabaseUpdateListener(databaseUpdateListener);
    }

    private void disableAll() {
        mAirTempSB.setProgress(0);
        mWaterTempSB.setProgress(0);
        mWaterLevelSB.setProgress(0);
        mHumiditySB.setProgress(0);
        mPhSB.setProgress(0);
        mAirTempSB.setEnabled(false);
        mWaterTempSB.setEnabled(false);
        mWaterLevelSB.setEnabled(false);
        mHumiditySB.setEnabled(false);
        mPhSB.setEnabled(false);
        mAirTempValueTV.setText("...");
        mWaterTempValueTV.setText("...");
        mWaterLevelValueTV.setText("...");
        mHumidityValueTV.setText("...");
        mPhValueTV.setText("...");
    }
}
