package com.example.APBook.presentation.fragments.projects;

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
import android.widget.EditText;
import android.widget.Toast;

import com.example.APBook.R;
import com.example.APBook.data.retrofit.ImageBBInstance;
import com.example.APBook.data.retrofit.repositories.PostsRepository;
import com.example.APBook.domain.models.PostModelRequest;
import com.example.APBook.domain.models.PostResponseModel;
import com.example.APBook.domain.models.PhotoModel;
import com.example.APBook.domain.models.projects.ProjectModel;
import com.example.APBook.domain.models.UploadResponse;
import com.google.android.material.textfield.TextInputLayout;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class AddPostFragment extends Fragment {

    TextInputLayout uploadPhotoEdit;
    List<PhotoModel> photos = new ArrayList<>();
    ProjectModel projectModel;

    public AddPostFragment(ProjectModel projectModel) {
        this.projectModel = projectModel;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_post_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextInputLayout editDescr = getView().findViewById(R.id.description_edit);
        uploadPhotoEdit = getView().findViewById(R.id.upload_post_photo);
        Button button = getView().findViewById(R.id.create_post_button);
        uploadPhotoEdit.getEditText().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageChooser();
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photos.add(new PhotoModel(uploadPhotoEdit.getEditText().getText().toString()));
                PostModelRequest post = new PostModelRequest(editDescr.getEditText().getText().toString(), photos);
                Call<PostResponseModel> call = new PostsRepository().addPost(projectModel.id, post);
                call.enqueue(new Callback<PostResponseModel>() {
                    @Override
                    public void onResponse(Call<PostResponseModel> call, Response<PostResponseModel> response) {
                        MyProjectInfoFragment myProjectInfoFragment = new MyProjectInfoFragment(projectModel);
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.flFragment, myProjectInfoFragment)
                                .commit();
                    }

                    @Override
                    public void onFailure(Call<PostResponseModel> call, Throwable t) {
                        Toast.makeText(getContext(), "Проверьте подключение к сети", Toast.LENGTH_SHORT).show();
                    }
                });
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
                String encodeImage = encodeImageTo(selectedImageBitmap);
                Toast.makeText(getContext(), "Подождите пока появится ссылка", Toast.LENGTH_SHORT).show();
                Call<UploadResponse> call = ImageBBInstance.getApiService().uploadImage(ImageBBInstance.API_KEY, encodeImage);
                call.enqueue(new Callback<UploadResponse>() {
                    @Override
                    public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                        if (response.body() != null) {
                            String uploadResponse = response.body().getData().getUrl();
                            Log.d("MyLog", uploadResponse);
                            uploadPhotoEdit.getEditText().setText(uploadResponse);
                        }
                        else Toast.makeText(getContext(), "Попробуйте еще раз", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Call<UploadResponse> call, Throwable t) {
                        Log.d("MyLog", t.toString());
                        Toast.makeText(getContext(), "Что-то пошло не так", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    });

    private String encodeImageTo(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }
}