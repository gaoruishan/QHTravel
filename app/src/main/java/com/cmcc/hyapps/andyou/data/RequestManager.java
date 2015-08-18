
package com.cmcc.hyapps.andyou.data;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.cmcc.hyapps.andyou.R;
import com.cmcc.hyapps.andyou.model.Token;
import com.cmcc.hyapps.andyou.util.AppUtils;
import com.cmcc.hyapps.andyou.util.ConstUtils;
import com.cmcc.hyapps.andyou.util.NetUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * RequestManager单例设计模式
 * <p/>
 * Created by kuloud on 14-8-15.
 */
public class RequestManager {
    private static RequestManager sInstance = new RequestManager();
    private Context mContext;

    private RequestManager() {
    }


    public static RequestManager getInstance() {
        return sInstance;
    }

    //volley的网络请求队列
    private RequestQueue mRequestQueue;
    //volley的图片加载器
    private ImageLoader mImageLoader;


    /**
     * 在appliction中初始化
     * @param context 成员变量
     */
    public void init(Context context) {

        mRequestQueue = Volley.newRequestQueue(context);
        mContext = context;

        int memCls = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE))
                .getMemoryClass();
        // Use 1/4th of the available memory for this memory cache.
        int cacheSize = 1024 * 1024 * memCls;
        mImageLoader = new ImageLoader(mRequestQueue, new BitmapLruCache(cacheSize));

    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue != null) {
            return mRequestQueue;
        } else {
            throw new IllegalStateException("RequestQueue not initialized");
        }
    }

    public void addRequest(Request<?> request, Object tag) {
        if (tag != null) {
            request.setTag(tag);
        }
        mRequestQueue.add(request);
    }

