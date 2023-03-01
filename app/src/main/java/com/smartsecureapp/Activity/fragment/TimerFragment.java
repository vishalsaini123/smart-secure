package com.smartsecureapp.Activity.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.smartsecureapp.Activity.activity.CallActivity;
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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class TimerFragment extends Fragment {
    TextView setTimer, timeDisplay, timePercentage, txt_emergency_one, txt_emergency_two;
    ProgressBar progressBar, loading;
    MaterialButton stopTimer;
    CountDownTimer countDownTimer;
    TimePickerDialog mTimePicker;
    boolean c = false;
    APIInterface apiInterface, apiInterface2;
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
    Double lat, lng;
    String loc;
    boolean animatorReset = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timer, container, false);
        setTimer = view.findViewById(R.id.setTimer);
        timeDisplay = view.findViewById(R.id.timeDisplay);
        timePercentage = view.findViewById(R.id.timePercentage);
        progressBar = view.findViewById(R.id.progressBar);
        txt_emergency_one = view.findViewById(R.id.txt_emergency_one);
        txt_emergency_two = view.findViewById(R.id.txt_emergency_two);
        stopTimer = view.findViewById(R.id.stopTimer);
        loading = view.findViewById(R.id.loading);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        rootLayout = view.findViewById(R.id.rootLayout);
        stopTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog dialog = new Dialog(getContext(), android.R.style.Theme_Dialog);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dlg_stop_timer);
                dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                TextInputEditText passwordEditText = (TextInputEditText) dialog.findViewById(R.id.passwordEditText);
                MaterialButton cancelButton = (MaterialButton) dialog.findViewById(R.id.cancelButton);
                MaterialButton doneButton = (MaterialButton) dialog.findViewById(R.id.confirmButton);
                doneButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!passwordEditText.getText().toString().isEmpty() && passwordEditText.getText().toString().equalsIgnoreCase(getLoginApiFromShared(Utils.MySharedPassword))) {
                            resetTimer();
                        }
                        dialog.dismiss();
                    }
                });
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });
        setTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                mTimePicker = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minuteOfDay) {
                        Calendar datetime = Calendar.getInstance();
                        datetime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        datetime.set(Calendar.MINUTE, minuteOfDay);
                        if (datetime.getTimeInMillis()<mcurrentTime.getTimeInMillis()){
                            showSnackBar("Invalid time selected.");
                            return;
                        }
                        setTimer.setVisibility(View.INVISIBLE);
                        txt_emergency_one.setVisibility(View.VISIBLE);
                        txt_emergency_two.setVisibility(View.VISIBLE);
                        stopTimer.setVisibility(View.VISIBLE);
                        long millis = ((long) minuteOfDay * 60 * 1000) + ((long) hourOfDay * 60 * 60 * 1000) - (minute * 60 * 1000) + (hour * 60 * 60 * 1000);
                        countDownTimer = null;
                        countDownTimer = new CountDownTimer(millis, 1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                                long secondsInMilli = 1000;
                                long minutesInMilli = secondsInMilli * 60;
                                long hoursInMilli = minutesInMilli * 60;
                                long tempMili = millisUntilFinished;
                                tempMili = tempMili % hoursInMilli;

                                long elapsedMinutes = tempMili / minutesInMilli;
                                tempMili = tempMili % minutesInMilli;

                                long elapsedSeconds = tempMili / secondsInMilli;
                                if (!c) {
                                    long animationTime = 0;
                                    if (elapsedMinutes > 0) {
                                        animationTime += animationTime + (elapsedMinutes * 60 * 1000);
                                    }
                                    showAnimation((elapsedSeconds * 1000) + animationTime);
                                    c = true;
                                }

                                String yy = String.format("%02d:%02d", elapsedMinutes, elapsedSeconds);
                                timeDisplay.setText(yy);
                                if (countDownTimer != null && elapsedSeconds == 0 && elapsedMinutes == 0) {
                                    countDownTimer.cancel();
                                    resetTimer();
                                    onEmergencyClick();
                                }
                            }

                            @Override
                            public void onFinish() {
                                Log.d("+++finish+++", "Finish");
                            }
                        }.start();
                    }
                }, hour, minute, false);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            }
        });
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US);
        Date now = new Date();
        // Record to the external cache directory for visibility
        fileName = getActivity().getExternalCacheDir().getAbsolutePath();
        fileName += "/" + formatter.format(now) + ".3gp";

        return view;
    }

    private void resetTimer() {
        animatorReset = true;
        timeDisplay.setText("00:00");
        txt_emergency_one.setVisibility(View.INVISIBLE);
        txt_emergency_two.setVisibility(View.INVISIBLE);
        stopTimer.setVisibility(View.INVISIBLE);
        setTimer.setVisibility(View.VISIBLE);
        timePercentage.setText("0%");
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        progressBar.setProgress(0);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    public void showAnimation(long l) {
        ValueAnimator animator = ValueAnimator.ofInt(0, progressBar.getMax());
        animator.setDuration(l);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (animatorReset){
                    animator.cancel();
                    progressBar.setProgress(0);
                    animatorReset = false;
                    c = false;
                    return;
                }
                progressBar.setProgress((Integer) animation.getAnimatedValue());
                timePercentage.setText(String.valueOf((Integer) animation.getAnimatedValue()) + "%");
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                // start your activity here
            }
        });
        animator.start();
    }

    private String getLoginApiFromShared(String key) {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(Utils.MyPref, MODE_PRIVATE);
        String value = sharedPreferences.getString(key, "");
        return value;
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
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_READ_CONTACTS_PERMISSION);
                                loading.setVisibility(View.INVISIBLE);
                                if (siren.equalsIgnoreCase("1")) {
                                    player.stop();
                                }
                                return;
                            }
                            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {

                                @Override
                                public void onSuccess(Location location) {
                                    if (location != null) {
                                        lat = location.getLatitude();
                                        lng = location.getLongitude();
                                    } else {
                                        lat = 37.0902;
                                        lng = -95.7129;
                                    }
                                    loc = getLoginApiFromShared(Utils.MySharedUsername) + String.valueOf(lat) + "," + String.valueOf(lng);
                                    Call<SmsContactApi> callMakeCall = apiInterface2.make_call(Utils.make_call, response.body().getContacts().get(0).getPhone(), loc);
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
                                                                        Call<GetEmailContact> callGetEmail = apiInterface.get_email_contacts(getLoginApiFromShared(Utils.MySharedId), Utils.get_email_contacts);
                                                                        callGetEmail.enqueue(new Callback<GetEmailContact>() {
                                                                            @Override
                                                                            public void onResponse(Call<GetEmailContact> call, Response<GetEmailContact> response) {
                                                                                if (response != null && response.body() != null && !response.body().getContacts().isEmpty()) {
                                                                                    Call<SmsContactApi> callSendEmail = apiInterface.send_email_contacts(getLoginApiFromShared(Utils.MySharedId), Utils.send_email_contacts, getLoginApiFromShared(Utils.MySharedUsername), loc);
                                                                                    callSendEmail.enqueue(new Callback<SmsContactApi>() {
                                                                                        @Override
                                                                                        public void onResponse(Call<SmsContactApi> call, Response<SmsContactApi> response) {
                                                                                            if (!response.body().getError()) {
                                                                                                showSnackBar("Email sent");
                                                                                                surroundingSound();
                                                                                            } else {
                                                                                                showSnackBar("Email not sent");
                                                                                                if (siren.equalsIgnoreCase("1")) {
                                                                                                    player.stop();
                                                                                                }
                                                                                                loading.setVisibility(View.GONE);
                                                                                            }
                                                                                        }

                                                                                        @Override
                                                                                        public void onFailure(Call<SmsContactApi> call, Throwable t) {
                                                                                            showSnackBar("Email not sent");
                                                                                            if (siren.equalsIgnoreCase("1")) {
                                                                                                player.stop();
                                                                                            }
                                                                                            loading.setVisibility(View.GONE);
                                                                                        }
                                                                                    });
                                                                                }
                                                                            }

                                                                            @Override
                                                                            public void onFailure(Call<GetEmailContact> call, Throwable t) {
                                                                                if (siren.equalsIgnoreCase("1")) {
                                                                                    player.stop();
                                                                                }
                                                                                showSnackBar("Email not sent");
                                                                                loading.setVisibility(View.GONE);
                                                                            }
                                                                        });
                                                                    } else {
                                                                        showSnackBar("Sms not sent");
                                                                        if (siren.equalsIgnoreCase("1")) {
                                                                            player.stop();
                                                                        }
                                                                        loading.setVisibility(View.INVISIBLE);
                                                                    }
                                                                }

                                                                @Override
                                                                public void onFailure(Call<SmsContactApi> call, Throwable t) {
                                                                    showSnackBar("Sms not sent");
                                                                    if (siren.equalsIgnoreCase("1")) {
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
                                                        if (siren.equalsIgnoreCase("1")) {
                                                            player.stop();
                                                        }
                                                        loading.setVisibility(View.INVISIBLE);
                                                    }
                                                });
                                            } else {
                                                showSnackBar("Call not initiated.");
                                                if (siren.equalsIgnoreCase("1")) {
                                                    player.stop();
                                                }
                                                loading.setVisibility(View.INVISIBLE);
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<SmsContactApi> call, Throwable t) {
                                            showSnackBar("Call not initiated.");
                                            if (siren.equalsIgnoreCase("1")) {
                                                player.stop();
                                            }
                                            loading.setVisibility(View.INVISIBLE);
                                        }
                                    });
                                }
                            });
                        } else {
                            if (siren.equalsIgnoreCase("1")) {
                                player.stop();
                            }
                            showSnackBar("No added contact.");
                            loading.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onFailure(Call<GetCallContact> call, Throwable t) {
                        if (siren.equalsIgnoreCase("1")) {
                            player.stop();
                        }
                        loading.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onFailure(Call<SirenModel> call, Throwable t) {
                if (siren.equalsIgnoreCase("1")) {
                    player.stop();
                }
                loading.setVisibility(View.GONE);
            }
        });
    }

    public void surroundingSound() {
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
        CountDownTimer countDownTimer = new CountDownTimer(10000, 1000) {
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
                Call<SmsContactApi> sendRecording = apiInterface.insert_update_history(funcBody, idBody, locBody, filePart);
                sendRecording.enqueue(new Callback<SmsContactApi>() {
                    @Override
                    public void onResponse(Call<SmsContactApi> call, Response<SmsContactApi> response) {
                        if (siren.equalsIgnoreCase("1")) {
                            player.stop();
                        }
                        loading.setVisibility(View.GONE);
                    }

                    @Override
                    public void onFailure(Call<SmsContactApi> call, Throwable t) {
                        if (siren.equalsIgnoreCase("1")) {
                            player.stop();
                        }
                        loading.setVisibility(View.GONE);
                    }
                });
            }
        }.start();
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