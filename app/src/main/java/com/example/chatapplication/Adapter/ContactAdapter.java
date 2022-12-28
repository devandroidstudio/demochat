package com.example.chatapplication.Adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapplication.Listener.UserListener;
import com.example.chatapplication.databinding.ItemContactUserBinding;
import com.example.chatapplication.model.User;

import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {
    List<User> list = new ArrayList<>();
    private final UserListener userListener;
    public ContactAdapter(List<User> list, UserListener userListener) {
        this.list = list;
        this.userListener = userListener;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ContactViewHolder(ItemContactUserBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        User user = list.get(position);
        holder.binding.setUsers(user);
        holder.binding.getRoot().setOnClickListener(v-> userListener.outUserClicked(user));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder{
        private final ItemContactUserBinding binding;

        public ContactViewHolder(ItemContactUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }
}
