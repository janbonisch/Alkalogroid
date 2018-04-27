package com.scurab.barcodescanner2.forest;

import com.google.gson.annotations.SerializedName;

// Uživatel
public class User {
    // key, required, length = 20
    @SerializedName("UserName")
    public String UserName;
    // required, length = 20
    @SerializedName("DisplayName")
    public String DisplayName;
    // required, default 0
    @SerializedName("Credit")
    public double Credit;
}
