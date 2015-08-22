package com.cmcc.hyapps.andyou.util;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.cmcc.hyapps.andyou.app.TravelApp;

/**
 * Created by Administrator on 2015/6/9.
 * 地理位置－工具
 */
public class LocationUtil {

    //经纬度
    private static double currentlatitude;
    private static double currentlongitude;
    private static final String KEY_LATITUDE = "key_latitude";
    private static final String KEY_LONGITUDE = "key_longitude";
    private static final String KEY_CITY = "key_city";

    private LocationManagerProxy mAMapLocationManager;
    private AMap aMap;

    private static LocationUtil mLocation = null;

    private LocationUtil(Context context) {
        aMap = new AMap();//监听 状态改变
        mAMapLocationManager = LocationManagerProxy.getInstance(context);
        //网络定位 每5s
        mAMapLocationManager.requestLocationData(LocationProviderProxy.AMapNetwork, 5000, 10, aMap);

    }

    public static LocationUtil getInstance(Context context) {
        if (mLocation == null) {
            mLocation = new LocationUtil(context);
        }
        return mLocation;
    }

    /**
     * 某一地点 距当前位置的距离
     */
    public String getDistance(double latitude, double longitude) {
        LatLng start = new LatLng(latitude, longitude);
        LatLng end = new LatLng(currentlatitude, currentlongitude);
        float my_distance = AMapUtils.calculateLineDistance(start, end);
        String str_distance;
        if (my_distance > 1000) {
            str_distance = (int) (my_distance / 1000) + "Km";
        } else str_distance = my_distance + "m";
        return str_distance;
    }

    public double getLatitude() {
        return currentlatitude;
    }

    public double getLongitude() {
        return currentlongitude;
    }

    public String getAddress() {
        return "";
    }

    private TravelApp mApp= (TravelApp) TravelApp.getAppContext();

    private class AMap implements AMapLocationListener {

        @Override
        public void onLocationChanged(Location location) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        //位置变化时，获取经纬度
        @Override
        public void onLocationChanged(AMapLocation location) {
            currentlatitude = location.getLatitude();
            currentlongitude = location.getLongitude();
            com.cmcc.hyapps.andyou.model.Location loc = new com.cmcc.hyapps.andyou.model.Location(location.getLatitude(),location.getLongitude());
            loc.city = location.getCity();
            loc.accuracy = location.getAccuracy();
            loc.speed = location.getSpeed();
            loc.bearing = location.getBearing();
            // 设置当前地理位置－TravelApp保存为成员变量
            mApp.setCurrentLocation(loc);
            Log.i("Location", currentlatitude + "," + currentlongitude);
        }
    }

    /**
     * 取消经纬度监听
     */
    public void destroyAMapLocationListener() {
        mAMapLocationManager.removeUpdates(aMap);
        mAMapLocationManager.destory();
        mAMapLocationManager = null;

    }

    /**
     * 获取地址名称   AMap_Services_V2.3.1.jar
     */
    public void getAddressName(Context context, LatLonPoint latLonPoint, GeocodeSearch.OnGeocodeSearchListener listener) {
        GeocodeSearch geocoderSearch = new GeocodeSearch(context);
        geocoderSearch.setOnGeocodeSearchListener(listener);
        // 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200, GeocodeSearch.AMAP);
        geocoderSearch.getFromLocationAsyn(query);
    }

    public static void setLastKnownLocation(Context context, com.cmcc.hyapps.andyou.model.Location location) {
        PreferencesUtils.putDouble(context, KEY_LATITUDE, location.latitude);
        PreferencesUtils.putDouble(context, KEY_LONGITUDE, location.longitude);
        PreferencesUtils.putString(context, KEY_CITY, location.city);
    }

    /**
     * Default location: Tiananmen
     */
    private static final float DEFAULT_LATITUDE = 39.915168F;
    private static final float DEFAULT_LONGITUDE = 116.403875F;

    private LocationUtil() {
    }
    // 获取最后的位置
    public static com.cmcc.hyapps.andyou.model.Location getLastKnownLocation(Context context) {
        com.cmcc.hyapps.andyou.model.Location location = new com.cmcc.hyapps.andyou.model.Location(PreferencesUtils.getDouble(context, KEY_LATITUDE,
                DEFAULT_LATITUDE),
                PreferencesUtils.getDouble(context, KEY_LONGITUDE, DEFAULT_LONGITUDE));
        location.city = PreferencesUtils.getString(context, KEY_CITY);
        return location;
    }

    public static String formatDistance(float meters) {
        if (meters < 1000) {
            return ((int) meters) + "m";
        } else if (meters < 10000) {
            return formatDec(meters / 1000f, 1) + "km";
        } else {
            return ((int) (meters / 1000f)) + "km";
        }
    }

    static String formatDec(float val, int dec) {
        int factor = (int) Math.pow(10, dec);

        int front = (int) (val);
        int back = (int) Math.abs(val * (factor)) % factor;

        return front + "." + back;
    }
}
