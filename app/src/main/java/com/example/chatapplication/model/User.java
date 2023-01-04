package com.example.chatapplication.model;

import androidx.databinding.BindingAdapter;
import androidx.databinding.ObservableField;

import com.example.chatapplication.R;
import com.squareup.picasso.Picasso;

import java.io.Serializable;

import de.hdodenhof.circleimageview.CircleImageView;

public class User implements Serializable {
    public String name,image,email,fcmToken,userId;
    public Long available;
    @BindingAdapter("imageUrl")
    public static void loadImage(CircleImageView view, String imageUrl) {
        Picasso.get().load(imageUrl)
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(view);
    }

//    public User(String name, String image, String email, String fcmToken, String userId) {
//        this.name = name;
//        this.image = image;
//        this.email = email;
//        this.fcmToken = fcmToken;
//        this.userId = userId;
//    }


    public User(String name, String image, String email, String fcmToken, String userId, Long available) {
        this.name = name;
        this.image = image;
        this.email = email;
        this.fcmToken = fcmToken;
        this.userId = userId;
        this.available = available;
    }

    public User() {
    }


}
