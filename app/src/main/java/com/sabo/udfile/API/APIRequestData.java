package com.sabo.udfile.API;

import com.sabo.udfile.Model.ResponseModel;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public interface APIRequestData {

    @Multipart
    @POST("uploadFile.php")
    Call<String> uploadFile(@Part MultipartBody.Part ID_MON,
                                   @Part MultipartBody.Part LAST_STATUS,
                                   @Part MultipartBody.Part file);

    @Streaming
    @GET
    Call<ResponseBody> downloadFile(@Url String url);
}
