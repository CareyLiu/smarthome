package com.smarthome.magic.activity.zhinengjiaju.peinet;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.flyco.roundview.RoundRelativeLayout;
import com.smarthome.magic.R;
import com.smarthome.magic.activity.zhinengjiaju.TianJiaPuTongSheBeiActivity;
import com.smarthome.magic.app.BaseActivity;
import com.smarthome.magic.app.UIHelper;
import com.smarthome.magic.model.FenLeiContentModel;

import butterknife.BindView;

public class SheBeiChongZhiActivity extends BaseActivity {

    String image;
    String titleName;
    @BindView(R.id.tv_title_name)
    TextView tvTitleName;
    @BindView(R.id.iv_image)
    ImageView ivImage;
    @BindView(R.id.tv_text)
    TextView tvText;
    @BindView(R.id.rrl_xiayibu)
    RoundRelativeLayout rrlXiayibu;
    String header;
    @BindView(R.id.iv_icon)
    ImageView ivIcon;
    @BindView(R.id.rrl_querencaozuo)
    RelativeLayout rrlQuerencaozuo;
    @BindView(R.id.rrl_zhezhao)
    RoundRelativeLayout rrlZhezhao;
    @BindView(R.id.tv_miaoshu)
    TextView tvMiaoshu;
    private String shiGouXuanZhong = "0";
    private String ccid;
    private String serverId;
    private FenLeiContentModel fenLeiContentModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        image = getIntent().getStringExtra("image");
        titleName = getIntent().getStringExtra("titleName");
        header = getIntent().getStringExtra("header");
        fenLeiContentModel = (FenLeiContentModel) getIntent().getSerializableExtra("FenLeiContentModel");
        tvTitleName.setText(fenLeiContentModel.item_name);
        Glide.with(mContext).load(fenLeiContentModel.img_detail_url).into(ivImage);
        tvMiaoshu.setText(fenLeiContentModel.item_detail);
        ivIcon.setBackgroundResource(R.mipmap.peiwang_icon_mima_weixuanze);
        rrlQuerencaozuo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (shiGouXuanZhong.equals("0")) {
                    ivIcon.setBackgroundResource(R.mipmap.peiwang_icon);
                    rrlZhezhao.setVisibility(View.GONE);
                    shiGouXuanZhong = "1";
                } else {
                    ivIcon.setBackgroundResource(R.mipmap.peiwang_icon_mima_weixuanze);
                    rrlZhezhao.setVisibility(View.VISIBLE);
                    shiGouXuanZhong = "0";
                }
            }
        });

        rrlXiayibu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rrlZhezhao.getVisibility() == View.VISIBLE) {
                    UIHelper.ToastMessage(mContext, "请确认已执行以上操作");
                } else {
//                    UIHelper.ToastMessage(mContext, "下一步");
                    if (fenLeiContentModel.type.equals("00")) {
                        ZhiNengJiaJuPeiWangActivity.actionStart(mContext);
                    } else if (fenLeiContentModel.type.equals("18")) {//摄像头
                        // TODO: 2021/2/3 添加摄像头 
                    } else {
                        TianJiaPuTongSheBeiActivity.actionStart(mContext, fenLeiContentModel);
                    }
                }
            }
        });
    }

    @Override
    public int getContentViewResId() {
        return R.layout.layout_shebeichongzhi;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean showToolBar() {
        return true;
    }

    @Override
    protected void initToolbar() {
        super.initToolbar();
        tv_title.setText("设备重置");
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


    /**
     * 用于其他Activty跳转到该Activity
     *
     * @param context
     */
    public static void actionStart(Context context, String titleName, String image, String header, FenLeiContentModel fenLeiContentModel) {
        Intent intent = new Intent(context, SheBeiChongZhiActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("titleName", titleName);
        intent.putExtra("image", image);
        intent.putExtra("header", header);
        intent.putExtra("FenLeiContentModel", fenLeiContentModel);
        context.startActivity(intent);
    }
}