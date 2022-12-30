package com.example.chatapplication.model;

import androidx.databinding.ObservableField;

public class AccountViewModel2 {
    public ObservableField<String> urlUserPost = new ObservableField<>();
    public ObservableField<String> displayName = new ObservableField<>();
    public ObservableField<String> urlImage = new ObservableField<>();
    public static ObservableField<String> url = new ObservableField<>();

    public AccountViewModel2() {
        urlUserPost.set(url.get());
    }
}
