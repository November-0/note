package com.example.memorandum.activity;


import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.memorandum.R;
import com.example.memorandum.adapter.DataAdapter;
import com.example.memorandum.bean.Data;
import com.example.memorandum.bean.User;
import com.example.memorandum.dao.DataDAO;
import com.example.memorandum.dao.UserDAO;
import com.example.memorandum.ui.DividerItemDecoration;
import com.example.memorandum.ui.SwipeItemLayout;
import com.example.memorandum.util.AppGlobal;

import org.litepal.LitePal;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import solid.ren.skinlibrary.base.SkinBaseActivity;



public class MainActivity extends SkinBaseActivity {
    private List<Data> dataList = new ArrayList<>();
    private DrawerLayout mDrawerLayout;
    private SwipeRefreshLayout swipeRefresh;
    private DataAdapter adapter;
    private SearchView searchView;
    private static final String TAG = "MainActivity";
    private final DataDAO dataDAO = new DataDAO();
    private final UserDAO userDAO = new UserDAO();
    private TextView signIn;
    NavigationView navigationView;
    View headerView;
    ImageView headerImage;
    static private int live = 0;

    BroadcastReceiver mybroad = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            Log.i(TAG, "Style");
            //finish();
            recreate();
        }
    };

//如果是晚上6点到早上6点自动切换夜间模式 onCreate里面
    private int timeStatus() {
        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat("HH");
        String str = df.format(date);
        int a = Integer.parseInt(str);
        if (a >= 0 && a <= 6) {
            return 1;
        }
        if (a > 6 && a <= 12) {
            return 2;
        }
        if (a > 12 && a <= 13) {
            return 3;
        }
        if (a > 13 && a <= 18) {
            return 4;
        }
        if (a > 18 && a <= 24) {
            return 5;
        }
        return 0;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //live是静态的防止多次设定，支持修改主题，如果每次进入都根据时间写死为dark 那后面修改主题就不行了
        if (live == 0 && (timeStatus() == 5 || timeStatus() == 1)) {
            AppGlobal.THEME = "dark";
            live = 1;
            //setTheme(R.style.DarkTheme);
        }
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


        setContentView(R.layout.activity_main);
        LitePal.getDatabase();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolkit);
        setTitle("Note");
        setSupportActionBar(toolbar);
//主页导航栏
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        headerView = navigationView.inflateHeaderView(R.layout.nav_header);
        // headerImage = (CircleImageView) headerView.findViewById(R.id.icon_image);
        headerImage = (ImageView) headerView.findViewById(R.id.icon_image);
        signIn = (TextView) headerView.findViewById(R.id.sign_in);
        TextView signComment = (TextView) headerView.findViewById(R.id.sign_comment);
        if (!AppGlobal.USERNAME.equals("")) {
            String currentImagePath = UserDAO.findImagePath(AppGlobal.USERNAME);
            Glide.with(this).load(currentImagePath).asBitmap().into(headerImage);
            signIn.setText(AppGlobal.NAME);
            signComment.setText("Welcome");
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.menu);
        }
        

