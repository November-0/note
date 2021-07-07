package com.example.memorandum.activity;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.memorandum.R;
import com.example.memorandum.dao.DataDAO;
import com.example.memorandum.dao.UserDAO;
import com.example.memorandum.ui.HTAlertDialog;
import com.example.memorandum.util.AppGlobal;
import com.example.memorandum.util.CommonUtility;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import static com.example.memorandum.util.UriToPathUtil.getDataColumn;
import static com.example.memorandum.util.UriToPathUtil.isDownloadsDocument;
import static com.example.memorandum.util.UriToPathUtil.isExternalStorageDocument;
//修改个人信息页面
public class ProfileActivity extends RegisterActivity implements View.OnClickListener {
    private RelativeLayout re_fxid;
    private ImageView iv_avatar;
    private TextView tv_name;
    private String sex;
    public static final int TAKE_PHOTO = 1;
    public static final int CHOOSE_PHOTO = 2;
    private Uri head_imageUri;
    private final Activity activity = this;
    UserDAO userDAO = new UserDAO();
    DataDAO dataDAO = new DataDAO();
    Intent intent;

    public ProfileActivity() {
    }
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

        setContentView(R.layout.activity_profile);
        setTitle("Information");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolkit);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.back);
        }

        iv_avatar = (ImageView) findViewById(R.id.iv_avatar);
        tv_name = (TextView) findViewById(R.id.tv_name);
        //    private TextView tv_fxid;
        TextView tv_memo = (TextView) findViewById(R.id.tv_memo);
        TextView tv_star = (TextView) findViewById(R.id.tv_star);
        TextView tv_pending = (TextView) findViewById(R.id.tv_pending);
        TextView memo_num = (TextView) findViewById(R.id.memo_num);
        TextView pending_num = (TextView) findViewById(R.id.pending_num);
        TextView reminder_num = (TextView) findViewById(R.id.reminder_num);
        TextView star_num = (TextView) findViewById(R.id.star_num);
        TextView image_num = (TextView) findViewById(R.id.image_num);
        if (AppGlobal.NAME != null && !AppGlobal.NAME.equals("")) {
            tv_name.setText(AppGlobal.NAME);
        }

        //获取控件
        intent = getIntent();
        String currentImagePath = intent.getStringExtra("imagePath");
        Glide.with(getContext()).load(currentImagePath).into(iv_avatar);
        RelativeLayout re_avatar = (RelativeLayout) findViewById(R.id.re_avatar);
        RelativeLayout re_name = (RelativeLayout) findViewById(R.id.re_name);
//        re_fxid = (RelativeLayout) findViewById(R.id.re_fxid);
        RelativeLayout re_memo = (RelativeLayout) findViewById(R.id.re_memo);
        RelativeLayout re_pending = (RelativeLayout) findViewById(R.id.re_pending);
        RelativeLayout re_star = (RelativeLayout) findViewById(R.id.re_star);
        RelativeLayout re_qrcode = (RelativeLayout) findViewById(R.id.re_qrcode);

        re_avatar.setOnClickListener(this);
        re_name.setOnClickListener(this);
//        re_fxid.setOnClickListener(this);
        re_memo.setOnClickListener(this);
        re_pending.setOnClickListener(this);
        re_star.setOnClickListener(this);
        re_qrcode.setOnClickListener(this);

        Context sContext = this;
        int userId = userDAO.findUserId(AppGlobal.USERNAME);
        int memoCount = dataDAO.memoCount(userId);
        int pendingCount = dataDAO.pendingCount(userId);
        int reminderCount = dataDAO.reminderCount(userId);
        int favoriteCount = dataDAO.favoriteCount(userId);
        int imageCount = dataDAO.imageCount(userId);
        memo_num.setText(Integer.toString(memoCount));
        pending_num.setText(Integer.toString(pendingCount));
        reminder_num.setText(Integer.toString(reminderCount));
        star_num.setText(Integer.toString(favoriteCount));
        image_num.setText(Integer.toString(imageCount));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.re_avatar:
                showCamera();
                break;
            case R.id.re_name:

                final EditText et = new EditText(this);
                et.setText(AppGlobal.NAME);
                AlertDialog alertDialog = new AlertDialog.Builder(this)
                        .setTitle("Name change?")
//                      .setIcon(android.R.drawable.ic_dialog_info)
                        .setView(et)
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String input = et.getText().toString();
                                if (input.equals("")) {
                                    Toast.makeText(getApplicationContext(), "Empty name..." + input, Toast.LENGTH_LONG).show();
                                } else {
                                    userDAO.resetNickname(AppGlobal.USERNAME, input);
                                    AppGlobal.NAME = input;
                                    tv_name.setText(AppGlobal.NAME);
                                }
                            }
                        })
                        .setNegativeButton("Cancel", null).create();
