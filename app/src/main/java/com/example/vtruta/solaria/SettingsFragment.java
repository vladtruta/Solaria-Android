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
import android.widget.Toast;

public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener,
        Preference.OnPreferenceClickListener, DialogManagePreference.OnManageDialogChangeListener {

    public static final String KEY_PREF_MANUAL_PAIR = "key_pref_manual_pair";
    public static final String KEY_PREF_REFRESH = "key_pref_refresh";

    private String mFragmentToLaunch;
    private PreferenceCategory preferenceCategory;
    private EditTextPreference mManualPairETPref;
    private Preference mRefreshPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFragmentToLaunch = getArguments().getString("setting");
        if ("manage".equals(mFragmentToLaunch)) {
            addPreferencesFromResource(R.xml.manage_preference);
            preferenceCategory = (PreferenceCategory) findPreference("pref_manage_paired_devices");

            addManageDevicesToCategory(5);

        } else if ("pair".equals(mFragmentToLaunch)) {
            addPreferencesFromResource(R.xml.pair_preference);
            mManualPairETPref = (EditTextPreference)findPreference(KEY_PREF_MANUAL_PAIR);
            mManualPairETPref.getEditText().setHint("Check the label on the box");
            mManualPairETPref.setOnPreferenceChangeListener(this);
            mRefreshPref = findPreference(KEY_PREF_REFRESH);
            mRefreshPref.setOnPreferenceClickListener(this);

            preferenceCategory = (PreferenceCategory) findPreference("pref_devices_nearby");
            addPairDevicesToCategory(8);
        }
    }

    private void addPairDevicesToCategory(int number)
    {
        int i;
        DialogPairPreference pref;
        String valueOfI;

        preferenceCategory.removeAll();
        for (i = 0; i < number; i++)
        {
            valueOfI = String.valueOf(i + 1);
            pref = new DialogPairPreference(getActivity(), null);
            pref.setKey("pref_pair_test_key" + valueOfI);
            pref.setTitle("Test Title " + valueOfI);
            pref.setSummary("Test Summary " + valueOfI);
            pref.setDialogTitle("Pair " + pref.getTitle());
            preferenceCategory.addPreference(pref);
        }
    }

    private void addManageDevicesToCategory(int number)
    {
        int i;
        DialogManagePreference pref;
        String valueOfI;

        preferenceCategory.removeAll();
        for (i = 0; i < number; i++)
        {
            valueOfI = String.valueOf(i + 1);
            pref = new DialogManagePreference(getActivity(), null);
            pref.setKey("pref_manage_test_key" + valueOfI);
            pref.setTitle("Test Title " + valueOfI);
            pref.setSummary("Test Summary " + valueOfI);
            pref.setDialogTitle("Manage " + pref.getTitle());
            pref.setOnManageDialogPreferenceChangeListener(this);

            preferenceCategory.addPreference(pref);
        }
    }

    @Override
    public void onManageDialogPreferenceChange(int result, DialogManagePreference pref) {
        switch(result)
        {
            case DialogInterface.BUTTON_POSITIVE:
                pref.setTitle(pref.getEditText().getText());
                // TODO: Rename the system in the database as well
                break;
            case DialogInterface.BUTTON_NEUTRAL:
                preferenceCategory.removePreference(pref);
                //TODO: Delete the system in the database as well
                break;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if ("pair".equals(mFragmentToLaunch)) {
            if (!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                Toast.makeText(getActivity(), "Bluetooth pairing not supported. Use manual pairing instead.",
                        Toast.LENGTH_SHORT).show();
                preferenceCategory.setEnabled(false);
            } else {
                preferenceCategory.setEnabled(true);
            }
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        switch (preference.getKey())
        {
            case KEY_PREF_MANUAL_PAIR:
                /* TODO: checks the Firebase Database for a system having the given ID and adds the current UserID to its user field */

                break;
        }
        return false;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        switch(preference.getKey())
        {
            case KEY_PREF_REFRESH:
                // TODO: Refresh the bluetooth scanning process
                return true;
        }
        return false;
    }
}
