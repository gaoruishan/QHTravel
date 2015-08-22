
package com.cmcc.hyapps.andyou.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cmcc.hyapps.andyou.R;
import com.cmcc.hyapps.andyou.activity.IndexActivity;
import com.cmcc.hyapps.andyou.adapter.BannerPagerAdapter;
import com.cmcc.hyapps.andyou.adapter.HomeAdapter;
import com.cmcc.hyapps.andyou.app.Const;
import com.cmcc.hyapps.andyou.app.MobConst;
import com.cmcc.hyapps.andyou.app.ServerAPI;
import com.cmcc.hyapps.andyou.app.ShareManager;
import com.cmcc.hyapps.andyou.data.DataLoader;
import com.cmcc.hyapps.andyou.data.DataLoader.DataLoaderCallback;
import com.cmcc.hyapps.andyou.data.GsonRequest;
import com.cmcc.hyapps.andyou.data.LocationDetector;
import com.cmcc.hyapps.andyou.data.LocationDetector.LocationListener;
import com.cmcc.hyapps.andyou.data.RequestManager;
import com.cmcc.hyapps.andyou.data.UrlListLoader;
import com.cmcc.hyapps.andyou.model.Location;
import com.cmcc.hyapps.andyou.model.HomeBanner;
import com.cmcc.hyapps.andyou.model.Recommand;
import com.cmcc.hyapps.andyou.util.ConstUtils;
import com.cmcc.hyapps.andyou.util.ExcessiveClickBlocker;
import com.cmcc.hyapps.andyou.util.Log;
import com.cmcc.hyapps.andyou.util.ScreenUtils;
import com.cmcc.hyapps.andyou.util.ToastUtils;
import com.cmcc.hyapps.andyou.widget.PullToRefreshRecyclerView;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.kuloud.android.widget.recyclerview.DividerItemDecoration;
import com.kuloud.android.widget.recyclerview.ItemClickSupport;
import com.kuloud.android.widget.recyclerview.ItemClickSupport.OnItemSubViewClickListener;
import com.umeng.analytics.MobclickAgent;

import java.util.List;


public class HomeFragment extends BaseFragment implements OnClickListener, DataLoaderCallback<Recommand.RecommandList> {
    private RecyclerView mRecyclerView;
    private HomeAdapter mAdapter;
    private int mId = -1;
    private View mLoadingProgress;
    private View mReloadView;
    private PullToRefreshRecyclerView mPullToRefreshView;

    private UrlListLoader<Recommand.RecommandList> recommandListUrlListLoader;
    private GsonRequest<HomeBanner.HomeBannerLists> mBannerRequest;
    private ViewGroup mRootView;
    private TextView locationCity;
    //特色推荐
    private Recommand mRecommand;
    //地理位置 检测者
    private LocationDetector mLocationDetector;
    private int HTTP_GET_PARAM_LIMIT = 10;
    private boolean isLoading = false;

    private List<Recommand> recommands ;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mId = getArguments().getInt(Const.EXTRA_ID);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationDetector = new LocationDetector(getActivity().getApplicationContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = (ViewGroup) LayoutInflater.from(getActivity()).inflate(R.layout.home_fragment, container, false);
        initViews();
        reload();
        return mRootView;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        //当fragment隐藏时，该方法会调用传入参数为true
        Log.d("onHiddenChanged, hidden=%s", hidden);
        super.onHiddenChanged(hidden);
    }

    /**
     * 重新加载 置空和取消网络请求
     */
    private void reload() {
        mPullToRefreshView.setMode(Mode.DISABLED);
        mRecyclerView.setVisibility(View.GONE);
        mLoadingProgress.setVisibility(View.VISIBLE);
        mReloadView.setVisibility(View.GONE);
        mAdapter.setHeader(null);
        mAdapter.setDataItems(null);
        RequestManager.getInstance().getRequestQueue().cancelAll(mRequestTag);
        loadHomeBanner();
        //定位检测者  LocationDetector
        mLocationDetector.detectLocation(mLocationListener, true, true);
    }
    //定位检测者 接口回调
    private LocationListener mLocationListener = new LocationListener() {

        @Override
        public void onReceivedLocation(Location loc) {
            ConstUtils.myCurrentLoacation = new Location(loc.latitude, loc.longitude);
            ConstUtils.myCurrentLoacation.city = loc.city;
            ConstUtils.myCurrentLoacation.city_en = loc.city_en;
            //设置城市名
            if (loc!=null&&loc.city!=null)
            locationCity.setText(loc.city);
            else
            Log.e("-------", "onReceivedLocation--");
        }

        @Override
        public void onLocationTimeout() {
            //显示手动选择位置
            ((IndexActivity) getActivity()).showLocationSelector();
            setDefaultCity();
            loadHomeBanner();
        }

        @Override
        public void onLocationError() {
            setDefaultCity();
            loadHomeBanner();
            Log.d("onLocationError");
        }
    };
    /**
     *   默认城市
     */
    private void setDefaultCity() {
        ConstUtils.myCurrentLoacation = new Location(39.908815, 116.397228);
        ConstUtils.myCurrentLoacation.city = "北京";
        ConstUtils.myCurrentLoacation.city_en = "beijing";
    }

