package com.scurab.barcodescanner2.forest;

import com.google.gson.annotations.SerializedName;

// Jídlo
public class Itemf {
    // key, auto-generated
    @SerializedName("ItemfID")
    public int ItemfID;
    // required, length = 20, refernce to User
    @SerializedName("Username")
    public String Username;
    // required
    @SerializedName("Price")
    public double Price;
}
