package com.example.memorandum.util;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import com.example.memorandum.bean.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;



public class CommonUtility {

    //static List<Data> dataList = new ArrayList<>();

    //重新定义图片大小
    public static Bitmap resizeImage(Bitmap originalBitmap, int newWidth, int newHeight) {
        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();
        float scanleWidth = (float) newWidth / width;
        float scanleHeight = (float) newHeight / height;
        //用矩阵存图片，矩阵几维图片几维，重新设定一个二维图的大小
        Matrix matrix = new Matrix();
        matrix.postScale(scanleWidth, scanleHeight);
        return Bitmap.createBitmap(originalBitmap, 0, 0, width, height, matrix, true);
    }

    //生成随机数
    public static String getRandNum(int charCount) {
        StringBuilder charValue = new StringBuilder();
        for (int i = 0; i < charCount; i++) {
            char c = (char) (randomInt(0, 10) + '0');
            charValue.append(String.valueOf(c));
        }
        return charValue.toString();
    }

    public static int randomInt(int from, int to) {
        Random r = new Random();
        return from + r.nextInt(to - from);
    }
}
