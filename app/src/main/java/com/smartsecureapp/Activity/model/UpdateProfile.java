package com.smartsecureapp.Activity.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UpdateProfile {
    @SerializedName("msg")
    @Expose
    private String msg;
    @SerializedName("alert")
    @Expose
    private String alert;
    @SerializedName("error")
    @Expose
    private Boolean error;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getAlert() {
        return alert;
    }

    public void setAlert(String alert) {
        this.alert = alert;
    }

    public Boolean getError() {
        return error;
    }

    public void setError(Boolean error) {
        this.error = error;
    }
}
