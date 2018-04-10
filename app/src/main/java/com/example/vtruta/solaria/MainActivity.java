package com.example.vtruta.solaria;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
        implements LogoutDialogFragment.LoginDialogListener, AdapterView.OnItemSelectedListener {

    private TabLayout mTabLayout;
    private ViewPager mPager;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private Toolbar mToolbar;
    private Spinner mSystemsSpinner;
    private SystemDataRepo mRepoInstance;
    private DrawerLayout.DrawerListener mDrawerLayoutListener;
    private TabLayout.OnTabSelectedListener mTabLayoutSelectedListener;

    private static final String CURRENT_SYSTEM_INDEX = "currentSystemIndex";
    private int currentSystemIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRepoInstance = SystemDataRepo.getInstance();
        loadTabs();
        loadDrawer();
        setUserDataDrawer();
        loadToolbar();
    }

    @Override
    protected void onStart() {
        loadSpinnerData();
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDrawerLayout.removeDrawerListener(mDrawerLayoutListener);
        mTabLayout.removeOnTabSelectedListener(mTabLayoutSelectedListener);
    }

    private void loadSpinnerData()
    {
        List<String> systemNamesList = mRepoInstance.getAllSystemNames();
        if (systemNamesList.size() > 0) {
            mSystemsSpinner.setEnabled(true);
            String[] systemNamesArray = new String[systemNamesList.size()];
            systemNamesArray = systemNamesList.toArray(systemNamesArray);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                    systemNamesArray);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mSystemsSpinner.setAdapter(adapter);
            mSystemsSpinner.setOnItemSelectedListener(this);
        } else {
            mSystemsSpinner.setEnabled(false);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle("No Systems");
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        currentSystemIndex = position;
        StatusFragment statusFragment = (StatusFragment)getSupportFragmentManager()
                .findFragmentByTag("android:switcher:" + R.id.fragment_pager + ":" + "0");
        ControlFragment controlFragment = (ControlFragment)getSupportFragmentManager()
                .findFragmentByTag("android:switcher:" + R.id.fragment_pager + ":" + "1");

        statusFragment.onDatabaseUpdate();
        controlFragment.onDatabaseUpdate();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(mRepoInstance.getSystemAt(currentSystemIndex).getName());
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void loadDrawer() {
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayoutListener = new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                int size = mNavigationView.getMenu().size();
                for (int i = 0; i < size; i++) {
                    mNavigationView.getMenu().getItem(i).setChecked(false);
                }
            }
            @Override
            public void onDrawerStateChanged(int newState) {

            }
        };
        mDrawerLayout.addDrawerListener(mDrawerLayoutListener);

        mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                mDrawerLayout.closeDrawers();
                switch (menuItem.getItemId()) {
                    case R.id.nav_settings:
                        Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                        startActivity(intent);
                        return true;
                    case R.id.nav_logout:
                        showNoticeDialog();
                        return true;
                }
                return false;
            }
        });
    }

    private void setUserDataDrawer() {
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mUser != null) {
            View hView = mNavigationView.getHeaderView(0);
            Uri photoUrl = mUser.getPhotoUrl();
            CircleImageView userImage = hView.findViewById(R.id.user_image);
            Glide.with(getApplicationContext())
                    .load(photoUrl)
                    .apply(new RequestOptions().centerCrop())
                    .into(userImage);
            String name = mUser.getDisplayName();
            TextView userName = hView.findViewById(R.id.user_name);
            userName.setText(name);
            String email = mUser.getEmail();
            TextView userEmail = hView.findViewById(R.id.user_email);
            userEmail.setText(email);
            mSystemsSpinner = hView.findViewById(R.id.current_system_spinner);
        }
    }

    private void loadToolbar() {
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }
    }

    private void loadTabs() {
        MainAdapter mAdapter = new MainAdapter(getSupportFragmentManager());
        mPager = findViewById(R.id.fragment_pager);
        mPager.setAdapter(mAdapter);

        mTabLayout = findViewById(R.id.tab_layout);
        mTabLayout.setupWithViewPager(mPager);
        mTabLayoutSelectedListener = new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        };
        mTabLayout.addOnTabSelectedListener(mTabLayoutSelectedListener);
    }

    private void showNoticeDialog() {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new LogoutDialogFragment();
        dialog.show(getSupportFragmentManager(), "LogoutDialogFragment");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLoginDialogPositiveClick() {
        mRepoInstance.removeAllListeners();
        signOutFirebase();
    }

    private void signOutFirebase() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getApplicationContext(), "Signed out successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    @Override
    public void onBackPressed() {
        showNoticeDialog();
    }

    public static class MainAdapter extends FragmentPagerAdapter {
        // Tab titles
        private String[] tabTitles = new String[]{"Status", "Control"};

        MainAdapter(FragmentManager fm) {
            super(fm);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }

        @Override
        public int getCount() {
            return tabTitles.length;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return StatusFragment.newInstance();
                case 1:
                    return ControlFragment.newInstance();
                default:
                    return null;
            }
        }
    }

    public int getCurrentSystemIndex() {
        return currentSystemIndex;
    }

    public SystemDataRepo getRepoInstance() {
        return mRepoInstance;
    }
}