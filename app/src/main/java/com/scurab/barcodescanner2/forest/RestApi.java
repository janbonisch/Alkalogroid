package com.scurab.barcodescanner2.forest;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface RestApi {

    @GET("Users")
    Observable<User[]> getUsers();

    @GET("ItemfViews/GetAvailable")
    Observable<ItemfView[]> getAvailableItemfs();

    @POST("Consfs")
    Observable<Empty> consfs(@Body Object Consfs);
}
