package com.scurab.barcodescanner2.forest;

import com.google.gson.annotations.SerializedName;

// Kredit - přehled
public class UserConsView {
    // user
    @SerializedName("Username")
    public String Username;
    // user - jméno
    @SerializedName("DisplayName")
    public String DisplayName;
    // user - vloženo
    @SerializedName("Credit")
    public double Credit;
    // lahví koupeno
    @SerializedName("ConsdIn")
    public int ConsdIn;
    // lahví zkonzumováno
    @SerializedName("ConsdOut")
    public double ConsdOut;
    // cena za koupené lahve
    @SerializedName("PricedInt")
    public double PricedInt;
    // cena za zkonzumované lahve
    @SerializedName("PricedOut")
    public double PricedOut;
    // cena za koupené jídlo
    @SerializedName("PrcefIn")
    public double PrcefIn;
    // cena za zkonzumované jídlo
    @SerializedName("PricefOut")
    public int PricefOut;
    // bilance
    @SerializedName("CreditTotal")
    public int CreditTotal;
}
