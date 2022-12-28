package com.example.chatapplication.Fragment.Login;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.chatapplication.R;
import com.example.chatapplication.databinding.FragmentSignUpBinding;
import com.example.chatapplication.model.RootViewModel;


public class SignUpFragment extends Fragment {


    public SignUpFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentSignUpBinding binding = FragmentSignUpBinding.inflate(inflater,container,false);
        binding.setUser(new RootViewModel(requireContext()));
        binding.textEditEmailSignUp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!Patterns.EMAIL_ADDRESS.matcher(s.toString()).matches()){
                    binding.editEmailSignUp.setError("Email is not matches (example:a@gmail.com)");
                }else {
                    binding.editEmailSignUp.setErrorEnabled(false);
                    binding.editEmailSignUp.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() <1){
                    binding.editEmailSignUp.setErrorEnabled(false);
                    binding.editEmailSignUp.setError(null);
                }
            }
        });

        binding.textEditPasswordSignUp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() < binding.editPasswordSingUp.getCounterMaxLength()){
                    binding.editPasswordSingUp.setError("Max character is length >"+ binding.editPasswordSingUp.getCounterMaxLength());
                }else {
                    binding.editPasswordSingUp.setErrorEnabled(false);
                    binding.editPasswordSingUp.setError(null);
                }
            }
        });
        binding.textFullNameSignUp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 3){
                    binding.editFullNameSignUp.setError(null);
                    binding.editFullNameSignUp.setErrorEnabled(false);
                }else {
                    binding.editFullNameSignUp.setError("Length of full name > 3");
                }
            }
        });
        return binding.getRoot();
    }
}