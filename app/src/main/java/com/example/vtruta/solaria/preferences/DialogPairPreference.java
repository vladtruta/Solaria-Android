package com.example.vtruta.solaria.preferences;

import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.util.AttributeSet;

public class DialogPairPreference extends DialogPreference {

    private static final String TAG = "DialogPairPreference";

    public interface OnPairDialogCloseListener {
        void onPairDialogPreferenceClose(int result, DialogPairPreference pref);
    }

    private int index;

    private OnPairDialogCloseListener pairDialogCloseListener;

    public DialogPairPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPositiveButtonText("Pair");
        setNegativeButtonText("Cancel");
        setDialogIcon(null);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        super.onClick(dialog, which);
        pairDialogCloseListener.onPairDialogPreferenceClose(which, this);
    }

    public void setOnPairDialogCloseListener(OnPairDialogCloseListener listener) {
        this.pairDialogCloseListener = listener;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}