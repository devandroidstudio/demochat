package com.example.chatapplication.model;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;

public class AccountViewModel {
    public static ObservableField<String> email = new ObservableField<>();
    public static ObservableField<String> url = new ObservableField<>();
    public static ObservableField<String> displayName = new ObservableField<>();
    public static ObservableField<Integer> available = new ObservableField<>();
}
