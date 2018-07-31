package com.scurab.barcodescanner2.forest;

import com.google.gson.annotations.SerializedName;

// Konzumace jídla
public class Consf {
    // key, auto-generated
    public int ConsfID;
    // required, refernce to Itemf
    public int ItemfID;
    // required, length = 100
    public String Imei;
}
