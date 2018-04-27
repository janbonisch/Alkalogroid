package com.scurab.barcodescanner2;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by jbruchanov on 05/04/2018.
 */
public class MainActivity extends AppCompatActivity {

    private TextView mTextView;
    private boolean mIsResumed;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = findViewById(R.id.result);

        findViewById(R.id.scan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScan();
            }
        });
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

    public void sendRestCall() {
        Lahef l = new Lahef();
        l.name = "Kontusovka";
        l.year = 1920;
        l.voltaaaz = 40;
        l.volume = 0.75;

        final Call<LahefResponse> call = getApi().send(l);
        //tohle je nejjednodussi,
        //pokud bys potreboval nejak retezit "pekne" a nebo delat neco slozitejsiho
        //tak na to je pak dalsi pekna libka...
        call.enqueue(new Callback<LahefResponse>() {
            @Override
            public void onResponse(Call<LahefResponse> call, Response<LahefResponse> response) {
                if (mIsResumed) {
                    //potreba bejt jistej, ze appka bezi, jinak delas neco na "pozadi" a nemusi to fachat
                }
            }

            @Override
            public void onFailure(Call<LahefResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });
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

    private RestApi mRestApi;

    private RestApi getApi() {
        if (mRestApi == null) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://www.mujwebik.cz/")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            mRestApi = retrofit.create(RestApi.class);
        }
        return mRestApi;
    }
}
