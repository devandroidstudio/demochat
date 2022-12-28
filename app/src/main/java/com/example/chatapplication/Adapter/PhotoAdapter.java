package com.example.chatapplication.Adapter;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapplication.R;
import com.example.chatapplication.databinding.ItemPhotoBinding;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    List<Uri> urls = new ArrayList<>();
    Context context;

    public PhotoAdapter(List<Uri> urls, Context context) {
        this.urls = urls;
        this.context = context;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PhotoViewHolder(ItemPhotoBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        Uri image = urls.get(position);
        Picasso.get().load(image).placeholder(R.drawable.ic_launcher_foreground).into(holder.binding.imagePhoto);
        holder.binding.btnImageMinus.setOnClickListener(v -> {
            urls.remove(position);
            notifyItemRemoved(position);
            notifyDataSetChanged();
        });
        holder.binding.imagePhoto.setOnClickListener(new View.OnClickListener() {
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

    @Override
    public int getItemCount() {
        return urls.size();
    }

    public static class PhotoViewHolder extends RecyclerView.ViewHolder{
        private final ItemPhotoBinding binding;

        private PhotoViewHolder(@NonNull ItemPhotoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }
}
