package com.example.chatapplication.model;

import androidx.databinding.BindingAdapter;
import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.chatapplication.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserViewModel extends ViewModel {
    private final MutableLiveData<List<User>> listUsers = new MutableLiveData<>();

    public MutableLiveData<List<User>> getListUsers() {
        return listUsers;
    }

    public void setListUsers(List<User> list) {
        listUsers.setValue(list);
    }


}
