package com.example.vtruta.solaria.database;

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

public class SystemDataRepo {
    private static final String TAG = "SystemDataRepo";

    public interface OnDatabaseUpdateListener {
        void onDatabaseUpdate();
    }

    public interface OnReceivePairSystemResultListener {
        void OnReceivePairSystemResult(int resultCode, String systemName);
    }

    private static SystemDataRepo instance;

    private String uid;
    private int dataChangeCount = 0;

    private Double airTempThreshold;
    private Double waterTempThreshold;
    private Double waterLevelThreshold;
    private Double pHThreshold;
    private Double humidityThreshold;

    private List<OnDatabaseUpdateListener> updateListenerList;
    private OnReceivePairSystemResultListener receivePairSystemResultListener;
    private ValueEventListener querySystemListener;
    private ValueEventListener sensorDataListener;
    private ValueEventListener sensorThresholdListener;

    private List<SystemData> systemDataList;
    private DatabaseReference systemsDatabaseReference;
    private DatabaseReference userParamsDatabaseReference;
    private Query sensorDataQuery;

    private SystemDataRepo() {
    }

    public static SystemDataRepo getInstance() {
        if (instance == null) {
            instance = new SystemDataRepo();
        }
        return instance;
    }

    public void initRepo() {
        uid = FirebaseAuth.getInstance().getUid();
        systemDataList = new ArrayList<>();
        updateListenerList = new ArrayList<>();
        systemsDatabaseReference = FirebaseDatabase.getInstance().getReference().child("systems");
        sensorDataQuery = systemsDatabaseReference.orderByChild("user")
                .equalTo(uid);
        userParamsDatabaseReference = FirebaseDatabase.getInstance().getReference().child("user_params");
        removeAllListeners();
        setListeners();
    }

    private void setListeners() {
        sensorDataListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int index = 0;
                boolean addIt;
                for (DataSnapshot issue : dataSnapshot.getChildren()) {
                    SystemData currentSystem;
                    if (getSystemAt(index) == null) {
                        currentSystem = new SystemData();
                        addIt = true;
                    } else {
                        currentSystem = systemDataList.get(index);
                        addIt = false;
                    }
                    addOrUpdateSystemList(issue, addIt, currentSystem);
                    index++;
                }
                notifyListenersUpdateData();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };
        sensorDataQuery.addValueEventListener(sensorDataListener);

        sensorThresholdListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(uid)) {
                    DataSnapshot thresholdData = dataSnapshot.child(uid);
                    airTempThreshold = thresholdData.child("air_temp").getValue(Double.class);
                    waterTempThreshold = thresholdData.child("water_temp").getValue(Double.class);
                    waterLevelThreshold = thresholdData.child("water_level").getValue(Double.class);
                    humidityThreshold = thresholdData.child("humidity").getValue(Double.class);
                    pHThreshold = thresholdData.child("ph").getValue(Double.class);
                } else {
                    userParamsDatabaseReference.child(uid).child("air_temp").setValue(10);
                    userParamsDatabaseReference.child(uid).child("water_temp").setValue(15);
                    userParamsDatabaseReference.child(uid).child("water_level").setValue(20);
                    userParamsDatabaseReference.child(uid).child("ph").setValue(7);
                    userParamsDatabaseReference.child(uid).child("humidity").setValue(70);
                }
                notifyListenersUpdateData();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };
        userParamsDatabaseReference.addValueEventListener(sensorThresholdListener);

        querySystemListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean foundChild = false;
                // Should have only one child anyway (access code is unique)
                for (DataSnapshot issue : dataSnapshot.getChildren()) {
                    foundChild = true;
                    if (issue.child("user").getValue() != null)
                        receivePairSystemResultListener.OnReceivePairSystemResult(1,
                                issue.child("name").getValue(String.class));
                    else {
                        SystemData systemData = new SystemData();
                        addOrUpdateSystemList(issue, true, systemData);
                        systemData.updateUserID(FirebaseAuth.getInstance().getUid());
                        receivePairSystemResultListener.OnReceivePairSystemResult(0,
                                issue.child("name").getValue(String.class));
                    }
                }
                if (!foundChild)
                    receivePairSystemResultListener.OnReceivePairSystemResult(-1, "");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };
    }

    public void notifyListenersUpdateData() {
        dataChangeCount++;
        if (dataChangeCount >= 2) {
            for (OnDatabaseUpdateListener list : updateListenerList) {
                list.onDatabaseUpdate();
            }
        }
    }

    private void addOrUpdateSystemList(DataSnapshot issue, boolean addIt, SystemData currentSystem) {
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
            systemDataList.add(currentSystem);
    }

    public void querySystemWithAccessCode(String accessCode) {
        final Query findSystemQuery = systemsDatabaseReference.child("systems").orderByChild("access_code")
                .equalTo(accessCode);
        findSystemQuery.addListenerForSingleValueEvent(querySystemListener);
    }

    public void addOnDatabaseUpdateListener(OnDatabaseUpdateListener listener) {
        this.updateListenerList.add(listener);
    }

    public void removeOnDatabaseUpdateListener(OnDatabaseUpdateListener listener) {
        this.updateListenerList.remove(listener);
    }

    public void setOnReceivePairSystemResultListener(OnReceivePairSystemResultListener listener) {
        this.receivePairSystemResultListener = listener;
    }

    public SystemData getSystemAt(int index) {
        if (index < 0 || index >= systemDataList.size())
            return null;
        return systemDataList.get(index);
    }

    public List<SystemData> getSystemList() {
        return systemDataList;
    }

    public List<String> getAllSystemNames() {
        List<String> namesList = new ArrayList<>();
        for (SystemData system : systemDataList) {
            namesList.add(system.getName());
        }
        return namesList;
    }

    public void removeSystemAt(int index) {
        if (index < 0 || index >= systemDataList.size())
            return;
        systemDataList.remove(index);
    }

    public void removeAllListeners() {
        if (sensorDataListener != null)
            sensorDataQuery.removeEventListener(sensorDataListener);
        if (sensorThresholdListener != null)
            userParamsDatabaseReference.removeEventListener(sensorThresholdListener);
    }

    public Double getAirTempThreshold() {
        return airTempThreshold;
    }

    public Double getWaterTempThreshold() {
        return waterTempThreshold;
    }

    public Double getWaterLevelThreshold() {
        return waterLevelThreshold;
    }

    public Double getpHThreshold() {
        return pHThreshold;
    }

    public Double getHumidityThreshold() {
        return humidityThreshold;
    }

    public void updateAirTempThreshold(Double airTempThreshold) {
        userParamsDatabaseReference.child(uid + "/air_temp").setValue(airTempThreshold);
    }

    public void updateWaterTempThreshold(Double waterTempThreshold) {
        userParamsDatabaseReference.child(uid + "/water_temp").setValue(waterTempThreshold);
    }

    public void updateWaterLevelThreshold(Double waterLevelThreshold) {
        userParamsDatabaseReference.child(uid + "/water_level").setValue(waterLevelThreshold);
    }

    public void updatepHThreshold(Double pHThreshold) {
        userParamsDatabaseReference.child(uid + "/ph").setValue(pHThreshold);
    }

    public void updateHumidityThreshold(Double humidityThreshold) {
        userParamsDatabaseReference.child(uid + "/humidity").setValue(humidityThreshold);
    }
}
