package org.ocandroid.hyperiondemoapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.Toast;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.readystatesoftware.chuck.ChuckInterceptor;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    private static final String SHARED_PREFERENCE_FILE = "demo";
    private static final String SHARED_PREFERENCE_KEY = "timestamp";

    @BindView(R.id.sampleImage)
    ImageView sampleImage;

    private OkHttpClient client;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupTimber();
        ButterKnife.bind(this);
        setupPicasso(this);
        prefs = getSharedPreferences(SHARED_PREFERENCE_FILE, MODE_PRIVATE);
        client = setupOkHttp();
    }

    @OnClick(R.id.button_load_image)
    void loadImage() {
        Timber.d("loadImageClicked");
        Picasso.with(this).load("https://developer.android.com/_static/images/android/touchicon-180.png").into(sampleImage);
    }

    @OnClick(R.id.button_load_data)
    void loadData() {
        Timber.d("loadDataClicked");
        Request request = new Request.Builder()
            .url("https://xkcd.com/info.0.json")
            .addHeader("User-Agent", System.getProperty("http.agent"))
            .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                toastMessage("Request Failed");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                toastMessage("Request Successful");
            }
        });
    }

    @OnClick(R.id.button_save_date)
    void saveDateClicked() {
        storeCurrentTime();
        toastMessage("Current Time Saved");
    }

    @OnClick(R.id.button_show_date)
    void showDateClicked() {
        toastMessage(prefs.getString(SHARED_PREFERENCE_KEY, "No Date Data Found"));
    }

    private void setupPicasso(Context context) {
        Picasso.Builder builder = new Picasso.Builder(context);
        OkHttp3Downloader downloader = new OkHttp3Downloader(setupOkHttp());
        builder.downloader(downloader);
        Picasso picasso = builder.build();
        picasso.setLoggingEnabled(BuildConfig.DEBUG);
        try {
            Picasso.setSingletonInstance(picasso);
        } catch (IllegalStateException e) { }
    }

    private OkHttpClient setupOkHttp() {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        if (BuildConfig.DEBUG) {
            clientBuilder.addInterceptor(new ChuckInterceptor(this));
        }
        return clientBuilder.build();
    }

    private void setupTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }

    private void toastMessage(final String message) {
        Timber.d("Toasting Message: %s", message);
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void storeCurrentTime() {
        prefs.edit().putString(SHARED_PREFERENCE_KEY, SimpleDateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime())).apply();
    }
}
