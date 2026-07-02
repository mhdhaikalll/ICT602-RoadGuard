package com.example.roadguard.ui.auth;

import android.app.Application;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.roadguard.data.remote.FirestoreDataSource;
import com.example.roadguard.data.repository.AuthRepository;
import com.example.roadguard.model.User;
import com.google.firebase.auth.FirebaseUser;

public class AuthViewModel extends AndroidViewModel {
    private final AuthRepository authRepository;
    private final MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Intent> googleSignInIntent = new MutableLiveData<>();
    public AuthViewModel(@NonNull Application application) {
        super(application);
        authRepository = new AuthRepository(application);
    }
    public LiveData<Boolean> getLoginSuccess() { return loginSuccess; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public void login(String email, String password) {
        authRepository.signIn(email, password)
                .addOnSuccessListener(authResult -> {
                    // After successful login
                    FirestoreDataSource.updateFCMTokenForCurrentUser();
                    loginSuccess.setValue(true);
                })
                .addOnFailureListener(e -> {
                    errorMessage.setValue(e.getMessage());
                });
    }
    public void register(String email, String password, String displayName) {
        authRepository.signUp(email, password)
                .addOnSuccessListener(authResult -> {
                    // Create user profile in Firestore
                    FirebaseUser firebaseUser = authResult.getUser();
                    // After successful login
                    FirestoreDataSource.updateFCMTokenForCurrentUser();
                    if (firebaseUser != null) {
                        User user = new User(firebaseUser.getUid(), email, displayName);
                        user.setAlertsEnabled(true);
                        user.setNotificationRadiusKm(3);
                        user.setTtsEnabled(true);
                        authRepository.createUserProfile(user)
                                .addOnSuccessListener(aVoid -> loginSuccess.setValue(true))
                                .addOnFailureListener(e -> errorMessage.setValue(e.getMessage()));
                    }
                })
                .addOnFailureListener(e -> errorMessage.setValue(e.getMessage()));
    }
    public boolean isUserLoggedIn() {
        return authRepository.getCurrentUser() != null;
    }
    public void logout() {
        authRepository.signOut();
    }
    public LiveData<Intent> getGoogleSignInIntent() {
        return googleSignInIntent;
    }
    public void startGoogleSignIn() {
        Intent intent = authRepository.getGoogleSignInIntent();
        googleSignInIntent.setValue(intent);
    }
    public void handleGoogleSignInResult(Intent data) {
        authRepository.handleGoogleSignInResult(data)
                .addOnSuccessListener(user -> loginSuccess.setValue(true))
                .addOnFailureListener(e -> errorMessage.setValue(e.getMessage()));
    }
}
