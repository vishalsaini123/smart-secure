package com.smartsecureapp.Activity.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.smartsecureapp.Activity.api.APIInterface;
import com.smartsecureapp.Activity.api.RetrofitClient;
import com.smartsecureapp.Activity.model.GetSmsContact;
import com.smartsecureapp.Activity.model.GetUserProfile;
import com.smartsecureapp.Activity.model.UpdateProfile;
import com.smartsecureapp.Activity.util.Utils;
import com.smartsecureapp.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PersonalActivity extends AppCompatActivity {
    TextView txt_privacy_policy, txt_term_condition;
    ImageView img_back;
    TextInputEditText firstName, last_name, country, phone, email;
    CheckBox male, female, other;
    APIInterface apiInterface;
    ProgressBar loading;
    String gender = "";
    MaterialButton saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal);

        txt_privacy_policy = findViewById(R.id.txt_privacy_policy);
        txt_term_condition = findViewById(R.id.txt_term_condition);
        img_back = findViewById(R.id.img_back);
        firstName = findViewById(R.id.firstName);
        last_name = findViewById(R.id.lastName);
        country = findViewById(R.id.country);
        phone = findViewById(R.id.phone);
        email = findViewById(R.id.email);
        male = findViewById(R.id.male);
        female = findViewById(R.id.female);
        other = findViewById(R.id.other);
        saveButton = findViewById(R.id.saveButton);
        loading = findViewById(R.id.loading);

        getProfile();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!email.getText().toString().isEmpty()) {
                    sendProfile();
                }
            }
        });

        male.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (male.isChecked()){
                    gender = "male";
                    female.setChecked(false);
                    other.setChecked(false);
                }
            }
        });
        female.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (female.isChecked()){
                    gender = "female";
                    male.setChecked(false);
                    other.setChecked(false);
                }
            }
        });
        other.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (other.isChecked()){
                    gender = "other";
                    male.setChecked(false);
                    female.setChecked(false);
                }
            }
        });

        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        txt_privacy_policy.setPaintFlags(txt_privacy_policy.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        txt_term_condition.setPaintFlags(txt_term_condition.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);


    }

    public void getProfile() {
        loading.setVisibility(View.VISIBLE);
        apiInterface = RetrofitClient.getClient().create(APIInterface.class);
        Call<GetUserProfile> callGetSms = apiInterface.fetchUser_id(getLoginApiFromShared(Utils.MySharedId), Utils.fetchUser_id);
        callGetSms.enqueue(new Callback<GetUserProfile>() {
            @Override
            public void onResponse(Call<GetUserProfile> call, Response<GetUserProfile> response) {
                loading.setVisibility(View.INVISIBLE);
                if (response != null && response.body() != null && response.body().getData()!=null && !response.body().getData().isEmpty()) {
                    firstName.setText(response.body().getData().get(0).getFirstName());
                    last_name.setText(response.body().getData().get(0).getLastName());
                    country.setText(response.body().getData().get(0).getLocation());
                    phone.setText(response.body().getData().get(0).getPhone());
                    email.setText(response.body().getData().get(0).getEmail());
                    if (response.body().getData().get(0).getGender().equalsIgnoreCase("male")) {
                        male.setChecked(true);
                        female.setChecked(false);
                        other.setChecked(false);
                        gender = "male";
                    }
                    if (response.body().getData().get(0).getGender().equalsIgnoreCase("female")) {
                        female.setChecked(true);
                        male.setChecked(false);
                        other.setChecked(false);
                        gender = "female";
                    }
                    if (response.body().getData().get(0).getGender().equalsIgnoreCase("other")) {
                        other.setChecked(true);
                        male.setChecked(false);
                        female.setChecked(false);
                        gender = "other";
                    }
                }
            }

            @Override
            public void onFailure(Call<GetUserProfile> call, Throwable t) {
                loading.setVisibility(View.INVISIBLE);
            }
        });
    }

    private String getLoginApiFromShared(String key) {
        SharedPreferences sharedPreferences = getSharedPreferences(Utils.MyPref, MODE_PRIVATE);
        String value = sharedPreferences.getString(key, "");
        return value;
    }

    public void sendProfile() {
        loading.setVisibility(View.VISIBLE);
        apiInterface = RetrofitClient.getClient().create(APIInterface.class);
        Call<UpdateProfile> call = apiInterface.update_profile(Utils.update_profile, email.getText().toString(), firstName.getText().toString(), last_name.getText().toString(), gender, phone.getText().toString(), country.getText().toString());
        call.enqueue(new Callback<UpdateProfile>() {
            @Override
            public void onResponse(Call<UpdateProfile> call, Response<UpdateProfile> response) {
                loading.setVisibility(View.INVISIBLE);
                if (response != null && response.body() != null && !response.body().getError()) {
                    getProfile();
                    showSnackBar(response.body().getAlert());
                    onBackPressed();
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