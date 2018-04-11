package com.example.vtruta.solaria;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
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

import static android.app.Activity.RESULT_OK;

public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener,
        Preference.OnPreferenceClickListener, DialogManagePreference.OnManageDialogChangeListener,
        SystemDataRepo.OnPairSystemResultListener, DialogPairPreference.OnPairDialogCloseListener {

    public static final String KEY_PREF_MANUAL_PAIR = "key_pref_manual_pair";
    public static final String KEY_PREF_REFRESH = "key_pref_refresh";

    private static final int REQUEST_ENABLE_BT = 66;

    private BluetoothAdapter mBluetoothAdapter;
    private int mPairedDeviceIndex;
    private boolean mScanning;
    private Handler mHandler;
    private static final long SCAN_PERIOD = 10000; // 10 seconds
    private BluetoothAdapter.LeScanCallback mLeScanCallback;

    private BroadcastReceiver mGattUpdateReceiver;

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
            mHandler = new Handler();
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
                mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
                    @Override
                    public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //TODO: set access code when scanning
                                addPairDeviceToCategory(device.getName(), mPairedDeviceIndex, "nocode");
                                mPairedDeviceIndex++;
                            }
                        });
                    }
                };
                checkBluetoothEnabled();
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
            pref.setKey("pref_manage_" + valueOfI);
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
                checkBluetoothEnabled();
                return true;
        }
        return false;
    }

    private void checkBluetoothEnabled() {
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            mBluetoothAdapter = bluetoothManager.getAdapter();
        }
        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            scanLeDevice(true);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == REQUEST_ENABLE_BT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                scanLeDevice(true);
            }
        }
    }

    @Override
    public void onPairDialogPreferenceClose(int result, DialogPairPreference pref) {
        switch (result) {
            case DialogInterface.BUTTON_POSITIVE:
                mDatabaseRepo.querySystemWithAccessCode(pref.getAccessCode());
                break;
        }
    }

    private void addPairDeviceToCategory(String name, int index, String accessCode) {
        DialogPairPreference pref;

        pref = new DialogPairPreference(getActivity(), null);
        pref.setKey("pref_pair_" + index);
        pref.setTitle(name);
        pref.setDialogTitle("Pair " + name);
        pref.setOnPairDialogCloseListener(this);
        pref.setAccessCode(accessCode);
        preferenceCategory.addPreference(pref);
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);

            preferenceCategory.removeAll();
            mPairedDeviceIndex = 0;
            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }
}
