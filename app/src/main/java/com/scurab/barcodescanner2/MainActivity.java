package com.scurab.barcodescanner2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.scurab.barcodescanner2.forest.XsampleLahef;
import com.scurab.barcodescanner2.forest.XsampleLahefResponse;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.security.MessageDigest;

import android.telephony.TelephonyManager;
import android.provider.Settings;
import android.app.Activity;
import android.content.Context;

/**
 * Created by jbruchanov on 05/04/2018.
 */
public class MainActivity extends AppCompatActivity {


    private static final String MESSAGE_DIGEST_ALGORITHM = "SHA-256"; //hasovaci algoritmus

    //==============================================================================================
    //
    // Staticke uzitecnosti

    private static void log(String tag, String msg) {
        Log.d(tag, msg);
    }

    private static void logLiveCycle(String msg) {
        log("Main activity live cycle", msg);
    }

    /**
     * Převod ID v podobě řetězce na pole bajtů.
     *
     * @param id řetězec
     * @return pole bajtů
     */
    public static byte[] str2id(String id) {
        try {
            MessageDigest md = MessageDigest.getInstance(MESSAGE_DIGEST_ALGORITHM); //spachame michacku (pokud problem, tak to padne)
            md.reset(); //pro sichr restart
            md.update(id.getBytes()); //vetkneme IDcko
            return md.digest();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Převod bloku bajtů na čitelný řetězec
     *
     * @param id pole
     * @return řetězec
     */
    public static String viewID(byte[] id, char separator) {
        StringBuilder sb = new StringBuilder(id.length * 3);
        for (byte anId : id) {
            sb.append(Integer.toHexString((anId >> 4) & 15));
            sb.append(Integer.toHexString(anId & 15));
            if (separator > 0) {
                sb.append(separator);
            }
        }
        return sb.toString();
    }

    /**
     * Redukce klíče
     *
     * @param key    vstupní klíč
     * @param newLen délka redukovaného klíče
     * @return redukovaný klíč
     */
    public static byte[] reduceKey(byte[] key, int newLen) {
        byte[] r = new byte[newLen]; //pole s vysledkem
        for (int i = 0; i < newLen; i++) { //neco tam napereme
            r[i] = (byte) i; //at to neni jen nulovy
        }
        int p = 0; //zapisovaci ukazovadlo
        for (byte aKey : key) { //projizdime zdroj
            r[p++] ^= aKey; //prixornem novy data
            if (p >= newLen) { //a zakruhujeme
                p = 0; //ukazovadlo v redukovanem klici
            }
        }
        return r; //a vracime vysledek
    }


    //==============================================================================================
    //
    // Akce okoli zjisteni spolehliveho ID vlastni cestou nutne potrebuji pristup k nekterym vecem,
    // ke kterym se z obecne tridy dostat nelze (getSystemService,getApplicationContext,atp),
    // takze jsem to hrde napral do (zatim jedine) hlavni aktivity cele aplikace.
    //


    private void ipAddrShow() {
        try {
            Process process = Runtime.getRuntime().exec("ip addr show");
            BufferedReader drd = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = drd.readLine()) != null) {
                log("ipAddrShow", line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Ve vystupu prikazu ip 'addr show' hleda mac adresu zadaneho zarizeni
     *
     * @param device zarizeni, jehoz macku hledame (treba wlan0 atp)
     * @return mac adresa v podobe retezce, pokud nenalezeno, pak null
     */
    protected static String getMacAddr(String device) {
        String r = null; //zatim nezname
        try {
            BufferedReader drd = new BufferedReader(new InputStreamReader((Runtime.getRuntime().exec("ip addr show")).getInputStream()));
            int state = 0; //stav na hledani zarizeni
            String line;
            while ((line = drd.readLine()) != null) {
                switch (state) {
                    case 0: //hledame pozadovane zarizeni
                        state = (line.contains(device)) ? 1 : 0; //pokud tam je, tak hura a hledame jeho macku
                        break;
                    case 1: //hledame macku podle
                        int pos = line.indexOf("link/ether"); //je tam co hledame
                        if (pos > 0) { //pokud jo
                            int start = line.indexOf(' ', pos) + 1; //hledame mezeru
                            int stop = line.indexOf(' ', start); //hledame dalsi mezeru
                            r = line.substring(start, stop); //vystrihneme macku
                            state = 2; //a prechazime do koncoveho stavu
                        }
                        break;
                    default: //uz jsme nasli, tak to jen dovycteme
                        break;
                }
            }
            drd.close(); //pro sichr zavirame kram
        } catch (Exception e) { //pokud cestou nejakej problem
            r = null; //tak jsme proste nic nenasli
        }
        return r; //vracime tezce vydrenej vysledek
    }

    /**
     * Vyroba jedinecneho identifikacniho retezce zarizeni.
     * Identifikacni retezec je lidsky citelny a je poskladan z EMAI telefonu, pripadne z MAC adresy
     * wifiny a pokud neni dostupne nic z predchozich dvou, pak je pouzito mene spolehlive ANDROID_ID
     *
     * @return ID zarizeni v podobe retezce
     */
    @SuppressLint("MissingPermission")
    static String getHwId(Activity a) {
        final String errMsg = "error"; //hlaska pro chybu
        StringBuilder sb = new StringBuilder("HWID:"); //na uvod dame tohle
        int idct = 0; //pocitadlo pouzitejch identifikatoru
        sb.append(" tel="); //emai
        try {
            sb.append(((TelephonyManager) a.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId());
            idct++;
        } catch (Exception e) {
            sb.append(errMsg);
        }
        sb.append(" wifi="); //wifina
        try {
            sb.append(getMacAddr("wlan0"));
            idct++;
        } catch (Exception e) {
            sb.append(errMsg);
        }
        if (idct == 0) { //android id v pripade, ze predchozi dva se nepodarilo natankovat
            sb.append(" android=");
            try {
                sb.append(Settings.Secure.getString(a.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID));
                idct++;
            } catch (Exception e) {
                sb.append(errMsg);
            }
        }
        if (idct == 0) { //pokud neni nic, tak je to hodne spatny
            throw new NullPointerException("No usefull ID for this device");
        }
        String result = sb.toString(); //spachame retezec
        log("MyAndroidId", result); //poslem si to do konzolky, se muze hodit
        return result; //a smytec
    }

    //==============================================================================================
    //
    // REST pro komunikaci se sluzbou
    //
    //

    private RestApi mRestApi;

    private RestApi getApi() {
        if (mRestApi == null) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://www.mujwebik.cz/")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            mRestApi = retrofit.create(RestApi.class);
        }
        return mRestApi;
    }

    public void sendRestCall() {
        XsampleLahef l = new XsampleLahef();
        l.name = "Kontusovka";
        l.year = 1920;
        l.voltaaaz = 40;
        l.volume = 0.75;

        final Call<XsampleLahefResponse> call = getApi().send(l);
        //tohle je nejjednodussi,
        //pokud bys potreboval nejak retezit "pekne" a nebo delat neco slozitejsiho
        //tak na to je pak dalsi pekna libka...
        call.enqueue(new Callback<XsampleLahefResponse>() {
            @Override
            public void onResponse(@NonNull Call<XsampleLahefResponse> call, @NonNull Response<XsampleLahefResponse> response) {
                if (mIsResumed) {
                    //potreba bejt jistej, ze appka bezi, jinak delas neco na "pozadi" a nemusi to fachat
                }
            }

            @Override
            public void onFailure(@NonNull Call<XsampleLahefResponse> call, @NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    //==============================================================================================
    //
    // Vlastni apka
    //
    //

    private TextView mTextView;
    private boolean mIsResumed;
    private byte[] id; //idecko jako pole bajtu
    private String idStr; //idce


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = findViewById(R.id.result);

        findViewById(R.id.scan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScan();
            }
        });

        idStr = getHwId(this); //vyrobime textovou identifikaci stroje
        id = str2id(idStr); //spachame identifikator
    }

    private void startScan() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("Scan a barcode");
        integrator.setCameraId(0);  // Use a specific camera of the device
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(true);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            final String scanResult = result.getContents();
            if (scanResult == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                mTextView.setText(scanResult);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsResumed = true;
    }

    @Override
    protected void onPause() {
        mIsResumed = false;
        super.onPause();
    }

}
