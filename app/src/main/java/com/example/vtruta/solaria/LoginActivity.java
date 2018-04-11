package com.example.vtruta.solaria;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.common.SignInButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Collections;
import java.util.List;

public class LoginActivity extends AppCompatActivity implements SystemDataRepo.OnDatabaseUpdateListener {

    private static final String TAG = "LoginActivity";
    private static int RC_SIGN_IN = 100;
    private SystemDataRepo dataRepo;
    private String userEmail;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SignInButton button = findViewById(R.id.sign_in_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFirebaseLoginUI();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            MyFirebaseInstanceIDService.sendRegistrationToServer(FirebaseInstanceId.getInstance().getToken());
            userEmail = currentUser.getEmail();
            signIn();
        }
    }

    private void openFirebaseLoginUI()
    {
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
                    signIn();
                }
            }
            else {
                Toast.makeText(this, "Connection refused.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void signIn()
    {
        dataRepo = SystemDataRepo.getInstance();
        dataRepo.initRepo();
        dataRepo.addOnDatabaseUpdateListener(this);
    }

    @Override
    public void onDatabaseUpdate() {
        dataRepo.removeOnDatabaseUpdateListener(this);
        Toast.makeText(this, "Signed in as " + userEmail, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
