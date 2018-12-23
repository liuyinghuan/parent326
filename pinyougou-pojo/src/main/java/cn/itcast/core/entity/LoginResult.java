package cn.itcast.core.entity;

import java.io.Serializable;

/**
 * 登录结果
 */
public class LoginResult implements Serializable{

    private boolean success;//是否登录成功
    private String loginname;//登录成功的用户名
    private Object data;//返回的结果

    public LoginResult(boolean success, String loginname, Object data) {
        this.success = success;
        this.loginname = loginname;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getLoginname() {
        return loginname;
    }

    public void setLoginname(String loginname) {
        this.loginname = loginname;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
