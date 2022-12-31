package com.example.chatapplication.Fragment.Home;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.chatapplication.Listener.ICallBackNewsListener;
import com.example.chatapplication.MainActivity;
import com.example.chatapplication.R;
import com.example.chatapplication.Utils.Constants;
import com.example.chatapplication.Utils.FileExtension;
import com.example.chatapplication.Utils.PreferenceManager;
import com.example.chatapplication.Utils.ShowCameraGallery;
import com.example.chatapplication.Validation.Validation;
import com.example.chatapplication.databinding.FragmentProfileBinding;
import com.example.chatapplication.model.AccountViewModel;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class ProfileFragment extends Fragment implements View.OnClickListener, ICallBackNewsListener {


    public ProfileFragment() {
        // Required empty public constructor
    }
    FragmentProfileBinding binding;
    private PreferenceManager preferenceManager;
    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private final FirebaseFirestore database = FirebaseFirestore.getInstance();
    private static final Map<String, Object> updateDatabase = new HashMap<>();
    private static final StorageReference storageReference = FirebaseStorage.getInstance().getReference("Profile");
    private ProgressDialog progressDialog;
    private final ActivityResultLauncher<Intent> startForProfileImageResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK){
                if (result.getData() != null){
                    Uri uri = result.getData().getData();
                    final StorageReference fileRef = storageReference.child(System.currentTimeMillis()+"."+ FileExtension.getFileExtension(uri,requireActivity()));
                    fileRef.putFile(result.getData().getData()).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()){
                                fileRef.getDownloadUrl().addOnSuccessListener(ProfileFragment.this::updatePhoto);
                            }
                        }
                    }).addOnFailureListener(e -> Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show());

                }
            }
        }
    });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater,container,false);
        preferenceManager = new PreferenceManager(requireContext());
        binding.layoutUsername.setOnClickListener(this);
        binding.layoutEmail.setOnClickListener(this);
        binding.layoutPass.setOnClickListener(this);
        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Uploading...");
        onAction();
        return binding.getRoot();
    }

    private void onAction() {
        binding.fabEditProfile.setOnClickListener(view -> requestPermission());
        binding.btnLogout.setOnClickListener(v -> signOut());
    }
    private void signOut(){
      if (user != null){
          Toast.makeText(requireContext(), "Signing out...", Toast.LENGTH_SHORT).show();
          HashMap<String,Object> updates = new HashMap<>();
          updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
          FirebaseFirestore database = FirebaseFirestore.getInstance();
          database.collection(Constants.KEY_COLLECTION_USERS)
                  .document(user.getUid())
                  .update(updates)
                  .addOnSuccessListener(new OnSuccessListener<Void>() {
                      @Override
                      public void onSuccess(Void unused) {
                          FirebaseAuth.getInstance().signOut();
                          startActivity(new Intent(requireContext(), MainActivity.class));
                          requireActivity().finish();
                      }
                  }).addOnFailureListener(new OnFailureListener() {
                      @Override
                      public void onFailure(@NonNull Exception e) {
                          Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                      }
                  });
      }


    }

    @Override
    public void onClick(View v) {
        View viewDialog = LayoutInflater.from(requireContext()).inflate(R.layout.layout_bottom_sheet_profile,null);
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(),R.style.BottomSheetTheme);
        bottomSheetDialog.setContentView(viewDialog);
        bottomSheetDialog.show();
        viewDialog.findViewById(R.id.btn_close_edit_profile).setOnClickListener(view -> bottomSheetDialog.dismiss());
        AppCompatButton button = viewDialog.findViewById(R.id.btn_save);
        TextInputLayout textInputLayout;
        if (v.getId() == R.id.layout_username){
            textInputLayout = viewDialog.findViewById(R.id.edit_full_name_profile);
            textInputLayout.setVisibility(View.VISIBLE);
            TextInputEditText textInputEditText = viewDialog.findViewById(R.id.text_fullName_profile);
            textInputEditText.setText(AccountViewModel.displayName.get());
            textInputEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() > 3){
                        textInputLayout.setError(null);
                        textInputLayout.setErrorEnabled(false);
                    }else {
                        textInputLayout.setError("Length of full name > 3");
                    }
                }
            });
            button.setOnClickListener(v1 -> updateDisplayName(Objects.requireNonNull(textInputEditText.getText()).toString(),bottomSheetDialog));
        }else if (v.getId() == R.id.layout_email){
            textInputLayout = viewDialog.findViewById(R.id.edit_email_profile);
            textInputLayout.setVisibility(View.VISIBLE);
            TextInputEditText textInputEditText = viewDialog.findViewById(R.id.text_edit_email_profile);
            textInputEditText.setText(AccountViewModel.email.get());
            textInputEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (!Patterns.EMAIL_ADDRESS.matcher(s.toString()).matches()){
                        textInputLayout.setError("Email is not matches (example:a@gmail.com)");
                    }else {
                        textInputLayout.setErrorEnabled(false);
                        textInputLayout.setError(null);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() <1){
                        textInputLayout.setErrorEnabled(false);
                        textInputLayout.setError(null);
                    }
                }
            });
            button.setOnClickListener(v1 -> updateEmail(Objects.requireNonNull(textInputEditText.getText()).toString()));
        }else if (v.getId() == R.id.layout_pass){
            textInputLayout = viewDialog.findViewById(R.id.edit_password_profile);
            textInputLayout.setVisibility(View.VISIBLE);
            TextInputEditText textInputEditText = viewDialog.findViewById(R.id.text_edit_password_profile);
            textInputEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() < textInputLayout.getCounterMaxLength()){
                        textInputLayout.setError("Max character is length >"+ textInputLayout.getCounterMaxLength());
                    }else {
                        textInputLayout.setErrorEnabled(false);
                        textInputLayout.setError(null);
                    }
                }
            });
            button.setOnClickListener(v1 -> updatePassword(Objects.requireNonNull(textInputEditText.getText()).toString()));
        }
    }
    private void updateDisplayName(String username, BottomSheetDialog bottomSheetDialog){
        progressDialog.show();
        if (user != null){
            if (username.equals(preferenceManager.getString(Constants.KEY_NAME))){
                showDialogSuccess("Display name not change");
            }else {
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(username)
                        .build();
                user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        showDialogSuccess("Update username is success");
                        user.reload();
                        AccountViewModel.displayName.set(user.getDisplayName());
                        bottomSheetDialog.dismiss();
                        preferenceManager.putString(Constants.KEY_NAME,user.getDisplayName());
                        updateDatabase.put(Constants.KEY_NAME,username);
                        database.collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID)).update(updateDatabase);
                    }else {
                        dialogUpdateProfile();
                    }
                });

            }
        }
    }
    private void updatePhoto(Uri uri){
        progressDialog.show();
        preferenceManager.putString(Constants.KEY_IMAGE,uri.toString());
        if (user != null){
            user.updateProfile(new UserProfileChangeRequest.Builder().setPhotoUri(uri).build()).addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    showDialogSuccess("Update photo is success");
                    AccountViewModel.url.set(Objects.requireNonNull(user.getPhotoUrl()).toString());
                    updateDatabase.put(Constants.KEY_IMAGE,uri.toString());
                    database.collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID)).update(updateDatabase);
                }
            });
        }
    }
    private void updateEmail(String email){
        progressDialog.show();
        if (user != null){
            if (!Validation.ValidationEmail(email)){
                showDialogSuccess("Email is not valid");
            }else if (email.equals(preferenceManager.getString(Constants.KEY_EMAIL))){
                showDialogSuccess("Email not change");
            }else {
                user.updateEmail(email).addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        showDialogSuccess("Update email is success");
                        AccountViewModel.email.set(user.getEmail());
                        preferenceManager.putString(Constants.KEY_EMAIL,email);
                        updateDatabase.put(Constants.KEY_EMAIL,email);
                        database.collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID)).update(updateDatabase);
                    }else {
                        dialogUpdateProfile();
                    }
                });
            }
        }
    }
    private void updatePassword(String password){
        if (user != null){
            if (!Validation.ValidationPassword(password)){
                Toast.makeText(requireContext(), "Password is valid", Toast.LENGTH_SHORT).show();
            }else {
                user.updatePassword(password).addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        showDialogSuccess("Update password is success");
                    }else {
                        dialogUpdateProfile();
                    }
                });
            }
        }
    }

    private void showDialogSuccess(String type){
      if (user != null){
          progressDialog.dismiss();
          AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
          builder.setTitle(R.string.notification);
          builder.setMessage(type);
          builder.setPositiveButton(R.string.ok, (dialog, which) -> dialog.dismiss());
          user.reload();
          AlertDialog alertDialog = builder.create();
          alertDialog.show();
      }
    }


    private void requestPermission() {
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                ShowCameraGallery.selectImageFromGallery(requireContext(),ProfileFragment.this);
            }

            @Override
            public void onPermissionDenied(@NonNull List<String> deniedPermissions) {
                Toast.makeText(requireContext(), "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }
        };
        TedPermission.create()
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA)
                .check();
    }
    private void dialogUpdateProfile(){
        final Dialog dialog = new Dialog(getActivity());
        dialog.setTitle("Please enter information login");
        dialog.setContentView(R.layout.custom_dialog_profile);
        Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);

        EditText editEmail = dialog.findViewById(R.id.edit_email_dialog);
        EditText editPassword = dialog.findViewById(R.id.edit_password_dialog);
        Button btnUpdateDialog = dialog.findViewById(R.id.btn_continues_dialog);
        dialog.show();
        btnUpdateDialog.setOnClickListener(v -> {
            reAuthenticate(editEmail.getText().toString(),editPassword.getText().toString());
            dialog.dismiss();
        });
    }
    @Override
    public void onCallBackCamera() {
        ImagePicker.with(this)
                .cameraOnly()
                .crop()
                .createIntent(intent -> {
                    startForProfileImageResult.launch(intent);
                    return null;
                });
    }

    @Override
    public void onCallBackGallery() {
        ImagePicker.with(this)
                .galleryOnly()
                .crop()
                .createIntent(intent -> {
                    startForProfileImageResult.launch(intent);
                    return null;
                });
    }
    public void reAuthenticate(String strEmail, String strPassword){
      if (user != null){
          AuthCredential credential = EmailAuthProvider
                  .getCredential(strEmail, strPassword);
          user.reauthenticate(credential)
                  .addOnCompleteListener(task -> {
                      if (task.isSuccessful()){
                          updateEmail(strEmail);
                          showDialogSuccess("success");
                      }
                  });
      }
    }
}