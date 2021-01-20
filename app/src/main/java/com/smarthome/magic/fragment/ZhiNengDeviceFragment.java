package com.smarthome.magic.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rairmmd.andmqtt.AndMqtt;
import com.rairmmd.andmqtt.MqttPublish;
import com.rairmmd.andmqtt.MqttSubscribe;
import com.smarthome.magic.R;
import com.smarthome.magic.activity.SuiYiTieActivity;
import com.smarthome.magic.activity.SuiYiTieOneActivity;
import com.smarthome.magic.activity.SuiYiTieThreeActivity;
import com.smarthome.magic.activity.SuiYiTieTwoActivity;
import com.smarthome.magic.activity.ZhiNengChuangLianActivity;
import com.smarthome.magic.activity.ZhiNengDianDengActivity;
import com.smarthome.magic.activity.ZhiNengJiaJuChaZuoActivity;
import com.smarthome.magic.activity.ZhiNengJiajuWeiYuAutoActivity;
import com.smarthome.magic.activity.ZhiNengJiaoHuaAutoActivity;
import com.smarthome.magic.activity.ZhiNengRoomDeviceDetailAutoActivity;
import com.smarthome.magic.activity.shuinuan.Y;
import com.smarthome.magic.activity.tuya_device.camera.TuyaCameraActivity;
import com.smarthome.magic.activity.tuya_device.device.DeviceChazuoActivity;
import com.smarthome.magic.activity.tuya_device.device.DeviceMenciActivity;
import com.smarthome.magic.activity.tuya_device.device.DeviceWgCzActivity;
import com.smarthome.magic.activity.tuya_device.utils.TuyaConfig;
import com.smarthome.magic.activity.tuya_device.device.DeviceWangguanActivity;
import com.smarthome.magic.activity.zckt.AirConditionerActivity;

import com.smarthome.magic.activity.zhinengjiaju.WenShiDuChuanGanQiActivity;
import com.smarthome.magic.activity.zhinengjiaju.function.LouShuiActivity;
import com.smarthome.magic.activity.zhinengjiaju.function.MenCiActivity;
import com.smarthome.magic.activity.zhinengjiaju.function.MenSuoActivity;
import com.smarthome.magic.activity.zhinengjiaju.function.SosActivity;
import com.smarthome.magic.activity.zhinengjiaju.function.YanGanActivity;
import com.smarthome.magic.activity.zhinengjiaju.peinet.PeiWangYinDaoPageActivity;
import com.smarthome.magic.adapter.ZhiNengDeviceListAdapter;
import com.smarthome.magic.app.AppManager;
import com.smarthome.magic.app.ConstanceValue;
import com.smarthome.magic.app.Notice;
import com.smarthome.magic.app.RxBus;
import com.smarthome.magic.app.UIHelper;
import com.smarthome.magic.baseadapter.baserecyclerviewadapterhelper.BaseQuickAdapter;
import com.smarthome.magic.basicmvp.BaseFragment;
import com.smarthome.magic.config.PreferenceHelper;
import com.smarthome.magic.mqtt_zhiling.ZnjjMqttMingLing;
import com.smarthome.magic.tools.NetworkUtils;
import com.smarthome.magic.util.GridAverageUIDecoration;
import com.smarthome.magic.model.ZhiNengHomeBean;
import com.tuya.smart.api.MicroContext;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.panelcaller.api.AbsPanelCallerService;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;

import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;


public class ZhiNengDeviceFragment extends BaseFragment {

