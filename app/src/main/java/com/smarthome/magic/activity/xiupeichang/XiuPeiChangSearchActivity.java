package com.smarthome.magic.activity.xiupeichang;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.flyco.roundview.RoundLinearLayout;
import com.smarthome.magic.R;
import com.smarthome.magic.activity.taokeshagncheng.Custom5SearchThingActivity;
import com.smarthome.magic.adapter.Custom5HistoryAdapter;
import com.smarthome.magic.app.BaseActivity;
import com.smarthome.magic.common.StringUtils;
import com.smarthome.magic.common.UIHelper;
import com.smarthome.magic.db.DBHelperConsumer;
import com.smarthome.magic.db.DBManager;
import com.smarthome.magic.db.HistoryRecordDao;
import com.smarthome.magic.db.table.HistoryRecord;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

import static com.smarthome.magic.db.DbField.XIUPEICHANGSEARCH;


public class XiuPeiChangSearchActivity extends BaseActivity {
    private static final String tag = XiuPeiChangSearchActivity.class.getSimpleName();
    @BindView(R.id.et_serarchKey)
    EditText etSerarchKey;
    @BindView(R.id.rl_search)
    RoundLinearLayout rlSearch;
    @BindView(R.id.iv_cancel)
    TextView ivCancel;
    @BindView(R.id.rl_title)
    RelativeLayout rlTitle;
    @BindView(R.id.rv_view2)
    RecyclerView rvView2;
    @BindView(R.id.ll_clear_all)
    LinearLayout llClearAll;
    @BindView(R.id.view_line)
    View viewLine;
    @BindView(R.id.ll_search)
    LinearLayout llSearch;
    @BindView(R.id.activity_custom3_search)
    RelativeLayout activityCustom3Search;

    private Custom5HistoryAdapter historyAdapter;
    private InputMethodManager imm;
    private DBHelperConsumer helper;
    List<HistoryRecord> searchKeywords1;
    public DBManager dbManager;


    /**
     * 用于其他activity跳转到该activity
     */
    public static void actionStart(Context context) {
        Intent intent = new Intent(context, XiuPeiChangSearchActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (searchKeywords1.size() > 0) {
            llClearAll.setVisibility(View.VISIBLE);
            viewLine.setVisibility(View.VISIBLE);

        } else {
            llClearAll.setVisibility(View.GONE);
            viewLine.setVisibility(View.GONE);
        }
    }

    TextView tv;//搜索字段
    public Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setBaseTitleGone();

        dbManager = DBManager.getInstance(this);
        dbManager.setDebug();
        searchKeywords1 = new ArrayList<>();
        context = this;
        // App.scaleScreenHelper.loadView((ViewGroup) getWindow().getDecorView());
        helper = new DBHelperConsumer(this);

        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        historyAdapter();
        etSerarchKey.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {

                //以下方法防止两次发送请求
                if (actionId == EditorInfo.IME_ACTION_SEND ||
                        (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    switch (event.getAction()) {
                        case KeyEvent.ACTION_UP:
                            tv = textView;
                            if (StringUtils.isEmpty(tv.getText().toString().trim())) {
                                UIHelper.ToastMessage(XiuPeiChangSearchActivity.this, "检索内容不能为空");
                            } else {
                                //发送请求
                                insertKeyword(textView.getText().toString().trim(), XIUPEICHANGSEARCH);
                                XiuPeiChangSearchThingActivity.actionStart(XiuPeiChangSearchActivity.this, textView.getText().toString().trim());
                            }

                            return true;
                        default:
                            return true;
                    }
                }

                return false;
            }
        });

