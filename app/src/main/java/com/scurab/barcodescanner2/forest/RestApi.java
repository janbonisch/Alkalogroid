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

    @POST("Consds")
    Observable<Empty> consds(@Body Consds data);

    @POST("Consfs")
    Observable<Empty> consfs(@Body Consfs data);


    @POST("UserDevices")
    Observable<User> setUserDevice(@Body Object data);
}
