package com.scurab.barcodescanner2;


import com.scurab.barcodescanner2.forest.Empty;
import com.scurab.barcodescanner2.forest.User;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface RestApi {

    @GET("Items/api/Users")
    Observable<User[]> getUsers();

    @POST("Items/api/Consfs")
    Observable<Empty> consfs(@Body Object Consfs);
}
