package com.scurab.barcodescanner2;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.scurab.barcodescanner2.base.DayInfo;
import com.scurab.barcodescanner2.base.RxLifecycleActivity;
import com.scurab.barcodescanner2.forest.Consds;
import com.scurab.barcodescanner2.forest.Consfs;
import com.scurab.barcodescanner2.forest.ItemfView;
import com.scurab.barcodescanner2.forest.RestApi;
import com.scurab.barcodescanner2.forest.UserDevices;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import io.reactivex.functions.Consumer;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import static retrofit2.converter.gson.GsonConverterFactory.create;


public class MainActivity extends RxLifecycleActivity {

    private static final int ID_EXTRAS = 99000000; //extra kody
    private static final int ID_EXT_MODE = ID_EXTRAS + 0; //logovani jidla
    private static final int ID_EXT_MODE_END = ID_EXTRAS + 1; //logovani jidla

    //==============================================================================================
    //
    // REST pro komunikaci se sluzbou
    //
    //
    private static final int ID_FOOD = ID_EXTRAS + 2; //logovani jidla
    private static final int ID_LOG = ID_EXTRAS + 3; //ukaz zakladni log
    private static final int ID_BUY_FOOD = ID_EXTRAS + 4; //zakoupeni jidla
    private static final int ID_BUY_FOOD_UNDO = ID_EXTRAS + 5; //zakoupeni jidla
    private static final int ID_STORE_BOTTLE = ID_EXTRAS + 6; //naskladneni flasky
    private static final int ID_STORE_BOTTLE_UNDO = ID_EXTRAS + 7; //odskladneni flasky
    private static final int ID_EXT_MENU = ID_EXTRAS + 8; //extra menu
    private static final int ID_ALLOW_WEB_ACCESS = ID_EXTRAS + 9; //povolime web
    private static final int ID_LOG_EXT = ID_EXTRAS + 10; //rozsireny log
    private static final int ID_LOG_HALP_GLASS= ID_EXTRAS + 11; //rozsireny log
    private static final int ID_WINE_BOTTLE_START = 100001; //pocatek kodu pro flasky vina
    private static final int ID_WINE_BOTTLE_END = 199999; //konec kodu pro flasky vina
    private static final String SCANNER_MODE = "scanner_mode"; //jak to prase si to ulozim do preferences, sichr je sichr


    //==============================================================================================
    //
    // Zpracovani caroveho kodu a dalsich prikazu
    //
    //
    private static final int SCANNER_MODE_BASE = 1;  //normalni provoz, tedy akce podle naskenovaneho kodu
    private static final int SCANNER_MODE_STORE_BOTTLE = 2;  //vkladame flasku
    private static final int SCANNER_MODE_STORE_BOTTLE_UNDO = 3; //odstraneni vlozene flasky
    private static final int SCANNER_MODE_HALF = 4; //pulsklenka
    private static final int SCANNER_MODE_BOTTLE = 5; //cela flaska

    //Muzeme vesele logovat, protoze zname itemfID
    private void logConsf(int itemfID) {
        Consfs c = new Consfs();
        c.Imei = getImei(); //kdo jsem
        c.ItemfID = itemfID; //k jakymu jidlu se hlasim
        getRestApi().consfs(c).compose(common()).subscribe(r -> {
            showOk(getResources().getString(R.string.log_consf_ok));
            updateDayInfo();
        }, err -> showError(getResources().getString(R.string.log_consf_error), err));
    }

    //prevede seznam jidel na retezce
    private String[] cosfToStrings(ItemfView[] items) {
        SimpleDateFormat fc = new SimpleDateFormat("dd.MM.yyyy ", Locale.getDefault());
        int len = items.length; //kolik toho bude
        String[] choices = new String[len]; //vyrobime pole
        for (int i = 0; i < len; i++) { //a plnime datama
            choices[i] = (fc.format(items[i].DtInsert)) + items[i].Username; //naformatujeme do lidskeho tvaru
        }
        return choices;
    }

