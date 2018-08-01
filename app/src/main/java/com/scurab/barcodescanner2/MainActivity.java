package com.scurab.barcodescanner2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.scurab.barcodescanner2.base.RxLifecycleActivity;
import com.scurab.barcodescanner2.forest.Consfs;
import com.scurab.barcodescanner2.forest.User;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import static retrofit2.converter.gson.GsonConverterFactory.create;
\
public class MainActivity extends RxLifecycleActivity {

    private String imei;

    private String getImei() {
        if (imei ==null) { //lina inicializace
            String idStr = Utils.getHwId(this); //vyrobime textovou identifikaci stroje
            byte[] id = Utils.str2id(idStr); //spachame identifikator
            imei =Utils.viewID(id,(char)0); //a udelam z toho retezec
            Utils.log("getImei","My IMEI "+imei);
        }
        return imei;
    }

    //==============================================================================================
    //
    // REST pro komunikaci se sluzbou
    //
    //

    private RestApi restApi;

    private RestApi getRestApi() {
        if (restApi == null) { //lina inicializace
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
                    .baseUrl(SetupActivity.getServiceUrl(prefs))
                    .client(client)
                    .addConverterFactory(create(gson))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
            restApi = retrofit.create(RestApi.class);
        }
        return restApi;
    }

    //zaznam jidla
    private void logConsf() {
        Consfs c = new Consfs();
        c.Imei = getImei(); //kdo jsem
        getRestApi().consfs(c)  //
                .compose(bindToLifecycle()) // tohle to svaze s cyklem activity na android, takze kdyz appku zavres behem tohohle dotazu, tak to vicemene zahodi
                .compose(bindToProgressBar(progressBarContainer))   //aktivace progressbaru
                .subscribeOn(Schedulers.io()) //tohle zase ze ty nasledujici callback funkce se maj zavolat v main thready, abys mohl hrabat do UI (pac android te nenecha pracovat s UI v nejakym jinym thread)
                .observeOn(AndroidSchedulers.mainThread()) //spusti se diskoteka a davas tomu 2 funkce, jedna ktera se zavola, kdyz mas vysledek a druha pro pripad problemu
                .subscribe(r -> {
                    showOk(); //hotovo
                }, err -> {
                    showError(err);
                });
    }

    private User[] users;
    private User[] getUsers() {
        if (users==null) {
            getRestApi().getUsers()
                    .compose(bindToLifecycle())
                    .compose(bindToProgressBar(progressBarContainer))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(users -> {
                        this.users = users;
                    }, err -> {
                        showError(err);
                    });
        }
        return users;
    }

    //zaznam sklenky
    private void logGlass(int itemId) throws Exception {
    }

    //==============================================================================================
    //
    // Zpracovani caroveho kodu a dalsich prikazu
    //
    //

    private static final int ID_FOOD = 0xFFFFFFFE; //extra kod pro jidlo
    private static final int ID_WINE_BOTTLE_START= 0x00000000; //pocatek kodu pro flasky vina
    private static final int ID_WINE_BOTTLE_END= 0x0000FFFF; //konec kodu pro flasky vina

    //provedeni akce podle ciselneho kodu
    private void barcodeAction(int code) {
        try {
            switch (code) { //kody mohou mit ruzny vyznam
                case ID_FOOD: //extra kod pro jidlo
                    logConsf(); //loguj jidlo
                    return; //a slus
                default: //ostatni kody jsou jeden kus sklenice
                    if ((code>=ID_WINE_BOTTLE_START)&&(code<=ID_WINE_BOTTLE_END)) {
                        logGlass(code);
                        return;
                    }
                    showError(String.format(getResources().getString(R.string.barcode_unknown_number),code)); //zobrazim chybu
                    return;
            }
        } catch (Exception e) { //pokud se vyskytl nejakej problem
            showError(e); //zobrazim chybu
        }
    }

    //procedeni akce podle kodu v textove podobe
    private void barcodeAction(String code) {
        if (code == null) { //neni nic
            showError(getResources().getString(R.string.canceled)); //pokud neni vubec nic, tak slus nahned
            return; //slus
        }
        try {
            barcodeAction(Integer.decode(code)); //zkusim z toho udelat numero a podle toho akce
        } catch (Exception e) { //pokud doslo k nejakemu problemu, tak hlasim, problem vznikne jen pri chybe dekodovani cisla
            showError(String.format(getResources().getString(R.string.barcode_bad_format),code)); //zobrazim chybu
        }
    }

    //==============================================================================================
    //
    // Vlastni apka
    //
    //

    private View progressBarContainer;

    private void showOk(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show(); //zobrazime uzivateli, co se stalo
    }

    private void showOk() {
        showOk(getResources().getString(R.string.ok)); //reknu ze dobry
    }

    private void showError(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show(); //zobrazime uzivateli, co se stalo
    }

    private void showError(Throwable err) {
        showError(err.getLocalizedMessage());
        err.printStackTrace();
    }

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBarContainer = findViewById(R.id.progress_bar_container);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.settings) { //pokud je to klikanec na menu setting
                restApi = null; //zahodime referenci na rest, bo to muze konfigurace zmenit, tak aby se to vyrobilo znova
                startActivity(new Intent(this, SetupActivity.class)); //a startujeme aktivitu s nastavenima
                return true; //nevim proc, asi ze jsem to zachytil
            }
            return false; //nevim proc, asi ze udalost nebyla zpracovana
        });
        findViewById(R.id.drink).setOnClickListener(v -> startScan());
        findViewById(R.id.fork).setOnClickListener(v -> {
            barcodeAction(ID_FOOD); //jako bychom naskenovali kod pro jedno jidlo
        });
    }

    private void startScan() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt(getString(R.string.barcode_scan));
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
            barcodeAction(result.getContents()); //zpracujeme prijaty kod
        } else { //co to sem leze za hovadiny
            super.onActivityResult(requestCode, resultCode, data); //hodime to dal a poptakach
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
}