    private void initViews() {
        mRootView.findViewById(R.id.search_content).setOnClickListener(this);
        locationCity= (TextView) mRootView.findViewById(R.id.action_bar_left);
        initListView();
        mPullToRefreshView = (PullToRefreshRecyclerView) mRootView.findViewById(R.id.pulltorefresh_twowayview);
        mPullToRefreshView.setOnRefreshListener(new OnRefreshListener<RecyclerView>() {

            @Override
            public void onRefresh(PullToRefreshBase<RecyclerView> refreshView) {
                if (refreshView.getCurrentMode() == Mode.PULL_FROM_START) {
                    reload();
                } else {
                    //下拉加载
                    if (isLoading) {
                        ToastUtils.show(getActivity(), "亲，正在加载……");
                    } else {
                        loadRecommand(DataLoader.MODE_LOAD_MORE);
                    }
                }
            }
        });
        mLoadingProgress = mRootView.findViewById(R.id.loading_progress);
        mReloadView = mRootView.findViewById(R.id.reload_view);
        mReloadView.setOnClickListener(this);
    }

    private void initListView() {

        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recyclerviews);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new HomeAdapter(getActivity(),
                new BannerPagerAdapter.IActionCallback<HomeBanner>() {
                    @Override
                    public void doAction(HomeBanner data) {
                        if (data == null) {
                            return;
                        }
                        //头部轮播图片点击
                        Intent intent = new Intent();
                        if (HomeBanner.SCENIC == data.stype) {//景区
                            MobclickAgent.onEvent(getActivity(), MobConst.ID_HOME_PPT1);
//                            intent = new Intent(HomeFragment.this.getActivity(), SecnicActivity.class);
                            int mId = Integer.parseInt(data.action);
                            //intent.putExtra(Const.QH_SECNIC, mScenic);
                            intent.putExtra(Const.QH_SECNIC_ID, mId);
                        } else if (HomeBanner.STRATEGY == data.stype) {//攻略
                            MobclickAgent.onEvent(getActivity(), MobConst.ID_HOME_PPT2);
//                            intent = new Intent(HomeFragment.this.getActivity(), StrategyDetailActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putInt("id", Integer.parseInt(data.action));
                            intent.putExtra("guide", bundle);
                        } else if (HomeBanner.H5 == data.stype) {//h5
                            MobclickAgent.onEvent(getActivity(), MobConst.ID_INDEX_HOME_ACT);
//                            intent.setClass(getActivity(), WebActivity.class);
                            intent.putExtra(Const.EXTRA_URI, data.action);
                        }
                        startActivity(intent);
                    }


                }
        );
        ItemClickSupport clickSupport = ItemClickSupport.addTo(mRecyclerView);
        clickSupport.setOnItemSubViewClickListener(new OnItemSubViewClickListener() {

            @Override
            public void onItemClick(View view, int position) {
                if (position == 0) {
                    //点击头部标签
                    onHeaderClicked(view);
                } else {
                    //点击 item
                    onItemClicked(view);
                }
            }
        });
        int scap = ScreenUtils.dpToPxInt(getActivity(), 13);
        DividerItemDecoration decor = new DividerItemDecoration(scap);
        decor.initWithRecyclerView(mRecyclerView);
        mRecyclerView.addItemDecoration(decor);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);
    }


    @Override
    public void onClick(View v) {
        if (ExcessiveClickBlocker.isExcessiveClick()) {
            return;
        }
        switch (v.getId()) {
            case R.id.reload_view: {
                reload();
                break;
            }
            case R.id.search_content:
                MobclickAgent.onEvent(getActivity(), MobConst.ID_HOME_BTN_CITY);
                //跳转到搜索页面
//                Intent intent = new Intent(getActivity(), SearchListActivity.class);
//                startActivity(intent);
                break;
            default:
                break;
        }
    }

    /**
     * 加载首页头部数据
     */
    private void loadHomeBanner() {
        isLoading = true;
        mBannerRequest = RequestManager.getInstance().sendGsonRequest(Method.GET, ServerAPI.BASE_URL + "banners/?format=json",
                HomeBanner.HomeBannerLists.class, null,
                new Response.Listener<HomeBanner.HomeBannerLists>() {
                    @Override
                    public void onResponse(HomeBanner.HomeBannerLists response) {
                        mPullToRefreshView.onRefreshComplete();
                        mAdapter.setDataItems(null);
                        mAdapter.setHeader(response);
                        mAdapter.notifyDataSetChanged();
                        //加载特色推荐
                        loadRecommand(DataLoader.MODE_REFRESH);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showReloadView();
                        mPullToRefreshView.onRefreshComplete();
                    }
                }, true, mRequestTag);

    }

    /**
     *  显示重新加载的视图
     */
    private void showReloadView() {
        mRecyclerView.setVisibility(View.GONE);
        mLoadingProgress.setVisibility(View.GONE);
        mReloadView.setVisibility(View.VISIBLE);
    }

    /**
     * 加载特色推荐
     * @param mode
     */
    private void loadRecommand(int mode) {
        if (recommandListUrlListLoader == null) {
            recommandListUrlListLoader = new UrlListLoader<Recommand.RecommandList>(mRequestTag, Recommand.RecommandList.class);
            recommandListUrlListLoader.setUrl(ServerAPI.BASE_URL + "recommends");
        }
        //加载更多
        recommandListUrlListLoader.loadMoreQHData(this, mode);
    }


    @Override
    public void onLoadFinished(Recommand.RecommandList list, int mode) {
        isLoading = false;
        recommands = list.results;
        mPullToRefreshView.onRefreshComplete();
        mLoadingProgress.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
        mAdapter.setDataItems(list.results);
    }

    @Override
    public void onLoadError(int mode) {
        mPullToRefreshView.onRefreshComplete();
        mLoadingProgress.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.INVISIBLE);
        mReloadView.setVisibility(View.VISIBLE);
    }

    /**
     * adapter item点击  进入详情页
     * @param v
     */
    private void onItemClicked(View v) {
        if (ExcessiveClickBlocker.isExcessiveClick()) {
            return;
        }

        mRecommand = (Recommand) v.getTag();
        if (mRecommand == null) {
            Log.e("NULL recommand");
            return;
        }

        switch (v.getId()) {
            case R.id.iv_cover_image:
                if (mRecommand.stype == 0)
                {
                    MobclickAgent.onEvent(getActivity(), MobConst.ID_INDEX_HOME_SECNIC);
//                    Intent intent = new Intent(getActivity(), SecnicActivity.class);
//                    int mId = Integer.parseInt(mRecommand.action);
//                    intent.putExtra(Const.QH_SECNIC, mScenic);
//                    intent.putExtra(Const.QH_SECNIC_ID, mId);
//                    startActivity(intent);
                }else {
                  //  MobclickAgent.onEvent(getActivity(), MobConst.ID_INDEX_HOME_SECNIC);
//                    Intent intent1 = new Intent(getActivity(), ShopDetailActivity.class);
//                    intent1.putExtra("shopID",mRecommand.action);
//                    startActivity(intent1);
                }
                break;
            default:
                break;
        }
    }

    /**
     * adapter 头部点击
     * @param v
     */
    private void onHeaderClicked(View v) {

        switch (v.getId()) {
            case R.id.home_tab_live://直播
                MobclickAgent.onEvent(getActivity(), MobConst.ID_HOME_HOME_BTN_BBQ);
//                Intent intent3 = new Intent(getActivity(), LiveActivity.class);
//                startActivity(intent3);
                break;
            case R.id.home_tab_nav://导航

                MobclickAgent.onEvent(getActivity(), MobConst.ID_HOME_BTN_HOTEL);
//                Intent intent1 = new Intent(getActivity(), QHNavigationActivity.class);
//                startActivity(intent1);
                break;
            case R.id.home_tab_strategy://攻略
                MobclickAgent.onEvent(getActivity(), MobConst.ID_HOME_BTN_SPECIALTY);
//                Intent intent2 = new Intent(getActivity(), StrategyActivity.class);

//                if (null != mScenicDetailsModel) {
//                    intent2.putExtra(Const.CITYNAME_EN, mScenicDetailsModel.city);
//                    intent2.putExtra(Const.CITYNAME, mScenicDetailsModel.cityZh);
//                } else {
//                    intent2.putExtra(Const.CITYNAME_EN, "beijing");
//                    intent2.putExtra(Const.CITYNAME, "北京");
//                }
//                startActivity(intent2);
                break;
            default:
                break;
        }


    }


    @Override
    public void onDestroy() {
        if (mLocationDetector != null) {
            mLocationDetector.close();
        }
        ShareManager.getInstance().onEnd();
        super.onDestroy();
    }



}
