package com.smartsecureapp.Activity.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.smartsecureapp.Activity.api.APIInterface;
import com.smartsecureapp.Activity.api.RetrofitClient;
import com.smartsecureapp.Activity.model.SendOtpModel;
import com.smartsecureapp.Activity.model.SmsContactApi;
import com.smartsecureapp.Activity.util.Utils;
import com.smartsecureapp.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PasswordResetActivity extends AppCompatActivity {
    TextView txt_privacy_policy,txt_term_condition;
    ImageView img_back;
    EditText email;
    MaterialButton submit;
    APIInterface apiInterface;
    ProgressBar loading;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset);

        email = findViewById(R.id.email);
        submit = findViewById(R.id.submit);
        loading = findViewById(R.id.loading);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!email.getText().toString().isEmpty()){
                    //passwordResetOne();
                    sendOtpEmail("1");
                }else {
                    showSnackBar("Email should not be empty.");
                }
            }
        });
        txt_privacy_policy = findViewById(R.id.txt_privacy_policy);
        txt_term_condition = findViewById(R.id.txt_term_condition);
        txt_term_condition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(PasswordResetActivity.this, TermsConditionsActivity.class);
                startActivity(browserIntent);
            }
        });
        img_back = findViewById(R.id.img_back);

        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

      //  txt_privacy_policy.setPaintFlags(txt_privacy_policy.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
       // txt_term_condition.setPaintFlags(txt_term_condition.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
    }

    private void showSnackBar(String msg) {


        Snackbar snackbar = Snackbar.make(email, msg, Snackbar.LENGTH_LONG);
        snackbar.setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        snackbar.dismiss();
                    }
                })
                .setActionTextColor(getResources().getColor(R.color.btn_red));
        snackbar.show();
    }


    private void sendOtpEmail(String callType){
        loading.setVisibility(View.VISIBLE);
        apiInterface = RetrofitClient.getClient().create(APIInterface.class);
        Call<SendOtpModel> callDeleteAccount = apiInterface.send_otp_email(email.getText().toString(),Utils.send_otp_email,getLoginApiFromShared(Utils.MySharedId),callType);
        callDeleteAccount.enqueue(new Callback<SendOtpModel>() {
            @Override
            public void onResponse(Call<SendOtpModel> call, Response<SendOtpModel> response) {
                if (response != null && response.body() != null && !response.body().isError()){
                    loading.setVisibility(View.GONE);
                    Toast.makeText(PasswordResetActivity.this, "OTP sent to your email successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(PasswordResetActivity.this,OtpPasswordActivity.class);
                    intent.putExtra("email",email.getText().toString());
                    startActivity(intent);

                }else {
                    Toast.makeText(PasswordResetActivity.this, "Internal Server Error , Try again later", Toast.LENGTH_SHORT).show();

                    loading.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<SendOtpModel> call, Throwable t) {
                loading.setVisibility(View.GONE);            }
        });
    }
    private void passwordResetOne(){
        loading.setVisibility(View.VISIBLE);
        apiInterface = RetrofitClient.getClient().create(APIInterface.class);
        Call<SmsContactApi> call = apiInterface.forgot_password_one(email.getText().toString(),Utils.forgot_password_one);
        call.enqueue(new Callback<SmsContactApi>() {
            @Override
            public void onResponse(Call<SmsContactApi> call, Response<SmsContactApi> response) {

                if (response.body() != null && !response.body().getError()){
                    loading.setVisibility(View.GONE);
                    Toast.makeText(PasswordResetActivity.this, "OTP sent to your email successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(PasswordResetActivity.this,OtpPasswordActivity.class);
                    intent.putExtra("email",email.getText().toString());
                    startActivity(intent);

                }else {
                    Toast.makeText(PasswordResetActivity.this, "Internal Server Error , Try again later", Toast.LENGTH_SHORT).show();

                    loading.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<SmsContactApi> call, Throwable t) {
                Toast.makeText(PasswordResetActivity.this, "Internal Server Error , Try again later", Toast.LENGTH_SHORT).show();

                loading.setVisibility(View.INVISIBLE);
            }
        });
    }
    private String getLoginApiFromShared(String key){
        SharedPreferences sharedPreferences = getSharedPreferences(Utils.MyPref,MODE_PRIVATE);
        String value = sharedPreferences.getString(key, "");
        return value;
    }
}