package com.scurab.barcodescanner2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.scurab.barcodescanner2.base.RxLifecycleActivity;
import com.scurab.barcodescanner2.forest.ItemdType;
import com.scurab.barcodescanner2.forest.ItemdTypeYearViews;
import com.scurab.barcodescanner2.forest.Itemds;
import com.scurab.barcodescanner2.forest.User;

import java.time.Year;

public class StoreBottleActivity extends RxLifecycleActivity {
    Spinner spinnerUser;
    Spinner spinnerVintage;
    Spinner spinnerBootleType;
    TextView price;
    User user[] = null;
    ItemdTypeYearViews vintage[] = null;
    ItemdType bottleType[] = null;

    private void setSpinnerLoading(Spinner spinner) {
        spinner.setEnabled(false); //zatim nepovoleno, az budou data
        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, android.R.id.text1, new String[]{getResources().getString(R.string.loading)})); //hodime hlasku
    }

    private void setSPinnerShow(Spinner spinner, String[] items) {
        spinner.setEnabled(true); //a povol do nej hrabat
        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, android.R.id.text1, items)); //vyrob adaptera a naper ho do spineru
    }

    private User getUser(Object id) {
        if (id instanceof String) { //pokud je to retezec, tak budeme hledat podle jmena
            String name = (String) id; //udelam z toho retezec
            for (User u : user) { //hlodame vybranyho typa
                if (name.equalsIgnoreCase(u.DisplayName)) { //tak je to von, hura
                    return u;
                }
            }
        }
        return null;
    }

    private void readUsers() {
        getRestApi().getUsers().compose(common()).subscribe(users -> {
                    this.user = users; //hura nacteno
                    makeUsers(); //vopravime spinera
                },
                err -> showError(getResources().getString(R.string.store_bottle), err));
    }

    private void makeUsers() {
        if (user == null) {
            setSpinnerLoading(spinnerUser); //ukaz hlaseni
            readUsers(); //spustim cteni uzivatelu a priste uvidime
            return; //a prozatim moncime
        }
        String items[];
        items = new String[user.length]; //udelame pole pro jmena uzivatelu
        int ptr = 0;
        for (User u : user) { //projdeme uzivatele
            items[ptr++] = u.DisplayName; //perem je do pole
        }
        setSPinnerShow(spinnerUser, items);
    }

    private void readVintage() {
        getRestApi().getItemdTypeYearViews().compose(common()).subscribe(vintage -> {
            boolean useDefault = this.vintage == null; //pokud tam je null, tak jsme se narodili a to si poznamename
            this.vintage = vintage; //hura nacteno
            makeVintage(useDefault?-1:SetupActivity.getLastBottleStorageYear(getSharedPreferences())); //vopravime spinera
        }, err -> showError(getResources().getString(R.string.store_bottle), err));
    }

    private void makeVintage(int preset) {
        String items[];
        if (vintage == null) {
            setSpinnerLoading(spinnerVintage);
            readVintage(); //spustim cteni rocniku
            return; //a prozatim moncime
        }
        items = new String[vintage.length]; //udelame pole pro jmena uzivatelu
        int ptr = 0;
        int ppos=-1;
        for (ItemdTypeYearViews u : vintage) { //projdeme uzivatele
            if (u.Year==preset) ppos=ptr;
            items[ptr++] = Integer.toString(u.Year); //perem je do pole
        }
        setSPinnerShow(spinnerVintage, items);
        if (ppos>=0) spinnerVintage.setSelection(ppos);
    }

    private void selectVintage() {
        if (vintage == null) return; //pokud neni z ceho vybirat, tak je to nejakej klikanec naprd
        Object o = spinnerVintage.getSelectedItem(); //na co nam to vokazuje
        if (!(o instanceof String)) return; //pokud to neni retezec, tak je to divny
        String year = (String) o; //tak z toho udelam retezec
        readBottleType(year);
    }

    private void readBottleType(String year) {
        getRestApi().getItemdTypes(year).compose(common()).subscribe(types -> {
            this.bottleType = types; //hura nacteno
            makeBottleType(); //vopravime spinera
        }, err -> showError(getResources().getString(R.string.store_bottle), err));
    }

    private void makeBottleType() {
        String items[];
        if (bottleType == null) {
            setSpinnerLoading(spinnerBootleType);
            return;
        }
        items = new String[bottleType.length]; //udelame pole pro jmena uzivatelu
        int ptr = 0;
        for (ItemdType u : bottleType) { //projdeme uzivatele
            items[ptr++] = u.Name; //perem je do pole
        }
        setSPinnerShow(spinnerBootleType, items);
    }

    private ItemdType getBottleType(Object id) {
        if (id instanceof String) { //pokud je to retezec, tak budeme hledat podle jmena
            String name = (String) id; //udelam z toho retezec
            for (ItemdType i : bottleType) { //hlodame vybranyho typa
                if (name.equalsIgnoreCase(i.Name)) { //tak je to von, hura
                    return i;
                }
            }
        }
        return null;
    }

    private void selectBottleType() {
        if (bottleType == null) return; //zatim neni z ceho vybirat
        ItemdType i = getBottleType(spinnerBootleType.getSelectedItem()); //zjistim co to je
        if (i!=null) { //pro sichr
            price.setText(Double.toString(i.Price)); //vyplnime default cenu
        } else {
            price.setText("0"); //dost divny, ze jsme nic nenasli, dame nula
        }
    }

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_bottle);
        Toolbar toolbar = findViewById(R.id.toolbar); //tady je horni radek
        toolbar.setLogo(R.drawable.fork_truck_icon_64); //naper k tomu ikonu
        progressBarContainer = findViewById(R.id.progress_bar_container);
        spinnerUser = (Spinner) findViewById(R.id.bottle_owner);
        spinnerVintage = (Spinner) findViewById(R.id.bottle_vintage);
        spinnerBootleType = (Spinner) findViewById(R.id.bottle_type);
        price = (TextView) findViewById(R.id.bottle_price);
        //posluchaci udalosti cudlu pouzivaji barcodeAction s prislusnym kodem, jak proste ;-)
        findViewById(R.id.store_bottle).setOnClickListener(v -> startScan());
        ((Button) findViewById(R.id.store_bottle_end)).setOnClickListener(v -> { //po klofnuti na cudl
            setResult(0); //rikam ze se nic nechce
            this.finish(); //a koncime
        });
        spinnerVintage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                selectVintage();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) { /* TODO Auto-generated method stub */}
        });
        spinnerBootleType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                selectBottleType();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) { /* TODO Auto-generated method stub */}
        });
        makeUsers(); //zahajime cteni uzivatelu, az se to nacte, tak se to napere do spineru
        makeVintage(-1);
        makeBottleType();

    }

    private void startScan() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt(getResources().getString(R.string.scan_a_barcode));
        integrator.setCameraId(SetupActivity.getCameraId(getSharedPreferences()));  // Use a specific camera of the device
        integrator.setTimeout(SetupActivity.getTimeout(getSharedPreferences())); //jakej to bude mit timeoutek
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(true);
        integrator.setOrientationLocked(false);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) { //pokud je to nejakej vysledek
            String scan = result.getContents(); //tohle jsme nacetli
            Itemds b = new Itemds();
            try { //odchyt pruseru pri tvorbe finalniho zaznamu
                b.ItemdID = Integer.decode(scan); //id lahve
                b.Username = getUser(spinnerUser.getSelectedItem()).Username; //ci je flaska
                b.ItemdTypeID = getBottleType(spinnerBootleType.getSelectedItem()).ItemdTypeID; //typ lahve
                b.Price = Double.parseDouble(price.getText().toString()); //cena
                SetupActivity.setLastBottleStorageYear(getSharedPreferences(),Integer.decode(spinnerVintage.getSelectedItem().toString())); //ulozim si posledni pouzity rocnik, abych ho priste nabidl
            } catch (Exception e) { //neco se podelalo, nebudeme ukladat
                showError(getResources().getString(R.string.store_bottle)+e.toString()); //nejako ukaz chybu
                return; //a slus
            }
            //showOk("DEBUG","\nb.ItemdID="+b.ItemdID+"\nb.Username="+b.Username+"\nb.ItemdTypeID="+b.ItemdTypeID+"\nb.Price="+b.Price );
            getRestApi().Itemds(b).compose(common()).subscribe(r -> {
                showOk(getResources().getString(R.string.bottle_store_ok));
            }, err -> showError(getResources().getString(R.string.bottle_store_err), err));
        } else { //co to sem leze za hovadiny
            //super.onActivityResult(requestCode, resultCode, data); //hodime to dal a poptakach
        }
    }
}
