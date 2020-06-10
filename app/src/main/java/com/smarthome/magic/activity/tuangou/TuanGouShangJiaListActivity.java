package com.smarthome.magic.activity.tuangou;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.model.Response;
import com.smarthome.magic.R;
import com.smarthome.magic.activity.WebViewActivity;
import com.smarthome.magic.activity.xin_tuanyou.TuanYouList;
import com.smarthome.magic.adapter.TuanGouShangJiaHeaderListAdapter;
import com.smarthome.magic.adapter.tuangou.TuanGouShangJiaListAdapter;
import com.smarthome.magic.baseadapter.baserecyclerviewadapterhelper.BaseQuickAdapter;
import com.smarthome.magic.callback.JsonCallback;
import com.smarthome.magic.config.AppResponse;
import com.smarthome.magic.config.GlideImageLoader;
import com.smarthome.magic.config.PreferenceHelper;
import com.smarthome.magic.get_net.Urls;
import com.smarthome.magic.model.TuanGouShangJiaListBean;
import com.smarthome.magic.project_A.tuangou.TuanGouShangJiaList;
import com.youth.banner.Banner;
import com.youth.banner.listener.OnBannerListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.smarthome.magic.app.App.JINGDU;
import static com.smarthome.magic.app.App.WEIDU;
import static com.smarthome.magic.get_net.Urls.LIBAOLIST;

public class TuanGouShangJiaListActivity extends AbStractTuanGouShangJia {

    @BindView(R.id.swipe_target)
    RecyclerView swipeTarget;
    @BindView(R.id.constrain_xx)
    LinearLayout constrainXx;
    @BindView(R.id.constrain)
    ConstraintLayout constrain;
    private String type;//首页图标类型 1.美食 2.电影/演出 3.酒店住宿 4.休闲娱乐 5.旅游

    List<TuanGouShangJiaListBean.DataBean> dataBeans = new ArrayList<>();
    Response<AppResponse<TuanGouShangJiaListBean.DataBean>> response;

