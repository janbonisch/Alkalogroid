package com.scurab.barcodescanner2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.scurab.barcodescanner2.base.DayInfo;
import com.scurab.barcodescanner2.base.RxLifecycleActivity;
import com.scurab.barcodescanner2.forest.ItemLogFood;
import com.scurab.barcodescanner2.forest.ItemdView;
import com.scurab.barcodescanner2.forest.ItemfView;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

@SuppressWarnings("Convert2MethodRef")
public class ListActivity extends RxLifecycleActivity {

    private RecyclerView mRecyclerView;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        mRecyclerView = findViewById(R.id.recycler_view);
        progressBarContainer = findViewById(R.id.progress_bar_container);

        ListAdapter adapter = new ListAdapter();
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL) {{
            Resources resources = getResources();
            Drawable drawable = resources.getDrawable(R.drawable.divider_black);
            setDrawable(drawable);
        }});
        Observable.fromCallable(() -> {
            Intent intent = getIntent(); //potrbujeme intent
            DayInfo di = (DayInfo) intent.getSerializableExtra(DayInfo.ID); //pres extra si vytahneme odeslana data
            List items = new ArrayList(); //stvorim novy list
            if (di != null) { //ochrana proti totalni blbosti
                for (ItemdView x : di.drinks) items.add(x); //ladujeme chlast
                for (ItemLogFood x : di.food) items.add(x); //ladujeme zradlo
            }
            //TODO: casem mozna doladujeme nejakou sumu nebo co ja vim za dalsi statistiky
            return items;
        })
                .compose(common())
                .subscribe(items -> adapter.setItems(items),
                        throwable -> showError(throwable));
    }

}
