package com.example.chatapplication.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.chatapplication.R;
import com.example.chatapplication.Utils.CheckConnection;
import com.example.chatapplication.Utils.Constants;
import com.example.chatapplication.Utils.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;

public class BaseActivity extends AppCompatActivity {

    private DocumentReference documentReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (CheckConnection.haveNetworkConnection(this)){
            PreferenceManager preferenceManager = new PreferenceManager(this);
            FirebaseFirestore database = FirebaseFirestore.getInstance();
            documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                    .document(preferenceManager.getString(Constants.KEY_USER_ID));
        }else {
            CheckConnection.ShowToast_Short(this);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (CheckConnection.haveNetworkConnection(this)){
            SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            String date = format.format(new Date());
            documentReference.update(Constants.KEY_AVAILABILITY,0);
            documentReference.update(Constants.KEY_DATE,date);
        }else {
            CheckConnection.ShowToast_Short(this);
        }
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (CheckConnection.haveNetworkConnection(this)){
//            SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//            String date = format.format(new Date());
//            documentReference.update(Constants.KEY_AVAILABILITY,0);
//            documentReference.update(Constants.KEY_DATE,date);
//        }else {
//            CheckConnection.ShowToast_Short(this);
//        }
//    }

    @Override
    protected void onResume() {
        super.onResume();
       if (CheckConnection.haveNetworkConnection(this)){
           documentReference.update(Constants.KEY_AVAILABILITY,1);
           documentReference.update(Constants.KEY_DATE, FieldValue.delete());
       }else {
           CheckConnection.ShowToast_Short(this);
       }
    }
}