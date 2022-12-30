package com.example.chatapplication.Fragment.Home;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
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
import com.example.chatapplication.R;
import com.example.chatapplication.Utils.Constants;
import com.example.chatapplication.Utils.PreferenceManager;
import com.example.chatapplication.Utils.ShowCameraGallery;
import com.example.chatapplication.Validation.Validation;
import com.example.chatapplication.databinding.FragmentProfileBinding;
import com.example.chatapplication.model.AccountViewModel;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;

import java.util.List;
import java.util.Objects;


public class ProfileFragment extends Fragment implements View.OnClickListener, ICallBackNewsListener {


    public ProfileFragment() {
        // Required empty public constructor
    }
    FragmentProfileBinding binding;
    private PreferenceManager preferenceManager;
    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private final ActivityResultLauncher<Intent> startForProfileImageResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK){
                if (result.getData() != null && user != null){
                    Uri uri = result.getData().getData();
                    preferenceManager.putString(Constants.KEY_IMAGE,uri.toString());
                    user.updateProfile(new UserProfileChangeRequest.Builder().setPhotoUri(uri).build()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                AccountViewModel.url.set(Objects.requireNonNull(user.getPhotoUrl()).toString());
                            }
                        }
                    });

                }
            }
        }
    });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater,container,false);
        preferenceManager = new PreferenceManager(requireContext());

//        binding.setName(preferenceManager.getString(Constants.KEY_NAME));
//        binding.setEmail(preferenceManager.getString(Constants.KEY_EMAIL));
//        binding.setUrl(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhotoUrl() == null ? preferenceManager.getString(Constants.KEY_IMAGE) : Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl()).toString());
        binding.layoutUsername.setOnClickListener(this);
        binding.layoutEmail.setOnClickListener(this);
        binding.layoutPass.setOnClickListener(this);

        onAction();
        getData();
        return binding.getRoot();
    }

    private void getData() {
        if (user != null){
            for (UserInfo profile : user.getProviderData()) {
                AccountViewModel.email.set(profile.getEmail());
                AccountViewModel.displayName.set(profile.getDisplayName());
                AccountViewModel.url.set(Objects.requireNonNull(profile.getPhotoUrl()).toString());
            }
        }
    }

    private void onAction() {
        binding.fabEditProfile.setOnClickListener(view ->{
            requestPermission();

        });
        binding.btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
        });
    }

    @Override
    public void onClick(View v) {
        View viewDialog = LayoutInflater.from(requireContext()).inflate(R.layout.layout_bottom_sheet_profile,null);
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(),R.style.BottomSheetTheme);
        bottomSheetDialog.setContentView(viewDialog);
        bottomSheetDialog.show();
        viewDialog.findViewById(R.id.btn_close_edit_profile).setOnClickListener(view ->{
            bottomSheetDialog.dismiss();
        });
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
            button.setOnClickListener(v1 -> {
                updateDisplayName(Objects.requireNonNull(textInputEditText.getText()).toString(),bottomSheetDialog);
            });
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
            button.setOnClickListener(v1 -> {
                updateEmail(Objects.requireNonNull(textInputEditText.getText()).toString());
            });
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
            button.setOnClickListener(v1 -> {
                updatePassword(Objects.requireNonNull(textInputEditText.getText()).toString());
            });
        }
    }
    private void updateDisplayName(String username, BottomSheetDialog bottomSheetDialog){
        if (user != null){
            if (username.equals(preferenceManager.getString(Constants.KEY_NAME))){
                Toast.makeText(requireContext(), "Display name not change", Toast.LENGTH_SHORT).show();
            }else {
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(username)
                        .build();
                user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            showDialogSuccess("username");
                            user.reload();
                            AccountViewModel.displayName.set(user.getDisplayName());
                            bottomSheetDialog.dismiss();
                            preferenceManager.putString(Constants.KEY_NAME,user.getDisplayName());
                        }else {
                            dialogUpdateProfile();
                        }
                    }
                });
            }
        }
    }
    private void updateEmail(String email){
        if (user != null){
            if (!Validation.ValidationEmail(email)){
                Toast.makeText(requireContext(), "Email is not valid", Toast.LENGTH_SHORT).show();
            }else if (email.equals(preferenceManager.getString(Constants.KEY_EMAIL))){
                Toast.makeText(requireContext(), "Email not change", Toast.LENGTH_SHORT).show();
            }else {
                user.updateEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            showDialogSuccess("email");
                            user.reload();
                            AccountViewModel.email.set(user.getEmail());
                            preferenceManager.putString(Constants.KEY_EMAIL,email);
                        }else {
                            dialogUpdateProfile();
                        }
                    }
                });
            }
        }
    }
    private void updatePassword(String password){
        if (user != null){
            if (!Validation.ValidationPassword(password)){

            }else {
                user.updatePassword(password).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            showDialogSuccess("password");
                            user.reload();
                        }else {
                            dialogUpdateProfile();
                        }
                    }
                });
            }
        }
    }

    private void showDialogSuccess(String type){
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.notification);
        builder.setMessage(String.format(type,"Update %d is success"));
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
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
        btnUpdateDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reAuthenticate(editEmail.getText().toString(),editPassword.getText().toString());
                dialog.dismiss();
            }
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
        AuthCredential credential = EmailAuthProvider
                .getCredential(strEmail, strPassword);
        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                        }
                    }
                });
    }
}