package com.smartsecureapp.Activity.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.smartsecureapp.Activity.api.APIInterface;
import com.smartsecureapp.Activity.api.RetrofitClient;
import com.smartsecureapp.Activity.model.UpdateProfile;
import com.smartsecureapp.Activity.util.Utils;
import com.smartsecureapp.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContactUsActivity extends AppCompatActivity {
    TextView txt_privacy_policy,txt_term_condition;
    ImageView img_back;
    TextView name,email;
    EditText subject,message;
    MaterialButton submit;
    ProgressBar loading;
    APIInterface apiInterface;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);

        txt_privacy_policy = findViewById(R.id.txt_privacy_policy);
        txt_term_condition = findViewById(R.id.txt_term_condition);
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        subject = findViewById(R.id.subject);
        message = findViewById(R.id.message);
        submit = findViewById(R.id.submit);
        loading = findViewById(R.id.loading);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!subject.getText().toString().isEmpty() && !message.getText().toString().isEmpty()){
                    loading.setVisibility(View.VISIBLE);
                    apiInterface = RetrofitClient.getClient().create(APIInterface.class);
                    Call<UpdateProfile> callContactUs = apiInterface.contact_us(Utils.contact_us,getLoginApiFromShared(Utils.MySharedEmail),getLoginApiFromShared(Utils.MySharedPhone),getLoginApiFromShared(Utils.MySharedUsername),message.getText().toString(),getLoginApiFromShared(Utils.MySharedId));
                    callContactUs.enqueue(new Callback<UpdateProfile>() {
                        @Override
                        public void onResponse(Call<UpdateProfile> call, Response<UpdateProfile> response) {
                            loading.setVisibility(View.INVISIBLE);
                            if (response != null && response.body() != null && !response.body().getError()) {
                                showSnackBar(response.body().getAlert());
                            } else {
                                showSnackBar(response.body().getAlert());
                            }
                        }

                        @Override
                        public void onFailure(Call<UpdateProfile> call, Throwable t) {
                            loading.setVisibility(View.INVISIBLE);
                        }
                    });

                }else {
                    showSnackBar("All fields are required.");
                }
            }
        });

        name.setText(getLoginApiFromShared(Utils.MySharedUsername));
        email.setText(getLoginApiFromShared(Utils.MySharedEmail));

        img_back = findViewById(R.id.img_back);

        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        txt_privacy_policy.setPaintFlags(txt_privacy_policy.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        txt_term_condition.setPaintFlags(txt_term_condition.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

    }

    private String getLoginApiFromShared(String key){
        SharedPreferences sharedPreferences = getSharedPreferences(Utils.MyPref,MODE_PRIVATE);
        String value = sharedPreferences.getString(key, "");
        return value;
    }

    private void showSnackBar(String msg) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG);
        snackbar.setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        snackbar.dismiss();
                        onBackPressed();
                    }
                })
                .setActionTextColor(getResources().getColor(R.color.btn_red));
        snackbar.show();
    }
}