/**
 * 导航栏点击事件关键代码
 * */
        headerImage.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ShowToast")
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "clicked header", Toast.LENGTH_SHORT);
                if (!AppGlobal.USERNAME.equals("")) {
                    String currentImagePath = UserDAO.findImagePath(AppGlobal.USERNAME);
                    Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                    intent.putExtra("imagePath", currentImagePath);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            }
        });
        navigationView.setCheckedItem(R.id.nav_memo);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                mDrawerLayout.closeDrawers();
                // Close menu
                switch (item.getItemId()) {
                    case R.id.nav_memo:
                        if (AppGlobal.USERNAME != null && !AppGlobal.USERNAME.equals("")) {
                            int userId = UserDAO.findUserId(AppGlobal.USERNAME);
                            initUserMemo(userId);
                        } else {
                            initMemo();
                        }
                        setTitle("Note");
                        break;
                    /*case R.id.nav_pending:
                        if (AppGlobal.USERNAME != null && !AppGlobal.USERNAME.equals("")) {
                            int userId = UserDAO.findUserId(AppGlobal.USERNAME);
                            initUserPending(userId);
                        } else {
                            initPending();
                        }
                        setTitle("待办");
                        break;*/
                    case R.id.nav_reminder:
                        if (AppGlobal.USERNAME != null && !AppGlobal.USERNAME.equals("")) {
                            int userId = UserDAO.findUserId(AppGlobal.USERNAME);
                            initUserReminder(userId);
                        } else {
                            initReminder();
                        }
                        setTitle("Remind");
                        break;
                    case R.id.nav_favorites:
                        if (AppGlobal.USERNAME != null && !AppGlobal.USERNAME.equals("")) {
                            int userId = UserDAO.findUserId(AppGlobal.USERNAME);
                            initUserFavorites(userId);
                        } else {
                            initFavorites();
                        }
                        setTitle("Favorites");
                        break;
                    case R.id.nav_settings:
                        if (AppGlobal.NAME != null && !AppGlobal.NAME.equals("")) {
                            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                            startActivity(intent);
                        } else {
                            startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        }
                        break;
                    case R.id.nav_logout:
                        if (AppGlobal.USERNAME != null && !AppGlobal.USERNAME.equals("")) {
                            AppGlobal.USERNAME = "";
                            AppGlobal.NAME = "";
                            AppGlobal.INSERT_IMAGE = false;
                            AppGlobal.currentImagePath = "";
                            finish();
                        }
                        break;
                }
                return true;

            }
        });
        /*
         * set the main btn for Adding.
         * 添加新的备忘录
         */
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MemorandumActivity.class);
                //to memorandumActivity.class
                v.getContext().startActivity(intent);
            }
        });

        /***
         * 模糊搜索
         * 对于搜索框，依靠SearchView实现，由于SearchView未提供修改文本输入光标及文字颜色属性的接口，
         * 在实际开发有修改光标及文字颜色的需求，在这里通过反射机制来实现。
         */

        searchView = (SearchView) findViewById(R.id.search_view);
        searchView.clearFocus();

        //下划线的id
        int searchPlateId = searchView.getContext().getResources()
                .getIdentifier("android:id/search_plate", null, null);
        //放大镜图标
        int searchMagId = searchView.getContext().getResources()
                .getIdentifier("android:id/search_mag_icon", null, null);
        //关闭按钮
        int searchClsId = searchView.getContext().getResources()
                .getIdentifier("android:id/search_close_btn", null, null);

        View searchPlate = searchView.findViewById(searchPlateId);
        ImageView searchMagIcon = (ImageView) this.findViewById(searchMagId);
        ImageView searchClsIcon = (ImageView) this.findViewById(searchClsId);
        if (searchMagIcon != null && "dark".equals(AppGlobal.THEME)) {
            searchMagIcon.setColorFilter(Color.WHITE);
        }
        if (searchClsIcon != null && "dark".equals(AppGlobal.THEME)) {
            searchClsIcon.setColorFilter(Color.WHITE);
        }
        if (searchPlate != null) {
            int searchTextId = searchPlate.getContext().getResources()
                    .getIdentifier("android:id/search_src_text", null, null);
            TextView searchText = (TextView) searchPlate.findViewById(searchTextId);

            if (searchText != null) {
                searchText.setTextSize(14);
                if ("dark".equals(AppGlobal.THEME)) {
                    searchText.setTextColor(Color.WHITE);
                    searchText.setHintTextColor(Color.WHITE);
                    try {
                        Field mCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
                        mCursorDrawableRes.setAccessible(true);
                    } catch (Exception ignored) {

                    }
                }
            }
        }

        /**
         *mDataList，会在主页载入的时候查询数据库之后被构建妥当。
         * */
        if (AppGlobal.USERNAME != null && !AppGlobal.USERNAME.equals("")) {
            int userId = UserDAO.findUserId(AppGlobal.USERNAME);
            dataList = dataDAO.getUserData(userId);
        } else {
            dataList = dataDAO.getData();
        }
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(layoutManager);
        adapter = new DataAdapter(dataList);
        // dataDAO.printList();
