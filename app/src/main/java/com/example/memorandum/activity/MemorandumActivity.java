package com.example.memorandum.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.memorandum.R;
import com.example.memorandum.bean.Data;
import com.example.memorandum.bean.User;
import com.example.memorandum.dao.DataDAO;
import com.example.memorandum.dao.UserDAO;
import com.example.memorandum.service.AlarmService;
import com.example.memorandum.ui.GooeyMenu;
import com.example.memorandum.ui.RichEditText;
import com.example.memorandum.util.AppGlobal;
import com.example.memorandum.util.BitmapToPathUtil;
import com.example.memorandum.util.CommonUtility;
import com.example.memorandum.util.UriToPathUtil;
import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import solid.ren.skinlibrary.base.SkinBaseActivity;

import static com.example.memorandum.util.CommonUtility.resizeImage;

public class MemorandumActivity extends SkinBaseActivity implements GooeyMenu.GooeyMenuInterface, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    private RichEditText richEditText;
    private Toast mToast;
    int currentId;
    //    private static int currentMemoId = -1;
    int currentStar;
    int currentPending;
    int currentReminder;
    String newContent;
    String currentContent;
    String currentDate;
    String currentTime;
    String currentImagePath;
    String lazyImgPath = null;
    Intent intent;
    Data data = new Data();
    DataDAO dataDAO = new DataDAO();
    UserDAO userDAO = new UserDAO();

    private static final int PHOTO_SUCCESS = 1;
    private static final int CAMERA_SUCCESS = 2;
//设置两个时间选择器，一个选择具体日期，一个选择具体时间
    DatePickerDialog datePickerDialog = null;
    TimePickerDialog timePickerDialog = null;
    String dateSet = "";
    String timeSet = "";
    StringBuilder timeSetTotal = new StringBuilder();
    public static final String DATEPICKER_TAG = "datepicker";
    public static final String TIMEPICKER_TAG = "timepicker";
    private static final String TAG = "MemorandumActivity";
    private static Context sContext = null;

    //    SpannableString spannableString = new SpannableString("[local]" + 2 + "[/local]");
    SpannableString spannableString = new SpannableString(" ");

    public static Context getContext() {
        return sContext;
    }
//主题广播接收关键代码
    BroadcastReceiver mybroad = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            Log.i(TAG, "Style");
            //finish();
            recreate();
        }
    };

    @Override
    protected void onResume() {

        super.onResume();
        Log.e(TAG, "onResume");
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "create");
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

        setContentView(R.layout.activity_memorandum);
        sContext = this;

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.back);
        }

        intent = getIntent();
        GooeyMenu gooeyMenu = (GooeyMenu) findViewById(R.id.gooey_menu);
        gooeyMenu.setOnMenuListener(this);
        //gooeyMenu.getBackground().setAlpha(0);
//        Drawable __tmp = gooeyMenu.getBackground();
//        __tmp.setAlpha(0) ;

        TextView textView = (TextView) findViewById(R.id.exact_time);
        richEditText = (RichEditText) findViewById(R.id.input);
        richEditText.setCursorVisible(true);
        richEditText.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEvent.ACTION_DOWN == event.getAction()) {
                    richEditText.setCursorVisible(true);
                    View curConfirm = toolbar.findViewById(R.id.confirm);
                    curConfirm.setVisibility(View.VISIBLE);
                }
                return false;
            }
        });
        gooeyMenu.bringToFront();
        // gooeyMenu.getBackground().setAlpha(0);
        // do not work --r0
        currentId = intent.getIntExtra("id", 0);
        currentContent = intent.getStringExtra("content");
        currentDate = intent.getStringExtra("date");
        currentTime = intent.getStringExtra("exactTime");
        Log.e(TAG, Integer.toString(intent.getIntExtra("reminder", 0)));

        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
        Date date = new Date(System.currentTimeMillis());
        richEditText.setText(currentContent);
        textView.setText(currentDate);
//        textView.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
//        richEditText.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        if (TextUtils.isEmpty(textView.getText())) {
            textView.setText(simpleDateFormat.format(date));
        }

