package com.example.memorandum.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.memorandum.R;
import com.example.memorandum.bean.User;
import com.example.memorandum.dao.UserDAO;
import com.example.memorandum.util.AppGlobal;
import com.example.memorandum.util.CommonUtility;

import solid.ren.skinlibrary.base.SkinBaseActivity;

public class ResetPasswordActivity extends SkinBaseActivity implements View.OnClickListener {
    private EditText et_usertel;
    private EditText et_code;
    private Button btn_code;
    private EditText et_password;
    private EditText et_password_confirm;
    private Button btn_ok;
    BroadcastReceiver mybroad = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            //finish();
            recreate();
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ("normal".equals(AppGlobal.THEME)) {
            setTheme(R.style.AppTheme);
        } else if ("pink".equals(AppGlobal.THEME)) {
            setTheme(R.style.PinkTheme);
        } else if ("dark".equals(AppGlobal.THEME)) {
            setTheme(R.style.DarkTheme);
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction("change style");
        this.registerReceiver(mybroad, filter);
        setContentView(R.layout.activity_reset_password);
        setTitle("Forget something?");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolkit);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.back);
        }
        et_usertel = (EditText) findViewById(R.id.et_usertel);
        et_code = (EditText) findViewById(R.id.et_code);
        btn_code = (Button) findViewById(R.id.btn_code);
        et_password = (EditText) findViewById(R.id.et_password);
        et_password_confirm = (EditText) findViewById(R.id.et_password_confirm);
        btn_ok = (Button) findViewById(R.id.btn_ok);
        btn_code.setOnClickListener(this);
        btn_ok.setOnClickListener(this);
        if (AppGlobal.USERNAME != null && !AppGlobal.USERNAME.equals("")) {
            et_usertel.setText(AppGlobal.USERNAME);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_code:
                et_code.setText(CommonUtility.getRandNum(6));
                break;
            case R.id.btn_ok:
                attemptReset();
                break;

        }
    }

    private void attemptReset() {
        et_usertel.setError(null);
        et_code.setError(null);
        et_password.setError(null);
        et_password_confirm.setError(null);

        String userName = et_usertel.getText().toString().trim();
        String code = et_code.getText().toString().trim();
        String password = et_password.getText().toString().trim();
        String password_confirm = et_password_confirm.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(userName)) {
            et_usertel.setError("Phone is empty.");
            focusView = et_usertel;
            cancel = true;
        }

        if (!userName.equals(AppGlobal.USERNAME)) {
            et_usertel.setError("Phone error.");
            focusView = et_usertel;
            cancel = true;
        }

        if (TextUtils.isEmpty(code)) {
            et_code.setError("Where is the verification code ?");
            focusView = et_code;
            cancel = true;
        }
        if (TextUtils.isEmpty(password)) {
            et_password.setError("Password is empty.");
            focusView = et_password;
            cancel = true;
        }
        if (TextUtils.isEmpty(password_confirm)) {
            et_password_confirm.setError("Do not forget to confirm.");
            focusView = et_password_confirm;
            cancel = true;
        }

        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            et_password.setError("The password requires at least four digits.");
            focusView = et_password;
            cancel = true;
        }
        if (!password.equals(password_confirm)) {
            et_password_confirm.setError("The two passwords entered are not consistent.");
            focusView = et_password_confirm;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            UserDAO userDAO = new UserDAO();
            boolean isSuccess = false;
            if (!userDAO.checkUsername(userName)) {
                et_usertel.setError("User not found.");
            } else {
                isSuccess = userDAO.checkPassword(userName, password);
                if (isSuccess) {
                    userDAO.resetPassword(userName, password);
                    Toast.makeText(ResetPasswordActivity.this, "Successfully.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(ResetPasswordActivity.this, "Isn't this the same as the original one?", Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 4;
    }
}
