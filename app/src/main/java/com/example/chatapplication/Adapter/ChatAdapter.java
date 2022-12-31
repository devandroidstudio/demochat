package com.example.chatapplication.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapplication.R;
import com.example.chatapplication.Utils.Constants;
import com.example.chatapplication.databinding.ItemContainerSentMessageBinding;
import com.example.chatapplication.databinding.ItemContainnerReceivedImageMessageBinding;
import com.example.chatapplication.databinding.ItemContainnerSentImageMessageBinding;
import com.example.chatapplication.databinding.ItemContanerReceivedMessageBinding;
import com.example.chatapplication.model.ChatMessage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    List<ChatMessage> list = new ArrayList<>();
    String image, senderId, type;
    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;
    public static Boolean check = false;
    public ChatAdapter(List<ChatMessage> list, String image, String senderId) {
        this.list = list;
        this.image = image;
        this.senderId = senderId;
    }

    public void setReceiverProfileImage(String image){
        this.image = image;
    }
    @Override
    public int getItemViewType(int position) {
        if (list.get(position).senderId.equals(senderId)){
            type = list.get(position).type;
            return VIEW_TYPE_SENT;
        }
        else {
            type = list.get(position).type;
            return VIEW_TYPE_RECEIVED;
        }

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT){
            if (type.equals(Constants.KEY_TEXT)){
                return new SendMessageViewHolder(ItemContainerSentMessageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
            }
            return new SendMessageImageViewHolder(ItemContainnerSentImageMessageBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
        }else {
            if (type.equals(Constants.KEY_TEXT)){
                return new ReceivedMessageViewHolder(ItemContanerReceivedMessageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
            }
            return new ReceivedMessageImageViewHolder(ItemContainnerReceivedImageMessageBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_SENT){
            if (type.equals(Constants.KEY_TEXT)){
                ((SendMessageViewHolder) holder).setData(list.get(position));
            }else if (type.equals(Constants.KEY_IMAGE)){
                ((SendMessageImageViewHolder) holder).setData(list.get(position));
            }
        }else {
            if (type.equals(Constants.KEY_TEXT)){
                ((ReceivedMessageViewHolder) holder).setData(list.get(position),image);
            }else if (type.equals(Constants.KEY_IMAGE)){
                ((ReceivedMessageImageViewHolder) holder).setData(list.get(position),image);
            }
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class SendMessageViewHolder extends RecyclerView.ViewHolder{
        private final ItemContainerSentMessageBinding binding;
        public SendMessageViewHolder(ItemContainerSentMessageBinding itemContainerSentMessageBinding) {
            super(itemContainerSentMessageBinding.getRoot());
            binding = itemContainerSentMessageBinding;
        }
        void setData(ChatMessage chatMessage){
            binding.txtMessage.setText(chatMessage.message);
            binding.txtDatetime.setText(chatMessage.dateTime);
            binding.txtMessage.setOnClickListener(v ->{
                check = !check;
                if (check){
                    binding.txtDatetime.setVisibility(View.VISIBLE);
                }else {
                    binding.txtDatetime.setVisibility(View.GONE);
                }
            });
        }
    }
    public static class SendMessageImageViewHolder extends RecyclerView.ViewHolder{
        private final ItemContainnerSentImageMessageBinding binding;

        public SendMessageImageViewHolder(ItemContainnerSentImageMessageBinding itemContainnerSentImageMessageBinding) {
            super(itemContainnerSentImageMessageBinding.getRoot());
            this.binding = itemContainnerSentImageMessageBinding;
        }
        void setData(ChatMessage chatMessage){
            Picasso.get().load(chatMessage.message).into(binding.imageMessage);
            binding.txtDatetime.setText(chatMessage.dateTime);
        }
    }

    public static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder{
        private final ItemContanerReceivedMessageBinding binding;

        public ReceivedMessageViewHolder(ItemContanerReceivedMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        void setData(ChatMessage chatMessage, String image){
            binding.txtMessage.setText(chatMessage.message);
            binding.txtDatetime.setText(chatMessage.dateTime);
            if (image != null){
                Picasso.get().load(image).placeholder(R.drawable.ic_launcher_foreground).into(binding.imageProfile);
            }
            binding.txtMessage.setOnClickListener(v -> {
                check = !check;
                if (check){
                    binding.txtDatetime.setVisibility(View.VISIBLE);
                }else {
                    binding.txtDatetime.setVisibility(View.GONE);
                }
            });
        }
    }
    public static class ReceivedMessageImageViewHolder extends RecyclerView.ViewHolder{
        private final ItemContainnerReceivedImageMessageBinding binding;

        public ReceivedMessageImageViewHolder(ItemContainnerReceivedImageMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        void setData(ChatMessage chatMessage, String image){
            Picasso.get().load(chatMessage.message).into(binding.imageMessage);
            binding.txtDatetime.setText(chatMessage.dateTime);
            if (image != null){
                Picasso.get().load(image).placeholder(R.drawable.ic_launcher_foreground).into(binding.imageProfile);
            }
        }
    }
}
