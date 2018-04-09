package com.example.vtruta.solaria;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;


public class StatusFragment extends Fragment implements ValueEventListener {

    private static final String TAG = "StatusFragment";

    private TextView mAirTempTV;
    private TextView mWaterTempTV;
    private TextView mWaterLevel;
    private TextView mLightLevelTV;
    private TextView mHumidityTV;
    private TextView mPhTV;

    private DatabaseReference mDatabase;
    private DecimalFormat decFormat;
    private Query mQuery;

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
        mWaterLevel = view.findViewById(R.id.water_level_tv);
        mLightLevelTV = view.findViewById(R.id.light_level_tv);
        mHumidityTV = view.findViewById(R.id.humidity_tv);
        mPhTV = view.findViewById(R.id.ph_tv);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mQuery = mDatabase.child("systems").orderByChild("user")
                .equalTo(FirebaseAuth.getInstance().getUid());
        mQuery.addValueEventListener(this);
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        DataSnapshot sensorData = null;
        for (DataSnapshot issue : dataSnapshot.getChildren()) {
            sensorData = issue.child("sensors");
        }
        try {
            Double airTemp = sensorData.child("air_temp").getValue(Double.class);
            mAirTempTV.setText(String.valueOf(decFormat.format(airTemp)) + "°C");
        } catch (Exception e) {
            mAirTempTV.setText("...");
        }
        try {
            Double waterTemp = sensorData.child("water_temp").getValue(Double.class);
            mWaterTempTV.setText(String.valueOf(decFormat.format(waterTemp)) + "°C");
        } catch (Exception e){
            mWaterTempTV.setText("...");
        }
        try {
            Double lightLevel = sensorData.child("light_level").getValue(Double.class);
            String lightStatus = lightLevel == 1 ? "High" : "Low";
            mLightLevelTV.setText(lightStatus);
        } catch(Exception e) {
            mLightLevelTV.setText("...");
        }
        try {
            Double waterLevel = sensorData.child("water_level").getValue(Double.class);
            mWaterLevel.setText(String.valueOf(decFormat.format(waterLevel)) + "%");
        } catch (Exception e) {
            mWaterLevel.setText("...");
        }
        try {
            Double pH = sensorData.child("ph").getValue(Double.class);
            mPhTV.setText(String.valueOf(decFormat.format(pH)));
        } catch (Exception e) {
            mPhTV.setText("...");
        }
        try {
            Double humidity = sensorData.child("humidity").getValue(Double.class);
            mHumidityTV.setText(String.valueOf(decFormat.format(humidity)) + "%");
        } catch (Exception e) {
            mHumidityTV.setText("...");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mQuery.removeEventListener(this);
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        // Failed to read value
        Log.w(TAG, "Failed to read values.", databaseError.toException());
        Toast.makeText(getActivity(), "Connection error", Toast.LENGTH_SHORT).show();
    }
}
