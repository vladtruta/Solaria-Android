package com.example.vtruta.solaria;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;

class DialogPairPreference extends DialogPreference {
    DialogPairPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPositiveButtonText("Pair");
        setNegativeButtonText("Cancel");
        setDialogIcon(null);
    }
}