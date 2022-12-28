package com.example.chatapplication.Utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;



public class CheckConnection {
    public static boolean haveNetworkConnection(Context context) {
       boolean haveConnectWifi = false;
       boolean haveConnectMobile = false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] networkInfos = cm.getAllNetworkInfo();
        for(NetworkInfo ni:networkInfos)
        {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"));
                if(ni.isConnected())
                    haveConnectWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectMobile = true;
        }
        return haveConnectWifi || haveConnectMobile;
    }
//    public static void ShowToast_Short(Context context, String note)
//    {
//        Toast.makeText(context, note, Toast.LENGTH_SHORT).show();
//    }

    public static void ShowToast_Short(Context context)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Please connect to the internet to proceed further").setCancelable(false).setPositiveButton("Connect", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                context.startActivity(intent);
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });
        builder.show();
    }

}
