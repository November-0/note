package com.example.memorandum.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatEditText;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.util.Log;

import com.example.memorandum.util.CommonUtility;

/**
 * Created by Boogie on 2021/06/06.
 */


public class RichEditText extends AppCompatEditText {
    public RichEditText(Context context) {
        super(context);
    }

    public RichEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void insertDrawable(int id) {
        final SpannableString ss = new SpannableString("easy");
        //得到drawable对象，即所有插入的图片
        Drawable d = getResources().getDrawable(id);
        d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
        //用这个drawable对象代替字符串easy
        ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BASELINE);
        //包括0但是不包括"easy".length()即：4。[0,4)
        ss.setSpan(span, 0, "easy".length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        append(ss);
    }

    public void insertBitmap(String path) {
        final SpannableString ss = new SpannableString(" ");
//        Drawable d = getResources().getDrawable(id);
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        // Resetting the width and height according to the proportion
        int bitmapHeight = bitmap.getHeight();
        int bitmapWidth = bitmap.getWidth();
        double proportion = (double)bitmapHeight / (double)bitmapWidth;
        int resetWidth = 1500;
        int resetHeight = (int) (resetWidth * proportion);
        Bitmap newBitmap = CommonUtility.resizeImage(bitmap, resetWidth, resetHeight);
        newBitmap = bitmapCombine(newBitmap, 20, 20,0x81ecec);
        Drawable d = new BitmapDrawable(newBitmap);
        d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
        //用这个drawable对象代替字符串easy
        ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BASELINE);
        //包括0但是不包括"easy".length()即：4。[0,4)
        ss.setSpan(span, 0, 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        append(ss);
    }
    /**
     * 获得添加边框了的Bitmap
     * @param bm 原始图片Bitmap
     * @param smallW 一条边框宽度
     * @param smallH 一条边框高度
     * @param color 边框颜色值
     * @return Bitmap 添加边框了的Bitmap
     */
    private Bitmap bitmapCombine(Bitmap bm, int smallW, int smallH,int color) {
        //防止空指针异常
        if(bm==null){
            return null;
        }
        Log.d("BITMAP", "add border") ;
        // 原图片的宽高
        final int bigW = bm.getWidth();
        final int bigH = bm.getHeight();

        // 重新定义大小
        int newW = bigW+smallW*2;
        int newH = bigH+smallH*2;

        // 绘原图
        Bitmap newBitmap = Bitmap.createBitmap(newW, newH, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        Paint p = new Paint();
        p.setColor(color);
        canvas.drawRect(new Rect(0, 0, newW, newH), p);

        // 绘边框
        canvas.drawBitmap(bm,
                    (newW - bigW - 2 * smallW) / 2 + smallW,
                    (newH - bigH - 2 * smallH) / 2 + smallH, null);


        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();

        return newBitmap;
    }

}
