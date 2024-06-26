package com.example.APBook.presentation.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.APBook.DownloadImageTask;
import com.example.APBook.R;
import com.example.APBook.data.retrofit.ImageBBInstance;
import com.example.APBook.data.retrofit.repositories.UsersRepository;
import com.example.APBook.domain.models.UploadResponse;
import com.example.APBook.domain.models.UserModel;
import com.example.APBook.Global;
import com.example.APBook.presentation.fragments.mainFragments.SettingsFragment;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangeUserDataFragment extends Fragment {

    UsersRepository usersRepository = new UsersRepository();

    ShapeableImageView imageView;
    TextInputLayout name, secName, ageEditText, photo;
    UserModel user;

    public ChangeUserDataFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Call<UserModel> call = new UsersRepository().getUserById(Global.userId);
        call.enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                if (response.body() != null) {
                    user = response.body();
                    loadData();
                } else {
                    Toast.makeText(getContext(), "Что-то пошло не так", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {
                Toast.makeText(getContext(), "Проверьте подключение к сети", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadData() {
        name.getEditText().setText(user.getFirstName());
        secName.getEditText().setText(user.getSecondName());
        ageEditText.getEditText().setText(String.valueOf(user.getAge()));
        new DownloadImageTask(imageView).execute(user.getPhoto());
        photo.getEditText().setText(user.getPhoto());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_change_user_data, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imageView = getView().findViewById(R.id.imageView1);
        name = getView().findViewById(R.id.editNameTextField);
        secName = getView().findViewById(R.id.editSecNameTextField);
        ageEditText = getView().findViewById(R.id.editAgeTF);
        photo = getView().findViewById(R.id.uploadPhotoButton);
        Button saveButton = getView().findViewById(R.id.save_button);

        photo.getEditText().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photo.getEditText().setText("");
                imageChooser();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nameStr = String.valueOf(name.getEditText().getText()).trim();
                String secNameStr = String.valueOf(secName.getEditText().getText()).trim();
                String ageStr = String.valueOf(ageEditText.getEditText().getText()).trim();
                String photoStr = String.valueOf(photo.getEditText().getText()).trim();

                if (!nameStr.isEmpty() && !secNameStr.isEmpty() && !ageStr.isEmpty()) {
                    UserModel userModel = new UserModel(Global.userId, Integer.parseInt(ageStr), user.getEmail(), user.getPassword(), nameStr, secNameStr, photoStr);
                    Call<UserModel> call = usersRepository.updateUserData(userModel);
                    call.enqueue(new Callback<UserModel>() {
                        @Override
                        public void onResponse(@NonNull Call<UserModel> call, @NonNull Response<UserModel> response) {
                            if (response.body() != null) {
                                DocumentReference docRef = FirebaseFirestore.getInstance()
                                        .collection("users")
                                        .document(FirebaseAuth.getInstance().getUid());
                                Map<String, Object> updates = new HashMap<>();
                                updates.put("photo", photoStr);
                                updates.put("first_name", nameStr);
                                updates.put("second_name", "");

                                docRef.update(updates)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d("MyLog", "Document successfully updated!");
                                            }

                                        });
                                SettingsFragment settingsFragment = new SettingsFragment();
                                getActivity().getSupportFragmentManager()
                                        .beginTransaction().replace(R.id.flFragment, settingsFragment).commit();
                            } else {
                                Toast.makeText(getContext(), "Что-то пошло не так", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<UserModel> call, Throwable t) {

                        }
                    });
                } else {
                    Toast.makeText(getContext(), "Проверьте введенные данные", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void imageChooser() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        launchSomeActivity.launch(i);
    }

    ActivityResultLauncher<Intent> launchSomeActivity = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            if (data != null && data.getData() != null) {
                Uri selectedImageUri = data.getData();
                Bitmap selectedImageBitmap = null;
                try {
                    selectedImageBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                imageView.setImageBitmap(selectedImageBitmap);
                String encodeImage = encodeImageTo(selectedImageBitmap);
                Toast.makeText(getContext(), "Подождите пока появится ссылка", Toast.LENGTH_LONG).show();
                Call<UploadResponse> call = ImageBBInstance.getApiService().uploadImage(ImageBBInstance.API_KEY, encodeImage);
                call.enqueue(new Callback<UploadResponse>() {
                    @Override
                    public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                        if (response.body() != null) {
                            UploadResponse uploadResponse = response.body();
                            Log.d("MyLog", uploadResponse.getData().getUrl());
                            photo.getEditText().setText(uploadResponse.getData().getUrl());
                        } else
                            Toast.makeText(getContext(), "Попробуйте еще раз", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Call<UploadResponse> call, Throwable t) {
                        Log.d("MyLog", t.toString());
                        Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    });

    private String encodeImageTo(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();
        String encImage = Base64.encodeToString(b, Base64.DEFAULT);

        return encImage;
    }

}