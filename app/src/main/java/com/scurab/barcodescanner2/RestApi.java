package com.scurab.barcodescanner2;


import com.scurab.barcodescanner2.forest.XsampleLahef;
import com.scurab.barcodescanner2.forest.XsampleLahefResponse;

import retrofit2.Call;
import retrofit2.http.POST;

public interface RestApi {

    @POST("api/ItemdTypes")
    Call<XsampleLahefResponse> send(XsampleLahef lahef);
}
