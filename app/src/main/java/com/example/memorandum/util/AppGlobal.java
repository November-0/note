package com.example.memorandum.util;


/**
 * 全局信息类
 * 用来标识
 */

public class AppGlobal {
    public static String USERNAME="";
    //当前登录用户用户名
    public static String NAME="";
    //当前登录用户昵称
    public static boolean INSERT_IMAGE = false;
    //当前是否插入头像
    public static String currentImagePath = "";
    public static String THEME = "normal" ;
    public static void main(String[] args) {
        System.out.println(USERNAME);
    }
}
