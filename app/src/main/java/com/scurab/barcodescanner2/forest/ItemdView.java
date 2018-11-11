package com.scurab.barcodescanner2.forest;

import java.io.Serializable;
import java.sql.Date;

// Sklenka - přehled
public class ItemdView implements Serializable {
    // key
    public int ConsdID;
    // ročník
    public short Year;
    // Odrůda
    public String Name;
    // Popis
    public String Description;
    // Procento alk.
    public double PercentVol;
    // objem
    public double Volume;
    // cena
    public int ConsPrice;
    // konzument
    public String Username;
    // Zkonzumováno kdy
    public Date DtCons;

}
