package com.example.chatapplication.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapplication.Adapter.HomeAdapter;
import com.example.chatapplication.Fragment.Home.ChatFragment;
import com.example.chatapplication.Fragment.Home.ContactFragment;
import com.example.chatapplication.Fragment.Home.NewsFragment;
import com.example.chatapplication.Fragment.Home.ProfileFragment;
import com.example.chatapplication.R;
import com.example.chatapplication.Transform.ZoomOutPageTransformer;
import com.example.chatapplication.Utils.Constants;
import com.example.chatapplication.Utils.PreferenceManager;
import com.example.chatapplication.databinding.ActivityHomeBinding;
import com.example.chatapplication.model.AccountViewModel;
import com.example.chatapplication.model.News;
import com.example.chatapplication.model.NewsViewModel;
import com.example.chatapplication.model.User;
import com.example.chatapplication.model.UserViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeActivity extends BaseActivity {
    ActivityHomeBinding binding;
    private static final float END_SCALE = 0.7f;
    private PreferenceManager preferenceManager;
    private FirebaseUser user;
    public static List<User> list = new ArrayList<>();
    private UserViewModel viewModel;
    private NewsViewModel newsViewModel;
    private DatabaseReference reference;
    private static List<News> listNews;
    private static final List<String> listImageCurrent = new ArrayList<>();
    private static Boolean isExits = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(binding.getRoot());
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null){
            Toast.makeText(this, account.getDisplayName()+account.getDisplayName()+account.getFamilyName(), Toast.LENGTH_SHORT).show();
        }
        init();
        getListDataUsers();
        getToken();
        ActionBar();

    }

    private void ActionBar() {
        setSupportActionBar(binding.toolbarHome);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,binding.drawerLayoutHome,binding.toolbarHome,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        binding.drawerLayoutHome.addDrawerListener(toggle);
        toggle.syncState();

        binding.navViewHome.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.nav_my_profile:
                        break;
                    case R.id.nav_qrCode:
                        showQrCodeDialog();
                        break;
                }
                binding.drawerLayoutHome.closeDrawer(GravityCompat.START);
                return true;
            }
        });
        binding.navViewHome.bringToFront();
        binding.navViewHome.setCheckedItem(R.id.nav_my_profile);
        animateNavigationDrawer();

    }

    private void showQrCodeDialog() {
        final Dialog dialogQrCode = new Dialog(this);
        dialogQrCode.setContentView(R.layout.layout_dialog_qrcode);
        Window window = dialogQrCode.getWindow();
        window.setLayout(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogQrCode.setCancelable(true);
        dialogQrCode.findViewById(R.id.btn_close_dialog_qrCode).setOnClickListener(v ->{
            dialogQrCode.dismiss();
        });
        ImageView imageView = dialogQrCode.findViewById(R.id.image_qrCode);
        dialogQrCode.show();
    }

    private void animateNavigationDrawer() {
        binding.drawerLayoutHome.setScrimColor(getResources().getColor(R.color.purple_200));
        binding.drawerLayoutHome.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                final float diffScaledOffset = slideOffset * (1 - END_SCALE);
                final float offsetScale = 1 - diffScaledOffset;
                binding.contentHome.setScaleX(offsetScale);
                binding.contentHome.setScaleY(offsetScale);
                final float xOffset = drawerView.getWidth() * slideOffset;
                final float xOffsetDiff = binding.contentHome.getWidth() * diffScaledOffset / 2;
                final float xTranslation = xOffset - xOffsetDiff;
                binding.contentHome.setTranslationX(xTranslation);
            }
            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });
    }

    void init(){
        preferenceManager = new PreferenceManager(this);
        user = FirebaseAuth.getInstance().getCurrentUser();
        TextView txtName =  binding.navViewHome.getHeaderView(0).findViewById(R.id.textView_name_user_nav_header);
        TextView txtEmail =  binding.navViewHome.getHeaderView(0).findViewById(R.id.textView_email_user_nav_header);
        CircleImageView imageView = binding.navViewHome.getHeaderView(0).findViewById(R.id.image_user_header);
        if (user != null) {
            for (UserInfo profile : user.getProviderData()) {
                txtName.setText(profile.getDisplayName());
                txtEmail.setText(profile.getEmail());
                AccountViewModel.url.set(profile.getPhotoUrl()== null ? preferenceManager.getString(Constants.KEY_IMAGE) : profile.getPhotoUrl().toString());
                AccountViewModel.displayName.set(profile.getDisplayName());
                AccountViewModel.email.set(profile.getEmail());
            }
        }

        Picasso.get().load(AccountViewModel.url.get()).placeholder(R.drawable.ic_baseline_person_pin_24).error(R.drawable.cool_background).into(imageView);
        viewModel = new ViewModelProvider(this).get(UserViewModel.class);
        newsViewModel = new ViewModelProvider(this).get(NewsViewModel.class);
        reference = FirebaseDatabase.getInstance().getReference("News");
        listNews = new ArrayList<>();
        binding.bottomHome.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.bottom_homes:
                        binding.viewPageHome.setCurrentItem(0);
                        binding.toolbarHome.setTitle(R.string.chat);
                        break;
                    case R.id.bottom_contact:
                        binding.viewPageHome.setCurrentItem(1);
                        binding.toolbarHome.setTitle(R.string.contact);
                        break;
                    case R.id.bottom_news:
                        binding.viewPageHome.setCurrentItem(2);
                        binding.toolbarHome.setTitle(R.string.news);
                        break;
                    case R.id.bottom_profile:
                        binding.viewPageHome.setCurrentItem(3);
                        binding.toolbarHome.setTitle(R.string.profile);
                        break;
                }
                return true;
            }
        });
        HomeAdapter adapter = new HomeAdapter(getSupportFragmentManager(),getLifecycle(),getListFragment());
        binding.viewPageHome.setAdapter(adapter);
        binding.viewPageHome.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        binding.viewPageHome.setPageTransformer(new ZoomOutPageTransformer());
        binding.viewPageHome.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                switch (position){
                    case 0:
                        binding.bottomHome.getMenu().findItem(R.id.bottom_homes).setChecked(true);
                        binding.toolbarHome.setTitle(R.string.chat);
                        break;
                    case 1:
                        binding.bottomHome.getMenu().findItem(R.id.bottom_contact).setChecked(true);
                        binding.toolbarHome.setTitle(R.string.contact);
                        break;
                    case 2:
                        binding.bottomHome.getMenu().findItem(R.id.bottom_news).setChecked(true);
                        binding.toolbarHome.setTitle(R.string.news);
                        break;
                    case 3:
                        binding.bottomHome.getMenu().findItem(R.id.bottom_profile).setChecked(true);
                        binding.toolbarHome.setTitle(R.string.profile);
                        break;
                }
                super.onPageSelected(position);

            }
        });

    }
    @NonNull
    private List<Fragment> getListFragment(){
        List<Fragment> list = new ArrayList<>();
        list.add(new ChatFragment());
        list.add(new ContactFragment());
        list.add(new NewsFragment());
        list.add(new ProfileFragment());
        return list;
    }

    private void getListDataUsers(){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                        if (task.isSuccessful()){
                            for (QueryDocumentSnapshot document: task.getResult()) {
                                User user = new User(document.getString(Constants.KEY_NAME),document.getString(Constants.KEY_IMAGE),document.getString(Constants.KEY_EMAIL),document.getString(Constants.KEY_FCM_TOKEN),document.getId());
                                list.add(user);
                            }
                            viewModel.setListUsers(list.stream().filter(x-> !Objects.equals(x.userId, currentUserId)).collect(Collectors.toList()));
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
                        builder.setMessage(e.getMessage());
                        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }
                });
    }

    private void getToken(){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }
    private void updateToken(String token){
        preferenceManager.putString(Constants.KEY_FCM_TOKEN,token);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .document(user.getUid())
                .update(Constants.KEY_FCM_TOKEN,token)
                .addOnSuccessListener(x -> Toast.makeText(this, "Token update successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(x-> Toast.makeText(this, "Unable to update token", Toast.LENGTH_SHORT).show());
    }

    private void addDataFirestore(){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String,Object> data = new HashMap<>();
        data.put(Constants.KEY_EMAIL,user.getEmail());
        data.put(Constants.KEY_NAME,user.getDisplayName());
        data.put(Constants.KEY_IMAGE,"https://cdn-icons-png.flaticon.com/512/166/166538.png");
        database.collection(Constants.KEY_COLLECTION_USERS)
                .document(user.getUid())
                .set(data)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(HomeActivity.this, "success", Toast.LENGTH_SHORT).show();
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                        preferenceManager.putString(Constants.KEY_NAME,user.getDisplayName());
                        preferenceManager.putString(Constants.KEY_USER_ID,user.getUid());
                        preferenceManager.putString(Constants.KEY_EMAIL,user.getEmail());

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(HomeActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    @Override
    protected void onStart() {
        super.onStart();
//        GoogleSignIn.getClient(this,new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build()).signOut();
//        FirebaseAuth.getInstance().signOut();
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayoutHome.isDrawerOpen(GravityCompat.START)){
            binding.drawerLayoutHome.closeDrawer(GravityCompat.START);
        }else if (binding.viewPageHome.getCurrentItem() == 0){
            super.onBackPressed();
        }else {
            binding.viewPageHome.setCurrentItem(binding.viewPageHome.getCurrentItem() - 1);
        }
    }
}