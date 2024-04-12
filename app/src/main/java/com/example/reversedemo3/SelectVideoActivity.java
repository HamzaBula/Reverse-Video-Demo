package com.example.reversedemo3;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class SelectVideoActivity extends AppCompatActivity {


    Button selectedVideoBtn;
    private int STORAGE_PERMISSION_CODE = 1;
    Uri videoUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_video);

        selectedVideoBtn = findViewById(R.id.select_video);

        selectedVideoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                requestStoragePermission();
            }
        });
    }

    @SuppressLint("ObsoleteSdkInt")
    private void requestStoragePermission() {

        //condition for check permission in android version 13 or above
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {

             if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED) {

                System.out.println("permission already granted..............");
                openVideo();

            } else {
                System.out.println("request permission");
                request_permission_launcher_storage_videos.launch(android.Manifest.permission.READ_MEDIA_VIDEO);
            }
        }
        //condition for check permission in android version 12 or less than 12
        else
        {
            ActivityCompat.requestPermissions(SelectVideoActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    //request permission code for Android version 13 or above
    private ActivityResultLauncher<String> request_permission_launcher_storage_videos =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                    isGranted -> {

                        if (isGranted) {
                            openVideo();
                        }
                        else {
                            Toast.makeText(this, "Please grant us the necessary permission", Toast.LENGTH_SHORT).show();
                        }
            });

    //request permission code for Android version 12 or less than 12
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_PERMISSION_CODE) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                openVideo();
            }
        }
    }


    public void openVideo() {

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("video/*");
        startActivityForResult(intent, 100);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


         if (resultCode == Activity.RESULT_OK && requestCode == 100) {

                assert data != null;
                videoUri = data.getData();

                Intent intent = new Intent(SelectVideoActivity.this, StartProcessActivity.class);
                intent.putExtra("uri", videoUri.toString());
                startActivity(intent);
         }
    }

}