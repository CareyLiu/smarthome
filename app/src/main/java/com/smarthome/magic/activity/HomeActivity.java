package com.smarthome.magic.activity;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.SystemClock;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.jaeger.library.StatusBarUtil;
import com.smarthome.magic.R;
import com.smarthome.magic.app.AppManager;
import com.smarthome.magic.app.BaseActivity;
import com.smarthome.magic.config.MyApplication;
import com.smarthome.magic.config.PreferenceHelper;
import com.smarthome.magic.fragment.HomeFragment;
import com.smarthome.magic.fragment.MessagerFragment;
import com.smarthome.magic.fragment.MineFragment;
import com.smarthome.magic.fragment.OnlineFragment;
import com.smarthome.magic.fragment.PlayerFragment;
import com.smarthome.magic.util.AlertUtil;
import com.smarthome.magic.util.AppToast;
import com.smarthome.magic.view.NoScrollViewPager;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;



public class HomeActivity extends BaseActivity {


    BottomNavigationViewEx mBnve;
    @BindView(R.id.vp)
    NoScrollViewPager mVp;
    @BindView(R.id.activity_with_view_pager)
    RelativeLayout activityWithViewPager;
    @BindView(R.id.layout_bg)
    LinearLayout layoutBg;
    private boolean isExit;

    private SparseIntArray items;

    @Override
    public int getContentViewResId() {
        return R.layout.activity_main;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtil.setLightMode(this);
        ButterKnife.bind(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");

        mBnve = findViewById(R.id.bnve);
        initView();
        initData();
        initEvent();

        com.smarthome.magic.app.AppManager.getAppManager().addActivity(this);

    }

    @Override
    protected void onRestart() {
        super.onRestart();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (!isExit) {
                AppToast.makeShortToast(this, "再按一次返回键退出");
                isExit = true;
                new Thread() {
                    public void run() {
                        SystemClock.sleep(3000);
                        isExit = false;
                    }

                }.start();
                return true;
            }
//            AppManager.AppExit();
            AppManager.getAppManager().finishAllActivity();
        }
        return super.onKeyDown(keyCode, event);

    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    @Override
    public void finish() {
        super.finish();
    }


    /**
     * change BottomNavigationViewEx style
     */
    private void initView() {


        mBnve.enableAnimation(false);
        mBnve.enableShiftingMode(false);
        mBnve.enableItemShiftingMode(false);
    }

    /**
     * create fragments
     */
    private void initData() {
        List<Fragment> fragments = new ArrayList<>(5);
        items = new SparseIntArray(5);

        HomeFragment homeFragment = new HomeFragment();
        //    PlayerFragment playerFragment = new PlayerFragment();
        OnlineFragment onlineFragment = new OnlineFragment();
        MessagerFragment messagerFragment = new MessagerFragment();
        MineFragment mineFragment = new MineFragment();
        fragments.add(homeFragment);
        //   fragments.add(playerFragment);
        fragments.add(onlineFragment);
        fragments.add(messagerFragment);
        fragments.add(mineFragment);
//
        items.put(R.id.i_home, 0);
        //items.put(R.id.i_play, 1);
        items.put(R.id.i_car_online, 1);
        items.put(R.id.i_message, 2);
        items.put(R.id.i_mine, 3);

        // set adapter
        VpAdapter adapter = new VpAdapter(getSupportFragmentManager(), fragments);
        //禁用懒加载，不然每次切换页面都会重新获取数据
        mVp.setOffscreenPageLimit(4);
        //viewPage禁止滑动
        mVp.setScroll(false);
        mVp.setAdapter(adapter);
    }

    /**
     * set listeners
     */
    private void initEvent() {
        // set listener to change the current item of view pager when click bottom nav item
        mBnve.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            private int previousPosition = -1;

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int position = items.get(item.getItemId());
                //   if (position == 3) {
                //  layoutBg.setBackground(getResources().getDrawable(R.color.app_bg));
                // StatusBarUtil.setDarkMode(HomeActivity.this);
                //    } else {
                //   layoutBg.setBackground(getResources().getDrawable(R.color.white));
                //   StatusBarUtil.setLightMode(HomeActivity.this);
                // }

                if (previousPosition != position) {
                    previousPosition = position;
                    mVp.setCurrentItem(position, false);
                }
                return true;
            }
        });

        mVp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mBnve.setCurrentItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }


    /**
     * view pager adapter
     */
    private static class VpAdapter extends FragmentPagerAdapter {
        private List<Fragment> data;

        VpAdapter(FragmentManager fm, List<Fragment> data) {
            super(fm);
            this.data = data;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Fragment getItem(int position) {
            return data.get(position);
        }
    }




    public static HomeActivity getInstance() {
        return new HomeActivity();
    }


}