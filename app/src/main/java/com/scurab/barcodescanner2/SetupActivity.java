package com.scurab.barcodescanner2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.EditText;

import java.sql.Date;
import java.util.Calendar;

public class SetupActivity extends AppCompatActivity {


    //==============================================================================================
    //
    // Perzistentni konfigurace
    //
    //

    public static final int ACTIVITY_RESULT_REGISTER =0x4574;
    public static final int ACTIVITY_RESULT_UPDATE =0x3580;
    public static final int ACTIVITY_RESULT_ABOUT =0x1234;
    public static final int ACTIVITY_RESULT_ENABLEWEB=0x4321;

    public static final String PREFS_NAME = "preferences";
    public static final int PREFS_MODE = Context.MODE_PRIVATE;
    private static final String PREFS_SERVICE_URL = "service_url";
    private static final String PREFS_SERVICE_URL_DEFAULT = "https://zpa.westeurope.cloudapp.azure.com:8016/Items/api/";
    private static final String PREFS_CAMERA_ID = "camera_number";
    private static final int PREFS_CAMERA_ID_DEFAULT = 0;
    private static final String PREFS_BARCODE_TIMEOUT = "barcode_timeout";
    private static final int PREFS_BARCODE_TIMEOUT_DEFAULT = 25000;
    private static final String PREFS_LAST_BOTTLE_STORAGE_YEAR = "last bottle storage year";
    private static final int PREFS_LAST_BOTTLE_STORAGE_YEAR_DEFAULT = Calendar.getInstance().get(Calendar.YEAR); //jako default se pekne hodi letosek
    private static final String PREFS_EXTMODE = "extmode";
    private static final boolean extmode = false;


    public static String getServiceUrl(SharedPreferences prefs) {
        if (prefs == null) return PREFS_SERVICE_URL_DEFAULT; //bonbensicher und idiotenfest
        String s = prefs.getString(PREFS_SERVICE_URL, PREFS_SERVICE_URL_DEFAULT); //vytahnem ze skladu
        if (s.length() < 1)
            s = PREFS_SERVICE_URL_DEFAULT; //pokud je to nejaka hovadina, tak default
        return s; //vracim vydrenej vysledek
    }

    public static int getTimeout(SharedPreferences prefs) {
        if (prefs == null) return PREFS_BARCODE_TIMEOUT_DEFAULT;
        return prefs.getInt(PREFS_BARCODE_TIMEOUT, PREFS_BARCODE_TIMEOUT_DEFAULT);
    }

    public static int getCameraId(SharedPreferences prefs) {
        if (prefs == null) return PREFS_CAMERA_ID_DEFAULT;
        return prefs.getInt(PREFS_CAMERA_ID, PREFS_CAMERA_ID_DEFAULT);
    }

    public static int getLastBottleStorageYear(SharedPreferences prefs) {
        if (prefs == null) return PREFS_LAST_BOTTLE_STORAGE_YEAR_DEFAULT;
        return prefs.getInt(PREFS_LAST_BOTTLE_STORAGE_YEAR, PREFS_LAST_BOTTLE_STORAGE_YEAR_DEFAULT);
    }

    public static void setLastBottleStorageYear(SharedPreferences prefs, int year) {
        if (prefs!=null) {
            prefs.edit().putInt(PREFS_LAST_BOTTLE_STORAGE_YEAR,year).apply();
        }
    }

    public static void setExtendMode(SharedPreferences prefs, boolean mode) {
        if (prefs!=null) {
            prefs.edit().putBoolean(PREFS_EXTMODE,mode).apply();
        }
    }

    public static boolean getExtmode(SharedPreferences prefs) {
        if (prefs == null) return false;
        return prefs.getBoolean(PREFS_EXTMODE,false);
    }

    //==============================================================================================
    //
    // Vlastni aktivita a jeji pomocnici
    //
    //

    private void storeData() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, PREFS_MODE); //zrobim preference
        prefs.edit().putString(PREFS_SERVICE_URL, ((EditText) findViewById(R.id.connection_string)).getText().toString()).apply();
        prefs.edit().putInt(PREFS_BARCODE_TIMEOUT, Integer.decode(((EditText) findViewById(R.id.barcode_timeout)).getText().toString())).apply();
        prefs.edit().putInt(PREFS_CAMERA_ID, Integer.decode(((EditText) findViewById(R.id.camera_id)).getText().toString())).apply();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        //TODO: nacpat do setuptitulku taky verzi
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, PREFS_MODE); //zrobim preference
        ((Button) findViewById(R.id.register)).setText(getString(R.string.register)+"\n"+getString(R.string.unregister));
        ((EditText) findViewById(R.id.connection_string)).setText(getServiceUrl(prefs)); //url
        ((EditText) findViewById(R.id.barcode_timeout)).setText(String.valueOf(getTimeout(prefs))); //timeoutu
        ((EditText) findViewById(R.id.camera_id)).setText(String.valueOf(getCameraId(prefs))); //editor kamery
        //cudl about
        ((Button) findViewById(R.id.about)).setOnClickListener(v -> { //po klofnuti na cudl
            storeData(); //ulozime novy data
            setResult(ACTIVITY_RESULT_ABOUT); //rikam ze se chce update
            this.finish(); //a koncime
        });
        //cudl update
        ((Button) findViewById(R.id.update)).setOnClickListener(v -> { //po klofnuti na cudl
            storeData(); //ulozime novy data
            setResult(ACTIVITY_RESULT_UPDATE); //rikam ze se chce update
            this.finish(); //a koncime
        });
        //cudl enable web
        ((Button) findViewById(R.id.enableweb)).setOnClickListener(v -> { //po klofnuti na cudl
            storeData(); //ulozime novy data
            setResult(ACTIVITY_RESULT_ENABLEWEB); //rikam ze se chce update
            this.finish(); //a koncime
        });
        //cudl aplikujici zmeny
        ((Button) findViewById(R.id.apply)).setOnClickListener(v -> { //po klofnuti na cudl
            storeData(); //ulozime novy data
            setResult(Activity.RESULT_OK); //rikam ze je to ok
            this.finish(); //a koncime
        });
        //cudl registrace zmeny
        ((Button) findViewById(R.id.register)).setOnClickListener(v -> { //po klofnuti na cudl
            storeData(); //ulozime novy data
            setResult(ACTIVITY_RESULT_REGISTER); //rikam ze je to ok
            this.finish(); //a koncime
        });
    }
}
