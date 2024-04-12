package com.example.reversedemo3;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

public class ProcessedVideoActivity extends AppCompatActivity {

    private static final String FILEPATH = "outputStringPath";
    VideoView processedVideoview;
    String filePath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_processed_video);

        processedVideoview = findViewById(R.id.processed_videoView);
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(processedVideoview);

        filePath = getIntent().getStringExtra(FILEPATH);

        if (filePath != null) {
            processedVideoview.setVideoURI(Uri.parse(filePath));
            processedVideoview.setMediaController(mediaController);
            processedVideoview.start();
        }
    }
}