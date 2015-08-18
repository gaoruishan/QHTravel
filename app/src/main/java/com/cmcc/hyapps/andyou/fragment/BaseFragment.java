/**
 * 
 */

package com.cmcc.hyapps.andyou.fragment;


import android.app.Fragment;
import android.os.Bundle;

import com.cmcc.hyapps.andyou.app.Const;
import com.umeng.analytics.MobclickAgent;

/**
 * Base fragment, all fragment should extends it.
 * 
 * @author Kuloud
 */
public class BaseFragment extends Fragment {
    protected static final boolean DEBUG = Const.DEBUG;
    String simpleName;
    protected String mRequestTag = BaseFragment.class.getName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (getArguments() != null && getArguments().containsKey(Const.ARGS_REQUEST_TAG)) {
            mRequestTag = getArguments().getString(Const.ARGS_REQUEST_TAG);
        }

        simpleName = this.getClass().getSimpleName();
//        else if (DEBUG){
//            throw new RuntimeException("A volley request tag must be provided");
//        }

        super.onCreate(savedInstanceState);
    }
    /**
     * 统计页面
     */
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(simpleName);
    }
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(simpleName);
    }
    public BaseFragment() {
    }

    public String getFragmentName(){
        return this.getClass().getSimpleName();
    }

}
