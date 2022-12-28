package com.example.chatapplication.Fragment.Login;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.example.chatapplication.databinding.FragmentSignInBinding;
import com.example.chatapplication.model.RootViewModel;


public class SignInFragment extends Fragment {



    public SignInFragment() {
        // Required empty public constructor
    }

    FragmentSignInBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSignInBinding.inflate(inflater,container,false);
        binding.setUser(new RootViewModel(requireContext()));
        binding.textEditEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!Patterns.EMAIL_ADDRESS.matcher(s.toString()).matches()){
                    binding.editEmailLogin.setError("Email is not matches (example:a@gmail.com)");
                }else {
                    binding.editEmailLogin.setErrorEnabled(false);
                    binding.editEmailLogin.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() <1){
                    binding.editEmailLogin.setErrorEnabled(false);
                    binding.editEmailLogin.setError(null);
                }
            }
        });
        binding.textEditPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() < binding.editPasswordLogin.getCounterMaxLength()){
                    binding.editPasswordLogin.setError("Max character is length >"+ binding.editPasswordLogin.getCounterMaxLength());
                }else {
                    binding.editPasswordLogin.setErrorEnabled(false);
                    binding.editPasswordLogin.setError(null);
                }
            }
        });

        return binding.getRoot();
    }

}