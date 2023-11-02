package com.smartsecureapp.Activity.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.CaptivePortal;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.smartsecureapp.Activity.activity.MainActivity;
import com.smartsecureapp.Activity.api.APIInterface;
import com.smartsecureapp.Activity.api.RetrofitClient;
import com.smartsecureapp.Activity.api.RetrofitClientMakeCall;
import com.smartsecureapp.Activity.model.CommonModel;
import com.smartsecureapp.Activity.model.GetCallContact;
import com.smartsecureapp.Activity.model.GetSmsContact;
import com.smartsecureapp.Activity.model.SendCallNotification;
import com.smartsecureapp.Activity.model.SirenModel;
import com.smartsecureapp.Activity.model.SmsContactApi;
import com.smartsecureapp.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Timer_Service extends Service {

    public static long recordingTime = 200000;
    CountDownTimer recordingCountDownTimer;
    public static String str_receiver = "com.countdowntimerservice.receiver";
    boolean isNotificationSent = false;
    boolean c = false;
    Double lat, lng;
    String loc;
    APIInterface apiInterface, apiInterface2;
    MediaPlayer player;
    private static final int REQUEST_READ_CONTACTS_PERMISSION = 0;
    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};
    String siren;
    private static String fileName;
    private MediaRecorder recorder;
    FusedLocationProviderClient fusedLocationProviderClient;

    public static long resetTimer = 2;
    private Handler mHandler = new Handler();
    Calendar calendar;
    SimpleDateFormat simpleDateFormat;
    String strDate;
    Date date_current, date_diff;
    SharedPreferences mpref;
    SharedPreferences.Editor mEditor;

    private Timer mTimer = null;
    public static final long NOTIFY_INTERVAL = 1000;
    Intent intent;
    CountDownTimer countDownTimer;
    String startTime;
    String endTime,duration;
    int id;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static final String CHANNEL_ID = "ForegroundServiceChannel";

    @Override
    public void onCreate() {
        super.onCreate();


        intent = new Intent(str_receiver);

    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(Timer_Service.this);
        id = startId;
        createNotificationChannel();
        Intent notificationIntent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Timer Service")
                .setContentText("Running...")
                .setSmallIcon(R.drawable.smart_secure_icon)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);
        // onEmergencyClick();





                startTime = getTimeDate(System.currentTimeMillis());
                duration = getTimeDate(intent.getLongExtra("time",0));
                endTime = getTimeDate(intent.getLongExtra("selected_time",0));
                onEmergencyClick(startTime,endTime,duration);

                countDownTimer = new CountDownTimer(intent.getLongExtra("time", 0), 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {

                        resetTimer = millisUntilFinished;

                        fn_update(millisUntilFinished);
                    }

                    @Override
                    public void onFinish() {

                        fn_update(0);
                        resetTimer = 0;
                       // stopSelf();



                        Log.d("+++finish+++", "Finish");
                    }
                }.start();




        return START_STICKY;


    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        stopEmergency(startTime,endTime);
        if (recordingCountDownTimer != null) {
            recordingCountDownTimer.onFinish();
        }
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        Log.e("Service finish", "Finish");
    }

    private void fn_update(long str_time) {

        intent.putExtra("millis", str_time);
        sendBroadcast(intent);
    }

    @SuppressLint("MissingPermission")
    public void stopEmergency(String starttime,String endTime){

        if (player!=null)
        {player.stop();}
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
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
                apiInterface = RetrofitClient.getClient().create(APIInterface.class);

                Call<CommonModel> emergencycall = apiInterface.timerApi(Utils.timer_api,getLoginApiFromShared(Utils.MySharedId),loc,starttime,endTime,"0","false");
                emergencycall.enqueue(new Callback<CommonModel>() {
                    @Override
                    public void onResponse(Call<CommonModel> call, Response<CommonModel> response) {
                        if (response != null && response.body() != null ){

                            if (response.body().getError()) {
                                showSnackBar(response.body().getMsg());
                            }
                        }
                    }
                    @Override
                    public void onFailure(Call<CommonModel> call, Throwable t) {
                        showSnackBar(t.getLocalizedMessage());
                        showSnackBar("Internal server error");

                    }
                });
            }
        });

    }

    public void onEmergencyClick(String StartTime,String endTime,String duration) {


        siren = "0";
        surroundingSound();
        apiInterface = RetrofitClient.getClient().create(APIInterface.class);
        if (ActivityCompat.checkSelfPermission(Timer_Service.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(Timer_Service.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
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
                Call<CommonModel> emergencycall = apiInterface.timerApi(Utils.timer_api,getLoginApiFromShared(Utils.MySharedId),loc,StartTime,duration,endTime,"true");
                emergencycall.enqueue(new Callback<CommonModel>() {
                    @Override
                    public void onResponse(Call<CommonModel> call, Response<CommonModel> response) {
                        if (response != null && response.body() != null){
                            showSnackBar(response.body().getMsg());
                        }

                    }

                    @Override
                    public void onFailure(Call<CommonModel> call, Throwable t) {
                        showSnackBar(t.getLocalizedMessage());

                    }
                });
            }
        });
       // apiInterface2 = RetrofitClientMakeCall.getClient().create(APIInterface.class);
        /*Call<SirenModel> call = apiInterface.get_user_settings(Utils.get_user_settings, getLoginApiFromShared(Utils.MySharedId));
        call.enqueue(new Callback<SirenModel>() {
            @Override
            public void onResponse(Call<SirenModel> call, Response<SirenModel> response) {

                Log.e("siren", response.body().getSiren());
                if (response != null && !response.body().getError() && response.body().getSiren().equalsIgnoreCase("1")) {
                    AssetFileDescriptor afd = null;
                    try {

                        Log.e("siren1", response.body().getSiren());
                        afd = getAssets().openFd("siren_sound.mp3");
                        Log.e("siren1", "" + afd.getLength());
                        player = new MediaPlayer();

                        player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                        player.prepare();
                        player.setLooping(true);
                        player.start();
                        siren = response.body().getSiren();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                }


                *//*Call<GetCallContact> callGetSms = apiInterface.get_call_contacts(getLoginApiFromShared(Utils.MySharedId), Utils.get_call_contacts);
                callGetSms.enqueue(new Callback<GetCallContact>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onResponse(Call<GetCallContact> call, Response<GetCallContact> response) {
                        if (response != null && response.body() != null && response.body().getContacts() != null && !response.body().getContacts().isEmpty()) {


                                if (siren.equalsIgnoreCase("1")) {
                                    //player.stop();
                                }



                            fusedLocationProviderClient.getLastLocation().addOnSuccessListener( new OnSuccessListener<Location>() {

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

                                    for ( int i =0 ; i<response.body().getContacts().size();i++)
                                    {
                                        Log.e("iii","loop"+i);
                                        Call<SmsContactApi> callMakeCall = apiInterface2.make_call(Utils.make_call,response.body().getContacts().get(i).getPhone(),loc);
                                        callMakeCall.enqueue(new Callback<SmsContactApi>() {
                                            @Override
                                            public void onResponse(Call<SmsContactApi> call, Response<SmsContactApi> response) {
                                                if (!response.body().getError()) {
                                                    showSnackBar("Call initiated.");
                                                    Call<SmsContactApi> callSendEmail = apiInterface.send_email_contacts(getLoginApiFromShared(Utils.MySharedId),Utils.send_email_contacts, getLoginApiFromShared(Utils.MySharedUsername), loc);
                                                    callSendEmail.enqueue(new Callback<SmsContactApi>() {
                                                        @Override
                                                        public void onResponse(Call<SmsContactApi> call, Response<SmsContactApi> response) {
                                                            if (response.body()!=null) {
                                                                showSnackBar("Email sent");

                                                                Call<GetSmsContact> callGetSms = apiInterface.get_sms_contacts(getLoginApiFromShared(Utils.MySharedId), Utils.get_sms_contacts);
                                                                callGetSms.enqueue(new Callback<GetSmsContact>() {
                                                                    @Override
                                                                    public void onResponse(Call<GetSmsContact> call, Response<GetSmsContact> response) {
                                                                        if (response.body() != null && response.body().getContacts()!=null && !response.body().getContacts().isEmpty()) {
                                                                            Call<SmsContactApi> callSendSms = apiInterface2.send_sms(Utils.send_sms, response.body().getContacts().get(0).getPhone(), loc);
                                                                            callSendSms.enqueue(new Callback<SmsContactApi>() {
                                                                                @Override
                                                                                public void onResponse(Call<SmsContactApi> call, Response<SmsContactApi> response) {
                                                                                    if (response.code()==200) {
                                                                                        showSnackBar("Sms sent");

                                                                                        if (!isNotificationSent)
                                                                                        {
                                                                                            isNotificationSent = true;
                                                                                            surroundingSound();
                                                                                            Call<SendCallNotification> callSendSms = apiInterface.send_notification(getLoginApiFromShared(Utils.MySharedId),Utils.send_call_notifications, getLoginApiFromShared(Utils.MySharedUsername), lat+","+lng);
                                                                                            callSendSms.enqueue(new Callback<SendCallNotification>() {
                                                                                                @Override
                                                                                                public void onResponse(Call<SendCallNotification> call, Response<SendCallNotification> response) {


                                                                                                    if (!response.body().isError()) {
                                                                                                        Call<SendCallNotification> callSendSms = apiInterface.send_notification(getLoginApiFromShared(Utils.MySharedId),Utils.send_sms_notifications, getLoginApiFromShared(Utils.MySharedUsername), lat+","+lng);
                                                                                                        callSendSms.enqueue(new Callback<SendCallNotification>() {
                                                                                                            @Override
                                                                                                            public void onResponse(Call<SendCallNotification> call, Response<SendCallNotification> response) {
                                                                                                                if (response.body()!=null){
                                                                                                                if (!response.body().isError()) {

                                                                                                                    Call<SendCallNotification> callSendSms = apiInterface.send_notification(getLoginApiFromShared(Utils.MySharedId),Utils.send_email_notifications, getLoginApiFromShared(Utils.MySharedUsername), lat+","+lng);
                                                                                                                    callSendSms.enqueue(new Callback<SendCallNotification>() {
                                                                                                                        @Override
                                                                                                                        public void onResponse(Call<SendCallNotification> call, Response<SendCallNotification> response) {
                                                                                                                            if (response.body()!=null){
                                                                                                                            if (!response.body().isError()) {

                                                                                                                                showSnackBar("Notification sent!");


                                                                                                                            }}else {
                                                                                                                                showSnackBar("Notification not sent");
                                                                                                                                if (siren.equalsIgnoreCase("1")){
                                                                                                                                    //player.stop();
                                                                                                                                }

                                                                                                                            }
                                                                                                                        }

                                                                                                                        @Override
                                                                                                                        public void onFailure(Call<SendCallNotification> call, Throwable t) {
                                                                                                                            showSnackBar("Sms not sent");
                                                                                                                            if (siren.equalsIgnoreCase("1")){
                                                                                                                                //player.stop();
                                                                                                                            }

                                                                                                                        }
                                                                                                                    });

                                                                                                                }}else {
                                                                                                                    showSnackBar("Sms not sent");
                                                                                                                    if (siren.equalsIgnoreCase("1")){
                                                                                                                        //player.stop();
                                                                                                                    }

                                                                                                                }
                                                                                                            }

                                                                                                            @Override
                                                                                                            public void onFailure(Call<SendCallNotification> call, Throwable t) {
                                                                                                                showSnackBar("Sms not sent");
                                                                                                                if (siren.equalsIgnoreCase("1")){
                                                                                                                    //player.stop();
                                                                                                                }

                                                                                                            }
                                                                                                        });


                                                                                                    }else {
                                                                                                        showSnackBar("Sms not sent");
                                                                                                        if (siren.equalsIgnoreCase("1")){
                                                                                                            //player.stop();
                                                                                                        }

                                                                                                    }
                                                                                                }

                                                                                                @Override
                                                                                                public void onFailure(Call<SendCallNotification> call, Throwable t) {
                                                                                                    showSnackBar("Sms not sent");
                                                                                                    if (player!=null)
                                                                                                        player.stop();

                                                                                                }
                                                                                            });
                                                                                        }




                                                                                    }else {
                                                                                        showSnackBar("Sms not sent");
                                                                                        if (player!=null)
                                                                                            player.stop();

                                                                                    }
                                                                                }

                                                                                @Override
                                                                                public void onFailure(Call<SmsContactApi> call, Throwable t) {
                                                                                    showSnackBar("Sms not sent");
                                                                                    if (player!=null)
                                                                                        player.stop();

                                                                                }
                                                                            });
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onFailure(Call<GetSmsContact> call, Throwable t) {
                                                                        showSnackBar("Call not initiated.");
                                                                        if (player!=null)
                                                                            player.stop();

                                                                    }
                                                                });

                                                            }else {
                                                                showSnackBar("Email not sent");
                                                                if (player!=null)
                                                                    player.stop();

                                                            }
                                                        }

                                                        @Override
                                                        public void onFailure(Call<SmsContactApi> call, Throwable t) {
                                                            showSnackBar("SMS not sent");
                                                            if (player!=null)
                                                                player.stop();

                                                        }
                                                    });

                                                }else{
                                                    showSnackBar("Call not initiated.");
                                                    if (player!=null)
                                                        player.stop();

                                                }
                                            }

                                            @Override
                                            public void onFailure(Call<SmsContactApi> call, Throwable t) {
                                                showSnackBar("Call not initiated.");
                                                if (player!=null)
                                                    player.stop();

                                            }
                                        });
                                    }

                                }
                            });
                        }else {
                            if (player!=null)
                                player.stop();
                            showSnackBar("No added contact.");
                            ;
                        }
                    }

                    @Override
                    public void onFailure(Call<GetCallContact> call, Throwable t) {
                        if (player!=null)
                            player.stop();
                    }
                });*//*
            }

            @Override
            public void onFailure(Call<SirenModel> call, Throwable t) {
                if (player!=null)
                    player.stop();
                ;
            }
        });*/
    }

    String getTimeDate(long milli){
        SimpleDateFormat formatter = new SimpleDateFormat("DD-MM-YYYY hh:mm:ss", Locale.US);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milli);
        return formatter.format(calendar.getTime());
    }
    File destinationFile;
    public void surroundingSound(){
        CaptivePortal captivePortal;
     
      File  dir = new File(Environment.getExternalStoragePublicDirectory(
              Environment.DIRECTORY_DOWNLOADS), "recording folder");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        destinationFile = new File(dir.getPath() + File.separator +
                "REC_" + timeStamp + ".mp3");
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setAudioSamplingRate(44100);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            recorder.setOutputFile(destinationFile);
        }
        else{
            recorder.setOutputFile(destinationFile.getPath());
        }

        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        recorder.start();
         recordingCountDownTimer= new CountDownTimer(recordingTime,1000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {

                if (recorder!=null)
                {
                    recorder.stop();
                    recorder.release();
                    recorder = null;

                }


                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        Log.e("final ","ff");

                        MultipartBody.Part filePart = MultipartBody.Part.createFormData("audio", destinationFile.getName(), RequestBody.create(MediaType.parse("audio/*"), destinationFile));
                        apiInterface = RetrofitClient.getClient().create(APIInterface.class);
                        RequestBody funcBody = RequestBody.create(MediaType.parse("multipart/form-data"), Utils.insert_update_history);
                        RequestBody idBody = RequestBody.create(MediaType.parse("multipart/form-data"), getLoginApiFromShared(Utils.MySharedId));
                        RequestBody locBody = RequestBody.create(MediaType.parse("multipart/form-data"), loc);
                        Call<SmsContactApi> sendRecording = apiInterface.insert_update_history(funcBody,idBody,locBody,filePart);
                        sendRecording.enqueue(new Callback<SmsContactApi>() {
                            @Override
                            public void onResponse(Call<SmsContactApi> call, Response<SmsContactApi> response) {


                                Log.e("final res",response.body().toString());
                                if (player!=null)
                                    player.stop();
                              //  showSnackBar("Recording shared..");
                                    

                                stopSelf(id);

                            }

                            @Override
                            public void onFailure(Call<SmsContactApi> call, Throwable t) {
                                Log.e("final ","failure"+t.getLocalizedMessage());

                                if (player!=null)
                                    player.stop();
                                ;
                            }
                        });
                    }
                },1000);
            }
        }.start();
    }
    private String getLoginApiFromShared(String key) {
        SharedPreferences sharedPreferences = getSharedPreferences(Utils.MyPref, MODE_PRIVATE);
        String value = sharedPreferences.getString(key, "");
        return value;
    }



    private void showSnackBar(String msg) {

        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

    }
}