package com.example.chatapplication.Utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.chatapplication.Listener.ICallBackNewsListener;
import com.example.chatapplication.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class ShowCameraGallery {
    public static void selectImageFromGallery(Context context, ICallBackNewsListener listener) {
        View viewDialog = LayoutInflater.from(context).inflate(R.layout.layout_bottom_sheet,null);
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context,R.style.BottomSheetTheme);
        bottomSheetDialog.setContentView(viewDialog);
        bottomSheetDialog.show();
        viewDialog.findViewById(R.id.btn_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onCallBackCamera();
                bottomSheetDialog.dismiss();
            }
        });
        viewDialog.findViewById(R.id.btn_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onCallBackGallery();
                bottomSheetDialog.dismiss();
            }
        });
        viewDialog.findViewById(R.id.btn_video).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onCallBackVideo();
                bottomSheetDialog.dismiss();
            }
        });
    }
}
