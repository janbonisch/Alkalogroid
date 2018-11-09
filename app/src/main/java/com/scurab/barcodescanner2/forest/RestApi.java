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

    @GET("Users")   //https://zpa.westeurope.cloudapp.azure.com:8016/Items/api/Users
    Observable<User[]> getUsers();

    @GET("ItemfViews/GetAvailable") //https://zpa.westeurope.cloudapp.azure.com:8016/Items/api/ItemfViews/GetAvailable
    Observable<ItemfView[]> getAvailableItemfs(); //pozor, toto nevraci co lze sezrat, ale jen to, co lze smazat

    @GET("ItemfViews/GetForDayInserted/{day}")   //https://zpa.westeurope.cloudapp.azure.com:8016/Items/api/ItemfViews/GetForDayInserted/2018-11-05
    Observable<ItemfView[]> GetForDayInserted(@Path("day") String den);

    @POST("Consds")
    Observable<Empty> consds(@Body Consds data);

    @POST("Consfs")
    Observable<Empty> consfs(@Body Consfs data);

    @POST("UserDevices")
    Observable<User> registrerDevice(@Body UserDevices data);

    @POST("Users/AllowWebAccess/{emai}") //https://zpa.westeurope.cloudapp.azure.com:8016/Items/api/Users/AllowWebAccess/<imei>
    Observable<User> AllowWebAccess(@Path("emai") String den);

    @DELETE("UserDevices/{emai}")
    Observable<Response<Void>> unregistrerDevice(@Path("emai") String emai);
}
