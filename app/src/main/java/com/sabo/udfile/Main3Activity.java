package com.sabo.udfile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.github.developerpaul123.filepickerlibrary.FilePickerActivity;
import com.github.developerpaul123.filepickerlibrary.FilePickerBuilder;
import com.github.developerpaul123.filepickerlibrary.enums.Request;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Main3Activity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_UPLOAD_CODE = 100001;
    private static final int PICKFILE_RESULT_CODE = 1001;
    private static final int REQUEST_DOWNLOAD_CODE = 12093;
    Button btnSelectFile, btnUpload, btnDownload;
    TextView tvFileName, tvTypeFile, tvExtension, tvFileNameDownload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        btnSelectFile = findViewById(R.id.btnSelectFile);
        btnUpload = findViewById(R.id.btnUpload);
        tvFileName = findViewById(R.id.tvFileName);
        tvTypeFile = findViewById(R.id.tvTypeFile);
        tvExtension = findViewById(R.id.tvExtension);
        btnDownload = findViewById(R.id.btnDownload);
        tvFileNameDownload = findViewById(R.id.tvFileNameDownload);

        btnSelectFile.setOnClickListener(this);
        btnDownload.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnSelectFile:
                checkRequestPermission();
                break;
            case R.id.btnDownload:
                checkRequestPermissionDownload();
                break;
        }
    }

    private void checkRequestPermissionDownload() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_DOWNLOAD_CODE);
        }
        else {
            downloadFile();
        }
    }

    private void downloadFile() {
        String namaFile = "Monitoring_1_RKR2019.302.pdf";
        String url = "http://192.168.1.7/Android/monaksi/Lampiran/"+ namaFile;
//        String url = "https://upload.wikimedia.org/wikipedia/commons/4/4d/Cat_November_2010-1a.jpg";
        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setTitle("Download"); // Set Tittle in download notification
        request.setDescription("Downloading file...");

        request.allowScanningByMediaScanner();
        request.setAllowedOverMetered(true);
        request.setAllowedOverRoaming(true);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, namaFile); // nama File
        request.setMimeType(getMimeType(Uri.parse(url)));

        // get download service and enqueue file

        manager.enqueue(request);
    }

    private String getMimeType(Uri uri){
        ContentResolver resolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(resolver.getType(uri));
    }

    private void checkRequestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_UPLOAD_CODE);
        }
        else {
            chooseFile();
        }
    }

    private void chooseFile() {
        new FilePickerBuilder(this)

                .withColor(android.R.color.holo_blue_bright)
                .withRequest(Request.FILE)
                .launch(PICKFILE_RESULT_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_UPLOAD_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            chooseFile();
        else if (requestCode == REQUEST_DOWNLOAD_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            downloadFile();
        else
            Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
    }


    String content_type, file_path;
    OkHttpClient client;
    RequestBody file_body;
    ProgressDialog progress;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        progress = new ProgressDialog(this);
        progress.setTitle("Uploading");
        progress.setMessage("Please wait...");
        progress.show();



        if (requestCode == PICKFILE_RESULT_CODE && resultCode == RESULT_OK){


            String x = data.getStringExtra(FilePickerActivity.FILE_EXTRA_DATA_PATH);
            Toast.makeText(this, x, Toast.LENGTH_SHORT).show();
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {

                    File f = new File(data.getStringExtra(FilePickerActivity.FILE_EXTRA_DATA_PATH));
                    content_type = getMimeType(f.getPath());


                    file_path = f.getAbsolutePath();
                    client = new OkHttpClient();
                    file_body = RequestBody.create(MediaType.parse("*/*"), f);

                    // ====================================================================================


                    // Type File
//                    tvTypeFile.setText(content_type);

                    // File Name
                    int cut = file_path.lastIndexOf('/');
                    if (cut != -1) {
                        file_path = file_path.substring(cut + 1);
                        tvFileName.setText(file_path);
                        btnSelectFile.setHint("Change File");
                    }

                    // Extension (.pdf | .rar | .docx | .mp3 | etc)
                    String rEx, extension;
                    File file = new File(f.getPath());
                    String fileName = file.getName();
                    if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) {
                        rEx = fileName.substring(fileName.lastIndexOf(".") + 1);
                        extension = rEx;
                        tvExtension.setText("." + extension);
                    }
                    // ==========================================================================================

                    String resultFileName, resultExtension, finalResultFileName;
                    resultFileName = "Monitoring_1_RKR2019.302"; // Nama File bs di ganti
                    resultExtension = tvExtension.getText().toString();
                    finalResultFileName = resultFileName+resultExtension;

                    RequestBody request_body = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
//                            .addFormDataPart("type", content_type)
                            .addFormDataPart("type", "*/*")
//                            .addFormDataPart("file", file_path.substring(file_path.lastIndexOf("/")+1), file_body)
                            .addFormDataPart("file", finalResultFileName, file_body)
                            .addFormDataPart("ID_MON", "1")
                            .addFormDataPart("LAST_STATUS", "2")
                            .build();

                    okhttp3.Request request = new okhttp3.Request.Builder()
                            .url(Common.URL)
                            .post(request_body)
                            .build();

                    try {
                        Response response = client.newCall(request).execute();

                        if (!response.isSuccessful())
                            throw new IOException("Error :" +response);

                        progress.dismiss();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            });
            t.start();
        }
        else
            progress.dismiss();
    }

    private String getMimeType(String path) {
        String extentsion = MimeTypeMap.getFileExtensionFromUrl(path);
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extentsion);
    }



}
