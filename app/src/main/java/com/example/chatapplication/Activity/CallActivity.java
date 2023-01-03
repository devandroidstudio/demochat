package com.example.chatapplication.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.chatapplication.R;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallFragment;

public class CallActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
        addFragment();
    }
    public void addFragment() {

        long appID = getIntent().getLongExtra("appID", 0L);
        String appSign = getIntent().getStringExtra("appSign");
        String userID = getIntent().getStringExtra("userID");
        String userName = getIntent().getStringExtra("userName");
        String callID = getIntent().getStringExtra("callID");
        String type = getIntent().getStringExtra("type");
        if (type.equals("voice")){
            ZegoUIKitPrebuiltCallConfig config = ZegoUIKitPrebuiltCallConfig.oneOnOneVoiceCall();
            ZegoUIKitPrebuiltCallFragment fragment = ZegoUIKitPrebuiltCallFragment.newInstance(appID, appSign, userID,
                    userName, callID, config);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commitNow();
        }else if (type.equals("video")){
            ZegoUIKitPrebuiltCallConfig config = ZegoUIKitPrebuiltCallConfig.oneOnOneVideoCall();
            ZegoUIKitPrebuiltCallFragment fragment = ZegoUIKitPrebuiltCallFragment.newInstance(appID, appSign, userID,
                    userName, callID, config);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commitNow();

        }

    }
}