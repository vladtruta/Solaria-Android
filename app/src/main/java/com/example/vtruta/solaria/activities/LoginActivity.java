package com.example.vtruta.solaria.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.example.vtruta.solaria.R;
import com.example.vtruta.solaria.database.MyFirebaseInstanceIDService;
import com.example.vtruta.solaria.database.SystemDataRepo;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.common.SignInButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Collections;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 100;

    private String userEmail;
    private boolean sensorDataLoaded;
    private boolean sensorThresholdLoaded;

    private SystemDataRepo.OnDatabaseUpdateListener databaseUpdateListener;
    private SystemDataRepo.OnThresholdUpdateValuesListener thresholdUpdateValuesListener;

    private SignInButton mSignInButton;

    private SystemDataRepo systemDataRepo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loadFields();
        setListeners();
    }

    @Override
    public void onStart() {
        super.onStart();
        sensorDataLoaded = false;
        sensorThresholdLoaded = false;
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            MyFirebaseInstanceIDService.sendRegistrationToServer(FirebaseInstanceId.getInstance().getToken());
            userEmail = currentUser.getEmail();
            startSignInProcedure();
        }
    }

    private void loadFields() {
        mSignInButton = findViewById(R.id.sign_in_button);
    }

    private void setListeners() {
        databaseUpdateListener = new SystemDataRepo.OnDatabaseUpdateListener() {
            @Override
            public void onDatabaseUpdate() {
                systemDataRepo.removeOnDatabaseUpdateListener(databaseUpdateListener);
                sensorDataLoaded = true;
                trySignIn();
            }
        };

        thresholdUpdateValuesListener = new SystemDataRepo.OnThresholdUpdateValuesListener() {
            @Override
            public void onThresholdUpdateValues() {
                systemDataRepo.setThresholdUpdateValuesListener(null);
                sensorThresholdLoaded = true;
                trySignIn();
            }
        };

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFirebaseLoginUI();
            }
        };
        mSignInButton.setOnClickListener(onClickListener);
    }

    private void openFirebaseLoginUI() {
        List<AuthUI.IdpConfig> providers = Collections.singletonList(
                new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build());
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setLogo(R.drawable.logo_crop)
                        .setTosUrl("https://superapp.example.com/terms-of-service.html")
                        .setPrivacyPolicyUrl("https://superapp.example.com/privacy-policy.html")
                        .build(),
                RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                // Successfully signed in
                if (response != null) {
                    MyFirebaseInstanceIDService.sendRegistrationToServer(FirebaseInstanceId.getInstance().getToken());
                    userEmail = response.getEmail();
                    startSignInProcedure();
                }
            } else {
                Toast.makeText(this, "Connection refused.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startSignInProcedure() {
        systemDataRepo = SystemDataRepo.getInstance();
        systemDataRepo.initRepo();
        systemDataRepo.addOnDatabaseUpdateListener(databaseUpdateListener);
        systemDataRepo.setThresholdUpdateValuesListener(thresholdUpdateValuesListener);
    }

    private void trySignIn() {
        if (sensorDataLoaded && sensorThresholdLoaded) {
            Toast.makeText(getApplicationContext(), "Signed in as " + userEmail,
                    Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }
    }
}
