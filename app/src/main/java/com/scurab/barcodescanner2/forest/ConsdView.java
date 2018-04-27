package com.scurab.barcodescanner2.forest;

import com.google.gson.annotations.SerializedName;

import java.util.Calendar;

// Sklenka - přehled
public class ConsdView {
    // key
    @SerializedName("ConsdID")
    public int ConsdID;
    // ročník
    @SerializedName("Year")
    public short Year;
    // Odrůda
    @SerializedName("Name")
    public String Name;
    // Popis
    @SerializedName("Description")
    public String Description;
    // Procento alk.
    @SerializedName("PercentVol")
    public double PercentVol;
    // objem
    @SerializedName("Volume")
    public double Volume;
    // cena
    @SerializedName("Price")
    public int Price;
    // Zkonzumováno kdy
    @SerializedName("DtConsd")
    public Calendar DtConsd;
}
