package com.smartsecureapp.Activity.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Paint;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.ozcanalasalvar.library.view.popup.TimePickerPopup;
import com.ozcanalasalvar.library.view.timePicker.TimePicker;
import com.smartsecureapp.Activity.activity.PasswordResetActivity;
import com.smartsecureapp.Activity.activity.TermsConditionsActivity;
import com.smartsecureapp.Activity.api.APIInterface;
import com.smartsecureapp.Activity.api.RetrofitClient;
import com.smartsecureapp.Activity.api.RetrofitClientMakeCall;
import com.smartsecureapp.Activity.model.GetCallContact;
import com.smartsecureapp.Activity.model.GetSmsContact;
import com.smartsecureapp.Activity.model.SendCallNotification;
import com.smartsecureapp.Activity.model.SirenModel;
import com.smartsecureapp.Activity.model.SmsContactApi;
import com.smartsecureapp.Activity.util.ProgressHelper;
import com.smartsecureapp.Activity.util.Timer_Service;
import com.smartsecureapp.Activity.util.Utils;
import com.smartsecureapp.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class TimerFragment extends Fragment {
    TextView setTimer, timeDisplay, timePercentage, txt_emergency_one, txt_emergency_two;
    ProgressBar progressBar;
    long resetTimer = 2;


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


    boolean animatorReset = false;

    TextView txt_term_condition;
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
        rootLayout = view.findViewById(R.id.rootLayout);



        txt_term_condition = view.findViewById(R.id.txt_term_condition);
        txt_term_condition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(getActivity(), TermsConditionsActivity.class);
                startActivity(browserIntent);
            }
        });
       // txt_term_condition.setPaintFlags(txt_term_condition.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        stopTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Dialog dialog = new Dialog(getContext(), android.R.style.Theme_Dialog);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dlg_stop_timer);
                dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                TextInputEditText passwordEditText = (TextInputEditText) dialog.findViewById(R.id.passwordEditText);
                TextView forgotpassbtn = (TextView) dialog.findViewById(R.id.timerdlg_forgotpassbtn);

                 forgotpassbtn.setOnClickListener(v ->  startActivity(new Intent(getActivity(), PasswordResetActivity.class)));
                MaterialButton cancelButton = (MaterialButton) dialog.findViewById(R.id.cancelButton);
                MaterialButton doneButton = (MaterialButton) dialog.findViewById(R.id.confirmButton);
                doneButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!passwordEditText.getText().toString().isEmpty() && passwordEditText.getText().toString().equalsIgnoreCase(getLoginApiFromShared(Utils.MySharedPassword))) {

                            Intent intent_service = new Intent(getActivity(), Timer_Service.class);

                            getActivity().stopService(intent_service);

                            resetTimer();
                            dialog.dismiss();
                        }
                        else {
                            Toast.makeText(getActivity(), "Please enter the correct password!", Toast.LENGTH_SHORT).show();

                        }

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
                setTimer.setEnabled(false);
                Calendar mcurrentTime = Calendar.getInstance();
                int currenthour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int currentminute = mcurrentTime.get(Calendar.MINUTE);

                if (isMyServiceRunning(Timer_Service.class))
                {
                    Toast.makeText(getActivity(), "Please wait to complete first process", Toast.LENGTH_SHORT).show();
                }
                else{
                   TimePickerPopup timePickerPopup = new TimePickerPopup.Builder()
                            .from(getActivity())
                            .offset(3)
                            .textSize(19)
                            .setTime(currenthour,currentminute)
                            .listener(new TimePickerPopup.OnTimeSelectListener() {
                                @Override
                                public void onTimeSelected(TimePicker timePicker, int hour, int minute) {

                                    Calendar datetime = Calendar.getInstance();
                                    datetime.set(Calendar.HOUR_OF_DAY, hour);
                                    datetime.set(Calendar.MINUTE, minute);
                                    if (datetime.getTimeInMillis()<mcurrentTime.getTimeInMillis()){
                                        showSnackBar("Invalid time selected.");
                                        return;
                                    }

                                    setTimer.setVisibility(View.INVISIBLE);
                                    txt_emergency_one.setVisibility(View.VISIBLE);
                                    txt_emergency_two.setVisibility(View.VISIBLE);
                                    stopTimer.setVisibility(View.VISIBLE);
                                    long millis = datetime.getTimeInMillis() - mcurrentTime.getTimeInMillis();


                                    //long millis = calendar.get(Calendar.MILLISECOND);
                                    Log.e("milis selected",""+millis);
                                    Log.e("milis selected",""+datetime.getTimeInMillis());
                                    Log.e("milis selected",""+mcurrentTime.getTimeInMillis());

                                    Intent intent_service = new Intent(getActivity(), Timer_Service.class);
                                    intent_service.putExtra("time",millis);
                                    intent_service.putExtra("selected_time",datetime.getTimeInMillis());
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        getActivity().startForegroundService(intent_service);
                                    }
                                    else{
                                        getActivity().stopService(intent_service);
                                    }
                                }

                            })
                            .build();
                   timePickerPopup.show();
                   timePickerPopup.setCancelable(false);
                   timePickerPopup.setCanceledOnTouchOutside(false);
                   timePickerPopup.setOnDismissListener(new DialogInterface.OnDismissListener() {
                       @Override
                       public void onDismiss(DialogInterface dialogInterface) {
                           setTimer.setEnabled(true);
                       }
                   });

                }


            }
        });
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US);
        Date now = new Date();
        // Record to the external cache directory for visibility


        return view;
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    private void resetTimer() {

        animatorReset = true;
        c = false;
        timeDisplay.setText("00:00");
        txt_emergency_one.setVisibility(View.VISIBLE);
        txt_emergency_two.setVisibility(View.VISIBLE);
        stopTimer.setVisibility(View.INVISIBLE);
        setTimer.setVisibility(View.VISIBLE);
        setTimer.setEnabled(true);
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

        getActivity().unregisterReceiver(broadcastReceiver);
    }

    public void showAnimation(long l) {
        Log.e("ll",""+l);
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



    private void showSnackBar(String msg) {

        if (rootLayout!=null)
        {
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
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {


            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    long millisUntilFinished = intent.getLongExtra("millis",0);
                    long hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished);

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
                        if (hours > 0)
                            animationTime += hours *elapsedMinutes * 60 * 1000;
                        if (elapsedMinutes > 0) {
                            animationTime += animationTime + (elapsedMinutes * 60 * 1000);
                        }
                        showAnimation(millisUntilFinished);
                        c = true;
                    }

                    String yy;
                    if (hours==0)
                        yy = String.format("%02d:%02d", elapsedMinutes, elapsedSeconds);
                    else
                        yy = String.format("%02d:%02d:%02d",hours, elapsedMinutes, elapsedSeconds);
                    timeDisplay.setText(yy);
                    if (  hours==0 && elapsedMinutes == 0 && elapsedSeconds == 0) {
                        stopTimer.setVisibility(View.VISIBLE);


                    }

                }
            });



        }
    };


    @Override
    public void onResume() {
        super.onResume();
        if (isMyServiceRunning(Timer_Service.class))
        {
           // setTimer.setVisibility(View.INVISIBLE);
          //  txt_emergency_one.setVisibility(View.VISIBLE);
           // txt_emergency_two.setVisibility(View.VISIBLE);
            stopTimer.setVisibility(View.VISIBLE);
        }

        if (Timer_Service.resetTimer==0)
        {
            stopTimer.setVisibility(View.VISIBLE);
        }

        getActivity().registerReceiver(broadcastReceiver,new IntentFilter(Timer_Service.str_receiver));

    }



}