//                      alertDialog.getButton(alertDialog.BUTTON_NEGATIVE).setLeft(0);
                alertDialog.show();

                break;
//            case R.id.re_fxid:
//                if (TextUtils.isEmpty(userJson.getString(HTConstant.JSON_KEY_FXID))) {
//                    startActivity(new Intent(getActivity(), ProfileUpdateActivity.class)
//                            .putExtra("type", ProfileUpdateActivity.TYPE_FXID));
//                }
//                break;
            case R.id.re_memo:
                break;
            case R.id.re_pending:
                break;
            case R.id.re_star:
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                Intent it = new Intent();
                it.setClass(ProfileActivity.this, MainActivity.class);
                it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(it);
                finish();
        }
        return true;
    }

    // 拍照
    private void showCamera() {
        //弹窗
        HTAlertDialog HTAlertDialog = new HTAlertDialog(getApplicationContext(), null, new String[]{getString(R.string.attach_take_pic), getString(R.string.image_manager)});
        HTAlertDialog.init(new HTAlertDialog.OnItemClickListner() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(int position) {
                switch (position) {
                    case 0:
                        File outputImage = new File(getExternalCacheDir(), UUID.randomUUID().toString() + ".jpg");
                        AppGlobal.currentImagePath = outputImage.getAbsolutePath();
//                        Log.d("RegisterActivity", outputImagePath);
//                        Toast.makeText(RegisterActivity.this, outputImagePath, Toast.LENGTH_SHORT).show();

                        try {
                            if (outputImage.exists()) {
                                outputImage.delete();
                            }
                            outputImage.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (Build.VERSION.SDK_INT >= 24) {
                            //在拿到uri之后进行版本判断大于等于24（即Android7.0）用最新的获取路径方式，否则用之前的方式
                            head_imageUri = FileProvider.getUriForFile(ProfileActivity.this, "com.exmaple.memorandum.fileprovider", outputImage);
                        } else {
                            head_imageUri = Uri.fromFile(outputImage);
                        }
                        ActivityCompat.requestPermissions(ProfileActivity.this,
                                new String[]{
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                        Manifest.permission.CAMERA
                                }, 200);
                        openCamera();
                        break;
//
                    case 1:
                        if (ContextCompat.checkSelfPermission(ProfileActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(ProfileActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                        } else {
                            openAlbum();
                        }
                        break;
//
                }
            }
        });
    }

    private void openCamera() {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, head_imageUri);
        startActivityForResult(intent, TAKE_PHOTO);
    }

    private void openAlbum() {
        // Intent getImage = new Intent(Intent.ACTION_GET_CONTENT);
        // 调用相册
        // 4.41之后不可行，改为ACTION_PICK
        // 0608: 改为ACTION_PICK之后路径会变成google的路径（如果安装了google商店）
        // 继续使用ACTION_GET_CONTENT，但是需要修改"content".equalsIgnoreCase(uri.getScheme())项的转换方式
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();
                    openCamera();
                } else {
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    try {
                        Bitmap originalPhotoBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(head_imageUri));

//                        int bitmapHeight = originalPhotoBitmap.getHeight();
//                        int bitmapWidth = originalPhotoBitmap.getWidth();
//                        double proportion = (double) bitmapHeight / (double) bitmapWidth;
//                        int resetWidth = 2000;
//                        int resetHeight = (int) (resetWidth * proportion);

                        Bitmap bitmap = CommonUtility.resizeImage(originalPhotoBitmap, 200, 200);

                        iv_avatar.setImageBitmap(bitmap);
                        // AppGlobal.INSERT_IMAGE = true;
                        userDAO.updateImagePath(AppGlobal.currentImagePath, AppGlobal.USERNAME);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    if (Build.VERSION.SDK_INT >= 19) {
                        Log.d("V", "high");
                        handleImageOnKitKat(data);
                    } else {
                        Log.d("V", "low");
                        handleImageBeforeKitKat(data);
                    }
                }
                break;
            default:
                break;
        }
    }

    private void handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        displayImage(imagePath);
    }

    /**
     * Google相册图片获取路径
     **/
    private static Uri getImageUrlWithAuthority(Context context, Uri uri) {
        InputStream is = null;

        if (uri.getAuthority() != null) {
            try {
                is = context.getContentResolver().openInputStream(uri);
                Bitmap bmp = BitmapFactory.decodeStream(is);
                //return writeToTempImageAndGetPathUri(context, bmp).toString();
                return writeToTempImageAndGetPathUri(context, bmp);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private static Uri writeToTempImageAndGetPathUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
        // 有bug 会额外生成一张略缩图到相册
//        ContentValues values = new ContentValues();
//        values.put(MediaStore.Images.Media.DATA, String.valueOf(inImage));
//        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
//        Uri ans = inContext.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
//        Log.d("R0 Insert", String.valueOf(ans)) ;
//        return ans ;
//        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//        intent.setData(ans);
//        inContext.sendBroadcast(intent);
//        return ans ;
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void handleImageOnKitKat(@NonNull Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        Log.d("URI ANS", String.valueOf(uri));
        Log.d("URI ANS", String.valueOf(DocumentsContract.isDocumentUri(this, uri)));
        if (DocumentsContract.isDocumentUri(this, uri)) {
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://download/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            } else if ("content".equalsIgnoreCase(uri.getScheme())) {
                // imagePath = getImagePath(uri, null);
                if (isExternalStorageDocument(uri)) {
//                Log.i(TAG,"isExternalStorageDocument***"+uri.toString());
//                Log.i(TAG,"docId***"+docId);
//                Test print：
//                isExternalStorageDocument***content://com.android.externalstorage.documents/document/primary%3ATset%2FROC2018421103253.wav
//                docId***primary:Test/ROC2018421103253.wav
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    if ("primary".equalsIgnoreCase(type)) {
                        imagePath = Environment.getExternalStorageDirectory() + "/" + split[1];
                    }
                }
            } else if (isDownloadsDocument(uri)) {
//                Log.i(TAG,"isDownloadsDocument***"+uri.toString());
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                imagePath = getDataColumn(this, contentUri, null, null);
            } else if ("file".equalsIgnoreCase(uri.getScheme())) {
                imagePath = uri.getPath();
            }
            Log.d("IMAGE ANS", imagePath);
            displayImage(imagePath);
        } else {
            if ("com.google.android.apps.photos.contentprovider".equals(uri.getAuthority())) {
                //imagePath = uri.getLastPathSegment();
                uri = getImageUrlWithAuthority(this, uri);
                imagePath = getImagePath(uri, null);
                Log.d("IMAGE ANS", imagePath);
                displayImage(imagePath);
            } else {
                Log.d("IMAGE ANS", "WTF");
            }
        }
    }

//    public boolean isExternalStorageDocument(Uri uri) {
//        return "com.android.externalstorage.documents".equals(uri.getAuthority());
//    }

//    public boolean isDownloadsDocument(Uri uri) {
//        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
//    }

//    public boolean isMediaDocument(Uri uri) {
//        return "com.android.providers.media.documents".equals(uri.getAuthority());
//    }

    private void displayImage(String imagePath) {
        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            File albumImage = new File(getExternalCacheDir(), UUID.randomUUID().toString() + ".jpg");
            String albumImagePath = albumImage.getAbsolutePath();
            AppGlobal.currentImagePath = albumImagePath;
            Log.d("RegisterActivity", albumImagePath);
            Toast.makeText(this, albumImagePath, Toast.LENGTH_SHORT).show();
            try {
                if (albumImage.exists()) {
                    albumImage.delete();
                }
                FileOutputStream out;
                out = new FileOutputStream(albumImage);
                if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)) {
                    out.flush();
                    out.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            iv_avatar.setImageBitmap(bitmap);
            userDAO.updateImagePath(AppGlobal.currentImagePath, AppGlobal.USERNAME);
        } else {
            Toast.makeText(this, "failed to get image", Toast.LENGTH_SHORT).show();
        }
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                /**
                 * 这种方法调用图库并获取图片在android4.4版本之前是可行的，但是之后就不可行了
                 * 根本原因是前者和后者返回的URI已经不是同一个了，前者URI中包含了文件的绝对路径，是有_data的，但是后者URI就没有了。所以拿不到
                 * <4.4 URI:content://media/external/images/media/164 含有文件的绝对路径
                 *
                 * >4.4URI ：content://com.android.providers.media.documents/document/image:3951，只有文件的相对编号
                 *
                 * 两者返回的内容也有所不同
                 * 原文链接：https://blog.csdn.net/qq_32534441/article/details/103526406
                 */
                //path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        Log.d("IMAGE", path);
        return path;
    }

    public Activity getActivity() {
        return activity;
    }
    @Override
    protected void onDestroy() {
        //取消
        super.onDestroy();
        unregisterReceiver(mybroad);
    }

}
