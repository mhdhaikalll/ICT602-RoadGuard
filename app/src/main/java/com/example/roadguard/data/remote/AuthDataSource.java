package com.example.roadguard.data.remote;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.roadguard.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class AuthDataSource {
    private final GoogleSignInClient googleSignInClient;
    private final FirebaseAuth auth;
    public Task<AuthResult> signUp(String email, String password) {
        return auth.createUserWithEmailAndPassword(email, password);
    }
    public Task<AuthResult> signIn(String email, String password) {
        return auth.signInWithEmailAndPassword(email, password);
    }
    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }
    public void signOut() {
        auth.signOut();
    }

    //Google
    public AuthDataSource(Context context) {
        this.auth = FirebaseAuth.getInstance();
        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id)) // set in strings.xml
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(context, gso);
    }

    public Intent getGoogleSignInIntent() {
        return googleSignInClient.getSignInIntent();
    }

    public Task<FirebaseUser> firebaseAuthWithGoogle(Intent data) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        return task.continueWithTask(t -> {
            if (!t.isSuccessful()) {
                Log.e("GOOGLE_SIGN_IN", "getSignedInAccountFromIntent failed", t.getException());
                throw t.getException();
            }
            GoogleSignInAccount account = t.getResult();
            Log.d("GOOGLE_SIGN_IN", "Account email: " + account.getEmail());
            AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
            return auth.signInWithCredential(credential);
        }).continueWith(t -> {
            if (!t.isSuccessful()) {
                Log.e("GOOGLE_SIGN_IN", "signInWithCredential failed", t.getException());
                throw t.getException();
            }
            Log.d("GOOGLE_SIGN_IN", "signInWithCredential succeeded");
            return t.getResult().getUser();
        });
    }

    // optional: sign out from Google too
    public void signOutGoogle() {
        googleSignInClient.signOut();
    }
}
