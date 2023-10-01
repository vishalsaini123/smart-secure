package com.smartsecureapp.Activity.model;

import com.google.gson.annotations.SerializedName;

public class SendCallNotification{

	@SerializedName("msg")
	private String msg;

	@SerializedName("error")
	private boolean error;

	public String getMsg(){
		return msg;
	}

	public boolean isError(){
		return error;
	}
}