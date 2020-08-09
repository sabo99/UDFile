package com.sabo.udfile;

import com.sabo.udfile.API.APIRequestData;
import com.sabo.udfile.API.RetrofitClient;

public class Common {

//    public static final String BASE_URL = "http://192.168.1.7/Android/monaksi/";
//    public static final String URL = "http://192.168.1.7/Android/monaksi/uploadFile.php";

    public static final String BASE_URL = "http://192.169.0.147/Android/monaksi/";
    public static final String URL = "http://192.169.0.147/Android/monaksi/uploadFile.php";

    public static APIRequestData getAPI() {
        return RetrofitClient.getClient(BASE_URL).create(APIRequestData.class);
    }

    public static APIRequestData getScalars() {
        return RetrofitClient.getScalars(BASE_URL).create(APIRequestData.class);
    }
}
