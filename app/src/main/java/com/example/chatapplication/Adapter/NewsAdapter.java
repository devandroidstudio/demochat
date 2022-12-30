package com.example.chatapplication.Adapter;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.chatapplication.Listener.ICallBackNewsListener;
import com.example.chatapplication.R;
import com.example.chatapplication.Transform.ZoomOutPageTransformer;
import com.example.chatapplication.Utils.Constants;
import com.example.chatapplication.Utils.PreferenceManager;
import com.example.chatapplication.Utils.ShowCameraGallery;
import com.example.chatapplication.databinding.ItemContainerNewsBinding;
import com.example.chatapplication.model.AccountViewModel;
import com.example.chatapplication.model.AccountViewModel2;
import com.example.chatapplication.model.News;
import com.example.chatapplication.model.TimeDifference;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.stream.Collectors;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {
    private List<News> list = new ArrayList<>();
    private final Context context;
    private int countCurrent = 0;
    private boolean isRunning = false;
    private final ICallBackNewsListener listener;
    private PreferenceManager preferenceManager;
    public NewsAdapter(List<News> list, Context context, ICallBackNewsListener listener) {
        this.list = list;
        this.context = context;
        this.listener = listener;
        this.preferenceManager = new PreferenceManager(context);
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NewsViewHolder(ItemContainerNewsBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        News news = list.get(position);
        AccountViewModel2 accountViewModel2 = new AccountViewModel2();
        accountViewModel2.displayName.set(news.namePost);
        accountViewModel2.urlImage.set(news.resource.get(0));
        accountViewModel2.urlUserPost.set(news.resourceUserPost);
        holder.binding.setNewsViewModel(accountViewModel2);
        holder.binding.setNumber(String.valueOf(news.resource.size()));

        if (position == 0){
            holder.binding.textNumberImage.setVisibility(View.GONE);
            holder.binding.getRoot().setOnClickListener(v->{
                requestPermission();
            });
            accountViewModel2.urlImage.set(AccountViewModel.url.get());
        } else if (news.userId.equals(preferenceManager.getString(Constants.KEY_USER_ID))){
            accountViewModel2.urlUserPost.set(AccountViewModel.url.get());
            accountViewModel2.displayName.set(AccountViewModel.displayName.get());
        }
        else {
            holder.binding.getRoot().setOnClickListener(view ->{
                View viewDialog = LayoutInflater.from(context).inflate(R.layout.layout_detail_bottom_sheet_news,null);
                final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context,R.style.BottomSheetTheme);
                bottomSheetDialog.setContentView(viewDialog);
                bottomSheetDialog.show();
                bottomSheetDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                RoundedImageView roundedImageView = viewDialog.findViewById(R.id.image_user_news);
                TextView textViewName = viewDialog.findViewById(R.id.text_view_name);
                TextView textViewTime = viewDialog.findViewById(R.id.text_view_times);
                ImageButton btnClose = viewDialog.findViewById(R.id.btn_close);
                Picasso.get().load(news.resourceUserPost).into(roundedImageView);
                textViewName.setText(news.namePost);
                System.out.println(news.date);
                textViewTime.setText(TimeDifference.findDateDiffStatus(news.date));
                btnClose.setOnClickListener(v->{
                    bottomSheetDialog.dismiss();
                });
                BottomSheetBehavior<View> bottomSheetBehavior = BottomSheetBehavior.from((View) viewDialog.getParent());
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                ViewPager2 viewPager2 = viewDialog.findViewById(R.id.view_page_detail_news);
                viewPager2.setAdapter(new DetailPhotoAdapter(news.resource));
                viewPager2.setPageTransformer(new ZoomOutPageTransformer());
                viewPager2.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
                LinearProgressIndicator linearProgressIndicator = viewDialog.findViewById(R.id.linear_progress);
                linearProgressIndicator.setMax(Constants.MAX_PROGRESS);
                Thread thread = new Thread(() -> {
                   isRunning = true;
                    while (isRunning){
                        try {
                            countCurrent += 1;
                            TimeUnit.MILLISECONDS.sleep(200);
                            linearProgressIndicator.setProgressCompat(countCurrent,true);
                            if (countCurrent == Constants.MAX_PROGRESS){
                                isRunning = false;
                                bottomSheetDialog.dismiss();
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                });
                thread.start();
                bottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        countCurrent = 0;
                        isRunning = false;
                        if (thread != null){
                            try {
                                thread.interrupt();
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                        System.out.println(countCurrent);
                    }
                });
            });

        }
    }

    private void requestPermission() {
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                ShowCameraGallery.selectImageFromGallery(context,listener);
            }

            @Override
            public void onPermissionDenied(@NonNull List<String> deniedPermissions) {
                Toast.makeText(context, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }
        };
        TedPermission.create()
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA)
                .check();
    }



    @Override
    public int getItemCount() {
        if (list.size() == 0){
            return 0;
        }
        return list.size();
    }

    public static class NewsViewHolder extends RecyclerView.ViewHolder{
        private final ItemContainerNewsBinding binding;

        public NewsViewHolder(@NonNull ItemContainerNewsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}
