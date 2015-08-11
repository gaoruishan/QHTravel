
package com.cmcc.hyapps.andyou.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.android.volley.Request;
import com.cmcc.hyapps.andyou.data.RequestManager;

/**
 * 这是个基类，所有的activity都有继承于它
 * 
 * @author gaoruishan
 */
public abstract class BaseActivity extends FragmentActivity {
    protected Activity activity;
    protected String requestTag;
    public final int REQUEST_CODE_LOGIN_NEW_COMMENT = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //进出和退出动画
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        requestTag = getClass().getName();
        activity = this;
    }


    @Override
    public void finish() {
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        super.finish();
    }


    @Override
    protected void onStop() {
        super.onStop();
        //页面不可见时，取消所有的requestTag，RequestManager是什么呢？
        RequestManager.getInstance().cancelAll(requestTag);
    }

    protected void executeRequest(Request<?> request) {
        RequestManager.getInstance().addRequest(request, requestTag);
    }
}
