package com.example.memorandum.bean;

import android.util.Log;

import org.litepal.annotation.Column;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;



public class User extends DataSupport{
    private int id;
    @Column(nullable = false)
    private String userName;
    //唯一标识用户 token
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private String nickName;
    //昵称可重复
    private String imagePath;
    private List<Data> dataList = new ArrayList<>();
    //用户的对应一个或多个data项的外键集合

    public User() {

    }
    public User(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }
    public User(String userName, String password, String nickName) {
        this.userName = userName;
        this.password = password;
        this.nickName = nickName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public List<Data> getDataList() {
        return dataList;
    }

    public void setDataList(List<Data> dataList) {
        this.dataList = dataList;
    }
}
