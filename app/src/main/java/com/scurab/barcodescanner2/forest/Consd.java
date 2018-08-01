package com.scurab.barcodescanner2.forest;

import java.sql.Date;

// Konzumace sklenek
public class Consd {
    // key, auto-generated (read-only)
    public int ConsdID;
    // required, reference to Itemd
    public int ItemdID;
    // required, length = 100
    public String Imei;
    // read-only, reference to User
    public String Username;
    // pokud neni zadana, tak 0.2
    public Double Amount;
    // read-only
    public Date DtCons;
}
