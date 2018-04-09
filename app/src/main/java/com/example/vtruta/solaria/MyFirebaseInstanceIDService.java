package com.example.vtruta.solaria;

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
    private static DatabaseReference database;
    private static ValueEventListener mListener;

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(refreshedToken);
    }

    public static void sendRegistrationToServer(final String refreshedToken) {
        final String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null)
            return;

        database = FirebaseDatabase.getInstance().getReference().child("tokens");
        mListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(uid))
                {
                    String currentToken = String.valueOf(dataSnapshot.child(uid).child("android").getValue());
                    if (!currentToken.equals(refreshedToken)) {
                        database.child(uid).child("android").setValue(refreshedToken);
                    }
                } else {
                    database.child(uid).child("android").setValue(refreshedToken);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        database.addListenerForSingleValueEvent(mListener);
    }
}
