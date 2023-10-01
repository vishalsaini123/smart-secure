package com.smartsecureapp.Activity.model;

import com.google.gson.annotations.SerializedName;

public class SendOtpModel{

	@SerializedName("msg")
	private String msg;

	@SerializedName("alert")
	private String alert;

	@SerializedName("error")
	private boolean error;

	public String getMsg(){
		return msg;
	}

	public String getAlert(){
		return alert;
	}

	public boolean isError(){
		return error;
	}
}