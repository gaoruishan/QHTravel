
package com.cmcc.hyapps.andyou.activity;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.cmcc.hyapps.andyou.R;
import com.cmcc.hyapps.andyou.app.AppManager;
import com.cmcc.hyapps.andyou.app.Const;
import com.cmcc.hyapps.andyou.app.MobConst;
import com.cmcc.hyapps.andyou.fragment.BlankFragment;
import com.cmcc.hyapps.andyou.fragment.BlankFragment1;
import com.cmcc.hyapps.andyou.fragment.HomeFragment;
import com.cmcc.hyapps.andyou.model.Location;
import com.cmcc.hyapps.andyou.service.LocationService;
import com.cmcc.hyapps.andyou.util.AppUtils;
import com.cmcc.hyapps.andyou.update.CheckUpdateUtil;
import com.cmcc.hyapps.andyou.util.ExcessiveClickBlocker;
import com.cmcc.hyapps.andyou.util.LocationUtil;
import com.cmcc.hyapps.andyou.util.Log;
import com.cmcc.hyapps.andyou.util.ToastUtils;
import com.cmcc.hyapps.andyou.widget.BottomTab;
import com.cmcc.hyapps.andyou.widget.BottomTab.OnTabSelected;
import com.umeng.analytics.MobclickAgent;

public class IndexActivity extends BaseActivity implements OnClickListener {
    /**
     * Position of tab.
     */
    private static final int POS_SCENIC = 0;
    private static final int POS_LIVE = POS_SCENIC + 1;
    private static final int POS_DISCOVERY = POS_LIVE + 1;
    private static final int POS_ME = POS_DISCOVERY + 1;
    private static final int REQ_SELECT_LOCATION = 1;

    private BottomTab mBottomTab;

    private long mBackPressedTime;

    private Fragment mScenicFragment = null;
    private Fragment mLiveFragment = null;
    private Fragment mDiscoveryFragment = null;
    private Fragment mMeFragment = null;
    private Fragment mCurrentFragment = null;

    private View mLocationSelectorView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);

        //检查版本更新
        try {
            CheckUpdateUtil.getInstance(this).getUpdataInfo(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //底部导航 点击切换fragment
        mBottomTab = (BottomTab) findViewById(R.id.bottom_tab);
        mBottomTab.setOnTabSelected(new OnTabSelected() {

            //点击时触发的点击事件，通过回调接口形式提供给IndexActivity
            @Override
            public void onTabSeledted(int index) {
                Fragment fragment = null;
                switch (index) {
                    //首页
                    case POS_SCENIC:
                        if (mScenicFragment == null) {
                            mScenicFragment = new HomeFragment();
                        }
                        fragment = mScenicFragment;
                        //友盟－统计事件发生次数
                        MobclickAgent.onEvent(getBaseContext(), MobConst.ID_INDEX_TAB, MobConst.VALUE_INDEX_TAB_SCENIC);
                        break;
                    // 发现
                    case POS_LIVE:
                        if (mLiveFragment == null) {
                            mLiveFragment=new BlankFragment1();
//                            mLiveFragment = new DiscoverFragment();
                        }
                        fragment = mLiveFragment;
                        MobclickAgent.onEvent(getBaseContext(), MobConst.ID_INDEX_TAB, MobConst.VALUE_INDEX_TAB_LIVE);
                        break;
                    //商户
                    case POS_DISCOVERY:
                        if (mDiscoveryFragment == null) {
                            mDiscoveryFragment=new BlankFragment();
//                            mDiscoveryFragment = new MarketFragment();
                        }
                        fragment = mDiscoveryFragment;
                        MobclickAgent.onEvent(getBaseContext(), MobConst.ID_INDEX_TAB, MobConst.VALUE_INDEX_TAB_DISCOVERY);
                        break;
                    //我的
                    case POS_ME:
                        if (mMeFragment == null) {
                            mMeFragment=new BlankFragment1();
//                            mMeFragment = new MeFragment();
                        }
                        fragment = mMeFragment;
                        MobclickAgent.onEvent(getBaseContext(), MobConst.ID_INDEX_TAB, MobConst.VALUE_INDEX_TAB_ME);
                        break;

                    default:
                        if (mScenicFragment == null) {
                            mScenicFragment = new HomeFragment();
                        }
                        fragment = mScenicFragment;
                        break;
                }

                if (!fragment.isAdded()) {
                    Bundle args = fragment.getArguments();
                    if (args == null) {
                        args = new Bundle();
                    }
                    args.putString(Const.ARGS_REQUEST_TAG, requestTag);
                    //传递标示 BaseFragment
                    fragment.setArguments(args);
                }

                if (activity != null && !activity.isFinishing()) {
                    //当前fragment为空
                    if (mCurrentFragment == null) {
                        getFragmentManager().beginTransaction()
                                .add(R.id.container, fragment).commitAllowingStateLoss();
                    } else if (fragment.isAdded()) {
                        //当前fragment不为空，但是要显示的fragment已经添加过了，隐藏当前currentFragment，直接显示fragment
                        getFragmentManager().beginTransaction().hide(mCurrentFragment)
                                .show(fragment)
                                .commitAllowingStateLoss();
                    } else {
                        //fragment还没有添加过，隐藏当前currentFragment，显示新的fragment
                        getFragmentManager().beginTransaction().hide(mCurrentFragment)
                                .add(R.id.container, fragment).commitAllowingStateLoss();
                    }
                } else {
                    Log.e("Activity is leaving");
                }
                mCurrentFragment = fragment;
            }
        });

        mLocationSelectorView = findViewById(R.id.scenic_select_location);
        mLocationSelectorView.setOnClickListener(this);

    }

    @Override
    public void onBackPressed() {
        if ((System.currentTimeMillis() - mBackPressedTime) > 2000) {
            mBackPressedTime = System.currentTimeMillis();
            ToastUtils.show(activity, R.string.press_back_to_exit);
        } else {
            super.onBackPressed();
            // stopService(new Intent(this, PlaybackService.class));
            stopService(new Intent(this, LocationService.class));
            //杀死进程前 保存统计信息
            MobclickAgent.onKillProcess(this);
            // 退出应用
            AppManager.getAppManager().AppExit(this);
        }
    }

    @Override
    public void onClick(View v) {
        if (ExcessiveClickBlocker.isExcessiveClick()) {
            return;
        }
        switch (v.getId()) {
            case R.id.scenic_select_location: {
                //选择 定位城市
                Intent intent = new Intent(this, CityChooseActivity.class);
                startActivityForResult(intent, REQ_SELECT_LOCATION);
                break;
            }
            default:
                break;
        }
    }

    public void showLocationSelector() {
        mLocationSelectorView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQ_SELECT_LOCATION && data != null) {
            Location location = (Location) data.getParcelableExtra(Const.EXTRA_COORDINATES);
            //接受来自 CityChooseActivity 的Loction
            if (location != null && location.isValid()) {
                mLocationSelectorView.setVisibility(View.GONE);
                Intent intent = new Intent(LocationService.ACTION_UPDATE_LOCATION);
                intent.putExtra(Const.EXTRA_COORDINATES, location);
                startService(intent);
            } else {
                showLocationSelector();
            }
        } else if (mCurrentFragment != null) {
            mCurrentFragment.onActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //取消经纬度监听
        LocationUtil.getInstance(AppUtils.getContext()).destroyAMapLocationListener();
    }

}
