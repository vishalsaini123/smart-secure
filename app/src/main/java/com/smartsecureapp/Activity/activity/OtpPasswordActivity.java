package com.smartsecureapp.Activity.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.smartsecureapp.Activity.api.APIInterface;
import com.smartsecureapp.Activity.api.RetrofitClient;
import com.smartsecureapp.Activity.model.LoginApi;
import com.smartsecureapp.Activity.model.SmsContactApi;
import com.smartsecureapp.Activity.util.Utils;
import com.smartsecureapp.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OtpPasswordActivity extends AppCompatActivity {

    ImageView img_back;
    EditText otp;
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
        Call<SmsContactApi> call = apiInterface.forgot_password_second(email, Utils.forgot_password_second,otp.getText().toString());
        call.enqueue(new Callback<SmsContactApi>() {
            @Override
            public void onResponse(Call<SmsContactApi> call, Response<SmsContactApi> response) {
                loading.setVisibility(View.INVISIBLE);
                if (response.body().getError() != null && !response.body().getError()){
                    Intent intent = new Intent(OtpPasswordActivity.this,PasswordResetActivityOtp.class);
                    intent.putExtra("email",email);
                    startActivity(intent);
                }else {
                    showSnackBar(response.body().getMsg());
                }
            }

            @Override
            public void onFailure(Call<SmsContactApi> call, Throwable t) {
                loading.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void loginApiCall() {
        loading.setVisibility(View.VISIBLE);
        hideKeyboard();
        apiInterface = RetrofitClient.getClient().create(APIInterface.class);
        Call<LoginApi> call = apiInterface.login(email,Utils.login,password);
        call.enqueue(new Callback<LoginApi>() {
            @Override
            public void onResponse(Call<LoginApi> call, Response<LoginApi> response) {
                if (response.body().getError() != null && !response.body().getError()){
                    loading.setVisibility(View.INVISIBLE);
                    setLoginApiToShared(response,password.toString());
                    loggedIn();
                }else {
                    loading.setVisibility(View.INVISIBLE);
                    showSnackBar(response.body().getMsg());
                }
            }

            @Override
            public void onFailure(Call<LoginApi> call, Throwable t) {
                loading.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void loggedIn() {
        startActivity(new Intent(OtpPasswordActivity.this, MainActivity.class));
    }

    private void setLoginApiToShared(Response<LoginApi> response, String passwordString) {
        if (response != null && response.body()!=null) {
            SharedPreferences sharedPreferences = getSharedPreferences(Utils.MyPref, MODE_PRIVATE);
            SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
            prefsEditor.putString(Utils.MySharedId, response.body().getItem() != null ? response.body().getItem().getId() : "");
            prefsEditor.putString(Utils.MySharedUsername, response.body().getItem() != null ? response.body().getItem().getUsername() : "");
            prefsEditor.putString(Utils.MySharedEmail, response.body().getItem() != null ? response.body().getItem().getEmail() : "");
            prefsEditor.putString(Utils.MySharedPhone, response.body().getItem() != null ? response.body().getItem().getPhone() : "");
            prefsEditor.putString(Utils.MySharedPassword, passwordString);
            prefsEditor.apply();
        }
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