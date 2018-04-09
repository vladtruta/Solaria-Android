package com.example.vtruta.solaria;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


public class ControlFragment extends Fragment implements ValueEventListener {

    private static final String TAG = "ControlFragment";

    private SwitchCompat mLightSwitch;
    private SwitchCompat mNutrientsSwitch;
    private DatabaseReference mDatabase;
    private String childString = "";
    private Query mQuery;

    public ControlFragment() {
    }

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
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mQuery = mDatabase.child("systems").orderByChild("user")
                .equalTo(FirebaseAuth.getInstance().getUid());
        mQuery.addValueEventListener(this);
        mLightSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int checkedInteger = isChecked ? 1 : 0;
                mDatabase.child("systems/" + childString + "/control/turn_lights").setValue(checkedInteger);
            }
        });
        mNutrientsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int checkedInteger = isChecked ? 1 : 0;
                mDatabase.child("systems/" + childString + "/control/drop_nutriments").setValue(checkedInteger);
            }
        });
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        DataSnapshot sensorData = null;
        for (DataSnapshot issue : dataSnapshot.getChildren()) {
            childString = issue.getKey();
            sensorData = issue.child("control");
        }
        Integer dropNutrients = null;
        if (sensorData != null) {
            dropNutrients = sensorData.child("drop_nutriments").getValue(Integer.class);
        }
        if (dropNutrients != null) {
            if (dropNutrients == 1)
                mNutrientsSwitch.setChecked(true);
            else
                mNutrientsSwitch.setChecked(false);
        }
        Integer turnLights = null;
        if (sensorData != null) {
            turnLights = sensorData.child("turn_lights").getValue(Integer.class);
        }
        if (turnLights != null) {
            if (turnLights == 1)
                mLightSwitch.setChecked(true);
            else
                mLightSwitch.setChecked(false);
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        Log.w(TAG, "Failed to read values.", databaseError.toException());
        Toast.makeText(getActivity(), "Connection error", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mQuery.removeEventListener(this);
    }
}
