package com.example.memorandum.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.memorandum.R;
import com.example.memorandum.dao.UserDAO;
import com.example.memorandum.util.AppGlobal;

import solid.ren.skinlibrary.base.SkinBaseActivity;

/**
 * Activity: To login , From Main Activity, Back to Main.
 */
public class LoginActivity extends SkinBaseActivity implements View.OnClickListener {
    // private AutoCompleteTextView mEmailView;
    private EditText mEmailView;
    private EditText mPasswordView;

    private SharedPreferences mPreferences;
    // private SharedPreferences.Editor editor;
    // now "editor" has been converted to a local variable.
    // instead of standing a global in this Activity.
    private CheckBox mRememberPass;
    static boolean mRememberFlag = false;
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
        IntentFilter filter=new IntentFilter();
        filter.addAction("change style");
        this.registerReceiver(mybroad, filter);
        setContentView(R.layout.activity_login);
        setTitle("Hello!");


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolkit);
        setSupportActionBar(toolbar);
//特定的返回键
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.back);
        }

        /**
         * 偏好设置
         * 用户在登陆的时候还可以选择“Remember me”，如果之后登录成功，系统则会保存登录信息，
         * 这样下次登录的时候就可以自动填入信息了。
         * 使用了安卓开发常用的偏好设置来完成这项工作的
         * */
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mEmailView = (EditText) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mRememberPass = (CheckBox) findViewById(R.id.remember_pass);

        mRememberFlag = mPreferences.getBoolean("remember_password", false);

        if (mRememberFlag) {
            String userName = mPreferences.getString("userName", "");
            String password = mPreferences.getString("password", "");
            mEmailView.setText(userName);
            mPasswordView.setText(password);
            mRememberPass.setChecked(true);
        }

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(this);

        Button mRegisterButton = (Button) findViewById(R.id.register);
        mRegisterButton.setOnClickListener(this);
    }
//页面重启的时候会调用的生命周期
    @Override
    protected void onResume() {
        super.onResume();
        if (!mRememberFlag) {
            mEmailView.setText("");
            mPasswordView.setText("");
            mRememberPass.setChecked(false);
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
            case R.id.email_sign_in_button:
                attemptLogin();//调用函数检查登陆信息是否合法
                break;
            case R.id.register:
                Intent intent = new Intent();
                intent.setClass(LoginActivity.this, RegisterActivity.class);
                LoginActivity.this.startActivity(intent);
                break;
        }
    }

    /**
     * 输入信息的检查
     */
    private void attemptLogin() {

        // 初始化错误信息为null
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // 获取输入信息.
        String userName = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;//是否是非法信息
        View focusView = null;

        //  检查邮箱
        if (TextUtils.isEmpty(userName)) {
            mEmailView.setError("请输入用户名");
            focusView = mEmailView;
            cancel = true;
        }

        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError("请输入密码");
            focusView = mPasswordView;
            cancel = true;
        }

        // 检查密码是否有效
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError("密码太短，请至少输入4位");
            focusView = mPasswordView;
            cancel = true;
        }

        /**
         * 登陆逻辑关键代码
         * */
        if (cancel) {//非法信息
            focusView.requestFocus();//标签用于指定屏幕内的焦点View。
        } else {//合法信息
            //登陆跳转逻辑
            UserDAO userDAO = new UserDAO();
            boolean sussess = userDAO.checkLogin(userName, password);
            if (sussess) {  //信息合法
                AppGlobal.NAME = userDAO.findNameByUsername(userName);//保存用户登录信息到全局变量中
                AppGlobal.USERNAME = userName;
                SharedPreferences.Editor editor = mPreferences.edit();
                if (mRememberPass.isChecked()) {
                    editor.putBoolean("remember_password", true);
                    editor.putString("userName", userName);
                    editor.putString("password", password);
                } else {
                    editor.clear();

                }
                editor.apply();
                Intent intent = new Intent();
                intent.setClass(LoginActivity.this, MainActivity.class);
                LoginActivity.this.startActivity(intent);
            } else {
                Toast.makeText(LoginActivity.this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 密码是否合法：至少需要4位
     *
     * @param password
     * @return
     */
    private boolean isPasswordValid(String password) {
        return password.length() >= 4;
    }
    @Override
    protected void onDestroy() {
        //取消注册
        super.onDestroy();
        unregisterReceiver(mybroad);
    }
}
