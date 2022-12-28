package com.example.chatapplication.Adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapplication.databinding.LayoutBottomSheetPhotoViewBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class DetailPhotoAdapter extends RecyclerView.Adapter<DetailPhotoAdapter.DetailPhotoViewHolder> {
    private List<String> list = new ArrayList<>();

    public DetailPhotoAdapter(List<String> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public DetailPhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DetailPhotoViewHolder(LayoutBottomSheetPhotoViewBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull DetailPhotoViewHolder holder, int position) {
        Picasso.get().load(list.get(position)).into(holder.binding.photoView);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class DetailPhotoViewHolder extends RecyclerView.ViewHolder{
        private final LayoutBottomSheetPhotoViewBinding binding;

        public DetailPhotoViewHolder(LayoutBottomSheetPhotoViewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
