package com.example.vtruta.solaria.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;


public class LogoutDialogFragment extends DialogFragment {

    private static final String TAG = "LogoutDialogFragment";

    public interface LogoutDialogListener {
        void onLogoutDialogPositiveClick();
    }

    private LogoutDialogListener logoutDialogListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        Context context = getActivity();
        if (context == null) {
            return super.onCreateDialog(savedInstanceState);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("You will be signed out.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        logoutDialogListener.onLogoutDialogPositiveClick();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        return builder.create();
    }

    public void setLogoutDialogListener(LogoutDialogListener logoutDialogListener) {
        this.logoutDialogListener = logoutDialogListener;
    }
}