package com.smartsecureapp.Activity.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Paint;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.smartsecureapp.Activity.activity.PasswordResetActivity;
import com.smartsecureapp.Activity.activity.PrivacyPolicyActivity;
import com.smartsecureapp.Activity.activity.SignInActivity;
import com.smartsecureapp.Activity.activity.TermsConditionsActivity;
import com.smartsecureapp.Activity.api.APIInterface;
import com.smartsecureapp.Activity.api.RetrofitClient;
import com.smartsecureapp.Activity.api.RetrofitClientMakeCall;
import com.smartsecureapp.Activity.model.CommonModel;
import com.smartsecureapp.Activity.model.GetCallContact;
import com.smartsecureapp.Activity.model.GetEmailContact;
import com.smartsecureapp.Activity.model.GetSmsContact;
import com.smartsecureapp.Activity.model.SendCallNotification;
import com.smartsecureapp.Activity.model.SirenModel;
import com.smartsecureapp.Activity.model.SmsContactApi;
import com.smartsecureapp.Activity.util.ProgressHelper;
import com.smartsecureapp.Activity.util.Utils;
import com.smartsecureapp.R;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
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

public class EmergencyFragment extends Fragment {
    TextView txt_privacy_policy, txt_term_condition;
    Dialog dialog;
    ImageView emergencyButton;
    ProgressHelper loading;
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
    int i =0;
    boolean isNotificationSent = false;
    Call<SmsContactApi> sendRecording;
    CountDownTimer countDownTimer;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_emergency, container, false);

        txt_privacy_policy = view.findViewById(R.id.txt_privacy_policy);
        txt_term_condition = view.findViewById(R.id.txt_term_condition);
        txt_term_condition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(getActivity(), TermsConditionsActivity.class);
                startActivity(browserIntent);
            }
        });
        txt_privacy_policy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(getActivity(), PrivacyPolicyActivity.class);
                startActivity(browserIntent);
            }
        });
      //  txt_privacy_policy.setPaintFlags(txt_privacy_policy.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
      //  txt_term_condition.setPaintFlags(txt_term_condition.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        emergencyButton = view.findViewById(R.id.emergencyButton);
        loading = new ProgressHelper();
        rootLayout = view.findViewById(R.id.rootLayout);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        emergencyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                emergencyButton.setEnabled(false);
                onEmergencyClick();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        emergencyButton.setEnabled(true);
                    }
                },2000);
               // showDialog();
            }
        });


        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US);
        Date now = new Date();
        // Record to the external cache directory for visibility
        fileName = getActivity().getExternalCacheDir().getAbsolutePath();
        fileName += "/" + formatter.format(now) + ".3gp";
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        if (canGetLocation())
        {
            if (!hasLocationPermission())
            {
                requestContactsPermission();
                requestLocationPermission();
            }

        }
        else{showSettingsAlert();}
    }

    private void requestContactsPermission() {
        if (!hasContactsPermission()) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.RECORD_AUDIO,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_READ_CONTACTS_PERMISSION);
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

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED;
    }

    public void onEmergencyClick() {


       loading.showDialog(getActivity(),"Loading...");
        siren = "0";
        showDialog();
        surroundingSound();
        apiInterface = RetrofitClient.getClient().create(APIInterface.class);
        apiInterface2 = RetrofitClientMakeCall.getClient().create(APIInterface.class);
        Call<SirenModel> call = apiInterface.get_user_settings(Utils.get_user_settings, getLoginApiFromShared(Utils.MySharedId));
        call.enqueue(new Callback<SirenModel>() {
            @Override
            public void onResponse(Call<SirenModel> call, Response<SirenModel> response) {

                Log.e("siren",response.body().getSiren());
                if (response != null && !response.body().getError() && response.body().getSiren().equalsIgnoreCase("1")) {
                    AssetFileDescriptor afd = null;
                    try {

                        Log.e("siren1",response.body().getSiren());
                        afd = getContext().getAssets().openFd("siren_sound.mp3");
                        player = new MediaPlayer();
                        player.setDataSource(afd.getFileDescriptor());
                        player.prepare();
                        player.setLooping(true);
                        player.start();
                        siren = response.body().getSiren();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                }
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_READ_CONTACTS_PERMISSION);


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

                                    /*for ( i =0 ; i<response.body().getContacts().size();i++)
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



                                                                                                    if (response.code()==200)
                                                                                                    {
                                                                                                        if (!response.body().isError()) {
                                                                                                            Call<SendCallNotification> callSendSms = apiInterface.send_notification(getLoginApiFromShared(Utils.MySharedId),Utils.send_sms_notifications, getLoginApiFromShared(Utils.MySharedUsername), lat+","+lng);
                                                                                                            callSendSms.enqueue(new Callback<SendCallNotification>() {
                                                                                                                @Override
                                                                                                                public void onResponse(Call<SendCallNotification> call, Response<SendCallNotification> response) {
                                                                                                                    if (response.body()!=null)
                                                                                                                    {
                                                                                                                        if (!response.body().isError()) {

                                                                                                                            Call<SendCallNotification> callSendSms = apiInterface.send_notification(getLoginApiFromShared(Utils.MySharedId),Utils.send_email_notifications, getLoginApiFromShared(Utils.MySharedUsername), lat+","+lng);
                                                                                                                            callSendSms.enqueue(new Callback<SendCallNotification>() {
                                                                                                                                @Override
                                                                                                                                public void onResponse(Call<SendCallNotification> call, Response<SendCallNotification> response) {
                                                                                                                                    if (response.body()!=null){
                                                                                                                                        if (!response.body().isError()) {

                                                                                                                                            showSnackBar("Notification sent!");


                                                                                                                                        }
                                                                                                                                    }else {
                                                                                                                                        showSnackBar("Notification not sent");
                                                                                                                                        if (siren.equalsIgnoreCase("1")){
                                                                                                                                            player.stop();
                                                                                                                                        }
                                                                                                                                        loading.dismissDialog();
                                                                                                                                    }
                                                                                                                                }

                                                                                                                                @Override
                                                                                                                                public void onFailure(Call<SendCallNotification> call, Throwable t) {
                                                                                                                                    showSnackBar("Notification not sent");
                                                                                                                                    if (siren.equalsIgnoreCase("1")){
                                                                                                                                        player.stop();
                                                                                                                                    }
                                                                                                                                    loading.dismissDialog();
                                                                                                                                }
                                                                                                                            });

                                                                                                                        }
                                                                                                                    }else {
                                                                                                                        showSnackBar("Notification not sent");
                                                                                                                        if (siren.equalsIgnoreCase("1")){
                                                                                                                            player.stop();
                                                                                                                        }
                                                                                                                        loading.dismissDialog();
                                                                                                                    }
                                                                                                                }

                                                                                                                @Override
                                                                                                                public void onFailure(Call<SendCallNotification> call, Throwable t) {
                                                                                                                    showSnackBar("Notification not sent");
                                                                                                                    if (siren.equalsIgnoreCase("1")){
                                                                                                                        player.stop();
                                                                                                                    }
                                                                                                                    loading.dismissDialog();
                                                                                                                }
                                                                                                            });


                                                                                                        }
                                                                                                    }

                                                                                                    else {
                                                                                                        showSnackBar("Notification not sent");
                                                                                                        if (siren.equalsIgnoreCase("1")){
                                                                                                            player.stop();
                                                                                                        }
                                                                                                        loading.dismissDialog();
                                                                                                    }
                                                                                                }

                                                                                                @Override
                                                                                                public void onFailure(Call<SendCallNotification> call, Throwable t) {
                                                                                                    showSnackBar("Notification not sent");
                                                                                                    if (siren.equalsIgnoreCase("1")){
                                                                                                        player.stop();
                                                                                                    }
                                                                                                    loading.dismissDialog();
                                                                                                }
                                                                                            });
                                                                                        }




                                                                                    }else {
                                                                                        showSnackBar("Please add sms contacts");
                                                                                        if (siren.equalsIgnoreCase("1")){
                                                                                            player.stop();
                                                                                        }
                                                                                        loading.dismissDialog();
                                                                                    }
                                                                                }

                                                                                @Override
                                                                                public void onFailure(Call<SmsContactApi> call, Throwable t) {
                                                                                    showSnackBar("Please add sms contacts");
                                                                                    if (siren.equalsIgnoreCase("1")){
                                                                                        player.stop();
                                                                                    }
                                                                                    loading.dismissDialog();
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
                                                                        loading.dismissDialog();
                                                                    }
                                                                });

                                                            }else {
                                                                showSnackBar("Email not sent");
                                                                if (siren.equalsIgnoreCase("1")){
                                                                    player.stop();
                                                                }
                                                                loading.dismissDialog();
                                                            }
                                                        }

                                                        @Override
                                                        public void onFailure(Call<SmsContactApi> call, Throwable t) {
                                                            showSnackBar("Email not sent");
                                                            if (siren.equalsIgnoreCase("1")){
                                                                player.stop();
                                                            }
                                                            loading.dismissDialog();
                                                        }
                                                    });

                                                }else{
                                                    showSnackBar("Call not initiated.");
                                                    if (siren.equalsIgnoreCase("1")){
                                                        player.stop();
                                                    }
                                                    loading.dismissDialog();
                                                }
                                            }

                                            @Override
                                            public void onFailure(Call<SmsContactApi> call, Throwable t) {
                                                showSnackBar("Call not initiated.");
                                                if (siren.equalsIgnoreCase("1")){
                                                    player.stop();
                                                }
                                                loading.dismissDialog();
                                            }
                                        });
                                    }*/

                    }
                });



                Call<CommonModel> callGetSms = apiInterface.emergency( Utils.emergency,getLoginApiFromShared(Utils.MySharedId),loc);
                callGetSms.enqueue(new Callback<CommonModel>() {
                    @Override
                    public void onResponse(Call<CommonModel> call, Response<CommonModel> response) {
                        if (response != null && response.body() != null ) {

                            showSnackBar(response.body().getMsg());


                        }
                        else {
                            if (siren.equalsIgnoreCase("1")){
                                player.stop();
                            }
                            showSnackBar(response.body().getMsg());
                            loading.dismissDialog();
                        }
                    }

                    @Override
                    public void onFailure(Call<CommonModel> call, Throwable t) {
                        if (siren.equalsIgnoreCase("1")){
                            player.stop();
                        }
                        loading.dismissDialog();
                    }
                });
            }

            @Override
            public void onFailure(Call<SirenModel> call, Throwable t) {
                if (siren.equalsIgnoreCase("1")){
                    player.stop();
                }
                loading.dismissDialog();
            }
        });
    }
    private String getDateAndTime(){
        @SuppressLint("SimpleDateFormat") DateFormat dfDate = new SimpleDateFormat("yyyyMMdd");
        String date=dfDate.format(Calendar.getInstance().getTime());
        @SuppressLint("SimpleDateFormat") DateFormat dfTime = new SimpleDateFormat("HHmm");
        String time = dfTime.format(Calendar.getInstance().getTime());
        return date + "-" + time;
    }

    File destinationFile;
    public void surroundingSound(){


        File  dir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), "recording folder");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        destinationFile = new File(dir.getPath() + File.separator +
                "REC_" + timeStamp + ".mp3");
        Log.e("destination file",destinationFile.getPath());

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
         countDownTimer = new CountDownTimer(200000,1000) {
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

                        MultipartBody.Part filePart = MultipartBody.Part.createFormData("audio", destinationFile.getName(), RequestBody.create(MediaType.parse("audio/*"), destinationFile));
                        apiInterface = RetrofitClient.getClient().create(APIInterface.class);
                        RequestBody funcBody = RequestBody.create(MediaType.parse("multipart/form-data"), Utils.insert_update_history);
                        RequestBody idBody = RequestBody.create(MediaType.parse("multipart/form-data"), getLoginApiFromShared(Utils.MySharedId));
                        RequestBody locBody = RequestBody.create(MediaType.parse("multipart/form-data"), loc);
                       sendRecording = apiInterface.insert_update_history(funcBody,idBody,locBody,filePart);
                        sendRecording.enqueue(new Callback<SmsContactApi>() {
                            @Override
                            public void onResponse(Call<SmsContactApi> call, Response<SmsContactApi> response) {
                                if (siren.equalsIgnoreCase("1")){
                                    player.stop();
                                }

                                loading.dismissDialog();
                            }

                            @Override
                            public void onFailure(Call<SmsContactApi> call, Throwable t) {
                                if (siren.equalsIgnoreCase("1")){
                                    player.stop();
                                }
                                loading.dismissDialog();
                            }
                        });
                    }

                },1000);
            }
        }.start();
    }

    private String getLoginApiFromShared(String key) {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(Utils.MyPref, MODE_PRIVATE);
        String value = sharedPreferences.getString(key, "");
        return value;
    }

    private void showSnackBar(String msg) {

        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();

    }

    void showDialog(){

      Dialog dialog = new Dialog(getContext(), android.R.style.Theme_Dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dlg_stop_emergencytimer);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.black);
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        TextInputEditText passwordEditText = (TextInputEditText) dialog.findViewById(R.id.passwordEditText);
        MaterialButton doneButton = (MaterialButton) dialog.findViewById(R.id.confirmButton);
        TextView forgotpassBtn = (TextView) dialog.findViewById(R.id.dlg_forgotpassbtn);
        forgotpassBtn.setOnClickListener(view ->  startActivity(new Intent(getActivity(), PasswordResetActivity.class)));

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("comes ","comes");
                Log.e("comes ","comes"+passwordEditText.getText().toString().equalsIgnoreCase(getLoginApiFromShared(Utils.MySharedPassword)));

                if (!passwordEditText.getText().toString().isEmpty() && passwordEditText.getText().toString().equalsIgnoreCase(getLoginApiFromShared(Utils.MySharedPassword))) {

                    Log.e("comes ","comes");
                    loading.dismissDialog();
                    dialog.dismiss();
                    if (sendRecording!=null)
                    { sendRecording.cancel();}

                    if (countDownTimer!=null)
                    {
                        countDownTimer.cancel();
                    }

                    if (recorder!=null)
                    {
                        recorder.stop();
                        recorder.release();
                        recorder =null;
                    }
                    if (player!=null)
                    {
                        player.stop();
                    }

                    if (countDownTimer!=null)
                        countDownTimer.onFinish();
                }
                else {
                    Toast.makeText(getActivity(), "Please enter the correct password!", Toast.LENGTH_SHORT).show();

                }

            }
        });

        dialog.show();
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

        // Setting Dialog Title
        alertDialog.setTitle("Error!");

        // Setting Dialog Message
        alertDialog.setMessage("Please press ok to enable location");

        // On pressing Settings button
        alertDialog.setPositiveButton(
                "ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                });

        alertDialog.show();
    }

    public boolean canGetLocation() {
        boolean result = true;
        LocationManager lm;
        boolean gpsEnabled = false;
        boolean networkEnabled = false;

        lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        // exceptions will be thrown if provider is not permitted.
        try {
            gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            networkEnabled = lm
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        return gpsEnabled && networkEnabled;
    }
}