    private View viewLayout;
    private LinearLayout ll_content_bg;
    private RecyclerView recyclerView;
    private ZhiNengDeviceListAdapter zhiNengDeviceListAdapter;
    private List<ZhiNengHomeBean.DataBean.DeviceBean> dataBean = new ArrayList<>();
    private String member_type = "";
    public ZnjjMqttMingLing mqttMingLing = null;
    private String family_id;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewLayout = inflater.inflate(R.layout.fragment_zhineng_device, container, false);
        initView(viewLayout);
        return viewLayout;
    }

    @Override
    protected void initLogic() {

    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_zhineng_device;
    }

    public static ZhiNengDeviceFragment newInstance(Bundle bundle) {
        ZhiNengDeviceFragment fragment = new ZhiNengDeviceFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    public void initView(View view) {
        ll_content_bg = view.findViewById(R.id.ll_content_bg);
        recyclerView = view.findViewById(R.id.recyclerView);
//        recyclerView.addItemDecoration(new RecycleItemSpance(20, 2));
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        recyclerView.addItemDecoration(new GridAverageUIDecoration(14, 10));

        recyclerView.setLayoutManager(layoutManager);
        zhiNengDeviceListAdapter = new ZhiNengDeviceListAdapter(R.layout.item_zhineng_device, dataBean);

        View view1 = LayoutInflater.from(getActivity()).inflate(R.layout.activity_zhineng_device_none, null);
        TextView tvBangDingZhuJi = view1.findViewById(R.id.tv_bangdingzhuji);
        tvBangDingZhuJi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PeiWangYinDaoPageActivity.actionStart(getActivity());
            }
        });
        zhiNengDeviceListAdapter.setEmptyView(view1);

        zhiNengDeviceListAdapter.openLoadAnimation();//默认为渐显效果
        recyclerView.setAdapter(zhiNengDeviceListAdapter);

        zhiNengDeviceListAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                switch (view.getId()) {
                    case R.id.iv_switch:
                        ZhiNengHomeBean.DataBean.DeviceBean bean = (ZhiNengHomeBean.DataBean.DeviceBean) adapter.getData().get(position);
                        if (bean.getDevice_type().equals("20")) {
                            clickTongtiao(bean, position);
                        } else {
                            mqttMingLing = new ZnjjMqttMingLing(getActivity(), bean.getDevice_ccid_up(), bean.getServer_id());

                            if (bean.getWork_state().equals("1")) {
                                mqttMingLing.setAction(bean.getDevice_ccid(), "02", new IMqttActionListener() {
                                    @Override
                                    public void onSuccess(IMqttToken asyncActionToken) {
                                        //UIHelper.ToastMessage(mContext, "当前装置开启");

                                        List<String> stringList = new ArrayList<>();
                                        stringList.add(bean.getDevice_ccid());
                                        stringList.add("2");

                                        Notice notice = new Notice();
                                        notice.type = ConstanceValue.MSG_SHEBEIZHUANGTAI;
                                        notice.content = stringList;
                                        Log.i("Rair", notice.content.toString());
                                        RxBus.getDefault().sendRx(notice);
                                    }

                                    @Override
                                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                        UIHelper.ToastMessage(getActivity(), "未发送指令");
                                    }
                                });

                            }

                            if (bean.getWork_state().equals("2")) {

                                mqttMingLing.setAction(bean.getDevice_ccid(), "01", new IMqttActionListener() {
                                    @Override
                                    public void onSuccess(IMqttToken asyncActionToken) {
                                        //UIHelper.ToastMessage(mContext, "当前装置开启");

                                        List<String> stringList = new ArrayList<>();
                                        stringList.add(bean.getDevice_ccid());
                                        stringList.add("1");

                                        Notice notice = new Notice();
                                        notice.type = ConstanceValue.MSG_SHEBEIZHUANGTAI;
                                        notice.content = stringList;
                                        Log.i("Rair", notice.content.toString());
                                        RxBus.getDefault().sendRx(notice);
                                    }

                                    @Override
                                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                        UIHelper.ToastMessage(getActivity(), "未发送指令");
                                    }
                                });

                            }

                            break;

                        }

                    case R.id.ll_content:
                        ZhiNengHomeBean.DataBean.DeviceBean deviceBean = (ZhiNengHomeBean.DataBean.DeviceBean) adapter.getItem(position);
                        /**
                         / 00 主机 01.灯 02.插座 03.喂鱼 04.浇花 05门锁 06.空调电视(开关，加风，减风，讯飞语音配置)
                         / 07.车库门  08.开关 09.晾衣架 10.窗磁 11.烟雾报警 12.门磁 13.漏水14.雷达
                         / 15.紧急开关 16.窗帘 17.电视(开关，加减音量，加减亮暗，讯飞语音配置) 18.摄像头
                         / 19.空气检测 20.温湿度检测 21.煤气管道关闭 22.自来水管道关闭 23.宠物喂食 24.宠物喂水
                         / 25.智能手环 26.排风 27背景音乐显示控制 28.电视遥控 29.空气净化 30.体质检测
                         / 31.光敏控制 32.燃气报警 33.风扇 34.雷达
                         */
                        if (deviceBean.getDevice_type().equals("20")) {//空调
                            String ccid = deviceBean.getDevice_ccid();
                            PreferenceHelper.getInstance(getContext()).putString("ccid", ccid);
                            PreferenceHelper.getInstance(getContext()).putString("share_type", "1");
                            PreferenceHelper.getInstance(getContext()).putString("sim_ccid_save_type", "1");
                            if (NetworkUtils.isConnected(getActivity())) {
                                Activity currentActivity = AppManager.getAppManager().currentActivity();
                                if (currentActivity != null) {
                                    AirConditionerActivity.actionStart(getActivity(), ccid, "智能空调");
                                }
                            } else {
                                UIHelper.ToastMessage(getActivity(), "请连接网络后重新尝试");
                            }
                        } else if (deviceBean.getDevice_type().equals("16")) {//窗帘
                            ZhiNengChuangLianActivity.actionStart(getActivity(), deviceBean.getDevice_id());
                        } else if (deviceBean.getDevice_type().equals("01")) {//灯
                            ZhiNengDianDengActivity.actionStart(getActivity(), deviceBean.getDevice_id(), deviceBean.getDevice_type());
                        } else if (deviceBean.getDevice_type().equals("03")) {//喂鱼
                            ZhiNengJiajuWeiYuAutoActivity.actionStart(getActivity(), deviceBean.getDevice_id(), deviceBean.getDevice_type());
                        } else if (deviceBean.getDevice_type().equals("04")) {//浇花
                            ZhiNengJiaoHuaAutoActivity.actionStart(getActivity(), deviceBean.getDevice_id(), deviceBean.getDevice_type());
                        } else if (deviceBean.getDevice_type().equals("02")) {
                            Bundle bundle = new Bundle();
                            bundle.putString("device_id", deviceBean.getDevice_id());
                            bundle.putString("device_type", deviceBean.getDevice_type());
                            bundle.putString("member_type", member_type);
                            bundle.putString("work_state", deviceBean.getWork_state());
                            startActivity(new Intent(getActivity(), ZhiNengJiaJuChaZuoActivity.class).putExtras(bundle));
                        } else if (deviceBean.getDevice_type().equals("12")) {
                            MenCiActivity.actionStart(getActivity(), deviceBean.getDevice_id(), member_type);
                        } else if (deviceBean.getDevice_type().equals("11")) {
                            YanGanActivity.actionStart(getActivity(), deviceBean.getDevice_id());
                        } else if (deviceBean.getDevice_type().equals("15")) {
                            SosActivity.actionStart(getActivity(), deviceBean.getDevice_id(), member_type);
                        } else if (deviceBean.getDevice_type().equals("05")) {
                            MenSuoActivity.actionStart(getActivity(), deviceBean.getDevice_id(), member_type);
                        } else if (deviceBean.getDevice_type().equals("13")) {
                            LouShuiActivity.actionStart(getActivity(), deviceBean.getDevice_id(), member_type);
                        } else if (deviceBean.getDevice_type().equals("08")) {//随意贴
                            //SuiYiTieActivity.actionStart(getActivity(), deviceBean.getDevice_ccid(), deviceBean.getDevice_ccid_up());
                            String strJiLian = deviceBean.getDevice_ccid().substring(2, 4);
                            Log.i("ZhiNengDeviceFragment", "strJiLian:" + strJiLian);
                            if (strJiLian.equals("01")) {
                                SuiYiTieOneActivity.actionStart(getActivity(), deviceBean.getDevice_ccid(), deviceBean.getDevice_ccid_up());
                            } else if (strJiLian.equals("02")) {
                                SuiYiTieTwoActivity.actionStart(getActivity(), deviceBean.getDevice_ccid(), deviceBean.getDevice_ccid_up());
                            } else if (strJiLian.equals("03")) {
                                SuiYiTieThreeActivity.actionStart(getActivity(), deviceBean.getDevice_ccid(), deviceBean.getDevice_ccid_up());
                            }
                        } else if (deviceBean.getDevice_type().equals("36")) {
                            WenShiDuChuanGanQiActivity.actionStart(getActivity(), deviceBean.getDevice_id());
                        } else {
                            String ty_device_ccid = deviceBean.getTy_device_ccid();
                            if (TextUtils.isEmpty(ty_device_ccid)) {
                                Bundle bundle = new Bundle();
                                bundle.putString("device_id", deviceBean.getDevice_id());
                                bundle.putString("device_type", deviceBean.getDevice_type());
                                bundle.putString("member_type", member_type);
                                bundle.putString("work_state", deviceBean.getWork_state());
                                startActivity(new Intent(getActivity(), ZhiNengRoomDeviceDetailAutoActivity.class).putExtras(bundle));
                            } else {
                                Y.e("设备的信息是什么啊  " + "device_category:" + deviceBean.getDevice_type() + "  produco:" + deviceBean.getDevice_category() + "  device_category_code:" + deviceBean.getDevice_category_code());

                                if (deviceBean.getDevice_type().equals(TuyaConfig.CATEGORY_CAMERA)) {//涂鸦摄像机
                                    TuyaCameraActivity.actionStart(getActivity(), member_type, deviceBean.getDevice_id(), ty_device_ccid, deviceBean.getDevice_name(), deviceBean.getRoom_name());
                                } else if (deviceBean.getDevice_type().equals(TuyaConfig.CATEGORY_CHAZUO)) {//涂鸦插座
                                    if (deviceBean.getDevice_category().equals(TuyaConfig.PRODUCTID_CHAZUO_WG)) {
                                        DeviceWgCzActivity.actionStart(getActivity(), member_type, deviceBean.getDevice_id(), ty_device_ccid, deviceBean.getDevice_name(), deviceBean.getRoom_name());
                                    } else {
                                        DeviceChazuoActivity.actionStart(getActivity(), member_type, deviceBean.getDevice_id(), ty_device_ccid, deviceBean.getDevice_name(), deviceBean.getRoom_name());
                                    }
                                } else if (deviceBean.getDevice_type().equals(TuyaConfig.CATEGORY_WANGGUAN)) {//涂鸦网关
                                    DeviceWangguanActivity.actionStart(getActivity(), member_type, deviceBean.getDevice_id(), ty_device_ccid, deviceBean.getDevice_name(), deviceBean.getRoom_name());
                                } else if (deviceBean.getDevice_type().equals(TuyaConfig.CATEGORY_WNYKQ)) {//万能遥控器
                                    AbsPanelCallerService service = MicroContext.getServiceManager().findServiceByInterface(AbsPanelCallerService.class.getName());
                                    service.goPanelWithCheckAndTip(getActivity(), ty_device_ccid);
                                } else {//其他涂鸦设备
                                    AbsPanelCallerService service = MicroContext.getServiceManager().findServiceByInterface(AbsPanelCallerService.class.getName());
                                    service.goPanelWithCheckAndTip(getActivity(), ty_device_ccid);
                                }
                            }
                        }
                        break;
                }
            }
        });

        _subscriptions.add(toObservable().observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Notice>() {
            @Override
            public void call(Notice message) {
                if (message.type == ConstanceValue.MSG_SHEBEIZHUANGTAI) {
                    List<String> messageList = (List<String>) message.content;
                    String zhuangZhiId = messageList.get(0);
                    String kaiGuanDengZhuangTai = messageList.get(1);
                    for (int i = 0; i < dataBean.size(); i++) {
                        if (dataBean.get(i).getDevice_ccid().equals(zhuangZhiId)) {
                            dataBean.get(i).setWork_state(kaiGuanDengZhuangTai);
                        }
                    }
                    zhiNengDeviceListAdapter.notifyDataSetChanged();
                }
            }
        }));


    }

    private void clickTongtiao(ZhiNengHomeBean.DataBean.DeviceBean bean, int pos) {
        String KT_ccid = bean.getDevice_ccid();
        String KT_Send = "zckt/cbox/hardware/" + KT_ccid;

        //注册向空调发送数据的主题
        AndMqtt.getInstance().subscribe(new MqttSubscribe()
                .setTopic(KT_Send)
                .setQos(2), new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {

            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

            }
        });

        String work_state = bean.getWork_state();
        if (work_state.equals("1")) {
            AndMqtt.getInstance().publish(new MqttPublish()
                    .setMsg("M030.")
                    .setQos(2).setRetained(false)
                    .setTopic(KT_Send), new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Y.t("关闭空调");
                    bean.setWork_state("2");
                    dataBean.set(pos, bean);
                    zhiNengDeviceListAdapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

                }
            });
        } else {
            AndMqtt.getInstance().publish(new MqttPublish()
                    .setMsg("M031.")
                    .setQos(2).setRetained(false)
                    .setTopic(KT_Send), new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Y.t("开启空调");
                    bean.setWork_state("1");
                    dataBean.set(pos, bean);
                    zhiNengDeviceListAdapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

                }
            });
        }
    }

    public void onRefresh() {
        if (getArguments() != null) {
            List<ZhiNengHomeBean.DataBean.DeviceBean> device = getArguments().getParcelableArrayList("device");
//            String strPhone = PreferenceHelper.getInstance(getActivity()).getString("user_phone", "");
//            if (strPhone.equals("15114684672") || strPhone.equals("17645185187")) {
//                ZhiNengHomeBean.DataBean.DeviceBean kongtiaoBean = new ZhiNengHomeBean.DataBean.DeviceBean();
//                kongtiaoBean.setDevice_ccid("kkkkkkkkkkkkkkkk90120018");
//                kongtiaoBean.setDevice_name("智能空調");
//                kongtiaoBean.setDevice_type("20");
//                kongtiaoBean.setDevice_type_pic("https://shop.hljsdkj.com/Frame/uploadFile/showImg?file_id=11711");
//                kongtiaoBean.setOnline_state("1");
//                kongtiaoBean.setServer_id("8/");
//                kongtiaoBean.setRoom_name("默认房间");
//                kongtiaoBean.setWork_state("2");
//                device.add(kongtiaoBean);
//            }
            member_type = getArguments().getString("member_type");
            family_id = getArguments().getString("family_id");
            dataBean.clear();
            dataBean.addAll(device);
            if (zhiNengDeviceListAdapter != null) {
                zhiNengDeviceListAdapter.notifyDataSetChanged();
            }
        }

        if (recyclerView != null) {
            for (int i = 0; i < recyclerView.getItemDecorationCount(); i++) {
                recyclerView.removeItemDecorationAt(i);
            }
            if (dataBean.size() == 0) {
                recyclerView.addItemDecoration(new GridAverageUIDecoration(0, 10));
            } else {
                recyclerView.addItemDecoration(new GridAverageUIDecoration(14, 10));
            }
        }
    }
}