    //zaznam sklenky
    private void logGlass(int itemId, double amount) throws Exception {
        if (itemId>=0) {
            Consds c = new Consds();
            c.Imei = getImei();
            c.ItemdID = itemId;
            c.Amount=amount;
            getRestApi().consds(c).compose(common()).subscribe(r -> {
                showOk(getResources().getString(R.string.log_glass_ok));
                updateDayInfo();
            }, err -> showError(getResources().getString(R.string.log_glass_error), err));
        }
    }

    private void logGlass(String itemId, double amount)  {
        try {
            logGlass(barcode2int(itemId), amount);
        } catch (Exception e) {
        }
    }

    //Povoleni pristupu na web
    private void allowWebAccess() {
        getRestApi().AllowWebAccess(getImei()).compose(common()).subscribe(r -> showOk(getResources().getString(R.string.allow_web_access_ok)), err -> showError(getResources().getString(R.string.allow_web_access_error), err));
    }

    //registrace telefounu
    private void register(String username) {
        UserDevices ud = new UserDevices();
        ud.Username = username;
        ud.Imei = getImei();
        getRestApi().registrerDevice(ud).compose(common()).subscribe(r -> showOk(getResources().getString(R.string.register_ok)), err -> showError(getResources().getString(R.string.register_error), err));
    }

    //odregistrovani po predeslem registrovani uzivatelskeho jmena
    private void unregister() {
        getRestApi().unregistrerDevice(getImei()).compose(common()).subscribe(r -> showOk(getResources().getString(R.string.unregister_ok)), err -> showError(getResources().getString(R.string.unregister_error), err));
    }

