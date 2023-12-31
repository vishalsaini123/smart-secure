package com.smartsecureapp.Activity.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.smartsecureapp.Activity.api.APIInterface;
import com.smartsecureapp.Activity.api.RetrofitClient;
import com.smartsecureapp.Activity.model.SmsContactApi;
import com.smartsecureapp.Activity.util.Utils;
import com.smartsecureapp.R;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpActivity extends AppCompatActivity {
    ImageView img_back;
    TextView txt_privacy_policy, txt_term_condition;
    TextInputEditText firstName, lastName, phoneNumber, emailId, password, confirmPassword;
    RadioButton male, female, other;
    MaterialButton submitButton;
    String gender = "";
    ProgressBar loading;
    APIInterface apiInterface;
    private static final int REQUEST_LOCATIOM_PERMISSION = 21;
    FusedLocationProviderClient fusedLocationProviderClient;
    Double lat, lng;
    String loc;
    String address="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        txt_privacy_policy = findViewById(R.id.txt_privacy_policy);
        txt_term_condition = findViewById(R.id.txt_term_condition);
        txt_term_condition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(SignUpActivity.this, TermsConditionsActivity.class);
                startActivity(browserIntent);
            }
        });
        img_back = findViewById(R.id.img_back);
        firstName = findViewById(R.id.firstName);
        lastName = findViewById(R.id.lastName);
        phoneNumber = findViewById(R.id.phoneNumber);
        emailId = findViewById(R.id.emailId);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirmPassword);
        male = findViewById(R.id.male);
        female = findViewById(R.id.female);
        other = findViewById(R.id.other);
        submitButton = findViewById(R.id.submitButton);
        loading = findViewById(R.id.loading);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        lat = location.getLatitude();
                        lng = location.getLongitude();
                        address = getCompleteAddressString(lat,lng);
                    } else {
                        lat = 37.0902;
                        lng = -95.7129;
                    }
                    loc = String.valueOf(lat) + "," + String.valueOf(lng);
                }
            });
        } else {
            requestLocationPermission();
        }

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!emailId.getText().toString().isEmpty()) {

                    if (!password.getText().toString().isEmpty() && !confirmPassword.getText().toString().isEmpty()) {

                        if (password.getText().toString().equalsIgnoreCase(confirmPassword.getText().toString())) {
                            signUp();
                        } else {
                            showSnackBar("Password & confirm password should match.");
                        }

                    } else {
                        showSnackBar("Password & confirm password is required.");
                    }

                } else {
                    showSnackBar("Email is required");
                }

            }
        });
        male.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    gender = "male";
                }
            }
        });

        female.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    gender = "female";
                }
            }
        });

        other.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    gender = "other";
                }
            }
        });

      //  txt_privacy_policy.setPaintFlags(txt_privacy_policy.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
      //  txt_term_condition.setPaintFlags(txt_term_condition.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


    }

    private void signUp() {
        loading.setVisibility(View.VISIBLE);
        hideKeyboard();
        apiInterface = RetrofitClient.getClient().create(APIInterface.class);
        Call<SmsContactApi> call = apiInterface.signup(emailId.getText().toString(), password.getText().toString(), Utils.signup, firstName.getText().toString(), lastName.getText().toString(), gender, phoneNumber.getText().toString(), "2", address, getSharedData(Utils.MyDeviceToken));
        call.enqueue(new Callback<SmsContactApi>() {
            @Override
            public void onResponse(Call<SmsContactApi> call, Response<SmsContactApi> response) {
                loading.setVisibility(View.INVISIBLE);
                if (response.body() != null && !response.body().getError()) {
                    Intent intent = new Intent(SignUpActivity.this, OtpActivity.class);
                    intent.putExtra("email", emailId.getText().toString());
                    intent.putExtra("password", password.getText().toString());
                    startActivity(intent);
                } else {
                    if (response.body().getMsg() != null) {
                        showSnackBar(response.body().getMsg());
                    }

                }
            }

            @Override
            public void onFailure(Call<SmsContactApi> call, Throwable t) {
                loading.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            Log.d("+++Exception+++", String.valueOf(e));
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

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATIOM_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATIOM_PERMISSION) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        lat = location.getLatitude();
                        lng = location.getLongitude();
                        address = getCompleteAddressString(lat,lng);
                    } else {
                        lat = 37.0902;
                        lng = -95.7129;
                    }
                    loc = String.valueOf(lat) + "," + String.valueOf(lng);
                }
            });

        }
    }

    private String getSharedData(String key){
        SharedPreferences sharedPreferences = getSharedPreferences(Utils.MyPref,MODE_PRIVATE);
        String value = sharedPreferences.getString(key, "");
        return value;
    }

    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                Log.w("My Current address", strReturnedAddress.toString());
            } else {
                Log.w("My Current address", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("My Current  address", "Canont get Address!");
        }
        return strAdd;
    }
}