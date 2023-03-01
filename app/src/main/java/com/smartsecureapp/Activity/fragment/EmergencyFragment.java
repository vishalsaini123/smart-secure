package com.smartsecureapp.Activity.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Paint;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.smartsecureapp.Activity.api.APIInterface;
import com.smartsecureapp.Activity.api.RetrofitClient;
import com.smartsecureapp.Activity.api.RetrofitClientMakeCall;
import com.smartsecureapp.Activity.model.GetCallContact;
import com.smartsecureapp.Activity.model.GetEmailContact;
import com.smartsecureapp.Activity.model.GetSmsContact;
import com.smartsecureapp.Activity.model.SirenModel;
import com.smartsecureapp.Activity.model.SmsContactApi;
import com.smartsecureapp.Activity.util.Utils;
import com.smartsecureapp.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmergencyFragment extends Fragment {
    TextView txt_privacy_policy, txt_term_condition;
    ImageView emergencyButton;
    ProgressBar loading;
    APIInterface apiInterface,apiInterface2;
    MediaPlayer player;
    private static final int REQUEST_READ_CONTACTS_PERMISSION = 0;
    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};
    String siren;
    private static String fileName;
    private MediaRecorder recorder;
    RelativeLayout rootLayout;
    FusedLocationProviderClient fusedLocationProviderClient;
    Double lat,lng;
    String loc;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_emergency, container, false);

        txt_privacy_policy = view.findViewById(R.id.txt_privacy_policy);
        txt_term_condition = view.findViewById(R.id.txt_term_condition);

        txt_privacy_policy.setPaintFlags(txt_privacy_policy.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        txt_term_condition.setPaintFlags(txt_term_condition.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        emergencyButton = view.findViewById(R.id.emergencyButton);
        loading = view.findViewById(R.id.loading);
        rootLayout = view.findViewById(R.id.rootLayout);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        emergencyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onEmergencyClick();
            }
        });

        requestContactsPermission();
        requestLocationPermission();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US);
        Date now = new Date();
        // Record to the external cache directory for visibility
        fileName = getActivity().getExternalCacheDir().getAbsolutePath();
        fileName += "/" + formatter.format(now) + ".3gp";
        return view;
    }

    private void requestContactsPermission() {
        if (!hasContactsPermission()) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_READ_CONTACTS_PERMISSION);
        }
    }

    private void requestLocationPermission(){
        ActivityCompat.requestPermissions(getActivity(),
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_READ_CONTACTS_PERMISSION);
    }

    private boolean hasContactsPermission() {
        return ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED;
    }

    public void onEmergencyClick() {
        loading.setVisibility(View.VISIBLE);
        siren = "0";
        apiInterface = RetrofitClient.getClient().create(APIInterface.class);
        apiInterface2 = RetrofitClientMakeCall.getClient().create(APIInterface.class);
        Call<SirenModel> call = apiInterface.get_user_settings(Utils.get_user_settings, getLoginApiFromShared(Utils.MySharedId));
        call.enqueue(new Callback<SirenModel>() {
            @Override
            public void onResponse(Call<SirenModel> call, Response<SirenModel> response) {
                if (response != null && !response.body().getError() && response.body().getSiren().equalsIgnoreCase("1")) {
                    AssetFileDescriptor afd = null;
                    try {
                        afd = getContext().getAssets().openFd("siren_sound.mp3");
                        player = new MediaPlayer();
                        player.setDataSource(afd.getFileDescriptor());
                        player.prepare();
                        player.start();
                        siren = response.body().getSiren();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                }
                Call<GetCallContact> callGetSms = apiInterface.get_call_contacts(getLoginApiFromShared(Utils.MySharedId), Utils.get_call_contacts);
                callGetSms.enqueue(new Callback<GetCallContact>() {
                    @Override
                    public void onResponse(Call<GetCallContact> call, Response<GetCallContact> response) {
                        if (response != null && response.body() != null && response.body().getContacts() != null && !response.body().getContacts().isEmpty()) {
                            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_READ_CONTACTS_PERMISSION);
                                loading.setVisibility(View.INVISIBLE);
                                if (siren.equalsIgnoreCase("1")) {
                                    player.stop();
                                }
                                return;
                            }
                            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {

                                @Override
                                public void onSuccess(Location location) {
                                    if (location!=null) {
                                        lat = location.getLatitude();
                                        lng = location.getLongitude();
                                    }else {
                                        lat = 37.0902;
                                        lng = -95.7129;
                                    }
                                    loc = getLoginApiFromShared(Utils.MySharedUsername)+String.valueOf(lat)+","+String.valueOf(lng);
                                    Call<SmsContactApi> callMakeCall = apiInterface2.make_call(Utils.make_call,response.body().getContacts().get(0).getPhone(),loc);
                                    callMakeCall.enqueue(new Callback<SmsContactApi>() {
                                        @Override
                                        public void onResponse(Call<SmsContactApi> call, Response<SmsContactApi> response) {
                                            if (!response.body().getError()) {
                                                showSnackBar("Call initiated.");
                                                Call<GetSmsContact> callGetSms = apiInterface.get_sms_contacts(getLoginApiFromShared(Utils.MySharedId), Utils.get_sms_contacts);
                                                callGetSms.enqueue(new Callback<GetSmsContact>() {
                                                    @Override
                                                    public void onResponse(Call<GetSmsContact> call, Response<GetSmsContact> response) {
                                                        if (response != null && response.body() != null && !response.body().getContacts().isEmpty()) {
                                                            Call<SmsContactApi> callSendSms = apiInterface2.send_sms(Utils.send_sms, response.body().getContacts().get(0).getPhone(), loc);
                                                            callSendSms.enqueue(new Callback<SmsContactApi>() {
                                                                @Override
                                                                public void onResponse(Call<SmsContactApi> call, Response<SmsContactApi> response) {
                                                                    if (!response.body().getError()) {
                                                                        showSnackBar("Sms sent");
                                                                        Call<GetEmailContact> callGetEmail = apiInterface.get_email_contacts(getLoginApiFromShared(Utils.MySharedId),Utils.get_email_contacts);
                                                                        callGetEmail.enqueue(new Callback<GetEmailContact>() {
                                                                            @Override
                                                                            public void onResponse(Call<GetEmailContact> call, Response<GetEmailContact> response) {
                                                                                if (response!=null && response.body()!=null && !response.body().getContacts().isEmpty()) {
                                                                                    Call<SmsContactApi> callSendEmail = apiInterface.send_email_contacts(getLoginApiFromShared(Utils.MySharedId),Utils.send_email_contacts, getLoginApiFromShared(Utils.MySharedUsername), loc);
                                                                                    callSendEmail.enqueue(new Callback<SmsContactApi>() {
                                                                                        @Override
                                                                                        public void onResponse(Call<SmsContactApi> call, Response<SmsContactApi> response) {
                                                                                            if (!response.body().getError()) {
                                                                                                showSnackBar("Email sent");
                                                                                                surroundingSound();
                                                                                            }else {
                                                                                                showSnackBar("Email not sent");
                                                                                                if (siren.equalsIgnoreCase("1")){
                                                                                                    player.stop();
                                                                                                }
                                                                                                loading.setVisibility(View.GONE);
                                                                                            }
                                                                                        }

                                                                                        @Override
                                                                                        public void onFailure(Call<SmsContactApi> call, Throwable t) {
                                                                                            showSnackBar("Email not sent");
                                                                                            if (siren.equalsIgnoreCase("1")){
                                                                                                player.stop();
                                                                                            }
                                                                                            loading.setVisibility(View.GONE);
                                                                                        }
                                                                                    });
                                                                                }
                                                                            }

                                                                            @Override
                                                                            public void onFailure(Call<GetEmailContact> call, Throwable t) {
                                                                                if (siren.equalsIgnoreCase("1")){
                                                                                    player.stop();
                                                                                }
                                                                                showSnackBar("Email not sent");
                                                                                loading.setVisibility(View.GONE);
                                                                            }
                                                                        });
                                                                    }else {
                                                                        showSnackBar("Sms not sent");
                                                                        if (siren.equalsIgnoreCase("1")){
                                                                            player.stop();
                                                                        }
                                                                        loading.setVisibility(View.INVISIBLE);
                                                                    }
                                                                }

                                                                @Override
                                                                public void onFailure(Call<SmsContactApi> call, Throwable t) {
                                                                    showSnackBar("Sms not sent");
                                                                    if (siren.equalsIgnoreCase("1")){
                                                                        player.stop();
                                                                    }
                                                                    loading.setVisibility(View.INVISIBLE);
                                                                }
                                                            });
                                                        }
                                                    }

                                                    @Override
                                                    public void onFailure(Call<GetSmsContact> call, Throwable t) {
                                                        showSnackBar("Call not initiated.");
                                                        if (siren.equalsIgnoreCase("1")){
                                                            player.stop();
                                                        }
                                                        loading.setVisibility(View.INVISIBLE);
                                                    }
                                                });
                                            }else{
                                                showSnackBar("Call not initiated.");
                                                if (siren.equalsIgnoreCase("1")){
                                                    player.stop();
                                                }
                                                loading.setVisibility(View.INVISIBLE);
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<SmsContactApi> call, Throwable t) {
                                            showSnackBar("Call not initiated.");
                                            if (siren.equalsIgnoreCase("1")){
                                                player.stop();
                                            }
                                            loading.setVisibility(View.INVISIBLE);
                                        }
                                    });
                                }
                            });
                        }else {
                            if (siren.equalsIgnoreCase("1")){
                                player.stop();
                            }
                            showSnackBar("No added contact.");
                            loading.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onFailure(Call<GetCallContact> call, Throwable t) {
                        if (siren.equalsIgnoreCase("1")){
                            player.stop();
                        }
                        loading.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onFailure(Call<SirenModel> call, Throwable t) {
                if (siren.equalsIgnoreCase("1")){
                    player.stop();
                }
                loading.setVisibility(View.GONE);
            }
        });
    }

    public void surroundingSound(){
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        recorder.start();
        CountDownTimer countDownTimer = new CountDownTimer(10000,1000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                recorder.stop();
                recorder.release();
                recorder = null;
                File file = new File(fileName);
                MultipartBody.Part filePart = MultipartBody.Part.createFormData("audio", file.getName(), RequestBody.create(MediaType.parse("audio/*"), file));
                    apiInterface = RetrofitClient.getClient().create(APIInterface.class);
                RequestBody funcBody = RequestBody.create(MediaType.parse("multipart/form-data"), Utils.insert_update_history);
                RequestBody idBody = RequestBody.create(MediaType.parse("multipart/form-data"), getLoginApiFromShared(Utils.MySharedId));
                RequestBody locBody = RequestBody.create(MediaType.parse("multipart/form-data"), loc);
                    Call<SmsContactApi> sendRecording = apiInterface.insert_update_history(funcBody,idBody,locBody,filePart);
                    sendRecording.enqueue(new Callback<SmsContactApi>() {
                        @Override
                        public void onResponse(Call<SmsContactApi> call, Response<SmsContactApi> response) {
                            if (siren.equalsIgnoreCase("1")){
                                player.stop();
                            }
                            loading.setVisibility(View.GONE);
                        }

                        @Override
                        public void onFailure(Call<SmsContactApi> call, Throwable t) {
                            if (siren.equalsIgnoreCase("1")){
                                player.stop();
                            }
                            loading.setVisibility(View.GONE);
                        }
                    });
            }
        }.start();
    }

    private String getLoginApiFromShared(String key) {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(Utils.MyPref, MODE_PRIVATE);
        String value = sharedPreferences.getString(key, "");
        return value;
    }

    private void showSnackBar(String msg) {
        Snackbar snackbar = Snackbar.make(rootLayout, msg, Snackbar.LENGTH_LONG);
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