package com.scurab.barcodescanner2.forest;

import com.google.gson.annotations.SerializedName;

import java.util.Calendar;

// Lahev - přehled
public class ItemdView {
    // key
    @SerializedName("ItemdID")
    public int ItemdID;
    // ročník
    @SerializedName("Year")
    public short Year;
    // Odrůda
    @SerializedName("Name")
    public String Name;
    // Popis
    @SerializedName("Description")
    public String Description;
    // Objem
    @SerializedName("Volume")
    public double Volume;
    // Procento alk.
    @SerializedName("PercentVol")
    public double PercentVol;
    // cena
    @SerializedName("Price")
    public int Price;
    // datum naskladnění
    @SerializedName("DtInsert")
    public Calendar DtInsert;
    // zkonzumovaná část
    @SerializedName("ConsAmount")
    public double ConsAmount;
    // start konzumace
    @SerializedName("DtConsStart")
    public Calendar DtConsStart;
    // konec konzumace
    @SerializedName("DtConsEnd")
    public Calendar DtConsEnd;
}
