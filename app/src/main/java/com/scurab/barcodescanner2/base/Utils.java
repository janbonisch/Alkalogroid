package com.scurab.barcodescanner2.base;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.security.MessageDigest;

public class Utils {

    //==============================================================================================
    //
    // Staticke uzitecnosti


    public static void sleep(int milis) {
        try {
            Thread.sleep(milis);
        } catch (Exception e) {
        }
    }

    private static final String MESSAGE_DIGEST_ALGORITHM = "SHA-256"; //hasovaci algoritmus

    static void log(String tag, String msg) {
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
                Utils.log("ipAddrShow", line);
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
}
