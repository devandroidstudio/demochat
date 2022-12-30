package com.example.chatapplication.Fragment.Home;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.chatapplication.Activity.ChatActivity;
import com.example.chatapplication.Activity.HomeActivity;
import com.example.chatapplication.Adapter.ContactAdapter;
import com.example.chatapplication.Listener.UserListener;

import com.example.chatapplication.Utils.Constants;

import com.example.chatapplication.databinding.FragmentContactBinding;
import com.example.chatapplication.model.User;
import com.example.chatapplication.model.UserViewModel;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public class ContactFragment extends Fragment implements UserListener {


    public ContactFragment() {
        // Required empty public constructor
    }

    FragmentContactBinding binding;
    private UserViewModel viewModel;
    private HomeActivity mainActivity;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentContactBinding.inflate(inflater,container,false);
        viewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        mainActivity = (HomeActivity) getActivity();
        getListUser();

        return binding.getRoot();
    }
    void getListUser(){
        viewModel.getListUsers().observe(getViewLifecycleOwner(),list -> {
           if (list.size() == 0){
               loading(true);
           }else {
               loading(false);
               ContactAdapter adapter = new ContactAdapter(list,this);
               binding.recycleViewContact.setAdapter(adapter);
               binding.recycleViewContact.setVisibility(View.VISIBLE);
               binding.recycleViewContact.setHasFixedSize(true);
               binding.recycleViewContact.setNestedScrollingEnabled(false);
               binding.recycleViewContact.setItemAnimator(new DefaultItemAnimator());
           }
        });

    }
    private void loading(@NonNull Boolean isLoading){
        if (isLoading){
            binding.progressContact.setVisibility(View.VISIBLE);
        }else {
            binding.progressContact.setVisibility(View.INVISIBLE);
        }
    }
    @Override
    public void outUserClicked(User user) {
        Intent intent = new Intent(mainActivity, ChatActivity.class);
        intent.putExtra(Constants.KEY_USER,user);
        startActivity(intent);

    }
}