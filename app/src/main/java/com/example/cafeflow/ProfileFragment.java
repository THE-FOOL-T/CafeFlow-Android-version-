package com.example.cafeflow;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private EditText profileNameEditText, profileEmailEditText, profilePasswordEditText;
    private Button updateProfileButton, logoutButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        profileNameEditText = view.findViewById(R.id.profileNameEditText);
        profileEmailEditText = view.findViewById(R.id.profileEmailEditText);
        profilePasswordEditText = view.findViewById(R.id.profilePasswordEditText);
        updateProfileButton = view.findViewById(R.id.updateProfileButton);
        logoutButton = view.findViewById(R.id.logoutButton);

        loadUserProfile();

        updateProfileButton.setOnClickListener(v -> updateProfile());
        logoutButton.setOnClickListener(v -> logout());

        return view;
    }

    private void loadUserProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    User currentUser = documentSnapshot.toObject(User.class);
                    profileNameEditText.setText(currentUser.getName());
                    profileEmailEditText.setText(currentUser.getEmail());
                }
            });
        }
    }

    private void updateProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String newName = profileNameEditText.getText().toString().trim();
            String newEmail = profileEmailEditText.getText().toString().trim();
            String newPassword = profilePasswordEditText.getText().toString().trim();

            if (!newName.isEmpty()) {
                db.collection("users").document(user.getUid()).update("name", newName);
            }
            if (!newEmail.isEmpty()) {
                user.updateEmail(newEmail);
                db.collection("users").document(user.getUid()).update("email", newEmail);
            }
            if (!newPassword.isEmpty()) {
                user.updatePassword(newPassword);
            }

            Toast.makeText(getContext(), "Profile Updated", Toast.LENGTH_SHORT).show();
        }
    }

    private void logout() {
        mAuth.signOut();
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}