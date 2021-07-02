package com.example.memorandum.activity;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.example.memorandum.R;
import com.example.memorandum.util.AppGlobal;

import solid.ren.skinlibrary.base.SkinBaseActivity;

public class SettingsActivity extends SkinBaseActivity implements View.OnClickListener {
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
        setContentView(R.layout.activity_settings);
        setTitle("Settings");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolkit);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.back);
        }
        View rlResetPassword = findViewById(R.id.rl_resetpassword);
        View rlSwitchTheme = findViewById(R.id.rl_switchtheme);
        rlResetPassword.setOnClickListener(this);
        rlSwitchTheme.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_resetpassword:
                startActivity(new Intent(SettingsActivity.this, ResetPasswordActivity.class));
                break;
            case R.id.rl_switchtheme:
                startActivity(new Intent(SettingsActivity.this, ThemeActivity.class));
                break;

            default:

                break;
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
    protected void onDestroy() {
        //取消注册
        super.onDestroy();
        unregisterReceiver(mybroad);
    }
}
