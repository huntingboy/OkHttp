package com.nomad.okhttp;

import android.nfc.Tag;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.nomad.interceptor.LoggingInterceptor;

import java.io.File;
import java.io.IOException;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private Handler handler;
    private TextView tvMain;

    private Callback mPostFormCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {

        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            final String result = response.body().string();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvMain.setText(result);
                }
            });
        }
    };

    int cacheSize = 10 * 1024 * 1024;
    File cacheDirectory = new File("E:\\android\\OkHttp\\cache");
    Cache cache = new Cache(cacheDirectory, cacheSize);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvMain = findViewById(R.id.tv_main);
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                Bundle bundle = msg.getData();
                tvMain.setText(bundle.getString("string"));
                return true;
            }
        });
        loadDataSync();
//        loadDataAsync();
//        postForm();
//        postJson();
    }

    private void loadDataSync() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = "https://publicobject.com/helloworld.txt";
                Request request = new Request.Builder().get().url(url).build();
                int cacheSize = 10 * 1024 * 1024;
                File cacheDirectory = new File("E:\\android\\OkHttp\\cache");
                Cache cache = new Cache(cacheDirectory, cacheSize);
                OkHttpClient okHttpClient = new OkHttpClient.Builder().cache(cache).build();

                try {
                    Response response = okHttpClient.newCall(request).execute();
                    String string = response.body().string();
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString("string", string);
                    message.setData(bundle);
                    handler.sendMessage(message);
                    Log.d(TAG, "loadDataSync: " + string);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void loadDataAsync() {
        String url = "https://publicobject.com/helloworld.txt";
        Request request = new Request.Builder().get().url(url).build();

        OkHttpClient okHttpClient = new OkHttpClient.Builder().addNetworkInterceptor(new LoggingInterceptor()).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().toString();
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("string", result);
                message.setData(bundle);
                handler.sendMessage(message);
                Log.d(TAG, "onResponse: " + result);
            }
        });
    }

    private void postForm() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().addInterceptor(new LoggingInterceptor()).build();

        RequestBody formBody = new FormBody.Builder().add("search", "Uncle").build();
        Request request = new Request.Builder().url("https://en.wikipedia.org/w/index.php").post(formBody).build();
        okHttpClient.newCall(request).enqueue(mPostFormCallback);
    }

    private void postJson() {
        String json = "";
        String url = "";
        int cacheSize = 10 * 1024 * 1024;
        File cacheDirectory = new File("E:\\android\\OkHttp\\cache");
        Cache cache = new Cache(cacheDirectory, cacheSize);
        OkHttpClient okHttpClient = new OkHttpClient.Builder().cache(cache).build();
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");

        RequestBody requestBody = RequestBody.create(mediaType, json);
        Request request = new Request.Builder().url(url).post(requestBody).build();
        okHttpClient.newCall(request).enqueue(mPostFormCallback);
    }



}