        searchKeywords1 = queryKeywordList(XIUPEICHANGSEARCH);
        historyAdapter.setNewData(searchKeywords1);
        if (searchKeywords1.size() > 0) {
            llClearAll.setVisibility(View.VISIBLE);
            viewLine.setVisibility(View.VISIBLE);
        } else {
            llClearAll.setVisibility(View.GONE);
            viewLine.setVisibility(View.GONE);
        }

    }

    /**
     * 插入
     *
     * @param keyword
     */
    public void insertKeyword(String keyword, int type) {
        if (keyword == null || keyword.isEmpty()) {
            return;
        }
        HistoryRecordDao historyRecordDao = dbManager.getDaoSession().getHistoryRecordDao();
        List<HistoryRecord> searchKeywordList = queryKeywordList(type);
        if (searchKeywordList.size() > 9) {
            historyRecordDao.queryBuilder().where(HistoryRecordDao.Properties.Date.eq(searchKeywordList.get(0).getDate())).buildDelete().executeDeleteWithoutDetachingEntities();
        }

        Iterator<HistoryRecord> iterator = searchKeywordList.iterator();
        while (iterator.hasNext()) {
            HistoryRecord next = iterator.next();
            if (keyword.equals(next.getName())) {
                iterator.remove();
                historyRecordDao.queryBuilder().where(HistoryRecordDao.Properties.Name.eq(next.getName())).buildDelete().executeDeleteWithoutDetachingEntities();
            }
        }

        HistoryRecord sk = new HistoryRecord();
        sk.setName(keyword);
        sk.setDate(System.currentTimeMillis());
        sk.setType(type);
        historyRecordDao.insertInTx(sk);

        if (historyAdapter != null) {
            searchKeywords1.clear();
            searchKeywords1 = queryKeywordList(XIUPEICHANGSEARCH);
            historyAdapter.setNewData(searchKeywords1);
        }
    }

    /**
     * 查询
     */
    public List<HistoryRecord> queryKeywordList(int type) {
        HistoryRecordDao historyRecordDao = dbManager.getDaoSession().getHistoryRecordDao();
        List<HistoryRecord> searchKeywordList = historyRecordDao.queryBuilder().where(HistoryRecordDao.Properties.Type.eq(type)).orderAsc(HistoryRecordDao.Properties.Date).build().list();
        return searchKeywordList;
    }

    /**
     * 删除
     */
    public void delAllKeyword(int type) {
        HistoryRecordDao historyRecordDao = dbManager.getDaoSession().getHistoryRecordDao();
        historyRecordDao.queryBuilder().where(HistoryRecordDao.Properties.Type.eq(type)).buildDelete().executeDeleteWithoutDetachingEntities();
        if (historyAdapter != null) {
            searchKeywords1.clear();
            searchKeywords1 = queryKeywordList(XIUPEICHANGSEARCH);
            historyAdapter.setNewData(searchKeywords1);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideInput(v, ev)) {

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
            return super.dispatchTouchEvent(ev);
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }

    public boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] leftTop = {0, 0};
            //获取输入框当前的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击的是输入框区域，保留点击EditText的事件
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getContentViewResId() {
        return R.layout.activity_custom5_search;
    }

    @OnClick({R.id.iv_cancel, R.id.ll_clear_all})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_clear_all:
                delAllKeyword(XIUPEICHANGSEARCH);
                llClearAll.setVisibility(View.GONE);
                viewLine.setVisibility(View.GONE);
                break;
            case R.id.iv_cancel:

                imm.hideSoftInputFromWindow(activityCustom3Search.getWindowToken(), 0);
                finish();
                break;
        }
    }

    //历史记录
    private void historyAdapter() {
        historyAdapter = new Custom5HistoryAdapter(searchKeywords1);
        rvView2.setLayoutManager(new LinearLayoutManager(this));
        rvView2.setAdapter(historyAdapter);
        historyAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                switch (view.getId()) {
                    case R.id.rl_layout:
                        //发送请求
                      //  Log.i("searchKey", searchKeywords1.size() + "");


                        XiuPeiChangSearchThingActivity.actionStart(XiuPeiChangSearchActivity.this, searchKeywords1.get(position).getName().toString());
                        insertKeyword(searchKeywords1.get(position).getName().toString(), XIUPEICHANGSEARCH);
                        break;
                    case R.id.iv_delete:
                        delOneItemword(searchKeywords1.get(position).getName().toString());
                        if (searchKeywords1.size() > 0) {
                            llClearAll.setVisibility(View.VISIBLE);
                            viewLine.setVisibility(View.VISIBLE);
                        } else {
                            llClearAll.setVisibility(View.GONE);
                            viewLine.setVisibility(View.GONE);
                        }
                        break;
                }
            }
        });
    }

    /**
     * 删除
     */
    public void delOneItemword(String name) {
        HistoryRecordDao historyRecordDao = dbManager.getDaoSession().getHistoryRecordDao();
        historyRecordDao.queryBuilder().where(HistoryRecordDao.Properties.Name.eq(name)).buildDelete().executeDeleteWithoutDetachingEntities();
        if (historyAdapter != null) {
            searchKeywords1.clear();
            searchKeywords1 = queryKeywordList(XIUPEICHANGSEARCH);
            historyAdapter.setNewData(searchKeywords1);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
