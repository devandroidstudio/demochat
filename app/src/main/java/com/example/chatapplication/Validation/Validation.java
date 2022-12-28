package com.example.chatapplication.Validation;

import android.text.TextUtils;
import android.util.Patterns;

public class Validation {
    public static Boolean ValidationEmail(String email){
        return Patterns.EMAIL_ADDRESS.matcher(email).matches() && !TextUtils.isEmpty(email);
    }
    public static Boolean ValidationPassword(String password){
        return !TextUtils.isEmpty(password) && password.length() > 0;
    }
}