/**
 * 查看界面数据
 * 本页面的最上方显示了备忘的归属者。如果是未登录用户创建的，则会显示Public Note；
 * 如果是某一特定用户创建的则会显示用户的昵称。
 * 此界面从主页进入，主页会向intent中放入必要的信息，而在此界面加载时则需要取出并展示（如果有）。
 * */
        currentImagePath = intent.getStringExtra("imagePath");
        if (currentImagePath != null && !currentImagePath.equals("")) {
            richEditText.insertBitmap(currentImagePath);
            Log.i("Note get", currentImagePath);
        }
        if (currentContent == null || currentContent.equals("")) {
            setTitle("New note");
        } else {
            User targetUser = dataDAO.getUserById(currentId);
            String targetName = null;
            if (targetUser != null) {
                targetName = targetUser.getNickName();
                setTitle(targetName + "'s note");
            } else {
                targetName = "unknown";
                setTitle("Public note");
            }
        }
//初始化
        final Calendar calendar = Calendar.getInstance();
        datePickerDialog = DatePickerDialog.newInstance(this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        timePickerDialog = TimePickerDialog.newInstance(this, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false, false);
        if (intent.getStringExtra("reminder") != null) {
            Log.e("Remind back", String.valueOf(currentId));
            currentReminder = Integer.parseInt(intent.getStringExtra("reminder"));
        }

//        if (intent.getStringExtra("backRemind") != null) {
//            currentReminder = 1;
//            Log.e("Remind back", String.valueOf(currentId));
//            if (AppGlobal.USERNAME != null && !AppGlobal.USERNAME.equals("")) {
//                User currentUser = UserDAO.findUser(AppGlobal.USERNAME);
//                Data newData = new Data(currentDate, currentContent, date, currentPending, currentReminder, currentStar, currentUser);
//                dataDAO.updateUserData(newData, currentId);
//            } else {
//                Data newData = new Data(currentDate, currentContent, date, currentPending, currentReminder, currentStar);
//                dataDAO.updateData(newData, currentId);
//            }
//        }

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                finish();
                break;
            case R.id.confirm:
                @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
                Date date = new Date(System.currentTimeMillis());
                currentId = intent.getIntExtra("id", 0);
                Data data = new Data(currentId);
//                Data data = getIntent().getParcelableExtra("data");

                currentContent = intent.getStringExtra("content");
                currentDate = simpleDateFormat.format(date);
                currentPending = data.getPending();
                currentReminder = data.getReminder();
                currentStar = data.getStar();

                User targetUser = dataDAO.getUserById(currentId);
                int targetId = 0;
                if (targetUser != null) {
                    targetId = targetUser.getId();
                    Log.e("Cur user id", String.valueOf(targetId));
                } else {
                    targetId = -1;
                    Log.e("Cur user id", "null");
                }

                if (!TextUtils.isEmpty(richEditText.getText().toString().trim())) {
                    if (currentContent == null || currentContent.equals("")) {
                        //代表开始创建
                        Log.d("NEW", "new one");
                        /**
                         * 编辑主要依靠RichEditText控件实现，而对于编辑功能，则需要判断当前用户是否已登录，
                         * 如果未登录那么就只能编辑未登录用户创建的备忘。最后利用dataDAO将数据更新到数据库。
                         * */
                        if (AppGlobal.USERNAME != null && !AppGlobal.USERNAME.equals("")) {
                            User currentUser = UserDAO.findUser(AppGlobal.USERNAME);
                            newContent = richEditText.getText().toString();
                            Data newData = new Data(currentDate, newContent, date, currentPending, currentReminder, currentStar, currentUser);
                            dataDAO.insertUserData(newData);
                            // Log.d("New data", String.valueOf(newData.getUser().getId())) ;
                        } else {
                            newContent = richEditText.getText().toString();
                            Data newData = new Data(currentDate, newContent, date, currentPending, currentReminder, currentStar);
                            dataDAO.insertData(newData);
                        }
                        currentId = DataDAO.findCurrentId();
                        if (lazyImgPath != null) {
                            dataDAO.updateImagePath(lazyImgPath, currentId);
                        }
                        //获取最新的id，如果程序走到这一步，说明此处一定已经插入一条新数据了。此id一定就是刚才插入的那条记录的id。
                    } else if (!richEditText.getText().toString().equals(currentContent)) {
                        //更新
                        if (AppGlobal.USERNAME != null && !AppGlobal.USERNAME.equals("")) {
                            User currentUser = UserDAO.findUser(AppGlobal.USERNAME);
                            newContent = richEditText.getText().toString();
                            Data newData = new Data(currentDate, newContent, date, currentPending, currentReminder, currentStar, currentUser);
                            dataDAO.updateUserData(newData, currentId);
                            if (lazyImgPath != null) {
                                dataDAO.updateImagePath(lazyImgPath, currentId);
                            }
                        } else {
                            //目前未登录，如果targetId为-1表示原本就是未登录的时候创建的，允许修改
                            //否则不允许修改
                            if (targetId == -1) {
                                newContent = richEditText.getText().toString();
                                Data newData = new Data(currentDate, newContent, date, currentPending, currentReminder, currentStar);
                                dataDAO.updateData(newData, currentId);
                                if (lazyImgPath != null) {
                                    dataDAO.updateImagePath(lazyImgPath, currentId);
                                }
                            } else {
                                Toast.makeText(getContext(), "Do not do bad things !", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                } else {
                    dataDAO.deleteData(currentId);
                }
                View confirm = findViewById(R.id.confirm);
                confirm.setVisibility(View.GONE);
                RichEditText richEditText = (RichEditText) findViewById(R.id.input);
                richEditText.setCursorVisible(false);
                finish();
                break;
            default:
        }
        return true;
    }

    @Override
    public void menuOpen() {
//        showToast("Menu Open");
    }

    @Override
    public void menuClose() {
//        showToast( "Menu Close");
    }

    @Override
    public void menuItemClicked(int menuNumber) {
        //处理GooeyMenu点击事件的方法

        currentId = intent.getIntExtra("id", 0);
        //接收intent传递的值
        Data data = getIntent().getParcelableExtra("data");
        switch (menuNumber) {
            case 1:
                /*
                if (currentId == 0) {
                    showToast("Empty Note....");
                } else {
                    // Intent getImage = new Intent(Intent.ACTION_GET_CONTENT);
                    // 调用相册
                    // 4.41之后不可行，改为ACTION_PICK
                    // 0608: 改为ACTION_PICK之后路径会变成google的路径（如果安装了google商店）
                    // 继续使用ACTION_GET_CONTENT，但是需要修改"content".equalsIgnoreCase(uri.getScheme())项的转换方式
                    Intent getImage = new Intent(Intent.ACTION_GET_CONTENT);
                    getImage.addCategory(Intent.CATEGORY_OPENABLE);
                    getImage.setType("image/*");
                    startActivityForResult(getImage, PHOTO_SUCCESS);
                }
                */
                //进入相册获取图片关键代码
                if (ContextCompat.checkSelfPermission(MemorandumActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MemorandumActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                } else {
                    Intent getImage = new Intent(Intent.ACTION_GET_CONTENT);
                    getImage.addCategory(Intent.CATEGORY_OPENABLE);
                    getImage.setType("image/*");
                    startActivityForResult(getImage, PHOTO_SUCCESS);
                }
                break;
            case 2: {
                /*
                if (currentId == 0) {
                    showToast("Empty Note....");
                } else {
                    Intent getImageByCamera = new Intent("android.media.action.IMAGE_CAPTURE");
                    startActivityForResult(getImageByCamera, CAMERA_SUCCESS);
                }
                */
                ActivityCompat.requestPermissions(MemorandumActivity.this,
                        new String[]{
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.CAMERA
                        }, 200);
                Intent getImageByCamera = new Intent("android.media.action.IMAGE_CAPTURE");
                startActivityForResult(getImageByCamera, CAMERA_SUCCESS);
                break;
            }
            case 3: {
                if (currentId != 0) {
                    if (data.getStar() == 2) {
                        data.setStar(1);
                        data.update(currentId);
                        currentStar = data.getStar();
                        Log.d(TAG, String.valueOf(currentStar));
                        showToast("取消收藏");
                    } else {
                        data.setStar(2);
                        data.update(currentId);
                        currentStar = data.getStar();
                        Log.d(TAG, String.valueOf(currentStar));
                        showToast("已收藏");
                    }
                } else {
                    showToast("请先创建备忘录再进行收藏");
                }
                break;
            }
            case 4: {
                //选择时间的代码,选完时间之后Activity就会自动调用下面的onTimeSet函数
                if (currentId != 0) {
                    //根据id判断当前是否已经创建新的备忘录
                    timePickerDialog.show(getSupportFragmentManager(), TIMEPICKER_TAG);
                    /**
                     * 在使用的时候，本App先弹出一个datePickerDialog让用户选择提醒的日期，
                     * 再弹出一个timePickerDialog让用户选择提醒的具体时间。
                     * */
                    //显示时间和日期dialog
                    datePickerDialog.setYearRange(1985, 2028);
                    datePickerDialog.show(getSupportFragmentManager(), DATEPICKER_TAG);
                } else {
                    showToast("Empty Note....");
                }
                break;
            }
            case 5: {
                if (currentId != 0) {
                    if (data.getPending() == 2) {
                        data.setPending(1);
                        data.update(currentId);
                        currentPending = data.getPending();
                        Log.d(TAG, String.valueOf(currentPending));
                        showToast("取消待办");
                    } else {
                        data.setPending(2);
                        data.update(currentId);
                        currentPending = data.getPending();
                        Log.d(TAG, String.valueOf(currentPending));
                        showToast("已待办");
                    }
                } else {
                    showToast("请先创建备忘录再进行待办");
                }
                break;
            }
            default:
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intentImage) {
        ContentResolver resolver = getContentResolver();
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PHOTO_SUCCESS:
                    //获得图片的uri
                    Uri originalUri = intentImage.getData();
                    String imagePath = UriToPathUtil.getImageAbsolutePath(this, originalUri);//获取图片地址
                    Log.i(TAG, imagePath);
                    currentId = intent.getIntExtra("id", 0);
                    Log.e("Cur pic id", String.valueOf(currentId));
                    User targetUser = dataDAO.getUserById(currentId);
                    int targetId = 0;
                    if (targetUser != null) {
                        targetId = targetUser.getId();
                        Log.e("This note user id", String.valueOf(targetId));
                    } else {
                        targetId = -1;
                        Log.e("This note user id", "null");
                    }

//                    if (currentId != 0) {
//                        Data newData = new Data(currentId);
//                        newData.setImagePath(imagePath);
//                        newData.update(currentId);
//                        dataDAO.updateImagePath(imagePath, currentId);
//                    } else {
//                        Data newData = new Data(currentId);
//                        newData.setImagePath(imagePath);
//                        newData.update(currentId);
//                        lazyImgPath = imagePath;
//                    }
                    Data newData = new Data(currentId);
                    if (AppGlobal.USERNAME != null && !AppGlobal.USERNAME.equals("")) {
                        //已登录

                    } else {
                        //未登录
                        if (targetId == -1) {
                            newData.setImagePath(imagePath);//更新到数据库
                        } else {
                            Toast.makeText(getContext(), "Do not do bad things !", Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }
                    newData.update(currentId);
                    lazyImgPath = imagePath;
/**
 * 升级了richEditText.setOnTouchListener，添加图片的关键就是使用了富文本编辑框richEditText。
 * 把图片从相册读取到备忘中，需要先获取图片本地存储的地址，然后进行转换，
 * 之后再利用BitmapFactory.decodeStream转为bitmap追加到EditText中。
 * */
                    Bitmap bitmap = null;
                    Bitmap originalBitmap = null;
                    try {
                        assert originalUri != null;
                        originalBitmap = BitmapFactory.decodeStream(resolver.openInputStream(originalUri));
                        int bitmapHeight = originalBitmap.getHeight();
                        int bitmapWidth = originalBitmap.getWidth();
                        double proportion = (double) bitmapHeight / (double) bitmapWidth;
                        int resetWidth = 1500;
                        int resetHeight = (int) (resetWidth * proportion);
                        bitmap = resizeImage(originalBitmap, resetWidth, resetHeight);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    if (originalBitmap != null) {
                        ImageSpan imageSpan = new ImageSpan(MemorandumActivity.this, originalBitmap);
                        spannableString.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                        int index = richEditText.getSelectionStart();
                        Editable edit_text = richEditText.getEditableText();
                        if (index < 0 || index >= edit_text.length()) {
                            edit_text.append(spannableString);
                        } else {
                            edit_text.insert(index, spannableString);
                        }
                        if (TextUtils.isEmpty(richEditText.getText().toString().trim())) {
                            edit_text.append("Say something for the nice pic ?\n");
                        }
                    } else {
                        Toast.makeText(MemorandumActivity.this, "获取图片失败", Toast.LENGTH_SHORT).show();
                    }

                    break;
                case CAMERA_SUCCESS:
                    Bundle extras = intentImage.getExtras();
                    assert extras != null;
                    Bitmap originalPhotoBitmap = (Bitmap) extras.get("data");
                    if (originalPhotoBitmap != null) {
                        String photoPath = BitmapToPathUtil.saveBitmap(this, originalPhotoBitmap);
                        Log.i(TAG, photoPath);
                        currentId = intent.getIntExtra("id", 0);
                        User camTargetUser = dataDAO.getUserById(currentId);
                        int camTargetId = 0;
                        if (camTargetUser != null) {
                            camTargetId = camTargetUser.getId();
                            Log.e("Cur user id", String.valueOf(camTargetId));
                        } else {
                            camTargetId = -1;
                            Log.e("Cur user id", "null");
                        }
//                        if (currentId != 0) {
//                            Data anotherData = new Data(currentId);
//                            anotherData.setImagePath(photoPath);
//                            anotherData.update(currentId);
//                            dataDAO.updateImagePath(photoPath, currentId);
//                        } else {
//                            Data anotherData = new Data(currentId);
//                            anotherData.setImagePath(photoPath);
//                            anotherData.update(currentId);
//                            lazyImgPath = photoPath;
//                        }
                        Data anotherData = new Data(currentId);
                        if (AppGlobal.USERNAME != null && !AppGlobal.USERNAME.equals("")) {

                        } else {
                            //未登录
                            if (camTargetId == -1) {
                                anotherData.setImagePath(photoPath);
                            } else {
                                Toast.makeText(getContext(), "Do not do bad things !", Toast.LENGTH_SHORT).show();
                                break;
                            }
                        }
                        anotherData.update(currentId);
                        lazyImgPath = photoPath;

                        int bitmapHeight = originalPhotoBitmap.getHeight();
                        int bitmapWidth = originalPhotoBitmap.getWidth();
                        double proportion = (double) bitmapHeight / (double) bitmapWidth;
                        int resetWidth = 1500;
                        int resetHeight = (int) (resetWidth * proportion);
                        bitmap = CommonUtility.resizeImage(originalPhotoBitmap, resetWidth, resetHeight);
                        //根据Bitmap对象创建ImageSpan对象
                        ImageSpan imageSpan = new ImageSpan(MemorandumActivity.this, bitmap);
                        //创建一个SpannableString对象，以便插入用ImageSpan对象封装的图像
                        // 用ImageSpan对象替换face
                        spannableString.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        //将选择的图片追加到EditText中光标所在位置
                        int index = richEditText.getSelectionStart();
                        //获取光标所在位置
                        Editable edit_text = richEditText.getEditableText();
                        if (index < 0 || index >= edit_text.length()) {
                            edit_text.append(spannableString);
                        } else {
                            edit_text.insert(index, spannableString);
                        }
                        if (TextUtils.isEmpty(richEditText.getText().toString().trim())) {
                            edit_text.append("Say something for the nice pic ?\n");
                        }
                    } else {
                        Toast.makeText(MemorandumActivity.this, "获取图片失败", Toast.LENGTH_SHORT).show();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public void showToast(String msg) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        mToast.setGravity(Gravity.CENTER, 0, 0);
        mToast.show();
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
        dateSet = String.format("%d-%02d-%02d", year, month + 1, day);
    }

/***
 * 重写onTimeSet方法做初步的合法性判断，之后再在其中调用AlarmService创建一个定时提醒。
 */

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
        timeSetTotal.delete(0, timeSetTotal.length());
        timeSet = (hourOfDay < 10 ? "0" + hourOfDay : hourOfDay) + ":" + (minute < 10 ? "0" + minute : minute);
        timeSetTotal = timeSetTotal.append(dateSet).append(" ").append(timeSet);
        Data data = getIntent().getParcelableExtra("data");
        currentId = intent.getIntExtra("id", 0);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date date;
        long notifyTime = 0;
        long currentTime = System.currentTimeMillis();
        try {
            date = simpleDateFormat.parse(timeSetTotal.toString());
            notifyTime = date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            Log.i(TAG, "日期转换出错");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "其他错误...");
        }
        if (notifyTime <= currentTime) {
            showToast("选择时间不能小于当前时间");
            data.setReminder(1);
            data.update(currentId);
            AlarmService.cleanAllNotification();
            return;
        }
        Log.i(TAG, timeSetTotal.toString());
        Log.i(TAG, "选择时间: " + Integer.toString((int) notifyTime));
        Log.i(TAG, "真实时间: " + Integer.toString((int) currentTime));
        int delayTime = (int) (notifyTime - currentTime);
        currentImagePath = intent.getStringExtra("imagePath");

        AlarmService.addNotification(delayTime, "tick", "您有一条新提醒", data.getContent(), data.getDate(), data.getId(), currentImagePath);
        data.setReminder(2);
        data.update(currentId);

        showToast("已提醒");
    }

    @Override
    protected void onDestroy() {
        //取消注册
        super.onDestroy();
        unregisterReceiver(mybroad);
    }
}
