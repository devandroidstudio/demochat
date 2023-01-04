package com.example.chatapplication.Adapter;

import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapplication.R;
import com.example.chatapplication.databinding.LayoutBottomSheetPhotoViewBinding;
import com.example.chatapplication.databinding.LayoutBottomSheetVideoViewBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class DetailPhotoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<String> list = new ArrayList<>();
    public static final int VIEW_TYPE_PHOTO = 1;
    public static final int VIEW_TYPE_VIDEO = 2;
    public static final String KEY_MP4 = ".jpg";
    public interface ISendListener{
        void ISendData(Boolean check);
    }
    private static ISendListener listener;
    public DetailPhotoAdapter(List<String> list, ISendListener listener) {
        this.list = list;
        DetailPhotoAdapter.listener = listener;
    }



    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_PHOTO){
            return new DetailPhotoViewHolder(LayoutBottomSheetPhotoViewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }else {
            return new DetailVideoHolder(LayoutBottomSheetVideoViewBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_PHOTO){
            ((DetailPhotoViewHolder) holder).setData(list.get(position));
        }else {
            ((DetailVideoHolder) holder).setData(list.get(position));
        }
    }

    @Override
    public int getItemViewType(int position) {
        String strUrl = list.get(position);
        if (strUrl.contains(KEY_MP4)){
            return VIEW_TYPE_PHOTO;
        }
        else {
            return VIEW_TYPE_VIDEO;
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class DetailPhotoViewHolder extends RecyclerView.ViewHolder{
        private final LayoutBottomSheetPhotoViewBinding binding;

        public DetailPhotoViewHolder(LayoutBottomSheetPhotoViewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        void setData(String data){
            Picasso.get().load(data).placeholder(R.drawable.ic_launcher_foreground).into(binding.photoView);
        }
    }
    public static class DetailVideoHolder extends RecyclerView.ViewHolder{
        private final LayoutBottomSheetVideoViewBinding binding;

        public DetailVideoHolder(LayoutBottomSheetVideoViewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        void setData(String data){
            binding.videoView.setVideoPath(data);
            binding.videoView.start();

            MediaController mediaController = new MediaController(itemView.getContext());
            binding.videoView.setMediaController(mediaController);
            mediaController.setAnchorView(binding.videoView);
            binding.videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    binding.progressBar.setVisibility(View.INVISIBLE);
                    binding.videoView.start();
                    listener.ISendData(true);
                }
            });
            binding.videoView.requestFocus();
            binding.videoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (binding.videoView.isPlaying()){
                        binding.videoView.pause();
                    }else {
                        binding.videoView.start();
                    }
                }
            });
        }
    }
}
