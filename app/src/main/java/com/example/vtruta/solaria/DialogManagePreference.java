package com.example.vtruta.solaria;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

public class DialogManagePreference extends DialogPreference {
    public interface OnManageDialogChangeListener
    {
        void onManageDialogPreferenceChange(int result, DialogManagePreference pref);
    }

    private EditText mDeviceNameET;
    private OnManageDialogChangeListener mListener;

    DialogManagePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.pref_manage_devices);
        setPositiveButtonText("Rename");
        setNegativeButtonText("Cancel");
        setDialogIcon(null);
    }

    @Override
    protected void onBindDialogView(View view) {
        mDeviceNameET = view.findViewById(R.id.device_name_et);
        mDeviceNameET.setText(getTitle());
        mDeviceNameET.setSelection(mDeviceNameET.getText().length());
        super.onBindDialogView(view);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder)
    {
        super.onPrepareDialogBuilder(builder);
        builder.setNeutralButton("Delete", this);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        super.onClick(dialog, which);
        mListener.onManageDialogPreferenceChange(which, this);
    }

    public void setOnManageDialogPreferenceChangeListener(OnManageDialogChangeListener mListener) {
        this.mListener = mListener;
    }

    public EditText getEditText() {
        return mDeviceNameET;
    }
}