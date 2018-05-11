package com.scurab.barcodescanner2.forest;

import com.google.gson.annotations.SerializedName;

// Uživatel
public class User {
    // key, required, length = 20
    public String UserName;
    // required, length = 20
    public String DisplayName;
    // required, default 0
    public double DtConsStart;
}
