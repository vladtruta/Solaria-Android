package com.example.vtruta.solaria;

import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.util.AttributeSet;

class DialogPairPreference extends DialogPreference {
    public interface OnPairDialogCloseListener
    {
        void onPairDialogPreferenceClose(int result, DialogPairPreference pref);
    }

    String accessCode;
    int index;
    private OnPairDialogCloseListener mListener;

    DialogPairPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPositiveButtonText("Pair");
        setNegativeButtonText("Cancel");
        setDialogIcon(null);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        super.onClick(dialog, which);
        mListener.onPairDialogPreferenceClose(which, this);
    }

    public void setOnPairDialogCloseListener(OnPairDialogCloseListener listener) {
        this.mListener = listener;
    }

    public void setAccessCode(String accessCode) {
        this.accessCode = accessCode;
    }

    public String getAccessCode() {
        return accessCode;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}