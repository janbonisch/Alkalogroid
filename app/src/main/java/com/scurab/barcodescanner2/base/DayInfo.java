package com.scurab.barcodescanner2.base;

import com.scurab.barcodescanner2.forest.ItemdView;
import com.scurab.barcodescanner2.forest.ItemfView;

import java.io.Serializable;

public class DayInfo implements Serializable {
    public static final String ID="DayInfo";
    public ItemdView[] drinks;
    public ItemfView[] food;
}
