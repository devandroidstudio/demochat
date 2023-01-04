package com.example.chatapplication.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapplication.Listener.UserListener;
import com.example.chatapplication.Utils.Constants;
import com.example.chatapplication.Utils.PreferenceManager;
import com.example.chatapplication.databinding.ItemStatusUsersBinding;
import com.example.chatapplication.model.TimeDifference;
import com.example.chatapplication.model.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class StatusAdapter extends RecyclerView.Adapter<StatusAdapter.StatusViewHolder> {
    List<User> list = new ArrayList<>();
    private final PreferenceManager preferenceManager;
    private final int available;
    private final UserListener userListener;
    public StatusAdapter(List<User> list, Context context, int available, UserListener userListener) {
        this.list = list;
        this.available = available;
        preferenceManager = new PreferenceManager(context);
        this.userListener = userListener;
    }

    @NonNull
    @Override
    public StatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new StatusViewHolder(ItemStatusUsersBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull StatusViewHolder holder, int position) {
        User user = list.get(position);
        holder.binding.setUser(user);
        if (user.available == 1){
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

        }
        holder.binding.getRoot().setOnClickListener(v->{
            userListener.outUserClicked(user);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class StatusViewHolder extends RecyclerView.ViewHolder{
        private final ItemStatusUsersBinding binding;

        public StatusViewHolder(ItemStatusUsersBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }
}
