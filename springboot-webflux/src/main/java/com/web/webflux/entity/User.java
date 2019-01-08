package com.web.webflux.entity;

/**
 * @ClassName User
 * @Description 用户实体类
 * @Author Neal
 * @Date 2019/1/8 9:55
 * @Version 1.0
 */
public class User {

    //用户ID
    private int userId;

    //用户姓名
    private String userName;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

}
