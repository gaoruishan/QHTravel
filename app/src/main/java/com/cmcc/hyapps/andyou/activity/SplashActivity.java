
package com.cmcc.hyapps.andyou.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.cmcc.hyapps.andyou.R;
import com.cmcc.hyapps.andyou.app.ServerAPI;
import com.cmcc.hyapps.andyou.data.RequestManager;
import com.cmcc.hyapps.andyou.helper.UIStartUpHelper;
import com.cmcc.hyapps.andyou.model.QHActiviteResponse;
import com.cmcc.hyapps.andyou.model.Token;
import com.cmcc.hyapps.andyou.model.Splash;
import com.cmcc.hyapps.andyou.model.Token;
import com.cmcc.hyapps.andyou.service.LocationService;
import com.cmcc.hyapps.andyou.utils.AppUtils;
import com.cmcc.hyapps.andyou.utils.FileUtils;
import com.cmcc.hyapps.andyou.utils.Log;
import com.cmcc.hyapps.andyou.utils.PreferencesUtils;
import com.cmcc.hyapps.andyou.utils.ScreenUtils;
import com.cmcc.hyapps.andyou.utils.TimeUtils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Splash page.
 * 
 * @author kuloud
 */
public class SplashActivity extends BaseActivity {
    /**
     * Duration for show Splash
     */
    private final long DURATION = 3000;

    private final int CODE_SHOW_INTRO = 1;

    private final String KEY_SPLASHS = "key_splashs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

//        setupBackground();

        // Start reqeust current location
        Intent intent = new Intent(this, LocationService.class);
        startService(intent);

        autheToken();

        UIStartUpHelper.executeOnSplashScreen();

        if (AppUtils.firstLaunch(activity)) {
            startActivityForResult(new Intent(activity, IntroActivity.class), CODE_SHOW_INTRO);
        } else {
            delayedInto();
        }

    }

    private void autheToken() {
        Token qhToken = AppUtils.getQHToken(this);
        String jsonBody = new Gson().toJson(qhToken);
        String url = ServerAPI.QHToken.buildAuthToken();
        RequestManager.getInstance().sendGsonRequest(Request.Method.POST, url,
                jsonBody,
                QHActiviteResponse.class, new Response.Listener<QHActiviteResponse>() {
                    @Override
                    public void onResponse(QHActiviteResponse response) {
                        //什么都不做
                        android.util.Log.i("law","success");
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //把当前的token和用户信息删掉
                        android.util.Log.i("law","error");
                        AppUtils.clearQHToken(SplashActivity.this);
                        AppUtils.clearQHUser(SplashActivity.this);
                    }
                }, requestTag);
    }


    private void setupBackground() {
        // load local splash
        ArrayList<Splash> splashs = loadSplashs();
        if (splashs != null) {
            List<Splash> refreshData = (List<Splash>) splashs.clone();
            long now = System.currentTimeMillis();
            for (Splash splash : splashs) {
                long endTime = TimeUtils.parseTimeToMills(splash.endTime);
                // Remove outdated data
                if (endTime < now) {
                    if (refreshData != null && refreshData.contains(splash)) {
                        FileUtils.cleanCacheBitmap(activity, splash.endTime);
                        refreshData.remove(splash);
                    }
                    continue;
                }
                // Load hit data
                long startTime = TimeUtils.parseTimeToMills(splash.startTime);
                if (startTime < now) {
                    ImageView bg = (ImageView) findViewById(R.id.root);
                    String url = FileUtils.getCachePath(activity, splash.endTime);
                    Bitmap bm = FileUtils.getLocalBitmap(url);
                    if (bm != null) {
                        bg.setImageBitmap(bm);
                    }
                    break;
                }
            }
            if (!refreshData.isEmpty()) {
                PreferencesUtils.putString(activity, KEY_SPLASHS, new Gson().toJson(refreshData));
            }
        }

        RequestManager.getInstance().sendGsonRequest(ServerAPI.Splash.buildUrl("beijing"),
                Splash.class,
                new Response.Listener<Splash>() {
                    @Override
                    public void onResponse(Splash splash) {
                        Log.d("onResponse, Splash=%s", splash);
                        onSplashLoaded(splash);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(error, "onErrorResponse");
                    }
                }, "splash");
    }

    private ArrayList<Splash> loadSplashs() {
        // read local
        String splashsJson = PreferencesUtils.getString(activity, KEY_SPLASHS);
        if (!TextUtils.isEmpty(splashsJson)) {
            Type type = new TypeToken<ArrayList<Splash>>() {
            }.getType();
            ArrayList<Splash> splashs = null;
            try {
                splashs = new Gson().fromJson(splashsJson, type);
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
            }
            return splashs;
        }
        return null;
    }

    protected void onSplashLoaded(final Splash splash) {
        ArrayList<Splash> tempSplashs = loadSplashs();
        if (tempSplashs == null) {
            tempSplashs = new ArrayList<Splash>();
        }
        // Get new data, update local records.
        if (tempSplashs != null && !tempSplashs.contains(splash)) {
            final ArrayList<Splash> splashs = tempSplashs;
            splashs.add(splash);
            int width = ScreenUtils.getScreenWidth(getBaseContext());
            int height = ScreenUtils.getScreenHeight(getBaseContext());
            RequestManager.getInstance().requestImage(splash.imageUrl, new Listener<Bitmap>() {
                @Override
                public void onResponse(Bitmap bitmap) {
                    FileUtils.cacheBitmap(getBaseContext(), bitmap, splash.endTime);
                    PreferencesUtils.putString(activity, KEY_SPLASHS, new Gson().toJson(splashs));
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(error, "onErrorResponse");
                }
            }, width, height, "splash");
        }
    }

    @Override
    protected void onActivityResult(int arg0, int arg1, Intent arg2) {
        super.onActivityResult(arg0, arg1, arg2);
        if (CODE_SHOW_INTRO == arg0) {
            AppUtils.setFirstLaunch(activity);
            delayedInto();
        }
    }

    private void delayedInto() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, IndexActivity.class);
                startActivity(intent);
                finish();
            }
        }, DURATION);
    }

}
