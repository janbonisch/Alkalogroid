package com.scurab.barcodescanner2.forest;

import com.google.gson.annotations.SerializedName;

// Konzumace sklenek
public class Consd {
    // key, auto-generated
    public int ConsdID;
    // required, reference to Itemd
    public int ItemdID;
    // required, length = 100
    public String Imei;
    // required
    public double Amount;
}
