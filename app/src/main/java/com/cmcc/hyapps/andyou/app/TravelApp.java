
package com.cmcc.hyapps.andyou.app;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cmcc.hyapps.andyou.data.ImageLoaderManager;
import com.cmcc.hyapps.andyou.data.RequestManager;
import com.cmcc.hyapps.andyou.helper.UIStartUpHelper;
import com.cmcc.hyapps.andyou.model.Location;
import com.cmcc.hyapps.andyou.model.User;
import com.cmcc.hyapps.andyou.service.LocationService;
import com.cmcc.hyapps.andyou.utils.AppUtils;
import com.cmcc.hyapps.andyou.utils.LocationUtil;
import com.cmcc.hyapps.andyou.utils.Log;
import com.cmcc.hyapps.andyou.utils.PreferencesUtils;
import com.umeng.analytics.MobclickAgent;
import com.umeng.fb.push.FeedbackPush;

/**
 * Created by kuloud on 14-8-16.
 * 整个应用的
 */
public class TravelApp extends Application {
    public static final String TAG = "Travel";
    public static TravelApp instance;
    //记录当前所在位置
    public Location mCurrentLocation;
    public static Context mContext;

    public static Context getContext(){
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        init();
    }
    /**
     *初始化 app 所需的工具和帮助类
     */
    private void init() {
        //崩溃时守护线程
        CrashHandler crashHandler = CrashHandler.getInstance();
        // 注册crashHandler
        crashHandler.init(getApplicationContext());

        mContext = getApplicationContext();
        //开启定位服务
        Intent service = new Intent(mContext,LocationService.class);
        startService(service);
        //友盟－推送初始化－禁止
        FeedbackPush.getInstance(this).init(false);
        //初始化
        LocationUtil.getInstance(mContext);

        //接口 切换开关
        ServerAPI.switchServer(PreferencesUtils.getBoolean(this, ServerAPI.KEY_DEBUG));

        //volley RequestQueue初始化
        RequestManager.getInstance().init(this);

        //创建默认的ImageLoader配置参数
        ImageLoaderManager.getInstance().init(this);

       // OfflinePackageManager.getInstance().init(this);

        //打开LOG 日志，正式上线时 关闭它
//        com.umeng.socialize.utils.Log.LOG = Const.UMENG_DEBUG;
        //打开Debug模式
        MobclickAgent.setDebugMode(Const.UMENG_DEBUG);
        //捕获错误日志
        MobclickAgent.setCatchUncaughtExceptions(true);

        UIStartUpHelper.executeWhenIdle(new Runnable() {

            @Override
            public void run() {
                tryUpdateUserInfoFromNet();
            }
        });
    }

    /**
     * 尝试更新用户信息
     */
    private void tryUpdateUserInfoFromNet() {
        // if user info exist local, try to update once. 用户存在，进行异步更新
        if (AppUtils.getUser(getBaseContext()) != null) {
            String url = ServerAPI.BASE_URL + "users/current/";
            RequestManager.getInstance().sendGsonRequest(url, User.class,
                    new Response.Listener<User>() {

                        @Override
                        public void onResponse(User user) {
                            Log.e("onResponse, User: " + user);
                            // Delete previous user avatar if need. 删除旧头像
                            User oldUserInfo = AppUtils.getUser(getBaseContext());

                            if (oldUserInfo != null&&null!=user.user_info.avatar_url
                                    && !oldUserInfo.user_info.avatar_url.equals(user.user_info.avatar_url)) {
                                AppUtils.setOldAvatarUrl(getBaseContext(), oldUserInfo.user_info.avatar_url);
                            }
                            AppUtils.saveUser(getBaseContext(), user);
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(error, "onErrorResponse");
                        }
                    }, TAG);
        }
    }
    //设置当前位置
    public void setCurrentLocation(Location location) {
        mCurrentLocation = location;

        if (location != null && location.isValid()) {
            LocationUtil.setLastKnownLocation(getApplicationContext(), location);
        }
    }


    /**
     * @return the main context of the Application
     */
    public static Context getAppContext()
    {
        return instance;
    }

    /**
     * @return the main resources from the Application
     */
    public static Resources getAppResources()
    {
        return instance.getResources();
    }
}
