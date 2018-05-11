package com.scurab.barcodescanner2;


import com.scurab.barcodescanner2.forest.User;
import com.scurab.barcodescanner2.forest.XsampleLahef;
import com.scurab.barcodescanner2.forest.XsampleLahefResponse;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface RestApi {

    @POST("api/ItemdTypes")
    Observable<XsampleLahefResponse> send(XsampleLahef lahef);

    @GET("Items/api/Users")
    Observable<User[]> getUsers();
}
