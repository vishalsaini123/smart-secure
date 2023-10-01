package com.smartsecureapp.Activity.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.chaos.view.PinView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.smartsecureapp.Activity.api.APIInterface;
import com.smartsecureapp.Activity.api.RetrofitClient;
import com.smartsecureapp.Activity.model.SendOtpModel;
import com.smartsecureapp.Activity.model.UpdateProfile;
import com.smartsecureapp.Activity.util.Utils;
import com.smartsecureapp.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OtpDeleteAccountActivity extends AppCompatActivity {

    ImageView img_back;
    PinView otp;
    MaterialButton submitButton;
    ProgressBar loading;
    String email,password;
    APIInterface apiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);
        img_back = findViewById(R.id.img_back);
        otp = findViewById(R.id.otp);
        submitButton = findViewById(R.id.submitButton);
        loading = findViewById(R.id.loading);
        if (getIntent()!=null && getIntent().getStringExtra("email")!=null && !getIntent().getStringExtra("email").isEmpty()) {
            email = getIntent().getStringExtra("email");
        }
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!otp.getText().toString().isEmpty()){
                    otpApi();
                }else {
                    showSnackBar("Please enter OTP.");
                }
            }
        });
        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void otpApi(){
        loading.setVisibility(View.VISIBLE);
        hideKeyboard();
        apiInterface = RetrofitClient.getClient().create(APIInterface.class);
        Call<SendOtpModel> call = apiInterface.verify_otp_email(getLoginApiFromShared(Utils.MySharedId), Utils.verify_otp_email,otp.getText().toString().trim());
        call.enqueue(new Callback<SendOtpModel>() {
            @Override
            public void onResponse(Call<SendOtpModel> call, Response<SendOtpModel> response) {
                loading.setVisibility(View.GONE);
                if (response.body() != null && !response.body().isError()){
                    deleteAccountApi();
                }else {
                    showSnackBar(response.body().getMsg());
                }
            }

            @Override
            public void onFailure(Call<SendOtpModel> call, Throwable t) {
                loading.setVisibility(View.GONE);
            }
        });
    }
    private void signOutFromShared() {
        SharedPreferences sharedPreferences = getSharedPreferences(Utils.MyPref, MODE_PRIVATE);
        sharedPreferences.edit().clear().commit();
    }

    private void deleteAccountApi(){
        loading.setVisibility(View.VISIBLE);
        apiInterface = RetrofitClient.getClient().create(APIInterface.class);
        Call<UpdateProfile> callDeleteAccount = apiInterface.deleteUser(Utils.deleteUser,getLoginApiFromShared(Utils.MySharedId));
        callDeleteAccount.enqueue(new Callback<UpdateProfile>() {
            @Override
            public void onResponse(Call<UpdateProfile> call, Response<UpdateProfile> response) {
                loading.setVisibility(View.GONE);
                if (response != null && response.body() != null && !response.body().getError()) {
                    signOutFromShared();
                    startActivity(new Intent( OtpDeleteAccountActivity.this,SignUpActivity.class));
                    finishAffinity();
                } else {
                    showSnackBar(response.body().getAlert());
                }
            }

            @Override
            public void onFailure(Call<UpdateProfile> call, Throwable t) {
                loading.setVisibility(View.INVISIBLE);
            }
        });
    }


    private String getLoginApiFromShared(String key) {
        SharedPreferences sharedPreferences = getSharedPreferences(Utils.MyPref, MODE_PRIVATE);
        String value = sharedPreferences.getString(key, "");
        return value;
    }

    private void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            Log.d("+++Exception+++",String.valueOf(e));
        }
    }
    private void showSnackBar(String msg) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG);
        snackbar.setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        snackbar.dismiss();
                    }
                })
                .setActionTextColor(getResources().getColor(R.color.btn_red));
        snackbar.show();
    }
}