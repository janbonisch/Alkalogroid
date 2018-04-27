package com.scurab.barcodescanner2.forest;

import com.google.gson.annotations.SerializedName;

// Lahev
public class Itemd {
    // key, required
    @SerializedName("ItemdID")
    public int ItemdID;
    // required, reference to ItemdType
    @SerializedName("ItemdType")
    public int ItemdType;
    // required, length = 20, reference to User
    @SerializedName("Username")
    public String Username;
    // pokud neni zadano, bere se z typu
    @SerializedName("Price")
    public double Price;
}