//    public void addSoapRequest(SoapRequest request, Object tag) {
//        if (tag != null) {
//            request.setTag(tag);
//        }
//        mSoapRequestQueue.add(request);
//    }

    public void cancelAll(Object tag) {
        mRequestQueue.cancelAll(tag);
    }

    /**
     * Returns instance of ImageLoader initialized with {@see FakeImageCache}
     * which effectively means that no memory caching is used. This is useful
     * for images that you know that will be show only once.
     *
     * @return
     */
    public ImageLoader getImageLoader() {
        if (mImageLoader != null) {
            return mImageLoader;
        } else {
            throw new IllegalStateException("ImageLoader not initialized");
        }
    }



    /**
     * Get original image.
     *
     * @param url
     * @param listener
     * @param errorListener
     * @param tag
     */
    public ImageRequest requestImage(String url,
                                     Response.Listener<Bitmap> listener,
                                     Response.ErrorListener errorListener, Object tag) {
        return requestImage(url, listener, errorListener, 0, 0, tag);
    }

    /**
     * Get image with given max width/height.
     *
     * @param url
     * @param listener
     * @param errorListener
     * @param maxWidth
     * @param maxHeight
     * @param tag
     */
    public ImageRequest requestImage(String url,
                                     Response.Listener<Bitmap> listener,
                                     Response.ErrorListener errorListener, int maxWidth, int maxHeight, Object tag) {
        ImageRequest imgRequest = new ImageRequest(url, listener, maxWidth, maxHeight,
                Config.ARGB_4444, errorListener);
        addRequest(imgRequest, tag);
        return imgRequest;
    }

    /**
     * Loading a image and dismiss the progress bar after finish loading.
     *
     * @param url
     * @param imageView
     * @param loadingView
     * @param tag
     * @return
     */
    public ImageRequest loadImage(String url, final ImageView imageView, final View loadingView,
                                  Object tag) {
        return requestImage(url, new Response.Listener<Bitmap>() {

            @Override
            public void onResponse(Bitmap response) {
                if (loadingView != null) {
                    loadingView.setVisibility(View.GONE);
                }
                imageView.setImageBitmap(response);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                if (loadingView != null) {
                    loadingView.setVisibility(View.GONE);
                }
                imageView.setImageResource(R.drawable.bg_image_error);
            }
        }, 0, 0, tag);
    }

    public <T> GsonRequest<T> sendGsonRequest(int method, String url, Class<T> cls, String body,
                                              Response.Listener<T> listener,
                                              Response.ErrorListener errorListener, boolean deliverExpiredCache, Object tag) {
        return sendGsonRequest(method, url, cls, body, listener, errorListener,
                deliverExpiredCache, null, null);
    }

    /**
     * Send post request with params
     *
     * @param url
     * @param cls
     * @param listener
     * @param errorListener
     * @param deliverExpiredCache
     * @param params
     */
    public <T> void sendGsonRequest(String url, Class<T> cls,
                                    Response.Listener<T> listener, Response.ErrorListener errorListener,
                                    boolean deliverExpiredCache, Map<String, String> params, Object tag) {
        sendGsonRequest(Request.Method.POST, url, cls, null, listener, errorListener, deliverExpiredCache,
                params, tag);
    }
    public <T> void sendMultipartGsonRequest(String url, Class<T> cls,Response.Listener<T> listener, Response.ErrorListener errorListener,
                                    boolean deliverExpiredCache, Map<String, String> params, Object tag) {
        sendGsonMultipartRequest(Request.Method.POST, url, cls, null, listener, errorListener, deliverExpiredCache, params, tag);
    }
    public <T> GsonRequest<T> sendGsonRequest(int method, String url, Class<T> cls, String body,
                                              Response.Listener<T> listener, Response.ErrorListener errorListener,
                                              boolean deliverExpiredCache, final Map<String, String> params, Object tag) {
        // Now send the request to fetch data
        android.util.Log.e("联网请求url:",""+url);
        if(null!=params&&params.size()>0){
            String strPar = ConstUtils.map2string(params);
            android.util.Log.e("联网请求params:",""+strPar);
        }

        GsonRequest<T> gsonRequest = new GsonRequest<T>(method, url, cls, body, listener, errorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                String uid = AppUtils.getUid(mContext);
                if (!TextUtils.isEmpty(uid)) {
                    headers.put("x-uid", uid);
//                    Content-type: application/x-www-form-urlencoded
                    headers.put("Content-Type","application/json");
//                    headers.put("Content-Type","application/x-www-form-urlencoded");
                }
                if(AppUtils.getTokenInfo(mContext)!=null){
                    Token tokenInfo = AppUtils.getQHToken(mContext);
                    if (tokenInfo != null && !TextUtils.isEmpty(tokenInfo.token)) {
                        headers.put("Authorization", "JWT " + tokenInfo.token);
                    }
                }

                return headers;
            }

//            @Override
//            public RetryPolicy getRetryPolicy() {
//                RetryPolicy retryPolicy = new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
//                return retryPolicy;
//            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return params;
            }

            @Override
            public String getBodyContentType() {
                // If params not null, set content-type form
                return (params == null) ? super.getBodyContentType()
                        : "application/json; charset="
                        + getParamsEncoding();
            }
        };

        boolean cacheDeliverd = false;
        if (deliverExpiredCache) {
            Cache cache = getRequestQueue().getCache();
            Cache.Entry entry = cache.get(gsonRequest.getCacheKey());
            if (entry != null/** && entry.isExpired() */
                    ) {
                Log.d("[CACHE] Using expired cache for url %s", url);
                Response<T> response = gsonRequest.parseNetworkResponse(
                        new NetworkResponse(entry.data, entry.responseHeaders));
                android.util.Log.i("law", "RequestManager+" + response.toString());
                gsonRequest.deliverResponse(response.result);
                cacheDeliverd = true;
            }
        }

        if (!cacheDeliverd && !NetUtils.isNetworkAvailable(mContext)) {
            Toast.makeText(mContext, R.string.network_unavailable,
                    Toast.LENGTH_SHORT).show();
            gsonRequest.deliverError(new NoConnectionError());
        }

        addRequest(gsonRequest, tag);
        Log.d("Sending gson request for url %s", url);

        return gsonRequest;
    }
    public <T> GsonRequest<T> sendGsonMultipartRequest(int method, String url, Class<T> cls, String body,
                                              Response.Listener<T> listener, Response.ErrorListener errorListener,
                                              boolean deliverExpiredCache, final Map<String, String> params, Object tag) {
        // Now send the request to fetch data
        android.util.Log.e("联网请求url:",""+url);
        if(null!=params&&params.size()>0){
            String strPar = ConstUtils.map2string(params);
            android.util.Log.e("联网请求params:",""+strPar);
        }

        GsonRequest<T> gsonRequest = new GsonRequest<T>(method, url, cls, body, listener, errorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                String uid = AppUtils.getUid(mContext);
                if (!TextUtils.isEmpty(uid)) {
                    headers.put("x-uid", uid);
                    headers.put("Content-Type","multipart/form-data");
                }
                Token tokenInfo = AppUtils.getQHToken(mContext);
                if (tokenInfo != null && !TextUtils.isEmpty(tokenInfo.token)) {
                    headers.put("Authorization", "JWT " + tokenInfo.token);
                }
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return params;
            }

            @Override
            public String getBodyContentType() {
                // If params not null, set content-type form
                return (params == null) ? super.getBodyContentType()
                        : "application/json; charset="
                        + getParamsEncoding();
            }
        };

        boolean cacheDeliverd = false;
        if (deliverExpiredCache) {
            Cache cache = getRequestQueue().getCache();
            Cache.Entry entry = cache.get(gsonRequest.getCacheKey());
            if (entry != null/** && entry.isExpired() */
                    ) {
                Log.d("[CACHE] Using expired cache for url %s", url);
                Response<T> response = gsonRequest.parseNetworkResponse(
                        new NetworkResponse(entry.data, entry.responseHeaders));
                android.util.Log.i("law", "RequestManager+" + response.toString());
                gsonRequest.deliverResponse(response.result);
                cacheDeliverd = true;
            }
        }

        if (!cacheDeliverd && !NetUtils.isNetworkAvailable(mContext)) {
            Toast.makeText(mContext, R.string.network_unavailable,
                    Toast.LENGTH_SHORT).show();
//            gsonRequest.deliverError(new NoConnectionError());
        }
//        if(!cacheDeliverd)//使用缓存就不在联网，否则掉两次onresponse，导致后面的联网连续请求两次。

        //有网络的时候从新请求，避免没网络的时候报 volleyError.
        if(NetUtils.isNetworkAvailable(mContext))addRequest(gsonRequest, tag);
        Log.d("Sending gson request for url %s", url);

        return gsonRequest;
    }
    public <T> GsonRequest<T> sendGsonRequest(String url, Class<T> cls,
                                              Response.Listener<T> listener,
                                              Response.ErrorListener errorListener, Object tag) {
        return sendGsonRequest(Request.Method.GET, url, cls, null, listener, errorListener, false, tag);
    }

    public <T> GsonRequest<T> sendGsonRequest(int method, String url, String body, Class<T> cls,
                                              Response.Listener<T> listener,
                                              Response.ErrorListener errorListener, Object tag) {
        return sendGsonRequest(method, url, cls, body, listener, errorListener, false, tag);
    }

    public interface RequestCallback {
        public byte[] getBody();

        public String getBodyContentType();

        public Map<String, String> getHeaders();

        public Map<String, String> getParams();
    }
}
