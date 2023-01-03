package com.example.chatapplication.model;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.example.chatapplication.Activity.HomeActivity;
import com.example.chatapplication.R;
import com.example.chatapplication.Utils.Constants;
import com.example.chatapplication.Utils.PreferenceManager;
import com.example.chatapplication.Validation.Validation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Objects;
import java.util.logging.Handler;

public class RootViewModel {
    private final Context context;
    private ProgressDialog progressDialog;
    private PreferenceManager preferenceManager;
    public RootViewModel(Context context) {
        this.context = context;
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Loading...");
        preferenceManager = new PreferenceManager(context);
    }

    public ObservableField<String> email = new ObservableField<>();
    public ObservableField<String> password = new ObservableField<>();
    public ObservableField<String> emailSignUp = new ObservableField<>();
    public ObservableField<String> passwordSignUp = new ObservableField<>();
    public ObservableField<String> nameSignUp = new ObservableField<>();

    public void onClickLogin(){
      progressDialog.show();
        if (Validation.ValidationPassword(password.get()) && Validation.ValidationEmail(email.get())) {
            FirebaseAuth.getInstance().signInWithEmailAndPassword(Objects.requireNonNull(email.get()), Objects.requireNonNull(password.get())).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        progressDialog.dismiss();
                        if (user !=null){
                            preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                            preferenceManager.putString(Constants.KEY_USER_ID, Objects.requireNonNull(user).getUid());
                            preferenceManager.putString(Constants.KEY_NAME,user.getDisplayName());
                            preferenceManager.putString(Constants.KEY_IMAGE,user.getPhotoUrl() == null ? "https://cdn-icons-png.flaticon.com/512/166/166538.png": user.getPhotoUrl().toString());
                            preferenceManager.putString(Constants.KEY_EMAIL,user.getEmail());
                            Intent intent = new Intent(context,HomeActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            context.startActivity(intent);
                        }
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage(e.getMessage())
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
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


    }

    public void onClickForgetPassword(){
        View viewForgetPassword = LayoutInflater.from(context).inflate(R.layout.dialog_forget_password,null);
        AlertDialog.Builder builder = new AlertDialog.Builder(viewForgetPassword.getContext());
        builder.setView(viewForgetPassword);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        TextInputEditText editText = viewForgetPassword.findViewById(R.id.text_edit_email_fg);
        TextInputLayout textInputLayout = viewForgetPassword.findViewById(R.id.edit_email_fg);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!Patterns.EMAIL_ADDRESS.matcher(s.toString()).matches()) {
                    textInputLayout.setError("Email is not matches (example:a@gmail.com)");
                }else {
                    textInputLayout.setErrorEnabled(false);
                    textInputLayout.setError(null);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() < 1){
                    textInputLayout.setError(null);
                    textInputLayout.setErrorEnabled(false);
                }
            }
        });

        viewForgetPassword.findViewById(R.id.btn_forget_password).setOnClickListener(v -> {
            if (Validation.ValidationEmail(Objects.requireNonNull(editText.getText()).toString())){
                FirebaseAuth.getInstance().sendPasswordResetEmail(Objects.requireNonNull(editText.getText()).toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(context, "Please check your email", Toast.LENGTH_SHORT).show();
                            alertDialog.dismiss();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        AlertDialog.Builder builderFP = new AlertDialog.Builder(viewForgetPassword.getContext());
                        builderFP.setMessage(e.getMessage());
                        builderFP.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        AlertDialog alertDialogFP = builderFP.create();
                        alertDialogFP.show();
                    }
                });
            }
            else {
                AlertDialog.Builder builderFP = new AlertDialog.Builder(viewForgetPassword.getContext());
                builderFP.setMessage("Please check your email");
                builderFP.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialogFP = builderFP.create();
                alertDialogFP.show();
            }
        });
    }
    public void onClickSignUp(){
       if (Validation.ValidationEmail(emailSignUp.get()) && Validation.ValidationPassword(passwordSignUp.get()) && !TextUtils.isEmpty(nameSignUp.get())){
           FirebaseAuth mAuth = FirebaseAuth.getInstance();
           FirebaseFirestore database = FirebaseFirestore.getInstance();
           mAuth.createUserWithEmailAndPassword(Objects.requireNonNull(emailSignUp.get()), Objects.requireNonNull(passwordSignUp.get())).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
               @Override
               public void onComplete(@NonNull Task<AuthResult> task) {
                   if (task.isSuccessful()){
                       FirebaseUser user = mAuth.getCurrentUser();
                       if (user != null){
                           user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                               @Override
                               public void onComplete(@NonNull Task<Void> task) {
                                   if (task.isSuccessful()){
                                       Toast.makeText(context, "Verification email has been sent", Toast.LENGTH_SHORT).show();
                                       dialogSuccess(user);
                                   }
                               }
                           }).addOnFailureListener(new OnFailureListener() {
                               @Override
                               public void onFailure(@NonNull Exception e) {
                                   Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                               }
                           });
                           user.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(nameSignUp.get()).build()).addOnCompleteListener(new OnCompleteListener<Void>() {
                               @Override
                               public void onComplete(@NonNull Task<Void> task) {
                                   if (task.isSuccessful()){
                                       preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                                       preferenceManager.putString(Constants.KEY_USER_ID, Objects.requireNonNull(user).getUid());
                                       preferenceManager.putString(Constants.KEY_NAME,user.getDisplayName());
                                       preferenceManager.putString(Constants.KEY_IMAGE, Objects.requireNonNull(user.getPhotoUrl()).toString());
                                       preferenceManager.putString(Constants.KEY_EMAIL,user.getEmail());
                                       HashMap<String,Object> data = new HashMap<>();
                                       data.put(Constants.KEY_EMAIL,user.getEmail());
                                       data.put(Constants.KEY_NAME,user.getDisplayName());
                                       data.put(Constants.KEY_IMAGE,user.getPhotoUrl() == null ? "https://cdn-icons-png.flaticon.com/512/166/166538.png" : user.getPhotoUrl());
                                       data.put(Constants.KEY_AVAILABILITY,0);
                                       database.collection(Constants.KEY_COLLECTION_USERS)
                                               .document(user.getUid())
                                               .set(data);
                                   }
                               }
                           });

                       }

                   }
               }
           }).addOnFailureListener(new OnFailureListener() {
               @Override
               public void onFailure(@NonNull Exception e) {
                   Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
               }
           });
       }else {
           Toast.makeText(context, "Please check your information", Toast.LENGTH_SHORT).show();
       }

    }
    private void dialogSuccess(FirebaseUser user){
        View viewModel = LayoutInflater.from(context).inflate(R.layout.dialog_verification,null);
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context,R.style.BottomSheetTheme);
        bottomSheetDialog.setContentView(viewModel);
        bottomSheetDialog.show();
        Button button = viewModel.findViewById(R.id.btn_verify_email);
        button.setOnClickListener(view ->{
            bottomSheetDialog.dismiss();
            context.startActivity(new Intent(context, HomeActivity.class));
        });
        viewModel.findViewById(R.id.tv_send_it_again).setOnClickListener(view ->{
            user.sendEmailVerification().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context, "Verification email has been sent", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }


}
