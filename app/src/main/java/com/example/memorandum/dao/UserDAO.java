package com.example.memorandum.dao;

import com.example.memorandum.bean.User;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;


public class UserDAO {
    private static List<User> userList = new ArrayList<>();



    public List<User> getData() {
        userList = DataSupport.findAll(User.class);
        return userList ;
    }

    public boolean checkUsername(String userName) {
        userList = DataSupport.select("userName").find(User.class);
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            if (userName.equals(user.getUserName())) {
                return true;
            }
        }
        return false;
    }

    public boolean insert(User user) {
        if (checkUsername(user.getUserName())) {
            return false;
        }
        try {
            userList.add(user);
            User newUser = new User();
            newUser.setUserName(user.getUserName());
            newUser.setPassword(user.getPassword());
            newUser.setNickName(user.getNickName());
            newUser.save();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String findNameByUsername(String userName) {
        userList = DataSupport.select("userName", "nickName").find(User.class);
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            if (userName.equals(user.getUserName())) {
                return user.getNickName();
            }
        }
        return "";
    }

    public boolean checkLogin(String userName, String password) {
        userList = DataSupport.select("userName", "password").find(User.class);
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            if (userName.equals(user.getUserName()) && password.equals(user.getPassword())) {
                return true;
            }
        }
        return false;
    }

    public boolean checkPassword(String userName, String password) {
        userList = DataSupport.select("userName", "password").where("userName = ?", userName).find(User.class);
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            if (!password.equals(user.getPassword())) {
                return true;
            }
        }
        return false;
    }

    public void resetPassword(String userName, String password) {
        userList = DataSupport.select("userName", "password").where("userName = ?", userName).find(User.class);
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            user.setPassword(password);
            user.updateAll("userName = ?", userName);
        }
    }

    public void resetNickname(String userName, String nickName) {
        userList = DataSupport.select("userName", "nickName").where("userName = ?", userName).find(User.class);
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            user.setNickName(nickName);
            user.updateAll("userName = ?", userName);
        }
    }

    public void updateImagePath(String imagePath, String userName) {
        User newUser = new User();
        newUser.setImagePath(imagePath);
        newUser.updateAll("userName = ?", userName);
    }

    public static String findImagePath(String userName) {
        userList = DataSupport.select("userName", "imagePath").find(User.class);
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            if (userName.equals(user.getUserName())) {
                return user.getImagePath();
            }
        }
        return "";
    }

    public static User findUser(String userName) {
        userList = DataSupport.select("userName").find(User.class);
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            if (userName.equals(user.getUserName())) {
                return user;
            }
        }
        return null;
    }

    public static int findUserId(String userName) {
        userList = DataSupport.select("id", "userName").find(User.class);
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            if (userName.equals(user.getUserName())) {
                return user.getId();
            }
        }
        return 0;
    }


}
