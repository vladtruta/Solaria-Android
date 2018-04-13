package com.example.vtruta.solaria.fragments;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.example.vtruta.solaria.R;
import com.example.vtruta.solaria.database.SystemData;
import com.example.vtruta.solaria.database.SystemDataRepo;
import com.example.vtruta.solaria.preferences.DialogManagePreference;
import com.example.vtruta.solaria.preferences.DialogPairPreference;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;

public class SettingsFragment extends PreferenceFragment {

    private static final String TAG = "SettingsFragment";
    public static final String KEY_PREF_MANUAL_PAIR = "key_pref_manual_pair";
    public static final String KEY_PREF_REFRESH = "key_pref_refresh";
    private static final UUID serviceUUID = UUID.fromString("0a1fe100-fb28-4e8c-afa0-21b036da5900");
    private static final UUID characteristicUUID = UUID.fromString("0a1fe100-fb28-4e8c-afa0-21b036da5901");
    private static final int REQUEST_ENABLE_BT = 66;
    private static final long SCAN_PERIOD = 10000; // 10 seconds

    private String fragmentToLaunchFromHeader;

    private DialogPairPreference.OnPairDialogCloseListener pairDialogCloseListener;
    private DialogManagePreference.OnManageDialogChangeListener manageDialogChangeListener;
    private BluetoothAdapter.LeScanCallback leScanCallback;
    private BluetoothGattCallback bluetoothGattCallback;

    private int mPairedDeviceIndex;
    private boolean isScanningBluetooth;

    private PreferenceCategory mManagePairedDevicesCategory;
    private Preference mRefreshPref;
    private EditTextPreference mManualPairETPref;

