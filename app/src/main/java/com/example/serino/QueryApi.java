package com.example.serino;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;


interface QueryApi {

    @Headers("Content-Type: application/json")
    @POST("webhooks/rest/webhook")
    Call<List<QueryResponse>> queryResponse(@Body Query query);

}
