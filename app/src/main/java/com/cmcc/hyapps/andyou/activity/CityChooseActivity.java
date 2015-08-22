/**
 *
 */

package com.cmcc.hyapps.andyou.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cmcc.hyapps.andyou.R;
import com.cmcc.hyapps.andyou.app.Const;
import com.cmcc.hyapps.andyou.app.ServerAPI;
import com.cmcc.hyapps.andyou.app.TravelApp;
import com.cmcc.hyapps.andyou.data.RequestManager;
import com.cmcc.hyapps.andyou.model.City;
import com.cmcc.hyapps.andyou.model.City.CityList;
import com.cmcc.hyapps.andyou.model.Location;
import com.cmcc.hyapps.andyou.support.OnClickListener;
import com.cmcc.hyapps.andyou.util.Log;
import com.cmcc.hyapps.andyou.util.ScreenUtils;
import com.cmcc.hyapps.andyou.widget.ActionBar;
import com.cmcc.hyapps.andyou.widget.ClearEditText;
import com.cmcc.hyapps.andyou.widget.pinyinsidebar.CharacterParser;
import com.cmcc.hyapps.andyou.widget.pinyinsidebar.PinyinComparator;
import com.cmcc.hyapps.andyou.widget.pinyinsidebar.PinyinSideBar;
import com.cmcc.hyapps.andyou.widget.pinyinsidebar.PinyinSideBar.OnTouchingLetterChangedListener;
import com.cmcc.hyapps.andyou.widget.pinyinsidebar.SortAdapter;
import com.cmcc.hyapps.andyou.widget.pinyinsidebar.SortModel;
import com.kuloud.android.widget.recyclerview.DividerItemDecoration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * @author kuloud
 */
public class CityChooseActivity extends BaseActivity {
    private ListView mCityListView;
    private PinyinSideBar mSideBar;
    private TextView mLetterDialog;
    private SortAdapter mSortAdapter;
    private ClearEditText mClearEditText;

    private CharacterParser mCharacterParser;
    private List<SortModel> mCityListModel = new ArrayList<SortModel>();

