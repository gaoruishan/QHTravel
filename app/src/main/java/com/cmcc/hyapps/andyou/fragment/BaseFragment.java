/**
 * 
 */

package com.cmcc.hyapps.andyou.fragment;


import android.annotation.TargetApi;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;

import com.cmcc.hyapps.andyou.app.Const;
import com.cmcc.hyapps.andyou.util.Log;
import com.umeng.analytics.MobclickAgent;

/**
 * Base fragment, all fragment should extends it.
 * 
 * @author Kuloud
 */
public class BaseFragment extends Fragment {
    protected static final boolean DEBUG = Const.DEBUG;
    protected String simpleName;
    protected String mRequestTag = BaseFragment.class.getName();

    @Override
    public void onCreate(Bundle savedInstanceState) {

        if (getArguments() != null && getArguments().containsKey(Const.ARGS_REQUEST_TAG)) {
            mRequestTag = getArguments().getString(Const.ARGS_REQUEST_TAG);
        }
        simpleName = this.getClass().getSimpleName();
        super.onCreate(savedInstanceState);
    }
    /**
     * 统计页面
     */
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(simpleName);
        Log.e(simpleName, "onResume");
    }
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(simpleName);
        Log.e(simpleName, "onPause");
    }
    public BaseFragment() {
    }

    public String getFragmentName(){
        return this.getClass().getSimpleName();
    }

}
