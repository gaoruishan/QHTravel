package com.cmcc.hyapps.andyou.utils;

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
        aMap = new AMap();
        mAMapLocationManager = LocationManagerProxy.getInstance(context);
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
     *
     * @param latitude
     * @param longitude
     * @return
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
}