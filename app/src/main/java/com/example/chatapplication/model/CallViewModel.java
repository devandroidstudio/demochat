package com.example.chatapplication.model;

import android.content.Context;
import android.content.Intent;

import com.example.chatapplication.Activity.CallActivity;

public class CallViewModel {
    Context context;
    User receiveUser;
    long appID = 1279747161;
    String appSign = "1f3eb7ec3b7e780ddc5697b72b0103476a8f4e6191228729778023f2c3cd7061";


    public CallViewModel(Context context, User receiveUser) {
        this.context = context;
        this.receiveUser = receiveUser;
    }

    public void onClickVoiceCall(){
        if (receiveUser != null){
            String userID = receiveUser.userId;
            String userName = userID + "_Name";
            String callID = "test_call_id";
            Intent intent = new Intent(context, CallActivity.class);
            intent.putExtra("appID", appID);
            intent.putExtra("appSign", appSign);
            intent.putExtra("userID", userID);
            intent.putExtra("userName", userName);
            intent.putExtra("callID", callID);
            intent.putExtra("type","voice");
            context.startActivity(intent);
        }
    }
    public void onClickVideoCall(){
        if (receiveUser != null){
            String userID = receiveUser.userId;
            String userName = userID + "_Name";
            String callID = "test_call_id";
            Intent intent = new Intent(context, CallActivity.class);
            intent.putExtra("appID", appID);
            intent.putExtra("appSign", appSign);
            intent.putExtra("userID", userID);
            intent.putExtra("userName", userName);
            intent.putExtra("callID", callID);
            intent.putExtra("type","video");
            context.startActivity(intent);
        }
    }
}
