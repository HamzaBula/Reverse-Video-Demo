package com.example.reversedemo3;

import static android.content.ContentValues.TAG;
import static android.os.Environment.DIRECTORY_MOVIES;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFmpegKitConfig;
import com.arthenica.ffmpegkit.FFmpegSession;
import com.arthenica.ffmpegkit.FFmpegSessionCompleteCallback;
import com.arthenica.ffmpegkit.LogCallback;
import com.arthenica.ffmpegkit.ReturnCode;
import com.arthenica.ffmpegkit.SessionState;
import com.arthenica.ffmpegkit.Statistics;
import com.arthenica.ffmpegkit.StatisticsCallback;
import java.io.File;
import java.io.IOException;
import android.media.MediaMetadataRetriever;


public class MainActivity extends AppCompatActivity {

    Button processBtn;
    ProgressBar progressBar;
    String inputPath,outputPath;
    String randomString ="testing"+System.currentTimeMillis()/1000;
    long inputFileDuration = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        processBtn = findViewById(R.id.process_button);
        progressBar = findViewById(R.id.progressbar);

        processBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                requestStoragePermission();
            }
        });
    }


    @SuppressLint("ObsoleteSdkInt")
    private void requestStoragePermission() {

        //permission code for android version 13 or above
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // only for TIRAMISU and newer versions

            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED) {

                System.out.println("permission already granted..............");
                startProcess();


            } else {
                System.out.println("request permission");
                request_permission_launcher_storage_videos.launch(android.Manifest.permission.READ_MEDIA_VIDEO);
            }
        }
    }
    private ActivityResultLauncher<String> request_permission_launcher_storage_videos =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                    isGranted -> {

                        if (isGranted) {
                            startProcess();

                        }
                        else {

                            Toast.makeText(this, "Please grant us the necessary permission", Toast.LENGTH_SHORT).show();
                        }
            });


    private void startProcess() {

        System.out.println("start process......");
        progressBar.setVisibility(View.VISIBLE);

        File fileParent = Environment.getExternalStoragePublicDirectory(DIRECTORY_MOVIES);
        File file = new File(fileParent, "Reverse_Demo_3");

        if(file.exists()) {
            System.out.println("file already exist......--->");
        }
        else {
            System.out.println("file not exist......--->");
            file.mkdir();
        }


        inputPath = Environment.getExternalStorageDirectory().getPath() + "/Movies/Testing_videos/myvideo1234.mp4";
        outputPath = Environment.getExternalStorageDirectory().getPath() + "/Movies/Reverse_Demo_3/"+randomString+".mp4";

        try {
            inputFileDuration = getVideoDuration(inputPath);
            System.out.println("inputFileDuration......."+inputFileDuration);
        } catch (IOException e) {
            System.out.println("e.toString()......"+e.toString());
            throw new RuntimeException(e);
        }
        partReverse();
    }




    private void partReverse(){

        System.out.println("partReverse called");

        FFmpegKitConfig.enableStatisticsCallback(new StatisticsCallback() {
            @Override
            public void apply(Statistics statistics) {
                // CALLED WHEN SESSION GENERATES STATISTICS
                System.out.println("statistics.toString()........."+statistics.toString());
                System.out.println("statistics.getTime()........."+statistics.getTime());
                float percentage = ((float) (((statistics.getTime() * 100) /inputFileDuration)));
                System.out.println("percentage......"+percentage);
                //FFmpegKitConfig.clearSessions();
            }
        });
        FFmpegKitConfig.enableLogCallback(new LogCallback() {
            @Override
            public void apply(com.arthenica.ffmpegkit.Log log) {
                System.out.println("Log Call back = "+log.toString());
            }
        });



//        MediaInformationSession mediaInformation = FFprobeKit.getMediaInformation(inputPath);
//        mediaInformation.getMediaInformation();
//        System.out.println("mediaInformation.toString() ="+mediaInformation.toString());


        FFmpegSession session = FFmpegKit.executeAsync("-i " + inputPath + " -filter_complex reverse " + outputPath, new FFmpegSessionCompleteCallback() {

            @Override
            public void apply(FFmpegSession session) {

                // CALLED WHEN SESSION IS EXECUTED
                progressBar.setVisibility(View.INVISIBLE);

                SessionState state = session.getState();
                ReturnCode returnCode = session.getReturnCode();

                if(returnCode.isValueSuccess()){
                    System.out.println("success....");

                } else if (returnCode.isValueError()) {
                    System.out.println("error.....");

                }else if(returnCode.isValueCancel()){
                    System.out.println("cancel....");
                }
                else {
                    System.out.println("fail.....");
                }
                Log.d(TAG, String.format("FFmpeg process exited with state %s and rc %s.%s", state, returnCode, session.getFailStackTrace()));
            }
        }, new LogCallback() {

            @Override
            public void apply(com.arthenica.ffmpegkit.Log log) {

                // CALLED WHEN SESSION PRINTS LOGS
                System.out.println("Log Call back = "+log.toString());
            }
        }, new StatisticsCallback() {

            @Override
            public void apply(Statistics statistics) {

                // CALLED WHEN SESSION GENERATES STATISTICS
                System.out.println("statistics.toString()........."+statistics.toString());
                System.out.println("statistics.getTime()........."+statistics.getTime());
                float percentage = ((float) (((statistics.getTime() * 100) /inputFileDuration)));
                System.out.println("percentage......"+percentage);
            }
        });
        System.out.println("session.getAllLogsAsString()......."+session.getAllLogsAsString());
        System.out.println("session.getOutput()......."+session.getOutput());
    }

    public long getVideoDuration(String videoPath) throws IOException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(videoPath); // set the data source
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long duration = Long.parseLong(time); // get duration in ms
            retriever.release();
            return duration;
        } catch (Exception e) {
            e.printStackTrace();
            retriever.release();
            return 0;
        }
    }
 }