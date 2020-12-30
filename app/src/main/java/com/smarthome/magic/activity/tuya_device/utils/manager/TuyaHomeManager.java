package com.smarthome.magic.activity.tuya_device.utils.manager;

import com.smarthome.magic.activity.shuinuan.Y;
import com.smarthome.magic.activity.tuya_device.utils.TuyaDialogUtils;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback;
import com.tuya.smart.home.sdk.callback.ITuyaResultCallback;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.tuya.smart.sdk.bean.SigMeshBean;

import java.util.List;

public class TuyaHomeManager {
    private static TuyaHomeManager instance;
    private HomeBean homeBean;
    private SigMeshBean sigMeshBean;
    private long homeId;

    public static TuyaHomeManager getHomeManager() {
        if (null == instance) {
            instance = new TuyaHomeManager();
        }
        return instance;
    }

    public void setHomeBean(HomeBean homeBean) {
        this.homeBean = homeBean;
    }

    public HomeBean getHomeBean() {
        return homeBean;
    }

    public long getHomeId() {
        return homeId;
    }

    public void setHomeId(long homeId) {
        this.homeId = homeId;
        initHome();
    }

    public SigMeshBean getSigMeshBean() {
        return sigMeshBean;
    }

    public void setSigMeshBean(SigMeshBean sigMeshBean) {
        this.sigMeshBean = sigMeshBean;
        Y.e("设置成功了么" + sigMeshBean.getMeshId());
    }

    private void initHome() {
        if (sigMeshBean != null) {
            TuyaHomeSdk.getTuyaSigMeshClient().destroyMesh(sigMeshBean.getMeshId());
        }

        TuyaHomeSdk.newHomeInstance(homeId).getHomeDetail(new ITuyaHomeResultCallback() {
            @Override
            public void onSuccess(HomeBean bean) {
                Y.e("设置家庭成功了");
                setHomeBean(bean);
                setMesh();
            }

            @Override
            public void onError(String errorCode, String errorMsg) {
                Y.e("设置家庭失败了");
            }
        });
    }

    private void setMesh() {
        List<SigMeshBean> meshList = TuyaHomeSdk.getSigMeshInstance().getSigMeshList();
        if (meshList.size() > 0) {
            setSigMeshBean(meshList.get(0));
        } else {
            TuyaHomeSdk.newHomeInstance(homeId)
                    .createSigMesh(new ITuyaResultCallback<SigMeshBean>() {
                        @Override
                        public void onError(String errorCode, String errorMsg) {
                            Y.e("创建SigMeshBean失败：" + errorMsg);
                        }

                        @Override
                        public void onSuccess(SigMeshBean sigMeshBean) {
                            Y.e("创建SigMeshBean成功");
                            setSigMeshBean(sigMeshBean);
                        }
                    });

        }
    }

    public DeviceBean isHaveDevice(String devId) {
        DeviceBean mineDeviceBean = null;
        if (homeBean != null) {
            List<DeviceBean> deviceList = homeBean.getDeviceList();
            for (int i = 0; i < deviceList.size(); i++) {
                DeviceBean deviceBean = deviceList.get(i);
                Y.e(" 綁定的设备 " + deviceBean.getName());

                if (devId.equals(deviceBean.getDevId())) {
                    mineDeviceBean = deviceBean;
                    Y.e("获取的功能点是什么啊啊  " + deviceBean.getSchema() + "    " + deviceBean.getIsOnline() + "     " + deviceBean.getDps().toString());
                }
            }
        }
        return mineDeviceBean;
    }
}