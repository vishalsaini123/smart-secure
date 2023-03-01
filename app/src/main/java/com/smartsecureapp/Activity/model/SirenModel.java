package com.smartsecureapp.Activity.model;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SirenModel {
    @SerializedName("msg")
    @Expose
    private String msg;
    @SerializedName("error")
    @Expose
    private Boolean error;
    @SerializedName("siren")
    @Expose
    private String siren;
    @SerializedName("call_preference")
    @Expose
    private String callPreference;

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

    public String getSiren() {
        return siren;
    }

    public void setSiren(String siren) {
        this.siren = siren;
    }

    public String getCallPreference() {
        return callPreference;
    }

    public void setCallPreference(String callPreference) {
        this.callPreference = callPreference;
    }
}
