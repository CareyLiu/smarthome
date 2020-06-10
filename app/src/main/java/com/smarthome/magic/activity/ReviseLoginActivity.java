package com.smarthome.magic.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.google.gson.Gson;
import com.jaeger.library.StatusBarUtil;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.model.Response;
import com.smarthome.magic.R;
import com.smarthome.magic.callback.JsonCallback;
import com.smarthome.magic.config.AppResponse;
import com.smarthome.magic.config.Constant;
import com.smarthome.magic.util.AlertUtil;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ReviseLoginActivity extends BaseActivity {


    @BindView(R.id.rl_back)
    RelativeLayout rlBack;
    @BindView(R.id.et_account)
    EditText etPassword;
    @BindView(R.id.et_repeat)
    EditText etRepeat;
    @BindView(R.id.btn_save)
    Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_revise_login_password);
        ButterKnife.bind(this);
        StatusBarUtil.setLightMode(this);
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length()>0 && !etRepeat.getText().toString().equals("")){
                    btnSave.setEnabled(true);
                    btnSave.setBackground(getResources().getDrawable(R.drawable.bg_shape_app));
                }else {
                    btnSave.setEnabled(false);
                    btnSave.setBackground(getResources().getDrawable(R.drawable.bg_shape_app_disabled));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        etRepeat.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length()>0 && !etPassword.getText().toString().equals("")){
                    btnSave.setEnabled(true);
                    btnSave.setBackground(getResources().getDrawable(R.drawable.bg_shape_app));
                }else {
                    btnSave.setEnabled(false);
                    btnSave.setBackground(getResources().getDrawable(R.drawable.bg_shape_app_disabled));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }


    @OnClick({R.id.rl_back, R.id.btn_save})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.rl_back:
                finish();
                break;
            case R.id.btn_save:
                if (etRepeat.getText().toString().equals(etPassword.getText().toString())){
                    requestData();
                }else{
                    AlertUtil.t(getApplicationContext(),"两次密码输入不一致，请重新输入");
                }

                break;
        }
    }

    /**
     * 修改登录密码
     */
    private void requestData() {
        Map<String, String> map = new HashMap<>();
        map.put("code", "03009");
        map.put("key", Constant.KEY);
        map.put("password",etPassword.getText().toString());
        map.put("sms_id", getIntent().getStringExtra("sms_id"));
        map.put("sms_code", getIntent().getStringExtra("sms_code"));
        Gson gson = new Gson();
        OkGo.<AppResponse>post(Constant.SERVER_URL + "msg")
                .tag(this)//
                .upJson(gson.toJson(map))
                .execute(new JsonCallback<AppResponse>() {
                    @Override
                    public void onSuccess(Response<AppResponse> response) {
                        AlertUtil.t(getApplicationContext(),response.body().msg);

                    }

                    @Override
                    public void onError(Response<AppResponse> response) {
                        AlertUtil.t(getApplication(), response.getException().getMessage());

                    }
                });

    }


}