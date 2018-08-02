package com.scurab.barcodescanner2.forest;

import java.sql.Date;
import java.util.Calendar;

// Naskladněné jídlo - přehled
public class ItemfView {
    // key
    public int ItemfID;
    // naskladnil
    public String Username;
    // naskladnil - jméno
    public String DisplayName;
    // cena
    public Double Price;
    // datum naskladneni
    public Date DtInsert;
    // počet konzumujících
    public int Consumers;

}