    //spusteni aktualiace apky
    private void update(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url.trim())); //a zkusime donutit robutka, aby to sosnul
        startActivity(intent); //trada
    }

    //Zruseni naskladneni lahve
    private void storeBottleUndo(String sid) {
        int id=barcode2int(sid); //prevedeme retezec na cislo
        if (id<0) return; //pokud to nedopadlo, tak slus
        getRestApi().deleteItemd(id).compose(common()).subscribe(r -> showOk(getResources().getString(R.string.bottle_delete_ok)), err -> showError(getResources().getString(R.string.bottle_delete_err), err));
    }

    //==============================================================================================
    //
    // Vlastni apka
    //
    //

    private void about() {
        showOk(getResources().getString(R.string.app_name),getResources().getString(R.string.app_version));
    }

    private void updateConfirm(String url) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this); //zrobime buildera dialogu
        builder.setTitle(R.string.update); //nejakej pokec
        builder.setMessage(getResources().getString(R.string.update_info)); //nadpis je aktualizace
        builder.setPositiveButton(R.string.update, (dialog, which) -> update(url)); //jdem do aktualizace
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {}); //nechceme aktualizovat
        AlertDialog dialog = builder.create(); //vyrobime dialog
        dialog.show(); //a zobrazim to
    }

    //pokus o aktualizaci
    private void update() {
        Request request = new Request.Builder() //stvorime pozadavek
                .url(getResources().getString(R.string.update_file_url)) //do nej naperu cestu k souboru.
                .build(); //hotovo
        OkHttpClient okHttpClient = new OkHttpClient(); //stvorim klienata
        getProgressBarContainer().setVisibility(View.VISIBLE); //ukaz progress
        okHttpClient
                .newCall(request) //tomu naperu pozadavek
                .enqueue(new Callback() { //a jedem s medem. Az to bude, tak callback
                    @Override
                    public void onFailure(Call call, IOException e) { //nejak to nedopadlo
                        runOnUiThread( () -> {
                            if(!isFinishing()) {
                                getProgressBarContainer().setVisibility(View.GONE); //schovej progress
                                showError(getResources().getString(R.string.update), e);
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException { //huraaa
                        runOnUiThread( () -> {
                            if(!isFinishing()) {
                                getProgressBarContainer().setVisibility(View.GONE); //schovej progress
                                try { //co se muze, to se podela, takze odlov problemu
                                    if (response.code()!=200) {
                                        throw new Exception(response.message());
                                    }
                                    BufferedReader br = new BufferedReader(response.body().charStream()); //zrobime pismenkoctecku
                                    String version;
                                    do {
                                        version = br.readLine(); //natankujeme verzi,
                                    } while (version.length() < 1); //prazdny radky zahazujeme
                                    String url;
                                    do {
                                        url = br.readLine(); //natankujeme url,
                                    } while (url.length() < 1); //prazdny radky zahazujeme
                                    br.close(); //rusime ctecku
                                    if (version.trim().equalsIgnoreCase(getResources().getString(R.string.app_version))) { //pokud tuhle verzi mame
                                        showOk(getResources().getString(R.string.update_actual)); //tak to nahlasim a muzeme pomalu koncit
                                    } else { //asi mame neco lepsiho
                                        updateConfirm(url);
                                    }
                                } catch (Exception e) { //neco nedopadlo dobre,
                                    showError(getResources().getString(R.string.update), e); //ukazem nejaky hlaseni
                                }
                            }
                        });
                    }
                });
    }

    //pridavne menu
    private void extMenu() throws Exception {
        AlertDialog.Builder builder = new AlertDialog.Builder(this); //zrobime buildera dialogu
        builder.setTitle(getResources().getString(R.string.ext_menu_title)); //titulek a zprava
        String[] items = {
                getResources().getString(R.string.drink_half),
                getResources().getString(R.string.drink_bottle),
        };
        builder.setSingleChoiceItems(items, 0, (dialog, which) -> { //poslouchadlo na klikanec
            dialog.dismiss();
            String what = items[which]; //tak co to bude
            if (what.equalsIgnoreCase(getResources().getString(R.string.drink_half))) {
                startScan(SCANNER_MODE_HALF, what); //spoustime skenovani
            } else if (what.equalsIgnoreCase(getResources().getString(R.string.drink_bottle))) {
                startScan(SCANNER_MODE_BOTTLE, what); //spoustime skenovani
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.cancel), null);
        AlertDialog dialog = builder.create(); //vyrobime dialog
        dialog.show(); //a zobrazim to
    }

    //Zaznam jidla. Nezname itemfID, takze ho musime vytahnout ze sluzby, pokud jich je vic, tak nechat vybrat a nasledne zalogovat.
    private void logConsf() {
        getRestApi().GetForDayInserted(date4rest()).compose(common()).subscribe(items -> { //nejprve si zjistime, k jakymu jidlu se budeme prihlasovat
            int len = items.length; //kolik je polozek
            if (len < 1) { //nejsou zadny polozky
                showError(getResources().getString(R.string.log_consf_nothink));
                return; //pokud neni z ceho vybirat, tak nic
                /* i jednojidlo se bude vybirat
            } else if (len == 1) { //pokud mame prave jednu polozku
                logConsf(items[0].ItemfID); //tak muzeme rovnou logovat bez nejakejch problemu
                */
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this); //zrobime buildera dialogu
                builder.setTitle(getResources().getString(R.string.log_consf_select)); //titulek a zprava
                builder.setSingleChoiceItems(cosfToStrings(items), 0, (dialog, which) -> { //poslouchadlo na klikanec
                    dialog.dismiss();
                    logConsf(items[which].ItemfID); //a to je von, trada logovat
                });
                builder.setNegativeButton(getResources().getString(R.string.cancel), null);
                AlertDialog dialog = builder.create(); //vyrobime dialog
                dialog.show(); //a zobrazim to
            }
        }, err -> showError(getResources().getString(R.string.log_consf_error), err));
    }

    //registrace se zadanim uzivatelskeho jmena
    private void register() {
        final EditText input = new EditText(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this); //zrobime buildera dialogu
        builder.setTitle(R.string.user_register_title); //titulek a zprava
        input.setImeOptions(EditorInfo.IME_ACTION_DONE);
        input.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        builder.setView(input);
        builder.setPositiveButton(R.string.register, (dialog, which) -> register(input.getText().toString())); //a jdem to registrovat
        builder.setNegativeButton(R.string.unregister, (dialog, which) -> unregister()); //a jdem to odregistrovat
        AlertDialog dialog = builder.create(); //vyrobime dialog
        dialog.show(); //a zobrazim to
    }

    private void simpleLog() {
        getDayInfo(dayInfo -> {
            showOk(getResources().getString(R.string.simple_log_title), String.format(getResources().getString(R.string.simple_log_text), dayInfo.drinks.length, dayInfo.food.length));
            setDayInfo(dayInfo); //schovam si to
        }, throwable -> {
            showError(getResources().getString(R.string.log_error), throwable);
        });
    }

    private void extLog() {
        getDayInfo(dayInfo -> { //natankujeme informace o dni
            setDayInfo(dayInfo); //schovam si to
            Intent intent = new Intent(this, ListActivity.class); //pripravime intent
            intent.putExtra(DayInfo.ID, dayInfo); //do nej napereme nacteny data
            startActivity(intent); //a jedem s medem
        }, throwable -> { //nejak to nedopadlo
            showError(getResources().getString(R.string.log_error), throwable);
        });
    }

    //provedeni akce podle caroveho kodu. Sem jsou smerovany i prikazy z UI
    private boolean barcodeActionBoolean(int code) {
        barcodeAction(code);
        return true;
    }

    //provedeni akce podle caroveho kodu. Sem jsou smerovany i prikazy z UI
    private void barcodeAction(int code) {
        try {
            switch (code) { //kody mohou mit ruzny vyznam
                case ID_EXT_MODE:
                    SetupActivity.setExtendMode(getSharedPreferences(), true);
                    setExtendedMode(true);
                    break;
                case ID_EXT_MODE_END:
                    SetupActivity.setExtendMode(getSharedPreferences(), false);
                    setExtendedMode(false);
                    break;
                case ID_FOOD: //extra kod pro jidlo
                    logConsf(); //loguj jidlo
                    break;
                case ID_STORE_BOTTLE:
                    startActivity(new Intent(this, StoreBottleActivity.class)); //a startujeme aktivitu s nastavenima
                    //startScan(SCANNER_MODE_STORE_BOTTLE, "");
                    break;
                case ID_STORE_BOTTLE_UNDO:
                    startScan(SCANNER_MODE_STORE_BOTTLE_UNDO, "");
                    break;
                case ID_LOG:
                    simpleLog();
                    break;
                case ID_LOG_EXT:
                    extLog();
                    break;
                case ID_EXT_MENU:
                    extMenu();
                    break;
                case ID_ALLOW_WEB_ACCESS:
                    allowWebAccess();
                    break;
                case ID_BUY_FOOD:
                case ID_BUY_FOOD_UNDO:
                default: //ostatni kody jsou jeden kus sklenice
                    if ((code >= ID_WINE_BOTTLE_START) && (code <= ID_WINE_BOTTLE_END)) {
                        logGlass(code,0.2);
                    } else {
                        showError(String.format(getResources().getString(R.string.barcode_unknown_number), code)); //zobrazim chybu
                    }
                    break;
            }
        } catch (Exception e) { //pokud se vyskytl nejakej problem
            showError(e); //zobrazim chybu
        }
    }

    private int barcode2int(String code) {
        int result=-1;
        if (code == null) { //neni nic
            showError(getResources().getString(R.string.barcode_error)); //pokud neni vubec nic, tak slus nahned
        }
        try {
            result=(Integer.decode(code)); //zkusim z toho udelat numero a podle toho akce
        } catch (Exception e) { //pokud doslo k nejakemu problemu, tak hlasim, problem vznikne jen pri chybe dekodovani cisla
            showError(String.format(getResources().getString(R.string.barcode_bad_format), code)); //zobrazim chybu
        }
        return result;
    }

    //procedeni akce podle kodu v textove podobe
    private void barcodeAction(String code) {
        int c=barcode2int(code); //prevedem na cislo
        if (c>=0) barcodeAction(c);
    }

    private int getScannerMode() {
        return getSharedPreferences().getInt(SCANNER_MODE, SCANNER_MODE_BASE);
    }

    private void setScannerMode(int mode) {
        getSharedPreferences().edit().putInt(SCANNER_MODE, mode).apply();
    }

    //Nastaveni rozsireneho modu
    private void setExtendedMode(boolean ext) {
        int visibility = ext ? View.VISIBLE : View.GONE;
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
                startActivityForResult(new Intent(this, SetupActivity.class), SetupActivity.ACTIVITY_RESULT_REGISTER); //a startujeme aktivitu s nastavenima
                return true; //nevim proc, asi ze jsem to zachytil
            }
            return false; //nevim proc, asi ze udalost nebyla zpracovana
        });
        //posluchaci udalosti cudlu pouzivaji barcodeAction s prislusnym kodem, jak proste ;-)
        findViewById(R.id.drink).setOnClickListener(v -> startScan(SCANNER_MODE_BASE, ""));
        findViewById(R.id.drink).setOnLongClickListener(v -> barcodeActionBoolean(ID_EXT_MENU));
        findViewById(R.id.fork).setOnClickListener(v -> barcodeAction(ID_FOOD));
        findViewById(R.id.show).setOnClickListener(v -> barcodeAction(ID_LOG));
        findViewById(R.id.show).setOnLongClickListener(v -> barcodeActionBoolean(ID_LOG_EXT));
        findViewById(R.id.buyFood).setOnClickListener(v -> barcodeAction(ID_BUY_FOOD));
        findViewById(R.id.buyFoodUndo).setOnClickListener(v -> barcodeAction(ID_BUY_FOOD_UNDO));
        findViewById(R.id.storeBottle).setOnClickListener(v -> barcodeAction(ID_STORE_BOTTLE));
        findViewById(R.id.storeBottleUndo).setOnClickListener(v -> barcodeAction(ID_STORE_BOTTLE_UNDO));
        setExtendedMode(SetupActivity.getExtmode(getSharedPreferences()));
        //setExtendedMode(true);
        setDayInfo(this.dayInfo);
    }

    private void startScan(int mode, String msg) {
        if (msg == null)
            msg = getResources().getString(R.string.scan_a_barcode); //kdyz neni, dame universalni
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
        if (requestCode == SetupActivity.ACTIVITY_RESULT_REGISTER) {
            switch (resultCode) {
                case SetupActivity.ACTIVITY_RESULT_REGISTER:
                    register();
                    return;
                case SetupActivity.ACTIVITY_RESULT_UPDATE:
                    update();
                    return;
                case SetupActivity.ACTIVITY_RESULT_ABOUT:
                    about();
                    return;
                case SetupActivity.ACTIVITY_RESULT_ENABLEWEB:
                    allowWebAccess();
                    return;
            }
        }
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) { //pokud je to nejakej vysledek
            int mode = getScannerMode(); //ztisim, v jakym rezimu jsme scaner volali
            setScannerMode(SCANNER_MODE_BASE); //a preklapime na zaklad, to se dela vzdycky, tak uz muzeme tadyk
            switch (mode) {
                case SCANNER_MODE_BASE:
                    barcodeAction(result.getContents()); //zpracujeme prijaty kod
                    break;
                case SCANNER_MODE_STORE_BOTTLE_UNDO:
                    storeBottleUndo(result.getContents());
                    break;
                case SCANNER_MODE_HALF:
                    logGlass(result.getContents(),0.1);
                    break;
                case SCANNER_MODE_BOTTLE:
                    logGlass(result.getContents(),1);
                    break;
                default:
                    showError("Unknown scanner mode " + mode);
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

}