    @Override
    public int getContentViewResId() {
        return R.layout.activity_tuan_gou_shang_jia_list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        type = getIntent().getStringExtra("type");
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_tuan_gou_shang_jia_list);
        ButterKnife.bind(this);
        initToolbar();
        constrain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                constrain.setVisibility(View.GONE);
            }
        });
    }

    Banner banner;
    List<TuanGouShangJiaListBean.DataBean.IconBean> iconListBeans = new ArrayList<>();
    List<TuanGouShangJiaListBean.DataBean.StoreListBean> storeListBeans = new ArrayList<>();

    @Override
    public void getBanner() {
        List<String> items = new ArrayList<>();
        if (response.body().data != null) {
            for (int i = 0; i < response.body().data.get(0).getBanner_list().size(); i++) {
                items.add(response.body().data.get(0).getBanner_list().get(i).getImg_url());
            }
        }

        //设置图片加载器
        banner.setImageLoader(new GlideImageLoader());
        if (banner != null) {
            //设置图片集合
            banner.setImages(items);
            //banner设置方法全部调用完毕时最后调用
            banner.start();
            banner.setOnBannerListener(new OnBannerListener() {
                @Override
                public void OnBannerClick(int position) {
                    startActivity(new Intent(TuanGouShangJiaListActivity.this, WebViewActivity.class).putExtra("url", response.body().data.get(0).getBanner_list().get(position).getHtml_url()));
                }
            });
        }
    }

    TuanGouShangJiaHeaderListAdapter ziJian_headerAdapter;

    @Override
    public void getHortialList() {

        ziJian_headerAdapter = new TuanGouShangJiaHeaderListAdapter(R.layout.item_zijian_header, iconListBeans);

        ziJian_headerAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {


                if (ziJian_headerAdapter.getData().get(position).getId().equals("11")) {
                    //本地加油
                    TuanYouList.actionStart(TuanGouShangJiaListActivity.this);

                } else {

                    item_id = ziJian_headerAdapter.getData().get(position).getHref_url();
                    three_img_id = ziJian_headerAdapter.getData().get(position).getThree_img_id();
                    type = ziJian_headerAdapter.getData().get(position).getId();
                    //image_type =
                    getNet_storeList();
                }
            }
        });
        //初始化一下
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 5, GridLayoutManager.VERTICAL, false);
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvcList.setLayoutManager(gridLayoutManager);


        //  taoKeListAdapter = new TaoKeListAdapter(R.layout.layout_taokeshop, dataBeanList);
        ziJian_headerAdapter.openLoadAnimation();//默认为渐显效果
        rvcList.setAdapter(ziJian_headerAdapter);
    }


    @Override
    public void shaixuan() {

    }

    @Override
    public void getVertialList() {

    }

    private String item_id = "";//经营项id（三级页时传）
    private String neibour = "";//三级页小图标对应id（三级页时传）
    private String order = ""; //排序：1.离我最近 2.好评优先 3.销量最高  .可传空，为智能排序
    private String inst_id = "";
    private String three_img_id = "";//三级页小图标对应的id
    private String meter = "";//order 第一个

    public void getNet_storeList() {

        //访问网络获取数据 下面的列表数据

        Map<String, String> map = new HashMap<>();
        map.put("code", "08011");
        map.put("key", Urls.key);
        map.put("y", PreferenceHelper.getInstance(TuanGouShangJiaListActivity.this).getString(JINGDU, "0X11"));
        map.put("x", PreferenceHelper.getInstance(TuanGouShangJiaListActivity.this).getString(WEIDU, "0X11"));
        map.put("img_type", type);
        map.put("item_id", item_id);
        map.put("neibour", neibour);
        map.put("three_img_id", three_img_id);
        map.put("order", order);
        map.put("inst_id", inst_id);
        map.put("meter", meter);

        map.put("more_type", "1");

        Gson gson = new Gson();
        Log.e("map_data", gson.toJson(map));
        OkGo.<AppResponse<TuanGouShangJiaListBean.DataBean>>post(LIBAOLIST)
                .tag(this)//
                .upJson(gson.toJson(map))
                .execute(new JsonCallback<AppResponse<TuanGouShangJiaListBean.DataBean>>() {
                    @Override
                    public void onSuccess(Response<AppResponse<TuanGouShangJiaListBean.DataBean>> response) {
                        TuanGouShangJiaListActivity.this.response = response;
                        dataBeans.clear();
                        //  iconListBeans.clear();
                        storeListBeans.clear();

                        dataBeans.addAll(response.body().data);
                        //iconListBeans.addAll(response.body().data.get(0).getIcon());
                        storeListBeans.addAll(response.body().data.get(0).getStore_list());


                        //   setHeader();
                        if (storeListBeans.size() == 0) {
                            constraintLayout.setVisibility(View.VISIBLE);
                            //ivNoneImage.setVisibility(View.VISIBLE);
                            //noneText.setVisibility(View.VISIBLE);
                        } else {
                            constraintLayout.setVisibility(View.GONE);
                            //ivNoneImage.setVisibility(View.GONE);
                            //noneText.setVisibility(View.GONE);
                        }
                        tuanGouShangJiaListAdapter.setNewData(storeListBeans);
                        tuanGouShangJiaListAdapter.notifyDataSetChanged();
                        //  getTurn();

                    }
                });
    }

    @Override
    public void getNet() {

        //访问网络获取数据 下面的列表数据

        Map<String, String> map = new HashMap<>();
        map.put("code", "08011");
        map.put("key", Urls.key);
        map.put("y", PreferenceHelper.getInstance(TuanGouShangJiaListActivity.this).getString(JINGDU, "0X11"));
        map.put("x", PreferenceHelper.getInstance(TuanGouShangJiaListActivity.this).getString(WEIDU, "0X11"));
        map.put("img_type", type);
        map.put("item_id", item_id);
        map.put("neibour", neibour);
        map.put("three_img_id", three_img_id);
        map.put("order", order);
        map.put("inst_id", inst_id);
        map.put("meter", meter);
        if (type.equals("10")) {
            map.put("more_type", "1");
        }
        Gson gson = new Gson();
        Log.e("map_data", gson.toJson(map));
        OkGo.<AppResponse<TuanGouShangJiaListBean.DataBean>>post(LIBAOLIST)
                .tag(this)//
                .upJson(gson.toJson(map))
                .execute(new JsonCallback<AppResponse<TuanGouShangJiaListBean.DataBean>>() {
                    @Override
                    public void onSuccess(Response<AppResponse<TuanGouShangJiaListBean.DataBean>> response) {
                        TuanGouShangJiaListActivity.this.response = response;
                        dataBeans.clear();
                        iconListBeans.clear();
                        storeListBeans.clear();

                        dataBeans.addAll(response.body().data);

                        if (iconListBeans.size() == 0) {
                            iconListBeans.addAll(response.body().data.get(0).getIcon());
                        }
                        storeListBeans.addAll(response.body().data.get(0).getStore_list());


                        setHeader();
                        if (storeListBeans.size() == 0) {
                            constraintLayout.setVisibility(View.VISIBLE);
                            //ivNoneImage.setVisibility(View.VISIBLE);
                            //noneText.setVisibility(View.VISIBLE);
                        } else {
                            constraintLayout.setVisibility(View.GONE);
                            //ivNoneImage.setVisibility(View.GONE);
                            //noneText.setVisibility(View.GONE);
                        }
                        tuanGouShangJiaListAdapter.setNewData(storeListBeans);
                        tuanGouShangJiaListAdapter.notifyDataSetChanged();
                        getTurn();

                    }
                });
    }

    RecyclerView rvcList;
    TuanGouShangJiaListAdapter tuanGouShangJiaListAdapter;

    TextView tvQuanBu;
    TextView tvZhiNengPaiXu;
    TextView tvShaiXuan;

    ImageView ivImage1;
    ImageView ivImage2;
    ImageView ivImage3;

    private TextView tvText;//empty
    private ImageView ivImage;//empty
    private List<String> listData = Arrays.asList("智能排序", "离我最近", "好评优先", "销量最高");


    private boolean quanbuShow = false;
    private boolean shaixuanShow = false;
    private boolean zhinengpaixuShow = false;

    private ImageView ivNoneImage;//没有图片
    private TextView noneText;//没有文字话术

    private ConstraintLayout constraintLayout;

    @Override
    public void setHeader() {
        View view = View.inflate(this, R.layout.layout_tuangou_shangjia_list_header, null);

        ivNoneImage = view.findViewById(R.id.empty_view);
        noneText = view.findViewById(R.id.tv_none);
        constraintLayout = view.findViewById(R.id.constrain);

//        tvText =
        rvcList = view.findViewById(R.id.rcv_list);
        banner = view.findViewById(R.id.banner);

        tvQuanBu = view.findViewById(R.id.tv_quanbu);
        tvZhiNengPaiXu = view.findViewById(R.id.tv_zhinengpaixu);
        tvShaiXuan = view.findViewById(R.id.tv_shaixuan);

        ivImage1 = view.findViewById(R.id.iv_image1);
        ivImage2 = view.findViewById(R.id.iv_image2);
        ivImage3 = view.findViewById(R.id.iv_image3);


        ivImage1.setBackgroundResource(R.mipmap.shangjia_icon_shouqi);
        ivImage2.setBackgroundResource(R.mipmap.shangjia_icon_shouqi);
        ivImage3.setBackgroundResource(R.mipmap.shangjia_icon_shouqi);
        tvQuanBu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (quanbuShow) {
                    //   quanbuShow = !quanbuShow;
                    constrain.setVisibility(View.GONE);
                    quanbuShow = false;
                    ivImage1.setBackgroundResource(R.mipmap.shangjia_icon_shouqi);
                    tvQuanBu.setTextColor(TuanGouShangJiaListActivity.this.getResources().getColor(R.color.black_666666));

                } else {
                    quanbuShow = true;
                    zhinengpaixuShow = false;
                    shaixuanShow = false;

                    tvShaiXuan.setTextColor(TuanGouShangJiaListActivity.this.getResources().getColor(R.color.black_666666));
                    tvQuanBu.setTextColor(TuanGouShangJiaListActivity.this.getResources().getColor(R.color.green_ff57c3b3));
                    tvZhiNengPaiXu.setTextColor(TuanGouShangJiaListActivity.this.getResources().getColor(R.color.black_666666));

                    ivImage1.setBackgroundResource(R.mipmap.shangjia_icon_tanchu);
                    ivImage2.setBackgroundResource(R.mipmap.shangjia_icon_shouqi);
                    ivImage3.setBackgroundResource(R.mipmap.shangjia_icon_shouqi);

                    constrain.setVisibility(View.VISIBLE);
                    constrainXx.setVisibility(View.VISIBLE);
                    constrain.getBackground().setAlpha(200);// 0~255透明度值
                    constrainXx.removeAllViews();

                    for (int i = 0; i < iconListBeans.size(); i++) {
                        View viewHortial = View.inflate(TuanGouShangJiaListActivity.this, R.layout.item_hortial_view, null);
                        TextView tvText = viewHortial.findViewById(R.id.tv_text);
                        tvText.setText(iconListBeans.get(i).getName());
                        final int finalI = i;
                        viewHortial.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                item_id = ziJian_headerAdapter.getData().get(finalI).getHref_url();
                                three_img_id = ziJian_headerAdapter.getData().get(finalI).getThree_img_id();
                                type = ziJian_headerAdapter.getData().get(finalI).getId();
                                //image_type =
                                getNet_storeList();
                                constrain.setVisibility(View.GONE);
                                quanbuShow = true;
                                zhinengpaixuShow = false;
                                shaixuanShow = false;
                            }
                        });
                        constrainXx.addView(viewHortial);
                    }

                }
            }
        });
        tvShaiXuan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (shaixuanShow) {
                    //   quanbuShow = !quanbuShow;
                    constrain.setVisibility(View.GONE);
                    shaixuanShow = false;
                    ivImage3.setBackgroundResource(R.mipmap.shangjia_icon_shouqi);
                    tvShaiXuan.setTextColor(TuanGouShangJiaListActivity.this.getResources().getColor(R.color.black_666666));

                } else {

                    tvShaiXuan.setTextColor(TuanGouShangJiaListActivity.this.getResources().getColor(R.color.green_ff57c3b3));
                    tvQuanBu.setTextColor(TuanGouShangJiaListActivity.this.getResources().getColor(R.color.black_666666));
                    tvZhiNengPaiXu.setTextColor(TuanGouShangJiaListActivity.this.getResources().getColor(R.color.black_666666));

                    ivImage3.setBackgroundResource(R.mipmap.shangjia_icon_tanchu);
                    ivImage1.setBackgroundResource(R.mipmap.shangjia_icon_shouqi);
                    ivImage2.setBackgroundResource(R.mipmap.shangjia_icon_shouqi);

                    constrain.setVisibility(View.VISIBLE);
                    constrainXx.setVisibility(View.VISIBLE);
                    constrain.getBackground().setAlpha(200);// 0~255透明度值
                    constrainXx.removeAllViews();

                    quanbuShow = false;
                    zhinengpaixuShow = false;
                    shaixuanShow = true;
                }
            }
        });

        tvZhiNengPaiXu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (zhinengpaixuShow) {
                    //   quanbuShow = !quanbuShow;
                    constrain.setVisibility(View.GONE);
                    zhinengpaixuShow = false;
                    ivImage2.setBackgroundResource(R.mipmap.shangjia_icon_shouqi);
                    tvZhiNengPaiXu.setTextColor(TuanGouShangJiaListActivity.this.getResources().getColor(R.color.black_666666));

                } else {

                    tvZhiNengPaiXu.setTextColor(TuanGouShangJiaListActivity.this.getResources().getColor(R.color.green_ff57c3b3));
                    tvShaiXuan.setTextColor(TuanGouShangJiaListActivity.this.getResources().getColor(R.color.black_666666));
                    tvQuanBu.setTextColor(TuanGouShangJiaListActivity.this.getResources().getColor(R.color.black_666666));

                    ivImage2.setBackgroundResource(R.mipmap.shangjia_icon_tanchu);
                    ivImage1.setBackgroundResource(R.mipmap.shangjia_icon_shouqi);
                    ivImage3.setBackgroundResource(R.mipmap.shangjia_icon_shouqi);

                    constrain.setVisibility(View.VISIBLE);
                    constrainXx.setVisibility(View.VISIBLE);
                    constrain.getBackground().setAlpha(200);// 0~255透明度值

                    constrainXx.removeAllViews();

                    quanbuShow = false;
                    zhinengpaixuShow = true;
                    shaixuanShow = false;
                    for (int i = 0; i < listData.size(); i++) {
                        View viewHortial = View.inflate(TuanGouShangJiaListActivity.this, R.layout.item_hortial_view, null);
                        TextView tvText = viewHortial.findViewById(R.id.tv_text);
                        tvText.setText(listData.get(i));
                        final int finalI = i;
                        viewHortial.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                //    private List<String> listData = Arrays.asList("智能排序", "离我最近", "好评优先", "销量最高");

                                if (listData.get(finalI).equals("")) {

                                    order = "";
                                } else if (listData.get(finalI).equals("")) {

                                    order = "1";
                                } else if (listData.get(finalI).equals("")) {

                                    order = "2";
                                } else if (listData.get(finalI).equals("")) {

                                    order = "3";
                                }


                                //image_type =
                                getNet_storeList();
                                constrain.setVisibility(View.GONE);
                                quanbuShow = true;
                                zhinengpaixuShow = false;
                                shaixuanShow = false;
                            }
                        });
                        constrainXx.addView(viewHortial);
                    }
                }

            }
        });


        tuanGouShangJiaListAdapter = new TuanGouShangJiaListAdapter(R.layout.item_shangjia, storeListBeans);
        tuanGouShangJiaListAdapter.setOnItemChildClickListener(new com.chad.library.adapter.base.BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(com.chad.library.adapter.base.BaseQuickAdapter adapter, View view, int position) {

                switch (view.getId()) {
                    case R.id.constrain:
                        TuanGouShangJiaDetailsActivity.actionStart(TuanGouShangJiaListActivity.this, tuanGouShangJiaListAdapter.getData().get(position).getInst_id(), type);
                        break;
                }
            }
        });

