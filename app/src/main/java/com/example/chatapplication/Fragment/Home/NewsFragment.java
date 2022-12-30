package com.example.chatapplication.Fragment.Home;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.example.chatapplication.Adapter.NewsAdapter;
import com.example.chatapplication.Adapter.PhotoAdapter;
import com.example.chatapplication.Listener.ICallBackNewsListener;
import com.example.chatapplication.R;
import com.example.chatapplication.Transform.ZoomOutPageTransformer;
import com.example.chatapplication.Utils.Constants;
import com.example.chatapplication.Utils.FileExtension;
import com.example.chatapplication.Utils.PreferenceManager;
import com.example.chatapplication.Utils.ShowCameraGallery;
import com.example.chatapplication.databinding.FragmentNewsBinding;

import com.example.chatapplication.model.AccountViewModel;
import com.example.chatapplication.model.News;

import com.example.chatapplication.model.NewsViewModel;


import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.time.LocalDate;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class NewsFragment extends Fragment implements ICallBackNewsListener {


    public NewsFragment() {

    }



    FragmentNewsBinding binding;
    private NewsViewModel newsViewModel;
    private PreferenceManager preferenceManager;
    private static List<Uri> list;
    private DatabaseReference reference;
    private StorageReference storageReference;
    private static List<String> uris, listImageCurrent, listImageCurrent2;
    private ProgressDialog progressDialog;
    private String date;
    private  NewsAdapter adapter;
    private static boolean isExits = false;
    private static List<News> newsList;
    private static String strNews = "";
    private FirebaseUser user;
    private final ActivityResultLauncher<Intent> startForProfileImageResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK){
                if (result.getData() != null){
                    int count = result.getData().getClipData().getItemCount();
                    if (count != 0){
                        for (int i = 0; i < count; i++) {
                            list.add(0,result.getData().getClipData().getItemAt(i).getUri());
                        }
                    }
                    showDialogResult(list);

                }
            }
        }
    });
    private final ActivityResultLauncher<Intent> startForProfileImageResultCamera = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK){
                if (result.getData() != null){
                   list.add(0,result.getData().getData());
                    showDialogResult(list);

                }
            }
        }
    });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentNewsBinding.inflate(inflater,container,false);
        preferenceManager = new PreferenceManager(requireContext());
        list = new ArrayList<>();
        uris = new ArrayList<>();
        listImageCurrent = new ArrayList<>();
        listImageCurrent2 = new ArrayList<>();
        newsList = new ArrayList<>();
        newsViewModel = new ViewModelProvider(requireActivity()).get(NewsViewModel.class);
        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Uploading...");
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        date = format.format(new Date());
        reference = FirebaseDatabase.getInstance().getReference("News");
        storageReference = FirebaseStorage.getInstance().getReference("News");
        binding.rcvNews.setLayoutManager(new GridLayoutManager(requireContext(),2, LinearLayoutManager.VERTICAL,false));
        binding.rcvNews.setItemAnimator(new DefaultItemAnimator());
        binding.rcvNews.setHasFixedSize(true);
        AccountViewModel.url.set(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString());

        return binding.getRoot();
    }
    private void getData(){

        reference.child(LocalDate.now().toString()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (newsList != null){
                    newsList.clear();
                }
                for (DataSnapshot postSnapshot : snapshot.getChildren()){
                    News news = postSnapshot.getValue(News.class);
                    if (news != null && news.userId.equals(preferenceManager.getString(Constants.KEY_USER_ID))){
                        strNews = postSnapshot.getKey();

                    }
                    newsList.add(news);
                }
                if (newsList.size() == 0){
                    loading(true);
                }else {
                    loading(false);
                    binding.rcvNews.setVisibility(View.VISIBLE);
                    listImageCurrent2.add(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhotoUrl() == null ? preferenceManager.getString(Constants.KEY_IMAGE) : AccountViewModel.url.get());
                    newsList.add(0,new News(listImageCurrent2,"https://cdn-icons-png.flaticon.com/512/3024/3024515.png","Add a news"));
                    adapter = new NewsAdapter(newsList, getContext(), NewsFragment.this);
                    binding.rcvNews.setAdapter(adapter);
                    adapter.notifyItemRangeInserted(0,newsList.size());
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }
    private void loading(@NonNull Boolean isLoading){
        if (isLoading){
            binding.progressBar.setVisibility(View.VISIBLE);
        }else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onCallBackCamera() {
        ImagePicker.with(this)
                .cameraOnly()
                .crop()
                .createIntent(intent -> {
                    startForProfileImageResultCamera.launch(intent);
                    return null;
                });
    }

    @Override
    public void onCallBackGallery() {
        Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
        intent.setAction(Intent.ACTION_PICK);
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startForProfileImageResult.launch(intent);
    }

    private void showDialogResult(List<Uri> list){
        View viewDialog = LayoutInflater.from(requireContext()).inflate(R.layout.layout_bottom_sheet_upload_image,null);
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(),R.style.BottomSheetTheme);
        bottomSheetDialog.setContentView(viewDialog);
        bottomSheetDialog.show();
        ViewPager2 viewPager2 = viewDialog.findViewById(R.id.view_page_news);
        viewPager2.setPageTransformer(new ZoomOutPageTransformer());
        viewPager2.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        PhotoAdapter adapter = new PhotoAdapter(list, requireContext());
        viewPager2.setAdapter(adapter);
        viewDialog.findViewById(R.id.fab_close).setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            list.clear();
        });
        viewDialog.findViewById(R.id.btn_addition_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
                ShowCameraGallery.selectImageFromGallery(requireContext(), NewsFragment.this);
            }
        });
        viewDialog.findViewById(R.id.btn_upload_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println(strNews);
                reference.child(LocalDate.now().toString()).child(strNews).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            isExits = true;
                            News news = snapshot.getValue(News.class);
                            if (news != null){
                                listImageCurrent.addAll(news.resource);
                            }
                        }else {
                            isExits = false;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(requireContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

                progressDialog.show();
                for (int i = 0; i < list.size(); i++) {
                    final StorageReference fileRef = storageReference.child(System.currentTimeMillis()+"."+ FileExtension.getFileExtension(list.get(i),requireActivity()));
                    fileRef.putFile(list.get(i)).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()){
                                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        uris.add(String.valueOf(uri));
                                        if (uris.size() == list.size()){
                                            listImageCurrent.addAll(uris);
                                            System.out.println(listImageCurrent.toString());
                                            if (isExits){
                                                Map<String,Object> result = new HashMap<>();
                                                result.put("resource",listImageCurrent);
                                                result.put("date",date);
                                                reference.child(LocalDate.now().toString()).child(strNews).updateChildren(result, new DatabaseReference.CompletionListener() {
                                                    @Override
                                                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                                        Toast.makeText(requireContext(), "Success", Toast.LENGTH_SHORT).show();
                                                        progressDialog.dismiss();
                                                        bottomSheetDialog.dismiss();
                                                        listImageCurrent.clear();
                                                    }
                                                });
                                            }else {
                                                News news = new News(uris,AccountViewModel.url.get(), preferenceManager.getString(Constants.KEY_NAME), date, preferenceManager.getString(Constants.KEY_USER_ID));
                                                reference.keepSynced(true);
                                                reference.child(LocalDate.now().toString()).push().setValue(news).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            progressDialog.dismiss();
                                                            bottomSheetDialog.dismiss();
                                                            uris.clear();
                                                            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                                                            builder.setTitle(R.string.notification);
                                                            builder.setMessage(R.string.success);
                                                            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    dialog.dismiss();

                                                                }
                                                            });
                                                            AlertDialog alertDialog = builder.create();
                                                            alertDialog.show();
                                                        }
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }

                                        }
                                    }
                                });
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getData();
    }
}