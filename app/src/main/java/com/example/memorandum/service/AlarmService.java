package com.example.memorandum.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.example.memorandum.R;
import com.example.memorandum.activity.MemorandumActivity;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 基于Service类、Timer类以及NotificationManager，本App实现了一个可以定时提醒并弹窗的AlarmService类。
 * */
public class AlarmService extends Service {
    static Timer timer = null;

    // 清除通知
    public static void cleanAllNotification() {
        NotificationManager mn = (NotificationManager) MemorandumActivity
                .getContext().getSystemService(NOTIFICATION_SERVICE);
        assert mn != null;
        mn.cancelAll();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    // 添加通知
    public static void addNotification(int delayTime, String tickerText,
                                       String contentTitle, String contentText, String currentDate, int currentId, String currentImagePath) {
        Intent intent = new Intent(MemorandumActivity.getContext(),
                AlarmService.class);
        intent.putExtra("delayTime", delayTime);
        intent.putExtra("tickerText", tickerText);
        intent.putExtra("contentTitle", contentTitle);
        intent.putExtra("content", contentText);
        intent.putExtra("date", currentDate);
        intent.putExtra("id", currentId);
        intent.putExtra("imagePath", currentImagePath);
        intent.putExtra("reminder", 1);
        MemorandumActivity.getContext().startService(intent);
    }

    public void onCreate() {
        Log.e("addNotification", "===========create===========");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

//    private String createNotificationChannel(String channelID, String channelNAME, int level) {
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            //如果api大于android8.0就创建渠道返回渠道id，否则返回空。
//            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//            NotificationChannel channel = new NotificationChannel(channelID, channelNAME, level);
//            manager.createNotificationChannel(channel);
//            return channelID;
//        } else {
//            return null;
//        }
//    }

    /**
     * 此service在实现时最关键的还有重写onStartCommand方法，在这里使用了一个timer定时器并开启一个新任务，
     * 在创建时将需要弹出的通知信息以及其他需要执行的任务加入到任务的“run”中。
     * */
    public int onStartCommand(final Intent intent, int flags, int startId) {

        long period = 24 * 60 * 60 * 1000;
        // 24小时一个周期
        int delay = intent.getIntExtra("delayTime", 0);
        if (null == timer) {
            timer = new Timer();
        }
        //弹窗通知设置
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//                AlarmService.this.
                Intent notificationIntent = new Intent();
                notificationIntent.setClass(AlarmService.this, MemorandumActivity.class);
                notificationIntent.putExtra("id", intent.getIntExtra("id", 0));
                notificationIntent.putExtra("date", intent.getStringExtra("date"));
                notificationIntent.putExtra("content", intent.getStringExtra("content"));
                notificationIntent.putExtra("imagePath", intent.getStringExtra("imagePath"));
                notificationIntent.putExtra("reminder", 1);
                //增加一位以调整reminder状态
//                AlarmService.this.startActivity(notificationIntent);
                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                Log.i("IMG", notificationIntent.getStringExtra("imagePath"));
                PendingIntent contentIntent = PendingIntent.getActivity(
                        AlarmService.this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//                String channelId = createNotificationChannel("my_channel_ID", "my_channel_NAME", NotificationManager.IMPORTANCE_HIGH);
                Notification notification = new NotificationCompat.Builder(AlarmService.this)
                        .setContentIntent(contentIntent)
                        .setContentTitle(intent.getStringExtra("contentTitle"))
                        .setContentText(intent.getStringExtra("content"))
                        .setTicker(intent.getStringExtra("tickerText"))
                        .setSmallIcon(R.drawable.back)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.food))
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setVibrate(new long[]{0, 2000, 1000, 4000})
                        .build();
                notification.flags = Notification.FLAG_INSISTENT;
                assert notificationManager != null;
                notificationManager.notify((int) System.currentTimeMillis(), notification);
            }
        }, delay, period);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