//        int a = dataList.get(1).getUser().getId();
//        int b = dataList.get(2).getUser().getId();
//        Log.d("A", String.valueOf(a));
//        Log.d("B", String.valueOf(b));
        recyclerView.setAdapter(adapter);
        //分割线
        recyclerView.addItemDecoration(new DividerItemDecoration(this));
        recyclerView.addOnItemTouchListener(new SwipeItemLayout.OnSwipeItemTouchListener(this));

        /**
         * 为SearchView设置一个文本监听，当搜索框内的文本发生改变时离开搜索相应的备忘。
         * 还需要根据当前选择的分类来进行相应的数据库的查询。
         * */
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
                LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                recyclerView.setLayoutManager(layoutManager);
                if (AppGlobal.USERNAME != null && !AppGlobal.USERNAME.equals("")) {
                    User currentUser = UserDAO.findUser(AppGlobal.USERNAME);
                    int userId = UserDAO.findUserId(AppGlobal.USERNAME);
                    Log.i(TAG, Integer.toString(userId));
                    if (!TextUtils.isEmpty(newText.trim())) {
                        //判定searchview的输入框是否非空
                        dataList.clear();
                        if (getTitle() == "Note") {
                            dataList = dataDAO.queryUserData(newText.trim(), userId);
                        } else if (getTitle() == "Remind") {
                            dataList = dataDAO.queryUserReminder(newText.trim(), userId);
                        } else if (getTitle() == "Favorites") {
                            dataList = dataDAO.queryUserFavorites(newText.trim(), userId);
                        }
                    } else {
                        dataList.clear();
                        if (getTitle() == "Remind") {
                            dataList = dataDAO.getUserReminder(userId);
                        } else if (getTitle() == "Favorites") {
                            dataList = dataDAO.getUserFavorites(userId);
                        } else {
                            dataList = dataDAO.getUserData(userId);
                        }
                    }
                } else {
                    if (!TextUtils.isEmpty(newText)) {
                        dataList.clear();
                        if (getTitle() == "Note") {
                            dataList = dataDAO.queryData(newText.trim());
                        } else if (getTitle() == "Remind") {
                            dataList = dataDAO.queryReminder(newText.trim());
                        } else if (getTitle() == "Favorites") {
                            dataList = dataDAO.queryFavorites(newText.trim());
                        }
                    } else {
                        if (getTitle() == "Remind") {
                            dataList = dataDAO.getReminder();
                        } else if (getTitle() == "Favorites") {
                            dataList = dataDAO.getFavorites();
                        } else {
                            dataList = dataDAO.getData();
                        }
                    }
                }
                adapter = new DataAdapter(dataList);
                recyclerView.setAdapter(adapter);
                return false;
            }
        });
        /**
         * 向下拉刷新
         * 使用SwipeRefreshLayout，为我们自带一个刷新控件。在这里需要为其指定一个“刷新动作”的监听器。
         * */
        SwipeRefreshLayout.OnRefreshListener listener = new SwipeRefreshLayout.OnRefreshListener() {
            public void onRefresh() {
                refreshData();
            }
        };
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        swipeRefresh.setOnRefreshListener(listener);
        listener.onRefresh();
        // refreshData() ;
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolkit, menu);
        return true;
    }

//toolbar关键代码
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            default:
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        searchView.clearFocus();
        //指清除所有聚焦状态
        searchView.setFocusable(false);
        //指不聚焦
        searchView.setFocusableInTouchMode(false);
        searchView.requestFocus();
        //指恢复已保存的聚焦状态
        if (AppGlobal.NAME != null && !AppGlobal.NAME.equals("")) {
            signIn.setText(AppGlobal.NAME);
        }
        if (AppGlobal.currentImagePath != null && !AppGlobal.currentImagePath.equals("")) {
            Glide.with(this).load(AppGlobal.currentImagePath).asBitmap().into(headerImage);
        }
        Log.d("MAIN", "resume");
        refreshData();
    }

    private void initMemo() {
        dataList.clear();
        dataList = dataDAO.getData();
        initRecyclerView();
    }

    private void initUserMemo(int userId) {
        dataList.clear();
        dataList = dataDAO.getUserData(userId);
        initRecyclerView();
    }

    private void initPending() {
        dataList.clear();
        dataList = dataDAO.getPending();
        initRecyclerView();
    }

    private void initUserPending(int userId) {
        dataList.clear();
        dataList = dataDAO.getUserPending(userId);
        initRecyclerView();
    }

    private void initReminder() {
        dataList.clear();
        dataList = dataDAO.getReminder();
        initRecyclerView();
    }

    private void initUserReminder(int userId) {
        dataList.clear();
        dataList = dataDAO.getUserReminder(userId);
        initRecyclerView();
    }

    private void initFavorites() {
        dataList.clear();
        dataList = dataDAO.getFavorites();
        initRecyclerView();
    }

    private void initUserFavorites(int userId) {
        dataList.clear();
        dataList = dataDAO.getUserFavorites(userId);
        initRecyclerView();
    }

    private void initRecyclerView() {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new DataAdapter(dataList);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            //drawer方向
        } else {
            super.onBackPressed();
        }
    }
/**
 *由于不希望刷新动作阻塞App当前的运行，造成用户使用感受上的卡顿，
 * 所以在响应刷新行为的时候使用一个额外的线程异步完成数据的更新、页面UI重绘等工作。
 * */
    private void refreshData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dataList.clear();
                        if (AppGlobal.USERNAME != null && !AppGlobal.USERNAME.equals("")) {
                            int userId = UserDAO.findUserId(AppGlobal.USERNAME);
                            if (getTitle() == "Favorites") {
                                initUserFavorites(userId);
                            } else {
                                initUserMemo(userId);
                            }
                        } else {
                            if (getTitle() == "Favorites") {
                                initFavorites();
                            } else {
                                initMemo();
                            }
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        //取消注册
        super.onDestroy();
        unregisterReceiver(mybroad);
    }
}
