package com.example.vtruta.solaria.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
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
import com.example.vtruta.solaria.R;
import com.example.vtruta.solaria.database.SystemDataRepo;
import com.example.vtruta.solaria.fragments.ControlFragment;
import com.example.vtruta.solaria.fragments.LogoutDialogFragment;
import com.example.vtruta.solaria.fragments.StatusFragment;
import com.example.vtruta.solaria.fragments.ThresholdFragment;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private int currentSystemIndex = 0;

    private DrawerLayout.DrawerListener drawerListener;
    private TabLayout.OnTabSelectedListener tabSelectedListener;
    private AdapterView.OnItemSelectedListener itemSelectedListener;

    private MainAdapter mMainAdapter;
    private TabLayout mTabLayout;
    private ViewPager mPager;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private Toolbar mToolbar;
    private CircleImageView mUserImageCIV;
    private TextView mUserNameTV;
    private TextView mUserEmailTV;
    private Spinner mSystemsSpinner;

    private LogoutDialogFragment logoutDialogFragment;
    private SystemDataRepo systemDataRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadFields();
        setListeners();

        loadTabs();
        setUserDataDrawer();
        loadToolbar();
    }

    @Override
    protected void onStart() {
        loadSpinnerData();
        super.onStart();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int size = mNavigationView.getMenu().size();
        for (int i = 0; i < size; i++) {
            mNavigationView.getMenu().getItem(i).setChecked(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDrawerLayout.removeDrawerListener(drawerListener);
        mTabLayout.removeOnTabSelectedListener(tabSelectedListener);
    }

    private void loadFields() {
        mPager = findViewById(R.id.fragment_pager);
        mPager.setOffscreenPageLimit(3);
        mTabLayout = findViewById(R.id.tab_layout);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mNavigationView = findViewById(R.id.nav_view);
        mToolbar = findViewById(R.id.toolbar);
        View mHeaderView = mNavigationView.getHeaderView(0);
        mUserImageCIV = mHeaderView.findViewById(R.id.user_image);
        mUserNameTV = mHeaderView.findViewById(R.id.user_name);
        mUserEmailTV = mHeaderView.findViewById(R.id.user_email);
        mSystemsSpinner = mHeaderView.findViewById(R.id.current_system_spinner);
        mMainAdapter = new MainAdapter(getSupportFragmentManager());
        logoutDialogFragment = new LogoutDialogFragment();
        systemDataRepo = SystemDataRepo.getInstance();
    }

    private void setListeners() {
        itemSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentSystemIndex = position;
                systemDataRepo.notifyListenersUpdateData();
                ActionBar actionBar = getSupportActionBar();
                if (actionBar != null) {
                    actionBar.setTitle(systemDataRepo.getSystemAt(currentSystemIndex).getName());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };

        drawerListener = new DrawerLayout.DrawerListener() {
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
        mDrawerLayout.addDrawerListener(drawerListener);

        tabSelectedListener = new TabLayout.OnTabSelectedListener() {
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
        mTabLayout.addOnTabSelectedListener(tabSelectedListener);

        NavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        mDrawerLayout.closeDrawers();
                        switch (menuItem.getItemId()) {
                            case R.id.nav_settings:
                                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                                startActivity(intent);
                                return true;
                            case R.id.nav_logout:
                                showLogoutDialog();
                                return true;
                        }
                        return false;
                    }
                };
        mNavigationView.setNavigationItemSelectedListener(navigationItemSelectedListener);

        LogoutDialogFragment.LogoutDialogListener logoutDialogListener = new LogoutDialogFragment.LogoutDialogListener() {
            @Override
            public void onLogoutDialogPositiveClick() {
                systemDataRepo.removeAllListeners();
                signOutFirebase();
            }
        };
        logoutDialogFragment.setLogoutDialogListener(logoutDialogListener);
    }

    private void loadTabs() {
        mPager.setAdapter(mMainAdapter);
        mTabLayout.setupWithViewPager(mPager);
    }

    private void loadToolbar() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }
    }

    private void setUserDataDrawer() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            Uri photoUrl = currentUser.getPhotoUrl();
            Glide.with(getApplicationContext())
                    .load(photoUrl)
                    .apply(new RequestOptions().centerCrop())
                    .into(mUserImageCIV);
            String name = currentUser.getDisplayName();
            mUserNameTV.setText(name);
            String email = currentUser.getEmail();
            mUserEmailTV.setText(email);
        }
    }

    private void loadSpinnerData() {
        List<String> systemNamesList = systemDataRepo.getAllSystemNames();
        if (systemNamesList.size() > 0) {
            mSystemsSpinner.setEnabled(true);
            String[] systemNamesArray = new String[systemNamesList.size()];
            systemNamesArray = systemNamesList.toArray(systemNamesArray);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, systemNamesArray);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mSystemsSpinner.setAdapter(adapter);
            mSystemsSpinner.setOnItemSelectedListener(itemSelectedListener);
        } else {
            mSystemsSpinner.setEnabled(false);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle("No Systems");
            }
        }
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

    private void signOutFirebase() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getApplicationContext(), "Signed out successfully",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    @Override
    public void onBackPressed() {
        showLogoutDialog();
    }

    private void showLogoutDialog() {
        // Create an instance of the dialog fragment and show it
        logoutDialogFragment.show(getSupportFragmentManager(), "LogoutDialogFragment");
    }

    public int getCurrentSystemIndex() {
        return currentSystemIndex;
    }

    public static class MainAdapter extends FragmentPagerAdapter {
        // Tab titles
        private final String[] tabTitles = new String[]{"Status", "Control", "Limits"};

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
                case 2:
                    return ThresholdFragment.newInstance();
                default:
                    return null;
            }
        }
    }
}