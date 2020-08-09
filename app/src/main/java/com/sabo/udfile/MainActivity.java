package com.sabo.udfile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sabo.udfile.API.APIRequestData;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_PERMISSION = 1;
    private static final int FILE_CODE = 102;
    Button btnSelectFile, btnUpload, btnGO2, btnGO3;
    TextView tvFileName, tvExtension;
    String extension, uriString, path;
    Uri selectedFileUri = null;
    APIRequestData mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mService = Common.getScalars();


        btnSelectFile = findViewById(R.id.btnSelectFile);
        tvFileName = findViewById(R.id.tvFileName);
        tvExtension = findViewById(R.id.tvExtension);
        btnUpload = findViewById(R.id.btnUpload);
        btnGO2 = findViewById(R.id.btnGo2);
        btnGO3 = findViewById(R.id.btnGo3);

        btnSelectFile.setOnClickListener(this);
        btnUpload.setOnClickListener(this);
        btnGO2.setOnClickListener(this);
        btnGO3.setOnClickListener(this);

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
            case R.id.btnGo2:
                startActivity(new Intent(this, Main2Activity.class));
                break;
            case R.id.btnGo3:
                startActivity(new Intent(this, Main3Activity.class));
                break;
        }
    }


    private void selectFile() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, REQUEST_PERMISSION);
        } else
            chooseFile();
    }


    private void chooseFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        startActivityForResult(Intent.createChooser(intent, "Choose File"), FILE_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            chooseFile();
        } else {
            Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        String result, rEx;
        if (requestCode == FILE_CODE) {
            if (resultCode == RESULT_OK) {

//                selectedFileUri = data.getData();
//                btnSelectFile.setHint("Change File");
//                if (selectedFileUri != null ) {
//                    result = selectedFileUri.getPath();
//
//
//                    // File Name
//                    int cut = result.lastIndexOf('/');
//                    if (cut != -1) {
//                        result = result.substring(cut + 1);
//                        tvFileName.setText(result);
////                        btnSelectFile.setHint("Change File");
//                    }
//
//                    // Extension (.pdf | .docx | .rar | .zip | etc)
//                    File file = new File(result);
//                    String fileName = file.getName();
//                    if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) {
//                        rEx = fileName.substring(fileName.lastIndexOf(".") + 1);
//                        extension = rEx;
//                        tvExtension.setText("." + extension);
//                    }
//                }

                // =======================================================================================================
                selectedFileUri = data.getData();
                path = getFilePathFromURI(MainActivity.this, selectedFileUri);
                btnSelectFile.setHint("Change File");
            }
        }
    }

    private String getFilePathFromURI(Context context, Uri uri) {
        String fileName = getFileName_extension(uri);
        File dir = new File(Environment.getExternalStorageDirectory() + "");
        if (!dir.exists())
            dir.mkdirs();
        if (!TextUtils.isEmpty(fileName)){
            File copyFile = new File(dir + File.separator + fileName);
            copy(context, uri, copyFile);
            return copyFile.getAbsolutePath();
        }
        return null;
    }

    private String getFileName_extension(Uri uri) {
        if (uri == null) return null;
        String fileName = null;
        String path = uri.getPath();
        int cut = path.lastIndexOf('/');
        if (cut != -1){
            fileName = path.substring(cut + 1);
            tvFileName.setText(fileName);

            tvExtension.setText(getMimeType(uri));
        }

        return fileName;
    }

    private String getMimeType(Uri uri) {
        ContentResolver resolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(resolver.getType(uri));
    }

    private void copy(Context context, Uri uri, File copyFile) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) return;
            OutputStream outputStream = new FileOutputStream(copyFile);
            copyStream(inputStream, outputStream);
            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private int copyStream(InputStream inputStream, OutputStream outputStream) throws IOException {
        int BUFFER_SIZE = 1024 * 2;
        byte[] buffer = new byte[BUFFER_SIZE];

        BufferedInputStream in = new BufferedInputStream(inputStream, BUFFER_SIZE);
        BufferedOutputStream out = new BufferedOutputStream(outputStream, BUFFER_SIZE);
        int count = 0, n = 0;
        try {
            while ((n = in.read(buffer, 0, BUFFER_SIZE)) != -1){
                out.write(buffer, 0, n);
                count += n;
            }
            out.flush();
        } finally {
            try {
                out.close();
            } catch (IOException e){
                Log.e(e.getMessage(), String.valueOf(e));
            }
            try {
                in.close();
            } catch (IOException e){
                Log.e(e.getMessage(), String.valueOf(e));
            }
        }
        return count;
    }


    private void uploadFile() {
        if (selectedFileUri != null) {

            if (getMimeType(selectedFileUri) == "pdf"){
                ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setTitle("Upload!");
                progressDialog.setMessage("Please wait...");
                progressDialog.show();

                File file = new File(path);

                String fileName = new StringBuilder("Monitoring_1_RK2019.302")
                        .append(".")
                        .append(getMimeType(selectedFileUri))
                        .toString();

                RequestBody req = RequestBody.create(MediaType.parse("*/*"), file);

                MultipartBody.Part body = MultipartBody.Part.createFormData("file", fileName, req);
                MultipartBody.Part ID_MON = MultipartBody.Part.createFormData("ID_MON", "1");
                MultipartBody.Part LAST_STATUS = MultipartBody.Part.createFormData("LAST_STATUS", "3");

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        mService.uploadFile(ID_MON, LAST_STATUS, body)
                                .enqueue(new Callback<String>() {
                                    @Override
                                    public void onResponse(Call<String> call, Response<String> response) {
                                        if (response.isSuccessful()) {
                                            progressDialog.dismiss();
                                            Toast.makeText(MainActivity.this, response.body(), Toast.LENGTH_SHORT).show();
                                            btnSelectFile.setHint("Select File");
                                            tvFileName.setText("");
                                            tvExtension.setText("");
                                        } else {
                                            progressDialog.dismiss();
                                            Toast.makeText(MainActivity.this, response.body(), Toast.LENGTH_SHORT).show();
                                        }

                                    }

                                    @Override
                                    public void onFailure(Call<String> call, Throwable t) {
                                        progressDialog.dismiss();
                                        Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                    }
                }).start();
            }
            else
                Toast.makeText(this, "Silahkan pilih file dengan format pdf!", Toast.LENGTH_LONG).show();


        } else
            Toast.makeText(this, "Silahkan Pilih File", Toast.LENGTH_SHORT).show();
    }


}
