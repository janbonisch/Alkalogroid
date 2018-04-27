package com.scurab.barcodescanner2.forest;

import com.google.gson.annotations.SerializedName;

// Druh lahve
public class ItemdType {
    // key, auto-generated
    @SerializedName("ItemdTypeID")
    public int ItemdTypeID;
    // required
    @SerializedName("Year")
    public short Year;
    // required, length = 40
    @SerializedName("Name")
    public String Name;
    // length = 100
    @SerializedName("Description")
    public String Description;
    // required
    @SerializedName("Price")
    public double Price;
    // required
    @SerializedName("Volume")
    public double Volume;
    // required
    @SerializedName("PercentVol")
    public double PercentVol;
}
