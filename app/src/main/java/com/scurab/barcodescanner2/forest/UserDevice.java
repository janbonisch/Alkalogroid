package com.scurab.barcodescanner2.forest;

import com.google.gson.annotations.SerializedName;

// Mobil
public class UserDevice {
    // key, required, length = 100
    @SerializedName("Imei")
    public String Imei;
    // required, length = 20, reference to User
    @SerializedName("UserName")
    public String UserName;
}
