package com.scurab.barcodescanner2.base;


import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.scurab.barcodescanner2.R;
import com.scurab.barcodescanner2.SetupActivity;
import com.scurab.barcodescanner2.forest.RestApi;
import com.scurab.barcodescanner2.forest.User;
import com.trello.rxlifecycle2.LifecycleProvider;
import com.trello.rxlifecycle2.LifecycleTransformer;
import com.trello.rxlifecycle2.RxLifecycle;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.trello.rxlifecycle2.android.RxLifecycleAndroid;

import java.util.Calendar;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.HttpException;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import static retrofit2.converter.gson.GsonConverterFactory.create;

public abstract class RxLifecycleActivity extends AppCompatActivity implements LifecycleProvider<ActivityEvent> {

    String imei;
    public RestApi restApi;
    SharedPreferences preferences;
    public View progressBarContainer;

    public View getProgressBarContainer() {
        return progressBarContainer;
    }


    //ladici datum, kdy se neco delo
    public Calendar debugDate() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, 2018);
        c.set(Calendar.MONTH, 10);
        c.set(Calendar.DAY_OF_MONTH, 5);
        return c;
    }

    //vraci aktualni datum ve spravnym formatu
    public String date4rest() {
        return date4rest(Calendar.getInstance());

    }

    //vraci datum ve spravnem formatu
    public String date4rest(Calendar c) {
        return c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.DAY_OF_MONTH);
    }

    public String getImei() {
        if (imei == null) { //lina inicializace
            String idStr = Utils.getHwId(this); //vyrobime textovou identifikaci stroje
            byte[] id = Utils.str2id(idStr); //spachame identifikator
            imei = Utils.viewID(id, (char) 0); //a udelam z toho retezec
            Utils.log("getImei", "My IMEI " + imei);
        }
        return imei;
    }

    public SharedPreferences getSharedPreferences() {
        if (preferences == null) {
            preferences = getSharedPreferences(SetupActivity.PREFS_NAME, SetupActivity.PREFS_MODE); //zrobim preference
        }
        return preferences;
    }

    public RestApi getRestApi() {
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


    private final BehaviorSubject<ActivityEvent> lifecycleSubject = BehaviorSubject.create();

    @Override
    @NonNull
    @CheckResult
    public final Observable<ActivityEvent> lifecycle() {
        return lifecycleSubject.hide();
    }

    @Override
    @NonNull
    @CheckResult
    public final <T> LifecycleTransformer<T> bindUntilEvent(@NonNull ActivityEvent event) {
        return RxLifecycle.bindUntilEvent(lifecycleSubject, event);
    }

    @Override
    @NonNull
    @CheckResult
    public final <T> LifecycleTransformer<T> bindToLifecycle() {
        return RxLifecycleAndroid.bindActivity(lifecycleSubject);
    }

    @Override
    @CallSuper
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lifecycleSubject.onNext(ActivityEvent.CREATE);
    }

    @Override
    @CallSuper
    protected void onStart() {
        super.onStart();
        lifecycleSubject.onNext(ActivityEvent.START);
    }

    @Override
    @CallSuper
    protected void onResume() {
        super.onResume();
        lifecycleSubject.onNext(ActivityEvent.RESUME);
    }

    @Override
    @CallSuper
    protected void onPause() {
        lifecycleSubject.onNext(ActivityEvent.PAUSE);
        super.onPause();
    }

    @Override
    @CallSuper
    protected void onStop() {
        lifecycleSubject.onNext(ActivityEvent.STOP);
        super.onStop();
    }

    @Override
    @CallSuper
    protected void onDestroy() {
        lifecycleSubject.onNext(ActivityEvent.DESTROY);
        super.onDestroy();
    }

    protected <T> ObservableTransformer<T, T> bindToProgressBar(final View progresBar) {
        return upstream -> upstream
                .doOnSubscribe(d -> progresBar.post(() -> progresBar.setVisibility(View.VISIBLE)))
                .doAfterTerminate(() -> progresBar.post(() -> progresBar.setVisibility(View.GONE)));
    }

    //spolecny vcpicarny pro praci s restApi, tak at to mam na jednom miste
    protected <T> ObservableTransformer<T, T> common() {
        return upstream ->
                upstream.compose(bindToLifecycle()) // tohle to svaze s cyklem activity na android, takze kdyz appku zavres behem tohohle dotazu, tak to vicemene zahodi
                        .compose(bindToProgressBar(getProgressBarContainer()))    //aktivace progressbaru
                        .subscribeOn(Schedulers.io()) //tohle zase ze ty nasledujici callback funkce se maj zavolat v main thready, abys mohl hrabat do UI (pac android te nenecha pracovat s UI v nejakym jinym thread)
                        .observeOn(AndroidSchedulers.mainThread()); //spusti se diskoteka a davas tomu 2 funkce, jedna ktera se zavola, kdyz mas vysledek a druha pro pripad problemu
    }

    //Toast.makeText(this, msg, Toast.LENGTH_LONG).show(); //zobrazime uzivateli, co se stalo
    protected void showOk(String msg) {
        showOk(getResources().getString(R.string.show_ok_tittle),msg);
    }

    //Toast.makeText(this, msg, Toast.LENGTH_LONG).show(); //zobrazime uzivateli, co se stalo
    protected void showOk(String title, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title); //titulek a zprava
        builder.setMessage(msg);
        builder.setPositiveButton(getResources().getString(R.string.ok),null);
        AlertDialog alert = builder.create();
        alert.show();
    }


    //Zobrazi chybu
    protected void showError(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.error)); //titulek a zprava
        builder.setMessage(msg);
        builder.setPositiveButton(getResources().getString(R.string.error_accept),null);
        AlertDialog alert = builder.create();
        alert.show();
    }

    //Vyrobi textovy popis chyby
    protected String getThrowableDescription(Throwable err) {
        if (err instanceof HttpException) {
            try {
                HttpException e = (HttpException) err;
                return e.getMessage();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return err.getMessage();
    }

    //Zobrazi chybu
    protected void showError(Throwable err) {
        showError(getThrowableDescription(err));
    }

    //Zobrazi chybu s uvodnim vysvetlenim.
    protected void showError(String msg, Throwable err) {
        err.printStackTrace();
        String es=getThrowableDescription(err);
        if (es.length()>0) { //pokud mame nejake zajimave upresneni
            msg+="\n"+"\n"+es; //tak to prihodime
        }
        showError(msg); //ukaz chybu
    }

    //----------------------------------------------------------------------------------------------

    protected DayInfo dayInfo;

    //vyrobi informaci o konkretnim dni pro aktualniho uzivatele
    protected void getDayInfo(Calendar c, Consumer<DayInfo> onNext, Consumer<? super Throwable> onError) {
        getRestApi().getCondsD(getImei(), date4rest(c)).compose(common()).subscribe(drinks -> { //provedem dotaz na zkonzumovany chlast
            DayInfo di = new DayInfo(); //zrobim si pomocnou tridu
            di.drinks = drinks; //do ktery to budu skladak
            getRestApi().getCondsF(getImei(), date4rest(c)).compose(common()).subscribe(foods -> { //provedem dotaz na zkonzumovany jidlo
                di.food = foods; //ulozime
                if (di.isEmpty()) {
                    onError.accept(new Exception("Tento den se nic nedělo."));
                    return;
                }
                setDayInfo(di); //hodime to i ostatnim castem, se muze hodit
                onNext.accept(di); //a predhodime to konzumentovi onNext
            }, err -> onError.accept(err)); //chubu jen prehodime dodanemu konzumentovi
        }, err -> onError.accept(err)); //chubu jen prehodime dodanemu konzumentovi
    }

    protected void updateDayInfo() {
        getDayInfo(dayInfo ->  setDayInfo(dayInfo) , throwable -> {});//provedeme cteni dayinfo a kdyz to klapne, tak to nastavim
    }

    //vyrobi informaci o dnesku pro aktualniho uzivatele
    protected void getDayInfo(Consumer<DayInfo> onNext, Consumer<? super Throwable> onError) {
        getDayInfo(Calendar.getInstance(), onNext, onError);
    }

    protected void setDayInfo(DayInfo di) {
        this.dayInfo=di; //si to schovam napotom
        if ((di==null)||(di.isEmpty())) { //necheme day info
            findViewById(R.id.smalllog).setVisibility(View.GONE); //chci to vided
        } else { //chceme day info
            String msg=String.format(getResources().getString(R.string.simple_log_text), dayInfo.drinks.length, dayInfo.food.length); //hlaska do vokynka
            msg=msg.replace("\n","  "); //vyhazime odradkovani
            ((TextView)findViewById(R.id.smalllog)).setText(msg); //a to chci videt
            findViewById(R.id.smalllog).setVisibility(View.VISIBLE); //chci to vided
        }
    }

    //----------------------------------------------------------------------------------------------

}