    private PinyinComparator mPinyinComparator;
    private RecyclerView mHotCities;
    private RecyclerView mDetectCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_chooser);
        mCharacterParser = CharacterParser.getInstance();
        mPinyinComparator = new PinyinComparator();
        //初始化actionbar 热门城市
        initViews();
        loadCityList();
    }

    private void initViews() {
        initActionBar();
        //1,热门城市
        mHotCities = (RecyclerView) findViewById(R.id.hot_city_view);//使用RecycleView 中的v7封装的GridLayoutManager
        GridLayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 3);
        mHotCities.setLayoutManager(layoutManager);
        mHotCities.setItemAnimator(new DefaultItemAnimator());
        int verticalGap = ScreenUtils.dpToPxInt(getApplicationContext(), 13);
        int horizontalGap = ScreenUtils.dpToPxInt(getApplicationContext(), 6);
        DividerItemDecoration decor = new DividerItemDecoration(verticalGap, horizontalGap);
        decor.initWithRecyclerView(mHotCities);
        mHotCities.addItemDecoration(decor);//默认item之间线隐藏，设置间距
        mHotCities.setAdapter(new CityAdapter());

        //2,定位城市
        mDetectCity = (RecyclerView) findViewById(R.id.detected_city_view);
        layoutManager = new GridLayoutManager(getApplicationContext(), 3);
        mDetectCity.setLayoutManager(layoutManager);
        mDetectCity.setItemAnimator(new DefaultItemAnimator());
        DividerItemDecoration decor2 = new DividerItemDecoration(verticalGap, horizontalGap);
        decor2.initWithRecyclerView(mDetectCity);
        mDetectCity.addItemDecoration(decor2);
        mDetectCity.setAdapter(new CityAdapter());
        //获得地理位置
        Location location = ((TravelApp) getApplication()).getCurrentLocation();
        if (location != null) {
            City city = new City();
            city.location = location;
            city.name = location.city;
            List<City> cityList = new ArrayList<City>();
            cityList.add(city);
            ((CityAdapter) mDetectCity.getAdapter()).setCityList(cityList);
        } else {
            //获取不到 Location 隐藏定位城市
            findViewById(R.id.detected_city_text).setVisibility(View.GONE);
            mDetectCity.setVisibility(View.GONE);
        }
        //3,城市列表
        mCityListView = (ListView) findViewById(R.id.city_letter_list);
        mCityListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                 //从适配器中取出 并选择
                City city = ((SortModel) mSortAdapter.getItem(position)).getCity();
                onCityChoosed(city);
            }
        });
        //城市分类 适配器
        mSortAdapter = new SortAdapter(this, mCityListModel);
        mCityListView.setAdapter(mSortAdapter);
        //右侧 拼音选择
        mSideBar = (PinyinSideBar) findViewById(R.id.sidebar);
        mLetterDialog = (TextView) findViewById(R.id.city_letter_dialog);
        mSideBar.setTextView(mLetterDialog);

        mSideBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {

            @Override
            public void onTouchingLetterChanged(String s) {
                int position = mSortAdapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    mCityListView.setSelection(position);
                }

            }
        });
        //输入框
        mClearEditText = (ClearEditText) findViewById(R.id.filter_edit);
        mClearEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 过滤数据
                filterData(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                    int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    /**
     * 点击 城市返回主页 结束当前
     * @param city
     */
    private void onCityChoosed(City city) {
        Intent intent = new Intent();
        city.location.city = city.name;
        intent.putExtra(Const.EXTRA_COORDINATES, city.location);
        // to IndexActivity  onActivityResult method
        setResult(RESULT_OK, intent);
        finish();
    }
    //初始化ActionBar
    private void initActionBar() {
        ActionBar actionBar = (ActionBar) findViewById(R.id.action_bar);
        actionBar.setTitle(R.string.action_bar_title_city);
        actionBar.getLeftView().setImageResource(R.drawable.ic_action_bar_back_selecter);
        actionBar.getLeftView().setOnClickListener(new OnClickListener() {

            @Override
            public void onValidClick(View v) {
                finish();
            }
        });
    }

    private List<SortModel> fillModel(List<City> cities) {
        List<SortModel> mSortList = new ArrayList<SortModel>();

        for (City city : cities) {
            SortModel sortModel = new SortModel();
            sortModel.setCity(city);
            // 城市名 － 拼音
            String pinyin = mCharacterParser.getSelling(city.name);
            sortModel.setPinyin(pinyin);
            String sortString = pinyin.substring(0, 1).toUpperCase(Locale.CHINESE);
            //匹配 set模型SortModel中
            if (sortString.matches("[A-Z]")) {
                sortModel.setSortLetters(sortString.toUpperCase(Locale.CHINESE));
            } else {
                sortModel.setSortLetters("#");
            }

            mSortList.add(sortModel);
        }
        return mSortList;

    }

    /**
     * 对输入框 数据过滤
     * @param filterStr
     */
    private void filterData(String filterStr) {
        List<SortModel> filterDateList = new ArrayList<SortModel>();

        if (TextUtils.isEmpty(filterStr)) {
            filterDateList = mCityListModel;
        } else {
            filterDateList.clear();
            for (SortModel sortModel : mCityListModel) {
                String name = sortModel.getCity().name;
                //遍历 mCityListModel 模糊匹配
                if (name.toUpperCase(Locale.CHINESE).indexOf(
                        filterStr.toString().toUpperCase(Locale.CHINESE)) != -1
                        || mCharacterParser.getSelling(name).toUpperCase(Locale.CHINESE)
                                .startsWith(filterStr.toString().toUpperCase(Locale.CHINESE))) {
                    filterDateList.add(sortModel);
                }
            }
        }

        Collections.sort(filterDateList, mPinyinComparator);
        mSortAdapter.updateListView(filterDateList);
    }

    /**
     * 热门城市 适配器－gridview
     */
    private class CityAdapter extends RecyclerView.Adapter<CityAdapter.ViewHolder> {

        private List<City> mDataItems;

        public CityAdapter() {
        }

        public CityAdapter(List<City> items) {
            this.mDataItems = items;
        }

        public void setCityList(List<City> items) {
            this.mDataItems = items;
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_name_box,
                    parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final City item = mDataItems.get(position);
            holder.itemView.setTag(item);
            holder.itemView.setOnClickListener(new OnClickListener() {

                @Override
                public void onValidClick(View v) {
                    //选择城市
                    onCityChoosed(item);
                }
            });

            holder.name.setText(item.name);
        }

        @Override
        public int getItemCount() {
            return mDataItems == null ? 0 : mDataItems.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private TextView name;

            public ViewHolder(View itemView) {
                super(itemView);
                name = (TextView) itemView.findViewById(R.id.name);
            }
        }
    }

    /**
     *  加载城市列表
     */
    private void loadCityList() {
        RequestManager.getInstance().sendGsonRequest(ServerAPI.CityList.URL, CityList.class,
                new Response.Listener<CityList>() {
                    @Override
                    public void onResponse(CityList cityList) {
                        Log.d("onResponse, CityList=%s", cityList);

                        if (cityList.list != null && !cityList.list.isEmpty()) {
                            onCityListLoaded(cityList);
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(error, "onErrorResponse");
                    }
                }, requestTag);
    }
    //分类取出 city
    private void onCityListLoaded(CityList cityList) {
        List<City> hotCities = new ArrayList<City>();

        for (City city : cityList.list) {
            if (city.isHot) {
                hotCities.add(city);
            }
        }
        //先setAdapter 才能getAdapter
        ((CityAdapter) mHotCities.getAdapter()).setCityList(hotCities);

        List<City> cities = cityList.list;
        //填充city 收集器分类 更新list
        mCityListModel = fillModel(cities);
        Collections.sort(mCityListModel, mPinyinComparator);
        mSortAdapter.updateListView(mCityListModel);
    }
}
