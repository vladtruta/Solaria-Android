package com.example.vtruta.solaria.database;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseInstanceIDSvc";

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        // Send the new InstanceID Token to the Server
        sendRegistrationToServer(refreshedToken);
    }

    public static void sendRegistrationToServer(final String refreshedToken) {
        final String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null)
            return;

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("tokens");
        if (databaseReference == null)
            return;
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(uid)) {
                    String currentToken = String.valueOf(dataSnapshot.child(uid).child("android").getValue());
                    if (!currentToken.equals(refreshedToken)) {
                        databaseReference.child(uid).child("android").setValue(refreshedToken);
                    }
                } else {
                    databaseReference.child(uid).child("android").setValue(refreshedToken);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        databaseReference.addListenerForSingleValueEvent(valueEventListener);
    }
}
