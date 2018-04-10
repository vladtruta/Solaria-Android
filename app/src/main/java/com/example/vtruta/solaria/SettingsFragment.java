package com.example.vtruta.solaria;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener,
        Preference.OnPreferenceClickListener, DialogManagePreference.OnManageDialogChangeListener,
        SystemDataRepo.OnPairSystemResultListener {

    public static final String KEY_PREF_MANUAL_PAIR = "key_pref_manual_pair";
    public static final String KEY_PREF_REFRESH = "key_pref_refresh";

    private String mFragmentToLaunch;
    private PreferenceCategory preferenceCategory;
    private Preference mRefreshPref;
    private SystemDataRepo mDatabaseRepo;
    private List<SystemData> mSystemsList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDatabaseRepo = SystemDataRepo.getInstance();
        mSystemsList = mDatabaseRepo.getSystemList();
        mFragmentToLaunch = getArguments().getString("setting");
        if ("manage".equals(mFragmentToLaunch)) {
            addPreferencesFromResource(R.xml.manage_preference);
            preferenceCategory = (PreferenceCategory) findPreference("pref_manage_paired_devices");
            addManageDevicesToCategory();
        } else if ("pair".equals(mFragmentToLaunch)) {
            addPreferencesFromResource(R.xml.pair_preference);
            EditTextPreference mManualPairETPref = (EditTextPreference) findPreference(KEY_PREF_MANUAL_PAIR);
            mManualPairETPref.getEditText().setHint("Check the label on the box");
            mManualPairETPref.getEditText().setSingleLine(true);
            mManualPairETPref.setOnPreferenceChangeListener(this);
            mDatabaseRepo.setOnReceivePairSystemResultListener(this);
            mRefreshPref = findPreference(KEY_PREF_REFRESH);
            mRefreshPref.setOnPreferenceClickListener(this);

            preferenceCategory = (PreferenceCategory) findPreference("pref_devices_nearby");
            addPairDevicesToCategory(8);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if ("pair".equals(mFragmentToLaunch)) {
            if (!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                Toast.makeText(getActivity(), "Bluetooth pairing not supported. Use manual pairing instead.",
                        Toast.LENGTH_SHORT).show();
                mRefreshPref.setEnabled(false);
                preferenceCategory.setEnabled(false);
            } else {
                mRefreshPref.setEnabled(true);
                preferenceCategory.setEnabled(true);
            }
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }


    /* -- MANAGING --- */
    @Override
    public void onManageDialogPreferenceChange(int result, DialogManagePreference pref) {
        switch (result) {
            case DialogInterface.BUTTON_POSITIVE:
                EditText prefEditText = pref.getEditText();
                SystemData currentSystem = mDatabaseRepo.getSystemAt(pref.getIndex());
                String newName = prefEditText.getText().toString().trim();
                if (!currentSystem.getName().equals(newName)) {
                    Toast.makeText(getActivity(), "Renamed " + currentSystem.getName() + " to "
                            + newName, Toast.LENGTH_SHORT).show();
                    pref.setTitle(newName);
                    currentSystem.updateName(newName);
                }
                break;
            case DialogInterface.BUTTON_NEUTRAL:
                preferenceCategory.removePreference(pref);
                SystemData toRemove = mDatabaseRepo.getSystemAt(pref.getIndex());
                if (toRemove != null) {
                    Toast.makeText(getActivity(), "Removed " + toRemove.getName() + " successfully!", Toast.LENGTH_SHORT).show();
                    toRemove.removeUserDatabase();
                    mDatabaseRepo.removeSystemAt(pref.getIndex());
                }
                break;
        }
    }

    private void addManageDevicesToCategory() {
        int i;
        DialogManagePreference pref;
        String valueOfI;

        preferenceCategory.removeAll();
        for (i = 0; i < mSystemsList.size(); i++) {
            valueOfI = String.valueOf(i + 1);
            pref = new DialogManagePreference(getActivity(), null);
            pref.setKey("pref_manage_test_key" + valueOfI);
            pref.setTitle(mSystemsList.get(i).getName());
            pref.setDialogTitle("Manage " + pref.getTitle());
            pref.setOnManageDialogPreferenceChangeListener(this);
            pref.setIndex(i);
            preferenceCategory.addPreference(pref);
        }
    }

    /* --- PAIRING --- */
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        switch (preference.getKey()) {
            case KEY_PREF_MANUAL_PAIR:
                String stringNewValue = String.valueOf(newValue);
                // This will call the callback below, OnReceivePairSystemResult(int resultCode)
                mDatabaseRepo.querySystemWithAccessCode(stringNewValue);
                break;
        }
        return false;
    }

    @Override
    public void OnReceivePairSystemResult(int resultCode) {
        switch (resultCode) {
            case 0: // ok
                Toast.makeText(getActivity(), "Success: Pairing successful!", Toast.LENGTH_SHORT).show();
                break;
            case 1: // system already paired with a user
                Toast.makeText(getActivity(), "Error: Device already paired!", Toast.LENGTH_SHORT).show();
                break;
            case -1: // no system with this access code exists
                Toast.makeText(getActivity(), "Error: Invalid access code!", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()) {
            case KEY_PREF_REFRESH:
                // TODO: Refresh the bluetooth scanning process
                return true;
        }
        return false;
    }

    private void addPairDevicesToCategory(int number) {
        int i;
        DialogPairPreference pref;
        String valueOfI;

        preferenceCategory.removeAll();
        for (i = 0; i < number; i++) {
            valueOfI = String.valueOf(i + 1);
            pref = new DialogPairPreference(getActivity(), null);
            pref.setKey("pref_pair_test_key" + valueOfI);
            pref.setTitle("Test Title " + valueOfI);
            pref.setSummary("Test Summary " + valueOfI);
            pref.setDialogTitle("Pair " + pref.getTitle());
            preferenceCategory.addPreference(pref);
        }
    }
}
