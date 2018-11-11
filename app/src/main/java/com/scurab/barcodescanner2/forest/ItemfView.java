package com.scurab.barcodescanner2.forest;

import java.io.Serializable;
import java.sql.Date;

// Jídlo - přehled
public class ItemfView implements Serializable {
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
