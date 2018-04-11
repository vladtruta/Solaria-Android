package com.example.vtruta.solaria;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

public class DialogManagePreference extends DialogPreference implements TextView.OnEditorActionListener,
        android.text.TextWatcher {

    public interface OnManageDialogChangeListener
    {
        void onManageDialogPreferenceChange(int result, DialogManagePreference pref);
    }

    private int mPreferenceListIndex;
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
        mDeviceNameET.setOnEditorActionListener(this);
        mDeviceNameET.addTextChangedListener(this);
        if (mDeviceNameET.requestFocus()) {
            final InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            mDeviceNameET.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    mDeviceNameET.requestFocus();
                    if (imm != null) {
                        imm.showSoftInput(mDeviceNameET, 0);
                    }
                }
            }, 100);
        }
        super.onBindDialogView(view);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        AlertDialog dial = (AlertDialog) getDialog();
        if (!validateInput(mDeviceNameET)) {
            dial.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        } else {
            dial.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        mDeviceNameET.removeTextChangedListener(this);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        boolean handled = true;
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            EditText et = (EditText)v;
            if (validateInput(et))
                handled = false;
        }
        return handled;
    }

    private boolean validateInput(EditText et)
    {
        String etString = et.getText().toString().trim();
        if (etString.length() > 15) {
            et.setError("Name too long!");
            return false;
        }
        else if (etString.length() == 0) {
            et.setError("Name cannot be empty!");
            return false;
        }
        return true;
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

    public int getIndex() {
        return mPreferenceListIndex;
    }

    public void setIndex(int index) {
        this.mPreferenceListIndex = index;
    }
}