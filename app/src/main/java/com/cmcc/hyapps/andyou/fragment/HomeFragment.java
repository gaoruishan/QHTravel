
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
import com.cmcc.hyapps.andyou.model.City;
import com.cmcc.hyapps.andyou.model.Comment;
import com.cmcc.hyapps.andyou.model.Location;
import com.cmcc.hyapps.andyou.model.HomeBanner;
import com.cmcc.hyapps.andyou.model.Scenic;
import com.cmcc.hyapps.andyou.model.Recommand;
import com.cmcc.hyapps.andyou.util.ConstUtils;
import com.cmcc.hyapps.andyou.util.ExcessiveClickBlocker;
import com.cmcc.hyapps.andyou.util.FormatUtils;
import com.cmcc.hyapps.andyou.util.Log;
import com.cmcc.hyapps.andyou.util.ScreenUtils;
import com.cmcc.hyapps.andyou.util.ToastUtils;
import com.cmcc.hyapps.andyou.widget.ActionBar;
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
    private final String TAG = "FreshHomeFragment";

    private final int REQUEST_CODE_LOGIN_NEW_COMMENT = 1;
    private final int REQUEST_CODE_LOGIN_VOTE = 2;
    private final int REQUEST_CODE_LOGIN_COMMENT_DETAIL = 3;
    private final int REQUEST_CODE_SEARCH = 4;
    private final int REQUEST_CODE_COMMENT_DETAIL = 5;
    private final int REQUEST_CODE_POST_COMMENT = 6;
    private final int REQUEST_CODE_SEARCH_ET = 7;//
    private static final int ACCELERATION_THRESOLD = 22;
    private ActionBar mActionBar;
    private RecyclerView mRecyclerView;
    private HomeAdapter mAdapter;
    private int mId = -1;
    private Location mLocation = new Location(39.908815, 116.397228);//我当前loc
    private Location secnicLoc = new Location(39.908815, 116.397228);//当前景区loc
    private View mLoadingProgress;
    private View mReloadView;
    private PullToRefreshRecyclerView mPullToRefreshView;

    private UrlListLoader<Recommand.RecommandList> recommandListUrlListLoader;
    private GsonRequest<HomeBanner.HomeBannerLists> mBannerRequest;
    private ViewGroup mRootView;
    private Vibrator mVibrator;
    private boolean mRandomLoad;
    //特色推荐
    private Recommand mRecommand;
    private LocationDetector mLocationDetector;
    private int HTTP_GET_PARAM_LIMIT = 10;
    //    private City dectedCity;
    private boolean isLoading = false;

    private List<Recommand> recommands ;
    private LocationListener mLocationListener = new LocationListener() {

        @Override
        public void onReceivedLocation(Location loc) {
            Log.d("onReceivedLocation, location=%s", loc);
            mLocation = loc;
            secnicLoc = loc;
            ConstUtils.myCurrentLoacation = new Location(loc.latitude, loc.longitude);
            ConstUtils.myCurrentLoacation.city = loc.city;
            ConstUtils.myCurrentLoacation.city_en = loc.city_en;
            Log.e("-------", "onReceivedLocation--");
        }

        @Override
        public void onLocationTimeout() {
            Log.d("onLocationTimeout");
            ((IndexActivity) getActivity()).showLocationSelector();
            mLocation = null;
            mLocation = new Location(39.908815, 116.397228);
            secnicLoc = new Location(39.908815, 116.397228);
            ConstUtils.myCurrentLoacation = new Location(39.908815, 116.397228);
            ConstUtils.myCurrentLoacation.city = "北京";
            ConstUtils.myCurrentLoacation.city_en = "beijing";
            loadHomeBanner();
        }

        @Override
        public void onLocationError() {
            mLocation = null;
            mLocation = new Location(39.908815, 116.397228);
            secnicLoc = new Location(39.908815, 116.397228);
            ConstUtils.myCurrentLoacation = new Location(39.908815, 116.397228);
            ConstUtils.myCurrentLoacation.city = "北京";
            ConstUtils.myCurrentLoacation.city_en = "beijing";
            loadHomeBanner();
            Log.d("onLocationError");
            // TODO
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationDetector = new LocationDetector(getActivity().getApplicationContext());
        mVibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
      //  ShareManager.getInstance().onStart(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = (ViewGroup) LayoutInflater.from(getActivity()).inflate(R.layout.home_fragment, container, false);
        initViews();
        reload();
        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(TAG);
    }

    @Override
    public void onPause() {
        MobclickAgent.onPageEnd(TAG);
        super.onPause();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mId = getArguments().getInt(Const.EXTRA_ID);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        Log.d("onHiddenChanged, hidden=%s", hidden);
        // hookSensorListener(!hidden);
        super.onHiddenChanged(hidden);
    }

    private void reload() {
        mPullToRefreshView.setMode(Mode.DISABLED);
        mRecyclerView.setVisibility(View.GONE);
        mLoadingProgress.setVisibility(View.VISIBLE);
        mReloadView.setVisibility(View.GONE);
        mAdapter.setHeader(null);
        mAdapter.setDataItems(null);
        RequestManager.getInstance().getRequestQueue().cancelAll(mRequestTag);
        loadHomeBanner();
        //定位观察者 －－
        mLocationDetector.detectLocation(mLocationListener, true, true);
    }

    private void initViews() {
        mRootView.findViewById(R.id.search_content).setOnClickListener(this);
        initListView();
        initPullToRefresh();

        mLoadingProgress = mRootView.findViewById(R.id.loading_progress);
        mReloadView = mRootView.findViewById(R.id.reload_view);
        mReloadView.setOnClickListener(this);
    }

    private void initPullToRefresh() {
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
                        Intent intent = new Intent();
                        if (HomeBanner.SCENIC == data.stype) {//景区
                            MobclickAgent.onEvent(getActivity(), MobConst.ID_HOME_PPT1);
//                            intent = new Intent(HomeFragment.this.getActivity(), SecnicActivity.class);
                            int mId = Integer.parseInt(data.action);
                            //intent.putExtra(Const.QH_SECNIC, mScenic);
                            intent.putExtra(Const.QH_SECNIC_ID, mId);
                            startActivity(intent);
                        } else if (HomeBanner.STRATEGY == data.stype) {//攻略
                            MobclickAgent.onEvent(getActivity(), MobConst.ID_HOME_PPT2);
//                            intent = new Intent(HomeFragment.this.getActivity(), StrategyDetailActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putInt("id", Integer.parseInt(data.action));
                            intent.putExtra("guide", bundle);
                        } else if (HomeBanner.VIDEO == data.stype) {//视频
                            MobclickAgent.onEvent(getActivity(), MobConst.ID_INDEX_HOME_ACT);
                            String url = ServerAPI.VideoList.buildItemDetailUrl(data.action);
                            RequestManager.getInstance().sendGsonRequest(Method.GET, url,
                                    Scenic.QHVideo.class, null,
                                    new Response.Listener<Scenic.QHVideo>() {
                                        @Override
                                        public void onResponse(final Scenic.QHVideo item) {
                                            // 0：使用白天视频 1：使用夜间视频
                                            Intent intent = null;
                                            switch (item.day_or_night) {
                                                case 0:
//                                                    intent = new Intent(getActivity(), VideoActivity.class);
                                                    intent.putExtra("url", item.video_day);
                                                    getActivity().startActivity(intent);
                                                    break;
                                                case 1:
//                                                    intent = new Intent(getActivity(), NightLiveActivity.class);
                                                    intent.putExtra("url", item.video_night);
                                                    getActivity().startActivity(intent);
                                                    break;
                                                default:
//                                                    intent = new Intent(getActivity(), VideoActivity.class);
                                                    intent.putExtra("url", item.video_day);
                                                    getActivity().startActivity(intent);
                                                    break;
                                            }

                                        }
                                    }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            Toast.makeText(getActivity(), "获取视频资源失败，请检查网络", Toast.LENGTH_SHORT).show();
                                        }
                                    }, false, mRequestTag);
                            return;
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
                    onHeaderClicked(view);
                } else {
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
            case R.id.action_bar_left:
                getActivity().finish();
                break;
            case R.id.action_bar_left_text: {
//                Intent intent = new Intent(getActivity(), /*VideoPlayer*//*MediaPlayerDemo_Video*/CityListActivity.class);
//                startActivityForResult(intent, REQUEST_CODE_SEARCH);
                MobclickAgent.onEvent(getActivity(), MobConst.ID_INDEX_SCENIC_SEARCH);
                break;
            }
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

    private void loadHomeBanner() {
        isLoading = true;
        final String url;
        url = ServerAPI.BASE_URL + "banners/?format=json";
        Log.e("Loading scenic details from %s", url);
        mBannerRequest = RequestManager.getInstance().sendGsonRequest(Method.GET, url,
                HomeBanner.HomeBannerLists.class, null,
                new Response.Listener<HomeBanner.HomeBannerLists>() {
                    @Override
                    public void onResponse(HomeBanner.HomeBannerLists response) {
                        mPullToRefreshView.onRefreshComplete();
                        Log.e("loadHomeBanner, ScenicDetails=%s,mAdapter.size=%d", response, mAdapter.getDataItems().size());
                        mRandomLoad = false;
                        mAdapter.setDataItems(null);
                        mAdapter.setHeader(response);
                        mAdapter.setMyLocation(mLocation);
                   //     mRecyclerView.setVisibility(View.VISIBLE);
                        mAdapter.notifyDataSetChanged();
                        loadRecommand(DataLoader.MODE_REFRESH);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(error, "Error loading scenic details from %s", url);
                        showReloadView();
                        mRandomLoad = false;
                        mPullToRefreshView.onRefreshComplete();
                    }
                }, true, mRequestTag);

    }

    private void showReloadView() {
        mRecyclerView.setVisibility(View.GONE);
        mLoadingProgress.setVisibility(View.GONE);
        mReloadView.setVisibility(View.VISIBLE);
    }


    private void loadRecommand(int mode) {
        if (recommandListUrlListLoader == null) {
            recommandListUrlListLoader = new UrlListLoader<Recommand.RecommandList>(mRequestTag, Recommand.RecommandList.class);
            String url;
            url = ServerAPI.BASE_URL + "recommends";
            recommandListUrlListLoader.setUrl(url);
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
        onListLoaded(list, mode);
    }

    @Override
    public void onLoadError(int mode) {
        mPullToRefreshView.onRefreshComplete();
        mLoadingProgress.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.INVISIBLE);
        mReloadView.setVisibility(View.VISIBLE);
    }


    private void onListLoaded(Recommand.RecommandList data, int mode) {
            mAdapter.setDataItems(data.results);
    }

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

    private void onHeaderClicked(View v) {
        /*if (mScenicDetailsModel == null) {
            return;
        }*/
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_LOGIN_NEW_COMMENT:
                    break;
                case REQUEST_CODE_LOGIN_VOTE:
                    break;
                case REQUEST_CODE_LOGIN_COMMENT_DETAIL:
                    break;
                case REQUEST_CODE_SEARCH:
                    City city = data.getParcelableExtra(Const.CITYMODE);
                    if (null != city) {
                        mId = 1;

//                        if (null == mScenicDetailsModel) {
//                            mLocation.latitude = city.location.latitude;
//                            mLocation.longitude = city.location.longitude;
//                            mScenicDetailsModel = new ScenicDetails();
//                        } else {
//                            mScenicDetailsModel.city = city.code;
//                            mScenicDetailsModel.cityZh = city.name;
//                            mScenicDetailsModel.location.latitude = city.location.latitude;
//                            mScenicDetailsModel.location.longitude = city.location.longitude;
//                        }
                        mActionBar.getLeftTextView().setText(FormatUtils.cutStringStartBy(city.name, 3));
                        secnicLoc.latitude = city.location.latitude;
                        secnicLoc.longitude = city.location.longitude;
                        secnicLoc.city = city.name;
                        secnicLoc.city_en = city.code;

                        mAdapter.setDataItems(null);
                     //   mScenicListLoader = null;
                     //   loadNearScenics(city.name);
                    } else {
                        Log.e("error onActivityResult, id = ", mId);
                    }
                    break;
                case REQUEST_CODE_SEARCH_ET:
                    int id = data.getIntExtra(Const.EXTRA_ID, -1);
                    if (id > -1 && mId != id) {
                        MobclickAgent.onEvent(getActivity(), MobConst.ID_INDEX_HOME_SECNIC);
//                        Intent intent = new Intent(getActivity(), SecnicActivity.class);
//                        intent.putExtra(Const.EXTRA_ID, id);
//                        intent.putExtra(Const.EXTRA_COORDINATES, mLocation);
//                        startActivity(intent);
                    } else {
                        Log.e("error onActivityResult, id = ", id);
                    }
                    break;
                case REQUEST_CODE_COMMENT_DETAIL:
                    break;
                case REQUEST_CODE_POST_COMMENT: {
                    Comment c = (Comment) data.getParcelableExtra(Const.EXTRA_COMMENT_DATA);
                    if (c != null) {
//                        mAdapter.getDataItems().add(0, c);
                        mAdapter.notifyDataSetChanged();
                    }
                    break;
                }
                default:
                    break;
            }
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
