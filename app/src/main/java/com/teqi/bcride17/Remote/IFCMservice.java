package com.teqi.bcride17.Remote;

import com.teqi.bcride17.Model.FCMResponse;
import com.teqi.bcride17.Model.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMservice {
    @Headers({
            "Content_Type:application/json",
            "Authorization:key=<AAAA0nnMRq8:APA91bFp1j5aKlmGFfFH_VXCRTfNpl5PT47nc6E0wFy1ZoYDBqhtmzRRkCv7awYd0NQttTPnyfxNM18LY7_J3kUriGEr1SPXBDsfyhe_A6aXWLmDVWCk9CFM_rVBO1yQnwg01ccWFD0H>"
    })
    @POST("fcm/send")
    Call<FCMResponse> sendMessage(@Body Sender body);
}
