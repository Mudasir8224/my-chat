package com.example.mychat.Fragments;

import com.example.mychat.Notification.MyResponse;
import com.example.mychat.Notification.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:Key=AAAAHsUhtmc:APA91bF-w2BuJik6r0bZweny1WdRD8Bcp0Zb5Dj1DabLG-216U0R4Gl-8FjaDe2N95h5JyPQxKxgEnw1ixnCdlrwyxF_DIOn9LmvY8QeFGCafDGnsXswRnL72WeB282voUjBtTJ2Yt9j"
    })
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
