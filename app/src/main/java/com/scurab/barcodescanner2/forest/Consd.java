package com.scurab.barcodescanner2.forest;

import com.google.gson.annotations.SerializedName;

// Konzumace sklenek
public class Consd {
    // key, auto-generated
    @SerializedName("ConsdID")
    public int ConsdID;
    // required, reference to Itemd
    @SerializedName("ItemdID")
    public int ItemdID;
    // required, length = 100
    @SerializedName("Imei")
    public String Imei;
    // required
    @SerializedName("Amount")
    public double Amount;
}
