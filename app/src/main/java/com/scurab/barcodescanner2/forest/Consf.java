package com.scurab.barcodescanner2.forest;

import com.google.gson.annotations.SerializedName;

import java.sql.Date;

// Jídlo
public class Consf {
    // key, auto-generated (read-only)
    public int ConsfID;
    // required, refernce to Itemf
    public int ItemfID;
    // required, length = 100
    public String Imei;
    // read-only, reference to User
    public String Username;
    // read-only
    public Date DtCons;
}
