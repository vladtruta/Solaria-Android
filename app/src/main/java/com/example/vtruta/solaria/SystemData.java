package com.example.vtruta.solaria;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SystemData {
    private String name;
    private String accessCode;
    private String rootName;
    private Double airTemp;
    private Double waterTemp;
    private String lightStatus;
    private Double waterLevel;
    private Double pH;
    private Double humidity;
    private Integer dropNutrients;
    private Integer turnLights;

    private DatabaseReference mDatabase;

    SystemData() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public String getName() {
        return name;
    }

    public Double getAirTemp() {
        return airTemp;
    }

    public Double getWaterTemp() {
        return waterTemp;
    }

    public String getLightStatus() {
        return lightStatus;
    }

    public Double getWaterLevel() {
        return waterLevel;
    }

    public Double getpH() {
        return pH;
    }

    public Double getHumidity() {
        return humidity;
    }

    public Integer getDropNutrients() {
        return dropNutrients;
    }

    public Integer getTurnLights() {
        return turnLights;
    }

    public String getAccessCode() {
        return accessCode;
    }

    public String getRootName() {
        return rootName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAirTemp(Double airTemp) {
        this.airTemp = airTemp;
    }

    public void setWaterTemp(Double waterTemp) {
        this.waterTemp = waterTemp;
    }

    public void setLightStatus(String lightStatus) {
        this.lightStatus = lightStatus;
    }

    public void setWaterLevel(Double waterLevel) {
        this.waterLevel = waterLevel;
    }

    public void setpH(Double pH) {
        this.pH = pH;
    }

    public void setHumidity(Double humidity) {
        this.humidity = humidity;
    }

    public void setDropNutrients(Integer dropNutrients) {
        this.dropNutrients = dropNutrients;
    }

    public void setTurnLights(Integer turnLights) {
        this.turnLights = turnLights;
    }

    public void setAccessCode(String accessCode) {
        this.accessCode = accessCode;
    }

    public void setRootName(String rootName) {
        this.rootName = rootName;
    }

    public void updateTurnLights(int value) {
        mDatabase.child("systems/" + rootName + "/control/turn_lights").setValue(value);
    }

    public void updateDropNutrients(int value) {
        mDatabase.child("systems/" + rootName + "/control/drop_nutriments").setValue(value);
    }
}
