package com.scurab.barcodescanner2;

import android.annotation.SuppressLint;
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

import com.scurab.barcodescanner2.base.RxLifecycleActivity;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

@SuppressWarnings("Convert2MethodRef")
public class ListActivity extends RxLifecycleActivity {

    private RecyclerView mRecyclerView;
    private View mProgressBar;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        mRecyclerView = findViewById(R.id.recycler_view);
        mProgressBar = findViewById(R.id.progress_bar_container);

        ListAdapter adapter = new ListAdapter();
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL){{
            Resources resources = getResources();
            Drawable drawable = resources.getDrawable(R.drawable.divider_black);
            setDrawable(drawable);
        }});

        Observable
                .fromCallable(() -> {
                    List<ListAdapter.ChlastItem> items = new ArrayList<>();
                    for (int i = 0, n = 100; i < n; i++) {
                        items.add(new ListAdapter.ChlastItem(
                                System.currentTimeMillis() - (86400_000 * i),
                                "Pifo numero:" + (i + 1),
                                System.nanoTime() % 1000L / 10.));
                    }
                    return items;
                })
                .compose(common())
                .subscribe(chlastItems -> adapter.setItems(chlastItems),
                        throwable -> showError(throwable));
    }

    @Override
    protected View getProgressBarContainer() {
        return mProgressBar;
    }
}
