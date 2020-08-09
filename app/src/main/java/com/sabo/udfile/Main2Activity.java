package com.sabo.udfile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;
import com.sabo.udfile.API.APIRequestData;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

public class Main2Activity extends AppCompatActivity implements View.OnClickListener {

    private static final int PERMISSION_UPLOAD_CODE = 100;
    private static final int FILE_CODE = 10;
    private static final int PERMISSION_DOWNLOAD_CODE = 900;
    Button btnSelectFile, btnUpload, btnDownload;
    TextView tvFileName, tvExtension, tvTypeFile, tvFileNameDownload;
    ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        btnSelectFile = findViewById(R.id.btnSelectFile);
        tvFileName = findViewById(R.id.tvFileName);
        tvExtension = findViewById(R.id.tvExtension);
        btnUpload = findViewById(R.id.btnUpload);
        tvTypeFile = findViewById(R.id.tvTypeFile);
        btnDownload = findViewById(R.id.btnDownload);

        btnSelectFile.setOnClickListener(this);
        btnUpload.setOnClickListener(this);
        btnDownload.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSelectFile:
                selectFile();
                break;
            case R.id.btnUpload:
                uploadFile();
                break;
            case R.id.btnDownload:
                checkPermissionDownload();
                break;

        }
    }

    private void checkPermissionDownload() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_DOWNLOAD_CODE);
        } else {
            downloadFile();
        }
    }
    String nama = "Monitoring_1_RKR2019.302.pdf";

    private void downloadFile() {
        progress = new ProgressDialog(this);
        progress.setTitle("Download");
        progress.setMessage("Please wait...");
        progress.show();

        String url = Common.BASE_URL + "Lampiran/Monitoring_1_RKR2019.302.pdf";


        APIRequestData mService = Common.getAPI();
        Call<ResponseBody> call = mService.downloadFile(url);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                new AsyncTask<Void, Void, Void>(){
                    @Override
                    protected Void doInBackground(Void... voids){
                        writeResponseBodyToDisk(response.body());
                        return null;
                    }
                }.execute();

                Toast.makeText(Main2Activity.this, "Download success!", Toast.LENGTH_SHORT).show();
                progress.dismiss();
//                AlertDialog.Builder builder = new AlertDialog.Builder(Main2Activity.this).setCancelable(false);
//                builder.setTitle("Sukses!").create().show();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    private boolean writeResponseBodyToDisk(ResponseBody body) {
        try {
            File futureStudioIconFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), nama);

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];
                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(futureStudioIconFile);

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);
                    fileSizeDownloaded += read;
                }

                outputStream.flush();

                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    private void selectFile() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_UPLOAD_CODE);
        }
        else {
            openMaterialFilePicker();
        }
    }

    private void openMaterialFilePicker() {
        new MaterialFilePicker()
                .withActivity(this)
//                .withFilter(Pattern.compile(".*\\.pdf$"))
                .withRequestCode(FILE_CODE)
                .withTitle("Choose File")
                .start();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_UPLOAD_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            openMaterialFilePicker();
        else if (requestCode == PERMISSION_DOWNLOAD_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            downloadFile();
        else
            Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
    }

    String content_type, file_path;
    OkHttpClient client;
    RequestBody file_body;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        progress = new ProgressDialog(this);
        progress.setTitle("Uploading");
        progress.setMessage("Please wait...");
        progress.show();

        if (requestCode == FILE_CODE && resultCode == RESULT_OK){
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {

                    File f = new File(data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH));
                    content_type = getMimeType(f.getPath());

                    file_path = f.getAbsolutePath();
                    client = new OkHttpClient();
                    file_body = RequestBody.create(MediaType.parse("*/*"), f);

                    // ====================================================================================


                    // Type File
                    //tvTypeFile.setText(content_type);

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

                    Request request = new Request.Builder()
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


    private void uploadFile() {



    }
}