//        refreshLayout.setEnableLoadMore(false);
//        refreshLayout.setEnableRefresh(false);
        //   initHeaderView(view);

        //初始化一下
        LinearLayoutManager gridLayoutManager = new LinearLayoutManager(this);
        swipeTarget.setLayoutManager(gridLayoutManager);

        //  taoKeListAdapter = new TaoKeListAdapter(R.layout.layout_taokeshop, dataBeanList);
        tuanGouShangJiaListAdapter.openLoadAnimation();//默认为渐显效果
        swipeTarget.setAdapter(tuanGouShangJiaListAdapter);
        //   refreshLayout.setEnableAutoLoadMore(true);
        //  smartRefreshLayout.setEnableRefresh(true);
        // refreshLayout.setEnableLoadMore(true);
        tuanGouShangJiaListAdapter.addHeaderView(view);
    }


    /**
     * 用于其他Activty跳转到该Activity
     *
     * @param context
     */
    public static void actionStart(Context context, String type) {
        Intent intent = new Intent(context, TuanGouShangJiaListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("type", type);
        context.startActivity(intent);

    }

    @Override
    public boolean showToolBar() {
        return true;
    }

    /**
     * iconList
     * 美团小图标	id	图标id   1.美食 2.电影 3.酒店 4.休闲娱乐 5.旅游 6.加油
     * 7.修配厂 8.体检 9.丽人/美发 10更多
     */
    @Override
    protected void initToolbar() {
        super.initToolbar();
        String strTitle = "项目";
        switch (type) {
            case "1":
                strTitle = "美食";
                break;
            case "2":
                strTitle = "电影";
                break;
            case "3":
                strTitle = "酒店";
                break;
            case "4":
                strTitle = "休闲娱乐";
                break;
            case "5":
                strTitle = "旅游";
                break;
            case "6":
                strTitle = "加油";
                break;
            case "7":
                strTitle = "修配厂";
                break;
            case "8":
                strTitle = "体检";
                break;
            case "9":
                strTitle = "丽人/美发";
                break;
            case "10":
                strTitle = "更多";
                break;
        }
        tv_title.setText(strTitle);
        tv_title.setTextSize(17);
        tv_title.setTextColor(getResources().getColor(R.color.black));
        mToolbar.setNavigationIcon(R.mipmap.backbutton);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //imm.hideSoftInputFromWindow(findViewById(R.id.cl_layout).getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                finish();
            }
        });
    }
}