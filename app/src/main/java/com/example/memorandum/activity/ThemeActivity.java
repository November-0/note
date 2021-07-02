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
import android.widget.Toast;

import com.example.memorandum.R;
import com.example.memorandum.ui.SwitchButton;
import com.example.memorandum.util.AppGlobal;

import solid.ren.skinlibrary.base.SkinBaseActivity;
/**
 * 切换主题页面
 * */
public class ThemeActivity extends SkinBaseActivity implements View.OnClickListener {
    private SwitchButton btn_normal;
    private SwitchButton btn_pink;
    private SwitchButton btn_dark;
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

        setContentView(R.layout.activity_theme);
        setTitle("Change Style?");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolkit);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.back);
        }
        View rlBlue = findViewById(R.id.rl_blue);
        View rlPink = findViewById(R.id.rl_pink);
        View rlNight = findViewById(R.id.rl_night);
        rlBlue.setOnClickListener(this);
        rlPink.setOnClickListener(this);
        rlNight.setOnClickListener(this);

        btn_normal = (SwitchButton) findViewById(R.id.switch_style_normal);
        btn_pink = (SwitchButton) findViewById(R.id.switch_style_pink);
        btn_dark = (SwitchButton) findViewById(R.id.switch_style_dark);
        if ("normal".equals(AppGlobal.THEME)) {
            btn_normal.openSwitch();
        } else if ("pink".equals(AppGlobal.THEME)) {
            btn_pink.openSwitch();
        } else if ("dark".equals(AppGlobal.THEME)) {
            btn_dark.openSwitch();
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
/**
 * 在选择对应的主题之后，会发送一个无序广播，收到广播的界面都会执行recreate() 重绘界面。同时切换全局主题标识。
 * 主题设置关键代码
 * */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_blue: {
                Toast.makeText(ThemeActivity.this, "Normal...", Toast.LENGTH_SHORT).show();
                btn_normal.openSwitch();
                btn_pink.closeSwitch();
                btn_dark.closeSwitch();
                AppGlobal.THEME = "normal";
                Intent exit=new Intent();
                exit.setAction("change style");
                sendBroadcast(exit);
                break;
            }

            case R.id.rl_pink: {
                Toast.makeText(ThemeActivity.this, "Pink now !!", Toast.LENGTH_SHORT).show();
                btn_normal.closeSwitch();
                btn_pink.openSwitch();
                btn_dark.closeSwitch();
                AppGlobal.THEME = "pink";
                Intent exit=new Intent();
                exit.setAction("change style");
                sendBroadcast(exit);
                break;
            }

            case R.id.rl_night: {
                Toast.makeText(ThemeActivity.this, "Dark is cool.", Toast.LENGTH_SHORT).show();
                btn_normal.closeSwitch();
                btn_pink.closeSwitch();
                btn_dark.openSwitch();
                AppGlobal.THEME = "dark";
                Intent exit=new Intent();
                exit.setAction("change style");
                sendBroadcast(exit);
                break;
            }

            default:
                break;
        }
        // recreate();
    }
    @Override
    protected void onDestroy() {
        //取消注册
        super.onDestroy();
        unregisterReceiver(mybroad);
    }
}
