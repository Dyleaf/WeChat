package com.dyleaf.bean;

import java.io.Serializable;

/**
 *@Author com.dyleaf [com.dyleaf.github.io]
 *@ceeate 2017/11/30
 * @description  the user model in com.dyleaf.Client
 */
public class ClientUser implements Serializable{
    /**
     * define username as userID
     */
    private String userName;
    /**
     * user' status outLine, inLine
     */
    private String status;

    private boolean notify;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isNotify() {
        return notify;
    }

    public void setNotify(boolean notify) {
        this.notify = notify;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
