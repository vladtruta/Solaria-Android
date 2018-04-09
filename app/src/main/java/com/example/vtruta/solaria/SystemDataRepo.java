package com.example.vtruta.solaria;

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
    public interface OnDatabaseUpdateListener {
        void onDatabaseUpdate();
    }

    private static SystemDataRepo instance;

    private List<OnDatabaseUpdateListener> mListenerList;
    private List<SystemData> mSystemList;

    private DatabaseReference mDatabase;
    private Query mQuery;

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
            index++;
        }
        for (OnDatabaseUpdateListener list : mListenerList) {
            list.onDatabaseUpdate();
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }

    public SystemData getSystemAt(int index) {
        if (index < 0 || index >= mSystemList.size())
            return null;
        return mSystemList.get(index);
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

    public void addOnDatabaseUpdateListener(OnDatabaseUpdateListener listener) {
        this.mListenerList.add(listener);
    }

    public void removeOnDatabaseUpdateListener(OnDatabaseUpdateListener listener) {
        this.mListenerList.remove(listener);
    }

    public void removeAllListeners()
    {
        mQuery.removeEventListener(this);
    }
}
