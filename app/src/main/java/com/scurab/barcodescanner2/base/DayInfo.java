package com.scurab.barcodescanner2.base;

import com.scurab.barcodescanner2.forest.ItemLogFood;
import com.scurab.barcodescanner2.forest.ItemdView;
import com.scurab.barcodescanner2.forest.ItemfView;

import java.io.Serializable;

public class DayInfo implements Serializable {
    public static final String ID="DayInfo";
    public ItemdView[] drinks;
    public ItemLogFood[] food;
    public boolean isEmpty() {
        int x=0;
        if ((drinks!=null)&&(drinks.length>0)) x++; //cukneme pokud mame nejakej chlast
        if ((food!=null)&&(food.length>0)) x++; //cukneme, pokud mame nejaky jidlo
        return (x==0);
    }
}
