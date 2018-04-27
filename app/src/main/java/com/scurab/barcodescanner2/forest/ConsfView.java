package com.scurab.barcodescanner2.forest;

import com.google.gson.annotations.SerializedName;

import java.util.Calendar;

// Jídlo - přehled
public class ConsfView {
    // key
    @SerializedName("ConsfID")
    public int ConsfID;
    // zkonzumovaná část
    @SerializedName("ConsAmount")
    public double ConsAmount;
    // cena
    @SerializedName("Price")
    public int Price;
    // Zkonzumováno kdy
    @SerializedName("DtConsf")
    public Calendar DtConsf;
}
