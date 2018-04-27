package com.scurab.barcodescanner2;


import retrofit2.Call;
import retrofit2.http.POST;

public interface RestApi {

    @POST("api/ItemdTypes")
    Call<LahefResponse> send(Lahef lahef);
}
