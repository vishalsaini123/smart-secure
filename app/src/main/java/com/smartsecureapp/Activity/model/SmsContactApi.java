package com.smartsecureapp.Activity.model;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SmsContactApi {
    @SerializedName("msg")
    @Expose
    private String msg;
    @SerializedName("error")
    @Expose
    private Boolean error;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Boolean getError() {
        return error;
    }

    public void setError(Boolean error) {
        this.error = error;
    }

}
