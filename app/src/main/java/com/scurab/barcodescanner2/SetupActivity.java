package com.scurab.barcodescanner2;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SetupActivity extends AppCompatActivity {


    //==============================================================================================
    //
    // Perzistentni konfigurace
    //
    //

    public static String PREFS_NAME="preferences";
    public static int PREFS_MODE=Context.MODE_PRIVATE;
    private static String PREFS_CON_STR = "connection_string";
    private static String PREFS_CON_STR_DEFAULT="https://nela.zpa.cz:8016/";

    public static String getConStr(SharedPreferences prefs) {
        if (prefs==null) return PREFS_CON_STR_DEFAULT; //bonbensicher und idiotenfest
        String s=prefs.getString(PREFS_CON_STR, PREFS_CON_STR_DEFAULT); //vytahnem ze skladu
        if (s.length()<1) s=PREFS_CON_STR_DEFAULT; //pokud je to nejaka hovadina, tak default
        return s; //vracim vydrenej vysledek
    }

    //==============================================================================================
    //
    // Vlastni aktivita a jeji pomocnici
    //
    //

    private void storeData() {
        EditText connectionStringEditText = (EditText) findViewById(R.id.connection_string); //vytahnem si referenci na policko s textnem
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME,PREFS_MODE ); //zrobim preference
        prefs.edit().putString(PREFS_CON_STR, connectionStringEditText.getText().toString()).apply(); //naperem to do preferencesu
        connectionStringEditText.setText(getConStr(prefs));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME,PREFS_MODE ); //zrobim preference

        //editor connection stringu
        EditText connectionStringEditText = (EditText) findViewById(R.id.connection_string); //vytahnem si referenci na policko s textnem
        connectionStringEditText.setText(getConStr(prefs)); //naperem do nej posledni znamou hodnotu

        //cudl aplikujici zmeny
        Button applyButton = (Button) findViewById(R.id.apply); //vytahnem si referenci butonek
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storeData();
            }
        });

        /* naprd, ale pro pripad potreby se inspirovat
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });         */
    }

}
