package com.scurab.barcodescanner2.base;


import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.scurab.barcodescanner2.R;
import com.trello.rxlifecycle2.LifecycleProvider;
import com.trello.rxlifecycle2.LifecycleTransformer;
import com.trello.rxlifecycle2.RxLifecycle;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.trello.rxlifecycle2.android.RxLifecycleAndroid;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import retrofit2.HttpException;

public abstract class RxLifecycleActivity extends AppCompatActivity implements LifecycleProvider<ActivityEvent> {
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

    protected abstract View getProgressBarContainer();

    //Toast.makeText(this, msg, Toast.LENGTH_LONG).show(); //zobrazime uzivateli, co se stalo
    protected void showOk(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.showOkTittle)); //titulek a zprava
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


}
