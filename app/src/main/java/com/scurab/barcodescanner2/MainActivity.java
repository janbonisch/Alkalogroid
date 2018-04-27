package com.scurab.barcodescanner2;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.scurab.barcodescanner2.forest.XsampleLahef;
import com.scurab.barcodescanner2.forest.XsampleLahefResponse;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import static retrofit2.converter.gson.GsonConverterFactory.create;

/**
 * Created by jbruchanov on 05/04/2018.
 */
public class MainActivity extends AppCompatActivity {

    //==============================================================================================
    //
    // REST pro komunikaci se sluzbou
    //
    //

    private RestApi mRestApi;

    private RestApi getApi() {
        if (mRestApi == null) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .build();

            Gson gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
                    .create();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://www.mujwebik.cz/")
                    .client(client)
                    .addConverterFactory(create(gson))
                    .build();

            mRestApi = retrofit.create(RestApi.class);
        }
        return mRestApi;
    }

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
    }

    //==============================================================================================
    //
    // Vlastni apka
    //
    //

    private TextView mTextView;
    private boolean mIsResumed;
    private byte[] id; //idecko jako pole bajtu
    private String idStr; //idce


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = findViewById(R.id.result);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.settings) {
                    Toast.makeText(MainActivity.this, "Settings", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            }
        });

        findViewById(R.id.scan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScan();
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
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            final String scanResult = result.getContents();
            if (scanResult == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                mTextView.setText(scanResult);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
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
