package com.scurab.barcodescanner2;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.scurab.barcodescanner2.base.RxLifecycleActivity;
import com.scurab.barcodescanner2.forest.Consds;
import com.scurab.barcodescanner2.forest.Consfs;
import com.scurab.barcodescanner2.forest.ItemfView;
import com.scurab.barcodescanner2.forest.RestApi;
import com.scurab.barcodescanner2.forest.UserDevices;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import static retrofit2.converter.gson.GsonConverterFactory.create;


public class MainActivity extends RxLifecycleActivity {

    private String imei;

    private String getImei() {
        if (imei == null) { //lina inicializace
            String idStr = Utils.getHwId(this); //vyrobime textovou identifikaci stroje
            byte[] id = Utils.str2id(idStr); //spachame identifikator
            imei = Utils.viewID(id, (char) 0); //a udelam z toho retezec
            Utils.log("getImei", "My IMEI " + imei);
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
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .build();
            Gson gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                    .create();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(SetupActivity.getServiceUrl(getSharedPreferences()))
                    .client(client)
                    .addConverterFactory(create(gson))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
            restApi = retrofit.create(RestApi.class);
        }
        return restApi;
    }

    //Muzeme vesele logovat, protoze zname itemfID
    private void logConsf(int itemfID) {
        Consfs c = new Consfs();
        c.Imei = getImei(); //kdo jsem
        c.ItemfID = itemfID; //k jakymu jidlu se hlasim
        getRestApi().consfs(c).compose(common()).subscribe(r -> showOk(getResources().getString(R.string.logConsfOk)), err -> showError(getResources().getString(R.string.logConsfError),err));
    }

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("hh:MM ", Locale.getDefault());

    private String[] cosfToStrings(ItemfView[] items) {
        int len=items.length; //kolik toho bude
        String[] choices = new String[len]; //vyrobime pole
        for (int i = 0; i < len; i++) { //a plnime datama
            choices[i]=(SIMPLE_DATE_FORMAT.format(items[i].DtInsert))+items[i].Username; //naformatujeme do lidskeho tvaru
        }
        return choices;
    }

