package com.scurab.barcodescanner2.forest;

import com.google.gson.annotations.SerializedName;

// Konzumace jídla
public class Consf {
    // key, auto-generated
    @SerializedName("ConsfID")
    public int ConsfID;
    // required, refernce to Itemf
    @SerializedName("ItemfID")
    public int ItemfID;
    // required, length = 100
    @SerializedName("Imei")
    public String Imei;
}
