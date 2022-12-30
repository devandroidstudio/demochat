package com.example.chatapplication.model;

import androidx.databinding.BindingAdapter;

import com.example.chatapplication.R;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
public class News implements Serializable {
    public String resourceUserPost, namePost,date,userId;
    public List<String> resource;


    public News(List<String> resource, String resourceUserPost, String namePost) {
        this.resourceUserPost = resourceUserPost;
        this.namePost = namePost;
        this.resource = resource;
    }

    public News(List<String> resource,String resourceUserPost, String namePost, String date, String userId) {
        this.resourceUserPost = resourceUserPost;
        this.namePost = namePost;
        this.date = date;
        this.resource = resource;
        this.userId = userId;
    }
    public Map<String,Object> updateNews(){
        Map<String,Object> result = new HashMap<>();
        result.put("resource",resource);
        return result;
    }
    @BindingAdapter("imageUrlNews")
    public static void loadImage(RoundedImageView view, String imageUrl) {
        Picasso.get().load(imageUrl)
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(view);
    }

    public News() {
    }

    @Override
    public String toString() {
        return "News{" +
                "resourceUserPost='" + resourceUserPost + '\'' +
                ", namePost='" + namePost + '\'' +
                ", resource=" + resource +
                '}';
    }
}
