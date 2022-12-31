package com.example.chatapplication.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapplication.Listener.ConversionListener;
import com.example.chatapplication.R;
import com.example.chatapplication.Utils.Constants;
import com.example.chatapplication.Utils.PreferenceManager;
import com.example.chatapplication.databinding.ItemContainerRecentConversionBinding;
import com.example.chatapplication.model.ChatMessage;
import com.example.chatapplication.model.TimeDifference;
import com.example.chatapplication.model.User;
import com.squareup.picasso.Picasso;

import java.util.List;

public class RecentConversionAdapter extends RecyclerView.Adapter<RecentConversionAdapter.ConversionViewHolder> {
    private final List<ChatMessage> list;
    private final ConversionListener conversionListener;
    private final PreferenceManager preferenceManager;
    private Integer available;
    public void setAvailable(Integer available) {
        this.available = available;
    }

    public RecentConversionAdapter(List<ChatMessage> list, ConversionListener conversionListener, PreferenceManager preferenceManager) {
        this.list = list;
        this.conversionListener = conversionListener;
        this.preferenceManager = preferenceManager;
    }

    @NonNull
    @Override
    public ConversionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversionViewHolder(ItemContainerRecentConversionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ConversionViewHolder holder, int position) {
        ChatMessage chatMessage = list.get(position);
        holder.binding.setConversation(chatMessage);
        holder.binding.setType(Constants.KEY_IMAGE);
        if (chatMessage.senderId.equals(preferenceManager.getString(Constants.KEY_USER_ID))){
            holder.binding.setText("You sent a image");
        }
        else {
            holder.binding.setText("You received a image");
        }
        holder.binding.getRoot().setOnClickListener(v-> {
            User user = new User();
            user.userId = chatMessage.conversionId;
            user.name = chatMessage.conversionName;
            user.image = chatMessage.conversionImage;
            conversionListener.onConversionClicked(user);
        });
        if (available == 1){
            holder.binding.viewStatusChat.setVisibility(View.VISIBLE);
            holder.binding.viewStatusOffStatus.setVisibility(View.GONE);
        }else {
            holder.binding.viewStatusOffStatus.setVisibility(View.VISIBLE);
            holder.binding.viewStatusChat.setVisibility(View.GONE);
            if (TimeDifference.findDateDiffStatus(preferenceManager.getString(Constants.KEY_DATE)).equals("24h")){
                holder.binding.viewStatusOffStatus.setVisibility(View.INVISIBLE);
            }else {
                holder.binding.viewStatusOffStatus.setText(TimeDifference.findDateDiffStatus(preferenceManager.getString(Constants.KEY_DATE)));
            }
            holder.binding.viewStatusOffStatus.setText(TimeDifference.findDateDiffStatus(preferenceManager.getString(Constants.KEY_DATE)));
        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ConversionViewHolder extends RecyclerView.ViewHolder{
       private final ItemContainerRecentConversionBinding binding;

        public ConversionViewHolder(ItemContainerRecentConversionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
