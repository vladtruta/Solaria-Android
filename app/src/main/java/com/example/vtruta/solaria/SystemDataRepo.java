package com.example.vtruta.solaria;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SystemDataRepo implements ValueEventListener {
    private static final String TAG = "SystemDataRepo";

    public interface OnDatabaseUpdateListener {
        void onDatabaseUpdate();
    }

    public interface OnPairSystemResultListener {
        void OnReceivePairSystemResult(int resultCode);
    }

    private static SystemDataRepo instance;
    private List<OnDatabaseUpdateListener> mListenerList;
    private List<SystemData> mSystemList;
    private DatabaseReference mDatabase;
    private Query mQuery;
    private OnPairSystemResultListener mPairListener;

    private SystemDataRepo() {
    }

    public static SystemDataRepo getInstance()
    {
        if (instance == null) {
            instance = new SystemDataRepo();
        }
        return instance;
    }

    public void initRepo() {
        mSystemList = new ArrayList<>();
        mListenerList = new ArrayList<>();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mQuery = mDatabase.child("systems").orderByChild("user")
                .equalTo(FirebaseAuth.getInstance().getUid());
        removeAllListeners();
        mQuery.addValueEventListener(this);
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        int index = 0;
        boolean addIt;
        for (DataSnapshot issue : dataSnapshot.getChildren()) {
            SystemData currentSystem;
            if (getSystemAt(index) == null) {
                currentSystem = new SystemData();
                addIt = true;
            }
            else {
                currentSystem = mSystemList.get(index);
                addIt = false;
            }
            addOrUpdateSystemList(issue, addIt, currentSystem);
            index++;
        }
        for (OnDatabaseUpdateListener list : mListenerList) {
            list.onDatabaseUpdate();
        }
    }

    private void addOrUpdateSystemList(DataSnapshot issue, boolean addIt, SystemData currentSystem)
    {
        // Name, Access Code & Root Name
        currentSystem.setName(issue.child("name").getValue(String.class));
        currentSystem.setAccessCode(issue.child("access_code").getValue(String.class));
        currentSystem.setRootName(issue.getKey());

        // Sensors
        DataSnapshot sensorData = issue.child("sensors");
        currentSystem.setAirTemp(sensorData.child("air_temp").getValue(Double.class));
        currentSystem.setWaterTemp(sensorData.child("water_temp").getValue(Double.class));
        Double lightLevel = sensorData.child("light_level").getValue(Double.class);
        if (lightLevel != null) {
            currentSystem.setLightStatus(lightLevel == 1 ? "High" : "Low");
        } else {
            currentSystem.setLightStatus(null);
        }
        currentSystem.setWaterLevel(sensorData.child("water_level").getValue(Double.class));
        currentSystem.setpH(sensorData.child("ph").getValue(Double.class));
        currentSystem.setHumidity(sensorData.child("humidity").getValue(Double.class));

        // Control
        DataSnapshot controlData = issue.child("control");
        currentSystem.setDropNutrients(controlData.child("drop_nutriments").getValue(Integer.class));
        currentSystem.setTurnLights(controlData.child("turn_lights").getValue(Integer.class));

        if (addIt)
            mSystemList.add(currentSystem);
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
    }

    public void querySystemWithAccessCode(String accessCode) {
        final Query findQuery = mDatabase.child("systems").orderByChild("access_code")
                .equalTo(accessCode);
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean foundChild = false;
                // Should have only one child anyway (access code is unique)
                for (DataSnapshot issue : dataSnapshot.getChildren()) {
                    foundChild = true;
                    if (issue.child("user").getValue() != null)
                        mPairListener.OnReceivePairSystemResult(1);
                    else {
                        SystemData systemData = new SystemData();
                        addOrUpdateSystemList(issue, true, systemData);
                        systemData.updateUserID(FirebaseAuth.getInstance().getUid());
                        mPairListener.OnReceivePairSystemResult(0 );
                    }
                }
                if (!foundChild)
                    mPairListener.OnReceivePairSystemResult(-1 );
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };
        findQuery.addListenerForSingleValueEvent(listener);
    }

    public SystemData getSystemAt(int index) {
        if (index < 0 || index >= mSystemList.size())
            return null;
        return mSystemList.get(index);
    }

    public void removeSystemAt(int index) {
        if (index < 0 || index >= mSystemList.size())
            return;
        mSystemList.remove(index);
    }

    public List<String> getAllSystemNames()
    {
        List<String> namesList = new ArrayList<>();
        for (SystemData system : mSystemList)
        {
            namesList.add(system.getName());
        }
        return namesList;
    }

    public List<SystemData> getSystemList() {
        return mSystemList;
    }

    public void addOnDatabaseUpdateListener(OnDatabaseUpdateListener listener) {
        this.mListenerList.add(listener);
    }

    public void removeOnDatabaseUpdateListener(OnDatabaseUpdateListener listener) {
        this.mListenerList.remove(listener);
    }

    public void setOnReceivePairSystemResultListener(OnPairSystemResultListener listener) {
        this.mPairListener = listener;
    }

    public void removeAllListeners()
    {
        mQuery.removeEventListener(this);
    }
}
