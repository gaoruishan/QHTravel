
package com.cmcc.hyapps.andyou.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.android.volley.Request;
import com.cmcc.hyapps.andyou.app.AppManager;
import com.cmcc.hyapps.andyou.data.RequestManager;
import com.umeng.analytics.MobclickAgent;

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
        AppManager.getAppManager().addActivity(this);
    }

    /**
     * 返回键---结束当前Activity（堆栈中最后一个压入的）
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        AppManager.getAppManager().finishActivity();
    }

    /**
     *  在每个activity中都绑定 在重载函数中为统计代码
     */
    @Override
    protected void onResume() {
        super.onResume();

        // 用来保证获取正确的新增用户、活跃用户、启动次数、使用时长等基本数据
        MobclickAgent.onResume(activity);
    }

    @Override
    protected void onPause() {
        MobclickAgent.onPause(activity);
        super.onPause();
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
}
