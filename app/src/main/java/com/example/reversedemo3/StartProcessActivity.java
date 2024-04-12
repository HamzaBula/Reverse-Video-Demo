package com.example.reversedemo3;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.os.Process;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.widget.MediaController;
import static android.content.ContentValues.TAG;
import static android.os.Environment.DIRECTORY_MOVIES;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.arthenica.ffmpegkit.FFmpegKit;
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
import android.widget.VideoView;


public class StartProcessActivity extends AppCompatActivity {

    Button processBtn;
    ProgressBar progressBar;
    String inputPath,outputPath;
    String randomString ="testing"+System.currentTimeMillis()/1000;
    long inputFileDuration = 0;
    Uri selectedVideoUri;
    VideoView videoView;
    public static final String FILEPATH = "outputStringPath";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_process);

        processBtn = findViewById(R.id.process_button);
        progressBar = findViewById(R.id.progressbar);

        videoView = findViewById(R.id.videoView);
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);

        Intent intent = getIntent();
        if (intent != null) {

            String videoPath = intent.getStringExtra("uri");

            if (videoPath != null) {
                selectedVideoUri = Uri.parse(videoPath);
                videoView.setVideoURI(selectedVideoUri);
                videoView.setMediaController(mediaController);
                videoView.start();
            }
        }

        processBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startProcess();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(videoView != null) {
            videoView.start();
        }
    }

    private void startProcess() {

        progressBar.setVisibility(View.VISIBLE);
        videoView.pause();

        File fileParent = Environment.getExternalStoragePublicDirectory(DIRECTORY_MOVIES);
        File file = new File(fileParent, "Reverse_Demo_3");

        if(!file.exists()) {
            file.mkdir();
        }

        inputPath = getRealPathFromUri(this,selectedVideoUri);
        outputPath = Environment.getExternalStorageDirectory().getPath() + "/Movies/Reverse_Demo_3/"+randomString+".mp4";

        try {
            inputFileDuration = getVideoDuration(inputPath);
         } catch (IOException e) {
             throw new RuntimeException(e);
        }

        ReverseMethod();
    }




    private void ReverseMethod(){

        FFmpegSession session = FFmpegKit.executeAsync("-i " + inputPath + " -vf reverse " + outputPath, new FFmpegSessionCompleteCallback() {

            @Override
            public void apply(FFmpegSession session) {

                // CALLED WHEN SESSION IS EXECUTED
                progressBar.setVisibility(View.INVISIBLE);

                SessionState state = session.getState();
                ReturnCode returnCode = session.getReturnCode();

                if(returnCode.isValueSuccess()){

                    Intent intent = new Intent(StartProcessActivity.this, ProcessedVideoActivity.class);
                    intent.putExtra(FILEPATH, outputPath);
                    startActivity(intent);

                } else if (returnCode.isValueError()) {
                    Toast.makeText(StartProcessActivity.this, "There is some error", Toast.LENGTH_SHORT).show();

                }else if(returnCode.isValueCancel()){
                    Toast.makeText(StartProcessActivity.this, "cancel", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(StartProcessActivity.this, "Fail", Toast.LENGTH_SHORT).show();
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

    public static String getRealPathFromUri(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }


    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }


}