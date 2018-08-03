package com.scurab.barcodescanner2.forest;

import io.reactivex.Completable;
import io.reactivex.Observable;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

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
    Observable<User> registrerDevice(@Body UserDevices data);

    @DELETE("UserDevices/{emai}")
    Observable<Response<Void>> unregistrerDevice(@Path("emai") String emai);

}
