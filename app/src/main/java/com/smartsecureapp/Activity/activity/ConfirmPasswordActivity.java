package com.smartsecureapp.Activity.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.smartsecureapp.Activity.api.APIInterface;
import com.smartsecureapp.Activity.api.RetrofitClient;
import com.smartsecureapp.Activity.model.LoginApi;
import com.smartsecureapp.Activity.util.Utils;
import com.smartsecureapp.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConfirmPasswordActivity extends AppCompatActivity {

    TextInputEditText email, password;
    TextView txt_privacy_policy, txt_term_condition, txt_create_account,forgotPassword;
    ProgressBar loading;
    MaterialButton btn_login;
    APIInterface apiInterface;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset_otp);

        if (getLoginApiFromShared(Utils.MySharedId)!= null && !getLoginApiFromShared(Utils.MySharedId).isEmpty()){
            loggedIn();
        }

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);

        txt_privacy_policy = findViewById(R.id.txt_privacy_policy);
        txt_term_condition = findViewById(R.id.txt_term_condition);
        txt_term_condition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Utils.term_and_conditions));
                startActivity(browserIntent);
            }
        });
        btn_login = findViewById(R.id.btn_login);
        txt_create_account = findViewById(R.id.txt_create_account);
        loading = findViewById(R.id.loading);
        forgotPassword = findViewById(R.id.forgotPassword);
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ConfirmPasswordActivity.this,PasswordResetActivity.class));
            }
        });

        txt_privacy_policy.setPaintFlags(txt_privacy_policy.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        txt_term_condition.setPaintFlags(txt_term_condition.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (email.getText()!= null && !email.getText().toString().isEmpty() && password.getText()!=null && !password.getText().toString().isEmpty()) {
                    loginApiCall();
                } else {
                    if (email.getText()!= null && email.getText().toString().isEmpty()) {
                        email.setError("Email cannot be empty");
                    } else if (password.getText()!=null && password.getText().toString().isEmpty()) {
                        password.setError("Password cannot be empty");
                    }
                }
            }
        });

        txt_create_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ConfirmPasswordActivity.this, SignUpActivity.class));
            }
        });

    }

    private void loginApiCall() {
        loading.setVisibility(View.VISIBLE);
        hideKeyboard();
        apiInterface = RetrofitClient.getClient().create(APIInterface.class);
        Call<LoginApi> call = apiInterface.login(email.getText().toString(),Utils.login,password.getText().toString());
        call.enqueue(new Callback<LoginApi>() {
            @Override
            public void onResponse(Call<LoginApi> call, Response<LoginApi> response) {
                if (response.body().getError() != null && !response.body().getError()){
                    loading.setVisibility(View.INVISIBLE);
                    setLoginApiToShared(response,password.getText().toString());
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

    private void loggedIn() {
        startActivity(new Intent(ConfirmPasswordActivity.this, MainActivity.class));
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

    private String getLoginApiFromShared(String key){
        SharedPreferences sharedPreferences = getSharedPreferences(Utils.MyPref,MODE_PRIVATE);
        String value = sharedPreferences.getString(key, "");
        return value;
    }
}