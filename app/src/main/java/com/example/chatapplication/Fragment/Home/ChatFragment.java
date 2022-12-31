package com.example.chatapplication.Fragment.Home;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import com.example.chatapplication.Activity.ChatActivity;
import com.example.chatapplication.Activity.HomeActivity;
import com.example.chatapplication.Adapter.StatusAdapter;
import com.example.chatapplication.Adapter.RecentConversionAdapter;
import com.example.chatapplication.Listener.ConversionListener;
import com.example.chatapplication.Utils.Constants;
import com.example.chatapplication.Utils.PreferenceManager;
import com.example.chatapplication.databinding.FragmentChatBinding;
import com.example.chatapplication.model.ChatMessage;
import com.example.chatapplication.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;


public class ChatFragment extends Fragment implements ConversionListener {


    public ChatFragment() {
        // Required empty public constructor
    }

    FragmentChatBinding binding;
    private FirebaseUser user;
    private PreferenceManager preferenceManager;
    private List<ChatMessage> conversations;
    private RecentConversionAdapter conversionAdapter;
    private FirebaseFirestore database;
    private Integer count = 0;
    private Integer count2 = 0;
    private int valueAvailable = 1;
    private List<User> list;
    private StatusAdapter adapter;
    private HomeActivity homeActivity;
    private Boolean isReceiverAvailable = false;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        homeActivity = (HomeActivity) getActivity();
        binding = FragmentChatBinding.inflate(inflater,container,false);
        init();
        listenConversations();

        return binding.getRoot();
    }

    private void init(){
        conversations = new ArrayList<>();
        list = new ArrayList<>();
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(requireContext());
        conversionAdapter = new RecentConversionAdapter(conversations, this, preferenceManager);
        binding.conversationRecyclerView.setAdapter(conversionAdapter);

        binding.recycleViewStatusChatUser.setHasFixedSize(true);
        binding.conversationRecyclerView.setHasFixedSize(true);

        binding.conversationRecyclerView.setNestedScrollingEnabled(false);
        binding.recycleViewStatusChatUser.setNestedScrollingEnabled(false);
        binding.recycleViewStatusChatUser.setItemAnimator(new DefaultItemAnimator());
        binding.conversationRecyclerView.setItemAnimator(new DefaultItemAnimator());
        user = FirebaseAuth.getInstance().getCurrentUser();

    }
    private void listenConversations(){
        database.collection(Constants.KEY_COLLECTION_CONVERSATION)
                .whereEqualTo(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CONVERSATION)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_USERS)
                .addSnapshotListener(eventListenerUser);
    }
    private final EventListener<QuerySnapshot> eventListenerUser = (value, error) ->{
        if (error != null){
            return;
        }
        if (value != null){
            for (DocumentChange documentChange: value.getDocumentChanges()) {

                if (documentChange.getType() == DocumentChange.Type.ADDED){
                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                    if (currentUserId.equals(documentChange.getDocument().getId())){
                        continue;
                    }
                    User user = new User(documentChange.getDocument().getString(Constants.KEY_NAME),documentChange.getDocument().getString(Constants.KEY_IMAGE),documentChange.getDocument().getString(Constants.KEY_EMAIL),documentChange.getDocument().getString(Constants.KEY_FCM_TOKEN),documentChange.getDocument().getId());
                    valueAvailable = Objects.requireNonNull(documentChange.getDocument().getLong(Constants.KEY_AVAILABILITY)).intValue();
                    list.add(user);
                }
                else if (documentChange.getType() == DocumentChange.Type.MODIFIED){
                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                    if (currentUserId.equals(documentChange.getDocument().getId())){
                        continue;
                    }
                    User user = new User(documentChange.getDocument().getString(Constants.KEY_NAME),documentChange.getDocument().getString(Constants.KEY_IMAGE),documentChange.getDocument().getString(Constants.KEY_EMAIL),documentChange.getDocument().getString(Constants.KEY_FCM_TOKEN),documentChange.getDocument().getId());
                    valueAvailable = Objects.requireNonNull(documentChange.getDocument().getLong(Constants.KEY_AVAILABILITY)).intValue();
                    for (User users: list) {
                        if (users.userId.equals(user.userId)){
                            list.remove(users);
                            list.add(user);
                        }
                    }
                }

            }
            adapter = new StatusAdapter(list,requireContext(),valueAvailable, this::onConversionClicked);
            conversionAdapter.setAvailable(valueAvailable);
            adapter.notifyDataSetChanged();
            conversionAdapter.notifyDataSetChanged();
            binding.recycleViewStatusChatUser.setAdapter(adapter);
            binding.recycleViewStatusChatUser.setVisibility(View.VISIBLE);
        }
    };


    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error !=null){
            return;
        }
        if (value != null){
            for (DocumentChange documentChange: value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED){
                    String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = senderId;
                    chatMessage.receiverId = receiverId;
                    if (preferenceManager.getString(Constants.KEY_USER_ID).equals(senderId)){
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE);
                        chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME);
                        chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        count2++;
                    }else {
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE);
                        chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_SENDER_NAME);
                        chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);

                        System.out.println("Count2 sender:"+count2);
                    }
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    chatMessage.type = documentChange.getDocument().getString(Constants.KEY_TYPE);
                    conversations.add(chatMessage);
                } else if (documentChange.getType() == DocumentChange.Type.MODIFIED){
                    for (int i = 0; i < conversations.size(); i++) {
                        String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        if (conversations.get(i).senderId.equals(senderId) && conversations.get(i).receiverId.equals(receiverId)){
                            conversations.get(i).message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                            conversations.get(i).dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                            conversations.get(i).type = documentChange.getDocument().getString(Constants.KEY_TYPE);
                            break;
                        }

                    }

                }

            }
            count2++;
            System.out.println("Count2:"+count2);
            Toast.makeText(requireContext(), "abc"+count2, Toast.LENGTH_SHORT).show();
            Collections.sort(conversations,(Comparator.comparing(o -> o.dateObject)));
            conversionAdapter.notifyDataSetChanged();
            binding.conversationRecyclerView.smoothScrollToPosition(0);
            binding.conversationRecyclerView.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);
        }
    };
    @Override
    public void onConversionClicked(User user) {
        Intent intent = new Intent(homeActivity, ChatActivity.class);
        intent.putExtra(Constants.KEY_USER,user);
        startActivity(intent);
    }
}