package com.example.chatapplication;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.chatapplication.Activity.HomeActivity;
import com.example.chatapplication.Adapter.RootAdapter;
import com.example.chatapplication.Fragment.Login.SignInFragment;
import com.example.chatapplication.Fragment.Login.SignUpFragment;
import com.example.chatapplication.Transform.ZoomOutPageTransformer;
import com.example.chatapplication.Utils.Constants;
import com.example.chatapplication.Utils.PreferenceManager;
import com.example.chatapplication.databinding.ActivityMainBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    GoogleSignInClient googleSignInClient;
    GoogleSignInOptions gso;
    FirebaseAuth mAuth;
    ProgressDialog progressDialog;
    PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyTheme();
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.MATCH_PARENT);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        binding.setLifecycleOwner(this);
        mAuth = FirebaseAuth.getInstance();
        preferenceManager = new PreferenceManager(this);
        RootAdapter adapter = new RootAdapter(getSupportFragmentManager(),getLifecycle(),getListFragment());
        binding.viewPager2Root.setAdapter(adapter);
        binding.viewPager2Root.setPageTransformer(new ZoomOutPageTransformer());
        binding.viewPager2Root.setOffscreenPageLimit(1);
        binding.viewPager2Root.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        new TabLayoutMediator(binding.tabLayoutRoot, binding.viewPager2Root, (tab, position) -> {
            switch (position){
                case 0:
                    tab.setText(R.string.signin);
                    break;
                case 1:
                    tab.setText(R.string.sign_up);
                    break;
            }
        }).attach();
        progressDialog = new ProgressDialog(this);
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.api_token))
                .requestEmail().build();
        googleSignInClient = GoogleSignIn.getClient(this,gso);
        binding.btnGoogle.setOnClickListener(view ->{
            Intent signInIntent = googleSignInClient.getSignInIntent();
            getResult.launch(signInIntent);
        });
        setContentView(binding.getRoot());
    }
    ActivityResultLauncher<Intent> getResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            progressDialog.setMessage("Loading...");
            progressDialog.show();
            Task<GoogleSignInAccount> signInAccountTask=GoogleSignIn
                    .getSignedInAccountFromIntent(result.getData());
            if(signInAccountTask.isSuccessful())
            {
                String s="Google sign in successful";
                displayToast(s);
                try {
                    GoogleSignInAccount googleSignInAccount=signInAccountTask
                            .getResult(ApiException.class);
                    if(googleSignInAccount!=null)
                    {
                        AuthCredential authCredential= GoogleAuthProvider
                                .getCredential(googleSignInAccount.getIdToken()
                                        ,null);
                        mAuth.signInWithCredential(authCredential)
                                .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if(task.isSuccessful())
                                        {
                                            FirebaseFirestore database = FirebaseFirestore.getInstance();
                                            FirebaseUser user = task.getResult().getUser();
                                            if (user != null){
                                                preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                                                preferenceManager.putString(Constants.KEY_USER_ID, Objects.requireNonNull(user).getUid());
                                                preferenceManager.putString(Constants.KEY_NAME,user.getDisplayName());
                                                preferenceManager.putString(Constants.KEY_IMAGE, Objects.requireNonNull(user.getPhotoUrl()).toString());
                                                preferenceManager.putString(Constants.KEY_EMAIL,user.getEmail());
                                                HashMap<String,Object> data = new HashMap<>();
                                                data.put(Constants.KEY_EMAIL,user.getEmail());
                                                data.put(Constants.KEY_NAME,user.getDisplayName());
                                                data.put(Constants.KEY_IMAGE,user.getPhotoUrl().toString());
                                                data.put(Constants.KEY_AVAILABILITY,0);
                                                database.collection(Constants.KEY_COLLECTION_USERS)
                                                        .document(user.getUid())
                                                        .set(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                startActivity(new Intent(MainActivity.this
                                                                        ,HomeActivity.class)
                                                                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                                                displayToast("Firebase authentication successful");
                                                                progressDialog.dismiss();
                                                            }
                                                        });

                                            }


                                        }
                                        else
                                        {
                                            displayToast("Authentication Failed :"+ Objects.requireNonNull(task.getException())
                                                    .getMessage());
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
                                        builder.setMessage(e.getMessage())
                                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                    }
                                                });
                                    }
                                });
                    }
                }
                catch (ApiException e)
                {
                    e.printStackTrace();
                }
            }
        }
    });
    private void displayToast(String s) {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();
    }
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (user == null && account == null){
            startActivity(getIntent());
        }else {
            startActivity(new Intent(MainActivity.this,HomeActivity.class));
            finishAffinity();
        }
    }

    private List<Fragment> getListFragment(){
        List<Fragment> list = new ArrayList<>();
        list.add(new SignInFragment());
        list.add(new SignUpFragment());
        return list;
    }
    private void applyTheme(){
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES){
            setTheme(R.style.Theme_ChatApplication);
        }else {
            setTheme(R.style.Theme_Dark);
        }
    }

}