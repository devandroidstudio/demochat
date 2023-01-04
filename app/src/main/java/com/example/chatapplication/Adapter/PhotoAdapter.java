package com.example.chatapplication.Adapter;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapplication.R;
import com.example.chatapplication.databinding.ItemPhotoBinding;
import com.example.chatapplication.databinding.ItemVideoBinding;
import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    List<Uri> urls = new ArrayList<>();
    Context context;
    public static final int VIEW_TYPE_IMAGE = 1;
    public static final int VIEW_TYPE_VIDEO = 2;
    public PhotoAdapter(List<Uri> urls, Context context) {
        this.urls = urls;
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        if (urls.get(position).toString().contains("video")){
            return VIEW_TYPE_VIDEO;
        }
        else {
            return VIEW_TYPE_IMAGE;
        }
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_IMAGE){
            return new PhotoViewHolder(ItemPhotoBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
        }else {
            return new VideoViewHolder(ItemVideoBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_IMAGE){
            ((PhotoViewHolder) holder).setData(urls.get(position));
        }else {
            ((VideoViewHolder) holder).setData(urls.get(position));
        }
    }
    @Override
    public int getItemCount() {
        return urls.size();
    }

    public class PhotoViewHolder extends RecyclerView.ViewHolder{
        private final ItemPhotoBinding binding;

        private PhotoViewHolder(@NonNull ItemPhotoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        void setData(Uri image){
            Picasso.get().load(image).placeholder(R.drawable.ic_launcher_foreground).into(binding.imagePhoto);
            binding.btnImageMinus.setOnClickListener(v -> {
                urls.remove(image);
                notifyDataSetChanged();
            });
            binding.imagePhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    View viewDialog = LayoutInflater.from(context).inflate(R.layout.layout_bottom_sheet_photo_view,null);
                    final Dialog dialog = new Dialog(context);
                    dialog.setContentView(viewDialog);
                    dialog.show();
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    PhotoView photoView = viewDialog.findViewById(R.id.photo_view);
                    photoView.setImageURI(image);
                }
            });
        }
    }

    public static class VideoViewHolder extends RecyclerView.ViewHolder{
        private final ItemVideoBinding binding;

        public VideoViewHolder(ItemVideoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        void setData(Uri video){
            binding.imagePhoto.setVideoURI(video);
            binding.imagePhoto.start();
            MediaController mediaController = new MediaController(itemView.getContext());
            binding.imagePhoto.setMediaController(mediaController);
            mediaController.setAnchorView(binding.imagePhoto);
            binding.imagePhoto.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    binding.imagePhoto.start();
                }
            });
            binding.imagePhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (binding.imagePhoto.isPlaying()){
                        binding.imagePhoto.pause();
                    }else {
                        binding.imagePhoto.start();
                    }
                }
            });
        }
    }

}
