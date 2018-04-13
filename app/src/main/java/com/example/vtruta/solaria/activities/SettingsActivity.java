package com.example.vtruta.solaria.activities;

import android.app.ActionBar;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.view.MenuItem;
import android.view.View;

import com.example.vtruta.solaria.R;
import com.example.vtruta.solaria.fragments.SettingsFragment;

import java.lang.reflect.Field;
import java.util.List;

public class SettingsActivity extends PreferenceActivity {

    private static final String TAG = "SettingsActivity";
    private SettingsActivity settingsActivity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingsActivity = this;
        ActionBar bar = getActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setTitle("Settings");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return SettingsFragment.class.getName().equals(fragmentName);
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preference_headers, target);

        final View breadcrumb = findViewById(android.R.id.title);
        if (breadcrumb == null) {
            // Single pane layout
            return;
        }

        try {
            final Field titleColor = breadcrumb.getClass().getDeclaredField("mTextColor");
            titleColor.setAccessible(true);
            titleColor.setInt(breadcrumb, ContextCompat.getColor(getApplicationContext(), R.color.colorWhiteCustom));
        } catch (final Exception ignored) {
            // Nothing to do
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
