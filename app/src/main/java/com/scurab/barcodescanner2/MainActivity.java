package com.scurab.barcodescanner2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.scurab.barcodescanner2.base.RxLifecycleActivity;
import com.scurab.barcodescanner2.forest.Consd;
import com.scurab.barcodescanner2.forest.Consf;
import com.scurab.barcodescanner2.forest.User;
import com.scurab.barcodescanner2.forest.XsampleLahef;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import static retrofit2.converter.gson.GsonConverterFactory.create;

/**
 * Created by jbruchanov on 05/04/2018.
 */
public class MainActivity extends RxLifecycleActivity {


    //==============================================================================================
    //
    // REST pro komunikaci se sluzbou
    //
    //

    private RestApi mRestApi;

    private RestApi getApi() {
        if (mRestApi == null) {
            SharedPreferences prefs = getSharedPreferences(SetupActivity.PREFS_NAME, SetupActivity.PREFS_MODE); //zrobim preference

            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .build();

            Gson gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                    .create();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(SetupActivity.getConStr(prefs))
                    .client(client)
                    .addConverterFactory(create(gson))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();

            mRestApi = retrofit.create(RestApi.class);
        }
        return mRestApi;
    }

        /*

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


        getApi().getUsers()
                .compose(bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(users -> {
                    for (User user : users) {
                        Log.d("Activity", "user:" + user.Username);
                    }
                }, err -> {
                    err.printStackTrace();
                });
        */

    //==============================================================================================
    //
    // Zpracovani prijateho kodu
    //
    //

    private static final int ID_FOOD = 0xFFFFFFFE; //extra kod pro jidlo

    private void doCommunication(Object send, Object response) throws Exception {
        //TODO: nejako posle send
        //TODO: odpoved zkusi narvat do response. Pokud je response==null, tak me to nezajima
        //TODO: muze to vyhodit vyjimku, bude odchycena a zobrazena
    }

    //zaznam jidla
    private void logConsf() throws Exception {
        Consf c = new Consf();
        c.Imei = this.idStr; //kdo
        c.ItemfID = 0; //TODO: nevim co to je, takze radeji nula
        c.ConsfID = 0; //TODO: nevim co to je, takze radeji nula
        doCommunication(c, null); //TODO: vysledek nas asi nezajima
    }

    //zaznam sklenky
    private void logItemConsumption(int itemId, int amount) throws Exception {
        Consd c = new Consd(); //vyrobim si tridu, do ktery to naladuju
        c.Imei = this.idStr; //kdo
        c.ItemdID = (int) itemId; //co
        c.Amount = amount; //kolik
        c.ConsdID = 0; //TODO: nevim co to je, takze radeji nula
        doCommunication(c, null); //TODO: vysledek nas asi nezajima
    }

    //provedeni akce podle ciselneho kodu
    private String barcodeAction(int code) {
        try {
            switch (code) { //kody mohou mit ruzny vyznam
                case ID_FOOD: //extra kod pro jidlo
                    logConsf(); //loguj jidlo
                    break; //a slus
                default: //ostatni kody jsou jeden kus sklenice
                    logItemConsumption(code, 1);
                    break;
            }
        } catch (Exception e) { //pokud se vyskytl nejakej problem
            return e.getLocalizedMessage(); //tak to vratim jako text pro zobrazeni uzivateli
        }
        return "OK";
    }

    //procedeni akce podle kodu v textove podobe
    private String barcodeAction(String code) {
        if (code == null) return "Canceled"; //pokud neni vubec nic, tak slus nahned
        try {
            return barcodeAction(Integer.decode(code)); //zkusim z toho udelat numero
        } catch (Exception e) {
            return ("Bad code format " + code);
        }
    }

    //==============================================================================================
    //
    // Vlastni apka
    //
    //

    private boolean mIsResumed;
    private byte[] id; //idecko jako pole bajtu
    private String idStr; //idce


    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RxLifecycleActivity toJsemJa = this;

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.settings) { //pokud je to klikanec na menu setting
                    mRestApi = null; //zahodime referenci na rest, bo to muze konfigurace zmenit, tak aby se to vyrobilo znova
                    startActivity(new Intent(toJsemJa, SetupActivity.class)); //a startujeme aktivitu s nastavenima
                    return true; //nevim proc, asi ze jsem to zachytil
                }
                return false; //nevim proc, asi ze udalost nebyla zpracovana
            }
        });

        findViewById(R.id.drink).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScan();
            }
        });

        findViewById(R.id.fork).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = barcodeAction(ID_FOOD); //jako bychom naskenovali kod pro jedno jidlo
                Toast.makeText(toJsemJa, msg, Toast.LENGTH_LONG).show(); //zobrazime uzivateli, co se stalo
            }

        });

        idStr = Utils.getHwId(this); //vyrobime textovou identifikaci stroje
        id = Utils.str2id(idStr); //spachame identifikator
    }

    private void startScan() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("Scan a barcode");
        integrator.setCameraId(0);  // Use a specific camera of the device
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(true);
        integrator.setOrientationLocked(false);
        integrator.setTimeout(25000);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) { //pokud je to nejakej vysledek
            String msg = barcodeAction(result.getContents()); //zpracujeme prijaty kod
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show(); //zobrazime uzivateli, co se stalo
        } else { //co to sem leze za hovadiny
            super.onActivityResult(requestCode, resultCode, data); //hodime to dal a poptakach
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
}
