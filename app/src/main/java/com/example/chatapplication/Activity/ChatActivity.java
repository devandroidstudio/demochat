package com.example.chatapplication.Activity;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.chatapplication.Adapter.ChatAdapter;
import com.example.chatapplication.Fragment.Home.ProfileFragment;
import com.example.chatapplication.Listener.ICallBackNewsListener;
import com.example.chatapplication.Network.ApiClient;
import com.example.chatapplication.Network.ApiService;
import com.example.chatapplication.R;
import com.example.chatapplication.Utils.Constants;
import com.example.chatapplication.Utils.FileExtension;
import com.example.chatapplication.Utils.PreferenceManager;
import com.example.chatapplication.Utils.ShowCameraGallery;
import com.example.chatapplication.databinding.ActivityChatBinding;
import com.example.chatapplication.model.ChatMessage;
import com.example.chatapplication.model.TimeDifference;
import com.example.chatapplication.model.User;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends BaseActivity implements ICallBackNewsListener {
    private ActivityChatBinding binding;
    private User receiverUser;
    private List<ChatMessage> chatMessages;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private String conversionId = null;
    private Boolean isReceiverAvailable = false;
    private ChatAdapter adapter;
    private String time = "";
    private static final StorageReference storageReference = FirebaseStorage.getInstance().getReference("Images");
    private final ActivityResultLauncher<Intent> startForProfileImageResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK){
                if (result.getData() != null){
                    Uri uri = result.getData().getData();
                    final StorageReference fileRef = storageReference.child(System.currentTimeMillis()+"."+ FileExtension.getFileExtension(uri,ChatActivity.this));
                    fileRef.putFile(result.getData().getData()).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()){
                                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        sendMessage(Constants.KEY_IMAGE,uri.toString());
                                    }
                                });
                            }
                        }
                    }).addOnFailureListener(e -> Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());

                }
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.MATCH_PARENT);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        loadReceiverDetails();
        init();
        listenMessages();
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.layoutSend.setOnClickListener(v-> {
          if (binding.inputMessage.getText().length() >0){
              sendMessage(Constants.KEY_TEXT,binding.inputMessage.getText().toString());
          }else {
              return;
          }
        } );
        binding.btnImageChat.setOnClickListener(v -> {
            ShowCameraGallery.selectImageFromGallery(ChatActivity.this,this);
        });
    }

    private void init(){
        preferenceManager = new PreferenceManager(this);
        database = FirebaseFirestore.getInstance();
        chatMessages = new ArrayList<>();
        adapter = new ChatAdapter(chatMessages,receiverUser.image,preferenceManager.getString(Constants.KEY_USER_ID));
        binding.chatRecycleView.setHasFixedSize(true);
        binding.chatRecycleView.setAdapter(adapter);
        binding.chatRecycleView.smoothScrollToPosition(chatMessages.size());
        
    }
    private void listenAvailabilityOfReceiver(){
        database.collection(Constants.KEY_COLLECTION_USERS)
                .document(receiverUser.userId)
                .addSnapshotListener(ChatActivity.this, (value, error) -> {
                    if (error != null){
                        return;
                    }
                    if (value != null){
                        if (value.getLong(Constants.KEY_AVAILABILITY) != null){
                            int availability = Objects.requireNonNull(value.getLong(Constants.KEY_AVAILABILITY)).intValue();
                            isReceiverAvailable = availability == 1;
                        }
                        receiverUser.fcmToken = value.getString(Constants.KEY_FCM_TOKEN);
                        if (receiverUser.image == null){
                            receiverUser.image = value.getString(Constants.KEY_IMAGE);
                            adapter.setReceiverProfileImage(receiverUser.image);
                            adapter.notifyItemRangeChanged(0,chatMessages.size());
                        }
                        Picasso.get().load(value.getString(Constants.KEY_IMAGE)).into(binding.imageChatUser);
                        time = value.getString(Constants.KEY_DATE);
                    }
                    if (isReceiverAvailable){
                        binding.txtAvailability.setVisibility(View.VISIBLE);
                        binding.viewStatusChat.setVisibility(View.VISIBLE);
                        binding.textViewStatusChatUser.setVisibility(View.GONE);
                        preferenceManager.remove(Constants.KEY_DATE);
                    }else {
                        binding.txtAvailability.setVisibility(View.GONE);
                        binding.viewStatusChat.setVisibility(View.GONE);
                        binding.textViewStatusChatUser.setVisibility(View.VISIBLE);
                        if (preferenceManager.getString(Constants.KEY_DATE) == null){
                            preferenceManager.putString(Constants.KEY_DATE,time);
                        }
                        binding.textViewStatusChatUser.setText(TimeDifference.findDateDiff(preferenceManager.getString(Constants.KEY_DATE)));
                    }
                });
    }

    private void listenMessages(){
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID,receiverUser.userId)
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID,receiverUser.userId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null){
            int count = chatMessages.size();
            for (DocumentChange documentChange: value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED){
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chatMessage.receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.dateTime = getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    chatMessage.type = documentChange.getDocument().getString(Constants.KEY_TYPE);
                    chatMessages.add(chatMessage);
                }
            }
            Collections.sort(chatMessages, (o1, o2) -> o1.dateObject.compareTo(o2.dateObject));
            if (count == 0){
                adapter.notifyDataSetChanged();
            }
            else {
                adapter.notifyItemRangeInserted(chatMessages.size(),chatMessages.size());
                binding.chatRecycleView.smoothScrollToPosition(chatMessages.size() - 1);
            }
            binding.chatRecycleView.setVisibility(View.VISIBLE);
        }
        binding.progressBar.setVisibility(View.GONE);
        if (conversionId == null){
            checkForConversion();
        }
    };
    private void sendMessage(String type, String inputMessage){
        HashMap<String,Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID,receiverUser.userId);
        message.put(Constants.KEY_MESSAGE,inputMessage);
        message.put(Constants.KEY_TIMESTAMP, new Date());
        message.put(Constants.KEY_TYPE,type);
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
        if (conversionId !=null){
            updateConversion(inputMessage, type);
        }else {
            HashMap<String,Object> conversion = new HashMap<>();
            conversion.put(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER_ID));
            conversion.put(Constants.KEY_SENDER_NAME,preferenceManager.getString(Constants.KEY_NAME));
            conversion.put(Constants.KEY_SENDER_IMAGE,preferenceManager.getString(Constants.KEY_IMAGE));
            conversion.put(Constants.KEY_RECEIVER_ID,receiverUser.userId);
            conversion.put(Constants.KEY_RECEIVER_NAME,receiverUser.name);
            conversion.put(Constants.KEY_RECEIVER_IMAGE,receiverUser.image);
            conversion.put(Constants.KEY_LAST_MESSAGE,binding.inputMessage.getText().toString());
            conversion.put(Constants.KEY_TIMESTAMP,new Date());
            conversion.put(Constants.KEY_TYPE,type);
            addConversion(conversion);
        }
        if (!isReceiverAvailable){
            try {
                JSONArray tokens = new JSONArray();
                tokens.put(receiverUser.fcmToken);

                JSONObject data = new JSONObject();
                data.put(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                data.put(Constants.KEY_NAME, preferenceManager.getString(Constants.KEY_NAME));
                data.put(Constants.KEY_FCM_TOKEN,preferenceManager.getString(Constants.KEY_FCM_TOKEN));
                data.put(Constants.KEY_MESSAGE,binding.inputMessage.getText().toString());
                JSONObject body = new JSONObject();
                body.put(Constants.REMOTE_MSG_DATA,data);
                body.put(Constants.REMOTE_MSG_REGISTRATION_IDS,tokens);
                sendNotification(body.toString());
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        binding.inputMessage.setText(null);
    }
    private void sendNotification(String messageBody){
        ApiClient.getClient().create(ApiService.class).sendMessage(Constants.getRemoteMsgHeaders(),messageBody).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()){
                    try {
                        if (response.body() != null){
                            JSONObject responseJson = new JSONObject(response.body());
                            JSONArray results = responseJson.getJSONArray("results");
                            if (responseJson.getInt("failure") == 1){
                                JSONObject error = (JSONObject) results.get(0);
                                Toast.makeText(ChatActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        Toast.makeText(ChatActivity.this, "Notification sent successfully", Toast.LENGTH_SHORT).show();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }else {
                    Toast.makeText(ChatActivity.this, "Error: "+response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Toast.makeText(ChatActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void loadReceiverDetails(){
        receiverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.textName.setText(receiverUser.name);

//        Toast.makeText(this, receiverUser.userId, Toast.LENGTH_SHORT).show();
    }
    private String getReadableDateTime(Date date){
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);

    }
    private void addConversion(HashMap<String,Object> conversion){
        database.collection(Constants.KEY_COLLECTION_CONVERSATION)
                .add(conversion)
                .addOnSuccessListener(documentReference -> conversionId = documentReference.getId());
    }
    private void updateConversion(String message, String type){
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_CONVERSATION).document(conversionId);
        documentReference.update(Constants.KEY_LAST_MESSAGE,message,Constants.KEY_TIMESTAMP,new Date(), Constants.KEY_TYPE,type);
    }
    private void checkForConversion(){
        if (chatMessages.size() !=0){
//            checkForConversionRemotely(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(),receiverUser.userId);
//            checkForConversionRemotely(receiverUser.userId,FirebaseAuth.getInstance().getCurrentUser().getUid());
            checkForConversionRemotely(preferenceManager.getString(Constants.KEY_USER_ID),receiverUser.userId);
            checkForConversionRemotely(receiverUser.userId,preferenceManager.getString(Constants.KEY_USER_ID));
        }
    }
    private void checkForConversionRemotely(String senderId,String receiverId){
        database.collection(Constants.KEY_COLLECTION_CONVERSATION)
                .whereEqualTo(Constants.KEY_SENDER_ID,senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,receiverId)
                .get()
                .addOnCompleteListener(conversionOnCompleteListener);
    }
    private final OnCompleteListener<QuerySnapshot> conversionOnCompleteListener = task -> {
        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0){
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversionId = documentSnapshot.getId();
        }
    };


    @Override
    protected void onResume() {
        super.onResume();
        listenAvailabilityOfReceiver();
    }

    @Override
    public void onCallBackCamera() {
        ImagePicker.with(this)
                .cameraOnly()
                .crop()
                .createIntent(intent -> {
                    startForProfileImageResult.launch(intent);
                    return null;
                });
    }

    @Override
    public void onCallBackGallery() {
        ImagePicker.with(this)
                .galleryOnly()
                .crop()
                .createIntent(intent -> {
                    startForProfileImageResult.launch(intent);
                    return null;
                });
    }
}