    //Zaznam jidla. Nezname itemfID, takze ho musime vytahnout ze sluzby, pokud jich je vic, tak nechat vybrat a nasledne zalogovat.
    private void logConsf() {
        //getRestApi().getAvailableItemfs().compose(common()).subscribe(items -> { //nejprve si zjistime, k jakymu jidlu se budeme prihlasovat
        Calendar c= Calendar.getInstance();
        String dateForMeca=c.get(Calendar.YEAR)+"-"+(c.get(Calendar.MONTH)+1)+"-"+c.get(Calendar.DAY_OF_MONTH);
        getRestApi().GetForDayInserted(dateForMeca).compose(common()).subscribe(items -> { //nejprve si zjistime, k jakymu jidlu se budeme prihlasovat
            int len = items.length; //kolik je polozek
            if (len < 1) { //nejsou zadny polozky
                showError(getResources().getString(R.string.logConsfNothink));
                return; //pokud neni z ceho vybirat, tak nic
                /* i jednojidlo se bude vybirat
            } else if (len == 1) { //pokud mame prave jednu polozku
                logConsf(items[0].ItemfID); //tak muzeme rovnou logovat bez nejakejch problemu
                */
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this); //zrobime buildera dialogu
                builder.setTitle(getResources().getString(R.string.logConsfSelect)); //titulek a zprava
                builder.setSingleChoiceItems(cosfToStrings(items), 0, (dialog, which) -> { //poslouchadlo na klikanec
                    dialog.dismiss();
                    logConsf(items[which].ItemfID); //a to je von, trada logovat
                });
                AlertDialog dialog = builder.create(); //vyrobime dialog
                dialog.show(); //a zobrazim to
            }
        }, err -> showError(getResources().getString(R.string.logConsfError),err));
    }

    //zaznam sklenky
    private void logGlass(int itemId) throws Exception {
        Consds c=new Consds();
        c.Imei=getImei();
        c.ItemdID=itemId;
        getRestApi().consds(c).compose(common()).subscribe(r -> showOk(getResources().getString(R.string.logGlassOk)), err -> showError(getResources().getString(R.string.logGlassError),err));
    }

    private void logGlassExt() throws Exception {
        AlertDialog.Builder builder = new AlertDialog.Builder(this); //zrobime buildera dialogu
        builder.setTitle(getResources().getString(R.string.drink_ext_title)); //titulek a zprava
        String[] items={
                getResources().getString(R.string.drink_ext_half),
                getResources().getString(R.string.drink_ext_bottle),
        };
        builder.setSingleChoiceItems(items, 0, (dialog, which) -> { //poslouchadlo na klikanec
            dialog.dismiss();
            String what=items[which]; //tak co to bude
            if (what.equalsIgnoreCase(getResources().getString(R.string.drink_ext_half))) {
                startScan(SCANNER_MODE_HALF,what); //spoustime skenovani
            } else if (what.equalsIgnoreCase(getResources().getString(R.string.drink_ext_bottle))) {
                startScan(SCANNER_MODE_BOTTLE,what); //spoustime skenovani
            }
        });
        AlertDialog dialog = builder.create(); //vyrobime dialog
        dialog.show(); //a zobrazim to
    }

    private void allowWebAccess() {
        getRestApi().AllowWebAccess(getImei()).compose(common()).subscribe(r -> showOk(getResources().getString(R.string.allowWebAccessOk)), err -> showError(getResources().getString(R.string.allowWebAccessError),err));
    }

    //registrace telefounu
    private void register(String username) {
        UserDevices ud=new UserDevices();
        ud.Username=username;
        ud.Imei=getImei();
        getRestApi().registrerDevice(ud).compose(common()).subscribe(r -> showOk(getResources().getString(R.string.registerOk)), err -> showError(getResources().getString(R.string.logGlassError),err));
    }

    //odregistrovani po predeslem registrovani uzivatelskeho jmena
    private void unregister() {
        getRestApi().unregistrerDevice(getImei()).compose(common()).subscribe(r -> showOk(getResources().getString(R.string.unregisterOk)), err -> showError(getResources().getString(R.string.unregisterError),err));
    }


    //registrace se zadanim uzivatelskeho jmena
    private void register() {
        final EditText input = new EditText(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this); //zrobime buildera dialogu
        builder.setTitle(R.string.user_register_title); //titulek a zprava
        input.setImeOptions(EditorInfo.IME_ACTION_DONE);
        input.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT));
        builder.setView(input);
        builder.setPositiveButton(R.string.register, (dialog, which) -> register(input.getText().toString())); //a jdem to registrovat
        builder.setNegativeButton(R.string.unregister, (dialog, which) -> unregister()); //a jdem to odregistrovat
        AlertDialog dialog = builder.create(); //vyrobime dialog
        dialog.show(); //a zobrazim to
    }

    //==============================================================================================
    //
    // Zpracovani caroveho kodu a dalsich prikazu
    //
    //

    private static final int ID_EXTRAS=99000000; //extra kody
    private static final int ID_EXT_MODE = ID_EXTRAS+0; //logovani jidla
    private static final int ID_EXT_MODE_END = ID_EXTRAS+1; //logovani jidla
    private static final int ID_FOOD = ID_EXTRAS+2; //logovani jidla
    private static final int ID_SHOW = ID_EXTRAS+3; //ukaz ruzny statistiky
    private static final int ID_BUY_FOOD = ID_EXTRAS+4; //zakoupeni jidla
    private static final int ID_BUY_FOOD_UNDO = ID_EXTRAS+5; //zakoupeni jidla
    private static final int ID_STORE_BOTTLE = ID_EXTRAS+6; //naskladneni flasky
    private static final int ID_STORE_BOTTLE_UNDO = ID_EXTRAS+7; //odskladneni flasky
    private static final int ID_DRINK_EXT= ID_EXTRAS+8; //extra sklinka
    private static final int ID_ALLOW_WEB_ACCESS= ID_EXTRAS+9; //povolime web

    private static final int ID_WINE_BOTTLE_START = 100001; //pocatek kodu pro flasky vina
    private static final int ID_WINE_BOTTLE_END =   199999; //konec kodu pro flasky vina

    private boolean barcodeActionBoolean(int code) {
        barcodeAction(code);
        return true;
    }

    //provedeni akce podle ciselneho kodu
    private void barcodeAction(int code) {
        try {
            switch (code) { //kody mohou mit ruzny vyznam
                case ID_EXT_MODE:
                    SetupActivity.setExtendMode(getSharedPreferences(),true);
                    setExtendedMode(true);
                    break;
                case ID_EXT_MODE_END:
                    SetupActivity.setExtendMode(getSharedPreferences(),false);
                    setExtendedMode(false);
                    break;
                case ID_FOOD: //extra kod pro jidlo
                    logConsf(); //loguj jidlo
                    return; //a slus
                case ID_STORE_BOTTLE:
                    startScan(SCANNER_MODE_STORE_BOTTLE,"");
                    return;
                case ID_STORE_BOTTLE_UNDO:
                    startScan(SCANNER_MODE_STORE_BOTTLE_UNDO,"");
                    return;
                case ID_SHOW:
                    startActivity(new Intent(this, ListActivity.class));
                    return;
                case ID_DRINK_EXT:
                    logGlassExt();
                    return;
                case ID_ALLOW_WEB_ACCESS:
                    allowWebAccess();
                    return;
                case ID_BUY_FOOD:
                case ID_BUY_FOOD_UNDO:
                default: //ostatni kody jsou jeden kus sklenice
                    if ((code >= ID_WINE_BOTTLE_START) && (code <= ID_WINE_BOTTLE_END)) {
                        logGlass(code);
                        return;
                    }
                    showError(String.format(getResources().getString(R.string.barcode_unknown_number), code)); //zobrazim chybu
                    return;
            }
        } catch (Exception e) { //pokud se vyskytl nejakej problem
            showError(e); //zobrazim chybu
        }
    }

    //procedeni akce podle kodu v textove podobe
    private void barcodeAction(String code) {
        if (code == null) { //neni nic
            showError(getResources().getString(R.string.barcode_error)); //pokud neni vubec nic, tak slus nahned
            return; //slus
        }
        try {
            barcodeAction(Integer.decode(code)); //zkusim z toho udelat numero a podle toho akce
        } catch (Exception e) { //pokud doslo k nejakemu problemu, tak hlasim, problem vznikne jen pri chybe dekodovani cisla
            showError(String.format(getResources().getString(R.string.barcode_bad_format), code)); //zobrazim chybu
        }
    }

    //==============================================================================================
    //
    // Vlastni apka
    //
    //

    private View progressBarContainer;
    private SharedPreferences preferences;
    private static final String SCANNER_MODE="scanner_mode"; //jak to prase si to ulozim do preferences, sichr je sichr
    private static final int SCANNER_MODE_BASE =1;  //normalni provoz, tedy akce podle naskenovaneho kodu
    private static final int SCANNER_MODE_STORE_BOTTLE =2;  //vkladame flasku
    private static final int SCANNER_MODE_STORE_BOTTLE_UNDO =3; //odstraneni vlozene flasky
    private static final int SCANNER_MODE_HALF =4; //pulsklenka
    private static final int SCANNER_MODE_BOTTLE =5; //cela flaska

    private int getScannerMode() {
        return getSharedPreferences().getInt(SCANNER_MODE,SCANNER_MODE_BASE);
    }

    private void setScannerMode(int mode) {
        getSharedPreferences().edit().putInt(SCANNER_MODE,mode).apply();
    }

    private SharedPreferences getSharedPreferences() {
        if (preferences == null) {
            preferences = getSharedPreferences(SetupActivity.PREFS_NAME, SetupActivity.PREFS_MODE); //zrobim preference
        }
        return preferences;
    }

    //Nastaveni rozsireneho modu
    private void setExtendedMode(boolean ext) {
        int visibility=ext?View.VISIBLE:View.GONE;
        findViewById(R.id.buyFood).setVisibility(visibility);
        findViewById(R.id.buyFoodUndo).setVisibility(visibility);
        findViewById(R.id.storeBottle).setVisibility(visibility);
        findViewById(R.id.storeBottleUndo).setVisibility(visibility);
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
                startActivityForResult(new Intent(this, SetupActivity.class),SetupActivity.ACTIVITY_RESULT_REGISTER); //a startujeme aktivitu s nastavenima
                return true; //nevim proc, asi ze jsem to zachytil
            }
            return false; //nevim proc, asi ze udalost nebyla zpracovana
        });
        findViewById(R.id.drink).setOnClickListener(v -> startScan(SCANNER_MODE_BASE,"")); //simulovane naskenovani pomoci cudlu
        findViewById(R.id.drink).setOnLongClickListener(v -> barcodeActionBoolean(ID_DRINK_EXT)); //simulovane naskenovani pomoci cudlu
        findViewById(R.id.fork).setOnClickListener(v -> barcodeAction(ID_FOOD)); //simulovane naskenovani pomoci cudlu
        findViewById(R.id.show).setOnClickListener(v -> barcodeAction(ID_SHOW)); //simulovane naskenovani pomoci cudlu
        findViewById(R.id.show).setOnLongClickListener(v -> barcodeActionBoolean(ID_ALLOW_WEB_ACCESS)); //simulovane naskenovani pomoci cudlu
        findViewById(R.id.buyFood).setOnClickListener(v -> barcodeAction(ID_BUY_FOOD)); //simulovane naskenovani pomoci cudlu
        findViewById(R.id.buyFoodUndo).setOnClickListener(v -> barcodeAction(ID_BUY_FOOD_UNDO)); //simulovane naskenovani pomoci cudlu
        findViewById(R.id.storeBottle).setOnClickListener(v -> barcodeAction(ID_STORE_BOTTLE)); //simulovane naskenovani pomoci cudlu
        findViewById(R.id.storeBottleUndo).setOnClickListener(v -> barcodeAction(ID_STORE_BOTTLE_UNDO)); //simulovane naskenovani pomoci cudlu
        setExtendedMode(SetupActivity.getExtmode(getSharedPreferences()));
    }

    private void startScan(int mode, String msg) {
        if (msg==null) msg=getResources().getString(R.string.scan_a_barcode); //kdyz neni, dame universalni
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt(msg);
        integrator.setCameraId(SetupActivity.getCameraId(getSharedPreferences()));  // Use a specific camera of the device
        integrator.setTimeout(SetupActivity.getTimeout(getSharedPreferences())); //jakej to bude mit timeoutek
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(true);
        integrator.setOrientationLocked(false);
        setScannerMode(mode); //poznamenam si pozadovany rezim scanovani
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode==SetupActivity.ACTIVITY_RESULT_REGISTER)&&(resultCode==SetupActivity.ACTIVITY_RESULT_REGISTER)) { //pokud setup chce udelat registraci
            register();
            return;
        }
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) { //pokud je to nejakej vysledek
            int mode=getScannerMode(); //ztisim, v jakym rezimu jsme scaner volali
            setScannerMode(SCANNER_MODE_BASE); //a preklapime na zaklad, to se dela vzdycky, tak uz muzeme tadyk
            switch (mode) {
                case SCANNER_MODE_BASE:
                    barcodeAction(result.getContents()); //zpracujeme prijaty kod
                    break;
                case SCANNER_MODE_STORE_BOTTLE:
                    showError("Delame ze nakladam flasku "+result.getContents());
                    break;
                case SCANNER_MODE_STORE_BOTTLE_UNDO:
                    showError("Delame ze rusime flasku "+result.getContents());
                    break;
                case SCANNER_MODE_HALF:
                    showError("Pulsklenice "+result.getContents());
                    break;
                case SCANNER_MODE_BOTTLE:
                    showError("Cela flaska "+result.getContents());
                    break;
                default:
                    showError("Unknown scanner mode "+mode);
                    break;
            }
        } else { //co to sem leze za hovadiny
            super.onActivityResult(requestCode, resultCode, data); //hodime to dal a poptakach
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    protected View getProgressBarContainer() {
        return progressBarContainer;
    }
}
