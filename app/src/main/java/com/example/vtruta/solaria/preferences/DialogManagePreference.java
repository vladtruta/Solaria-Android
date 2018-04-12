package com.example.vtruta.solaria.preferences;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.example.vtruta.solaria.R;

public class DialogManagePreference extends DialogPreference {

    private static final String TAG = "DialogManagePreference";

    public interface OnManageDialogChangeListener {
        void onManageDialogPreferenceChange(int result, DialogManagePreference pref);
    }

    private int index;

    private OnManageDialogChangeListener manageDialogChangeListener;

    private EditText mDeviceNameET;

    private TextWatcher textWatcher;

    public DialogManagePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.pref_manage_device);
        setPositiveButtonText("Rename");
        setNegativeButtonText("Cancel");
        setDialogIcon(null);
    }

    @Override
    protected void onBindDialogView(View view) {
        loadViews(view);
        setListeners();
        setDeviceNameProperties();
        super.onBindDialogView(view);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        mDeviceNameET.removeTextChangedListener(textWatcher);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
        builder.setNeutralButton("Delete", this);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        super.onClick(dialog, which);
        manageDialogChangeListener.onManageDialogPreferenceChange(which, this);
    }

    private void loadViews(View view) {
        mDeviceNameET = view.findViewById(R.id.device_name_et);
    }

    private void setListeners() {
        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                AlertDialog dial = (AlertDialog) getDialog();
                if (dial != null) {
                    if (!validateInput(mDeviceNameET)) {
                        dial.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    } else {
                        dial.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    }
                }
            }
        };
        mDeviceNameET.addTextChangedListener(textWatcher);

        TextView.OnEditorActionListener editorActionListener = new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = true;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    EditText et = (EditText) v;
                    if (validateInput(et))
                        handled = false;
                }
                return handled;
            }
        };
        mDeviceNameET.setOnEditorActionListener(editorActionListener);
    }

    private void setDeviceNameProperties() {
        mDeviceNameET.setText(getTitle());
        mDeviceNameET.setSelection(mDeviceNameET.getText().length());
        if (mDeviceNameET.requestFocus()) {
            final InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            mDeviceNameET.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mDeviceNameET.requestFocus();
                    if (imm != null) {
                        imm.showSoftInput(mDeviceNameET, 0);
                    }
                }
            }, 100);
        }
    }

    private boolean validateInput(EditText et) {
        String etString = et.getText().toString().trim();
        if (etString.length() > 15) {
            et.setError("Name too long!");
            return false;
        } else if (etString.length() == 0) {
            et.setError("Name cannot be empty!");
            return false;
        }
        return true;
    }

    public void setOnManageDialogPreferenceChangeListener(OnManageDialogChangeListener mListener) {
        this.manageDialogChangeListener = mListener;
    }

    public EditText getEditText() {
        return mDeviceNameET;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}