    private List<BluetoothDevice> bluetoothDevicesList;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private List<SystemData> systemsList;
    private SystemDataRepo systemDataRepo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addResourceLayout();
        loadFields();
        setListeners();
        loadCurrentFragmentProperties();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        checkBluetoothExistsOnDevice(savedInstanceState);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (bluetoothAdapter != null)
            scanLeDevice(false);
        // Closes the Gatt Server
        closeGatt();
    }

    private void addResourceLayout() {
        fragmentToLaunchFromHeader = getArguments().getString("setting");
        if (fragmentToLaunchFromHeader != null) {
            switch (fragmentToLaunchFromHeader) {
                case "manage":
                    addPreferencesFromResource(R.xml.preference_manage);
                    break;
                case "pair":
                    addPreferencesFromResource(R.xml.preference_pair);
                    break;
            }
        }
    }

    private void loadFields() {
        systemDataRepo = SystemDataRepo.getInstance();
        systemsList = systemDataRepo.getSystemList();

        switch (fragmentToLaunchFromHeader) {
            case "manage":
                mManagePairedDevicesCategory = (PreferenceCategory) findPreference("pref_manage_paired_devices");
                break;
            case "pair":
                mManagePairedDevicesCategory = (PreferenceCategory) findPreference("pref_devices_nearby");
                mManualPairETPref = (EditTextPreference) findPreference(KEY_PREF_MANUAL_PAIR);
                mRefreshPref = findPreference(KEY_PREF_REFRESH);
                bluetoothDevicesList = new ArrayList<>();
                break;
        }
    }

    private void setListeners() {
        switch (fragmentToLaunchFromHeader) {
            case "manage":
                manageDialogChangeListener = new DialogManagePreference.OnManageDialogChangeListener() {
                    @Override
                    public void onManageDialogPreferenceChange(int result, DialogManagePreference pref) {
                        switch (result) {
                            case DialogInterface.BUTTON_POSITIVE:
                                EditText prefEditText = pref.getEditText();
                                SystemData currentSystem = systemDataRepo.getSystemAt(pref.getIndex());
                                String newName = prefEditText.getText().toString().trim();
                                if (!currentSystem.getName().equals(newName)) {
                                    Toast.makeText(getActivity(), "Renamed " + currentSystem.getName() + " to "
                                            + newName, Toast.LENGTH_SHORT).show();
                                    pref.setTitle(newName);
                                    currentSystem.updateName(newName);
                                }
                                break;
                            case DialogInterface.BUTTON_NEUTRAL:
                                mManagePairedDevicesCategory.removePreference(pref);
                                SystemData toRemove = systemDataRepo.getSystemAt(pref.getIndex());
                                if (toRemove != null) {
                                    Toast.makeText(getActivity(), "Removed " + toRemove.getName() + " successfully!", Toast.LENGTH_SHORT).show();
                                    toRemove.removeUserDatabase();
                                    systemDataRepo.removeSystemAt(pref.getIndex());
                                }
                                break;
                        }
                    }
                };
                break;
            case "pair":
                Preference.OnPreferenceClickListener preferenceClickListener = new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        switch (preference.getKey()) {
                            case KEY_PREF_REFRESH:
                                checkBluetoothEnabled(null);
                                return true;
                        }
                        return false;
                    }
                };
                mRefreshPref.setOnPreferenceClickListener(preferenceClickListener);

                Preference.OnPreferenceChangeListener preferenceChangeListener = new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        switch (preference.getKey()) {
                            case KEY_PREF_MANUAL_PAIR:
                                String stringNewValue = String.valueOf(newValue);
                                // This will call the callback OnReceivePairSystemResult(int resultCode)
                                systemDataRepo.querySystemWithAccessCode(stringNewValue);
                                break;
                        }
                        return false;
                    }
                };
                mManualPairETPref.setOnPreferenceChangeListener(preferenceChangeListener);

                SystemDataRepo.OnReceivePairSystemResultListener receivePairSystemResultListener = new SystemDataRepo.OnReceivePairSystemResultListener() {
                    @Override
                    public void OnReceivePairSystemResult(int resultCode, String systemName) {
                        switch (resultCode) {
                            case 0: // ok
                                Toast.makeText(getActivity(), "Success: Pairing " + systemName + " successful!", Toast.LENGTH_SHORT).show();
                                break;
                            case 1: // system already paired with a user
                                Toast.makeText(getActivity(), "Error: Device " + systemName +" already paired!", Toast.LENGTH_SHORT).show();
                                break;
                            case -1: // no system with this access code exists
                                Toast.makeText(getActivity(), "Error: Invalid access code!", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                };
                systemDataRepo.setOnReceivePairSystemResultListener(receivePairSystemResultListener);

                pairDialogCloseListener = new DialogPairPreference.OnPairDialogCloseListener() {
                    @Override
                    public void onPairDialogPreferenceClose(int result, DialogPairPreference pref) {
                        switch (result) {
                            case DialogInterface.BUTTON_POSITIVE:
                                BluetoothDevice currentDevice = bluetoothDevicesList.get(pref.getIndex());
                                closeGatt();
                                bluetoothGatt = currentDevice.connectGatt(getActivity(), false, bluetoothGattCallback);
                                break;
                        }
                    }
                };
                break;
        }
    }

    private void loadCurrentFragmentProperties() {
        switch (fragmentToLaunchFromHeader) {
            case "manage":
                addManageDevicesToCategory();
                break;
            case "pair":
                mManualPairETPref.getEditText().setHint("Check the label on the box");
                mManualPairETPref.getEditText().setSingleLine(true);
                mRefreshPref.setTitle("Refresh");
                break;
        }
    }

    private void addManageDevicesToCategory() {
        mManagePairedDevicesCategory.removeAll();
        for (int i = 0; i < systemsList.size(); i++) {
            String valueOfI = String.valueOf(i + 1);
            DialogManagePreference pref = new DialogManagePreference(getActivity(), null);
            pref.setKey("pref_manage_" + valueOfI);
            pref.setTitle(systemsList.get(i).getName());
            pref.setDialogTitle("Manage " + pref.getTitle());
            pref.setOnManageDialogPreferenceChangeListener(manageDialogChangeListener);
            pref.setIndex(i);
            mManagePairedDevicesCategory.addPreference(pref);
        }
    }

    private void checkBluetoothExistsOnDevice(Bundle savedInstanceState) {
        switch (fragmentToLaunchFromHeader) {
            case "pair": // Only the Pair menu fragment has Bluetooth features
                if (getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                    mRefreshPref.setEnabled(true);
                    mManagePairedDevicesCategory.setEnabled(true);
                    loadBluetoothPairCallbacks();
                    checkBluetoothEnabled(savedInstanceState);
                } else {
                    mRefreshPref.setEnabled(false);
                    mManagePairedDevicesCategory.setEnabled(false);
                    Toast.makeText(getActivity(), "Bluetooth pairing not supported. Use manual pairing instead.",
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void loadBluetoothPairCallbacks() {
        bluetoothGattCallback = new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(final BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            gatt.discoverServices();
                        }
                    });
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);

                if (status != BluetoothGatt.GATT_SUCCESS) {
                    Log.w(TAG, "onServicesDiscovered received: " + status);
                    return;
                }
                BluetoothGattService service = gatt.getService(serviceUUID);
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicUUID);
                bluetoothGatt.readCharacteristic(characteristic);
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicRead(gatt, characteristic, status);
                String accessCodeString = characteristic.getStringValue(0);
                systemDataRepo.querySystemWithAccessCode(accessCodeString);
            }
        };

        leScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (bluetoothDevicesList.indexOf(device) == -1) {
                            bluetoothDevicesList.add(device);
                            addPairDeviceToCategory(device.getName(), mPairedDeviceIndex);
                            mPairedDeviceIndex++;
                        }
                    }
                });
            }
        };
    }

    private void addPairDeviceToCategory(String name, int index) {
        DialogPairPreference pref = new DialogPairPreference(getActivity(), null);
        pref.setKey("pref_pair_" + index);
        pref.setTitle(name);
        pref.setDialogTitle("Pair " + name);
        pref.setOnPairDialogCloseListener(pairDialogCloseListener);
        pref.setIndex(index);
        mManagePairedDevicesCategory.addPreference(pref);
    }

    private void checkBluetoothEnabled(Bundle savedInstanceState) {
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            bluetoothAdapter = bluetoothManager.getAdapter();
        }
        // Requests the user to enable Bluetooth
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            //TODO: might remove it
            if (savedInstanceState == null)
                scanLeDevice(true);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                scanLeDevice(true);
            }
        }
    }

    private void scanLeDevice(final boolean enable) {
        Handler handler = new Handler();
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isScanningBluetooth = false;
                    bluetoothAdapter.stopLeScan(leScanCallback);
                    mRefreshPref.setTitle("Refresh");
                }
            }, SCAN_PERIOD);

            isScanningBluetooth = true;
            mPairedDeviceIndex = 0;
            mManagePairedDevicesCategory.removeAll();
            bluetoothDevicesList.clear();
            UUID[] array = {serviceUUID};
            bluetoothAdapter.startLeScan(array, leScanCallback);
            mRefreshPref.setTitle("Scanning...");
        } else {
            isScanningBluetooth = false;
            bluetoothAdapter.stopLeScan(leScanCallback);
            mRefreshPref.setTitle("Refresh");
        }
    }

    private void closeGatt() {
        if (bluetoothGatt == null) {
            return;
        }
        bluetoothGatt.close();
        bluetoothGatt = null;
    }
}
