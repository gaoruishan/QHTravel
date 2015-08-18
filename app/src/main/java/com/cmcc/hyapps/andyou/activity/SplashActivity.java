
package com.cmcc.hyapps.andyou.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cmcc.hyapps.andyou.R;
import com.cmcc.hyapps.andyou.app.ServerAPI;
import com.cmcc.hyapps.andyou.data.RequestManager;
import com.cmcc.hyapps.andyou.model.QHActiviteResponse;
import com.cmcc.hyapps.andyou.model.Token;
import com.cmcc.hyapps.andyou.util.AppUtils;
import com.google.gson.Gson;

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

        // 开启位置服务 广播更新信息
//        Intent intent = new Intent(this, LocationService.class);
//        startService(intent);
        //验证Token
        autheToken();

       // UIStartUpHelper.executeOnSplashScreen();

        //判断是否是第一次使用
        if (AppUtils.firstLaunch(activity)) {
             //点击 返回键结束IntroActivity (此时已启IndexActivity)
            startActivityForResult(new Intent(activity, IntroActivity.class), CODE_SHOW_INTRO);
        } else {
            delayedInto();
        }

    }

    /**
     * 验证Token  POST /api-token-verify/
     */
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
                        android.util.Log.i("law", "success");
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //把当前的token和用户信息删掉
                        android.util.Log.i("law", "error");
                        AppUtils.clearQHToken(SplashActivity.this);
                        AppUtils.clearQHUser(SplashActivity.this);
                    }
                }, requestTag);
    }



    @Override
    protected void onActivityResult(int arg0, int arg1, Intent arg2) {
        super.onActivityResult(arg0, arg1, arg2);
        if (CODE_SHOW_INTRO == arg0) {
            //保存第一次登录信息－版本号
            AppUtils.setFirstLaunch(activity);
            //同时 开启IndexActivity
            delayedInto();
        }
    }

    //延迟 3s 开启主界面acitivity
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
