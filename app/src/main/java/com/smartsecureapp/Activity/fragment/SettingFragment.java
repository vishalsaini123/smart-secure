package com.smartsecureapp.Activity.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.smartsecureapp.Activity.activity.ActivityEmailContact;
import com.smartsecureapp.Activity.activity.ActivityHistory;
import com.smartsecureapp.Activity.activity.Activity_WhatsApp;
import com.smartsecureapp.Activity.activity.CallActivity;
import com.smartsecureapp.Activity.activity.ContactUsActivity;
import com.smartsecureapp.Activity.activity.DeleteAccountActivity;
import com.smartsecureapp.Activity.activity.HistoryActivity;
import com.smartsecureapp.Activity.activity.PasswordResetActivity;
import com.smartsecureapp.Activity.activity.PersonalActivity;
import com.smartsecureapp.Activity.activity.SMSActivity;
import com.smartsecureapp.Activity.activity.SignInActivity;
import com.smartsecureapp.Activity.activity.SignOutActivity;
import com.smartsecureapp.Activity.activity.SignUpActivity;
import com.smartsecureapp.Activity.api.APIInterface;
import com.smartsecureapp.Activity.api.RetrofitClient;
import com.smartsecureapp.Activity.model.SirenModel;
import com.smartsecureapp.Activity.model.UpdateProfile;
import com.smartsecureapp.Activity.util.Utils;
import com.smartsecureapp.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingFragment extends Fragment {

    RelativeLayout relative_personal,relative_Call,relative_Email,relative_whatsApp,relative_sms,rootLayout,
    relative_history,relative_password_reset,relative_contact_us,relative_sign_out,relative_delete_account;
    TextView txt_privacy_policy,txt_term_condition;
    Switch simpleSwitch2;
    APIInterface apiInterface;
    ProgressBar loading;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_setting, container, false);

        relative_personal = view.findViewById(R.id.relative_personal);
        relative_Call = view.findViewById(R.id.relative_Call);
        relative_Email = view.findViewById(R.id.relative_Email);
        relative_whatsApp = view.findViewById(R.id.relative_whatsApp);
        relative_sms = view.findViewById(R.id.relative_sms);
        relative_history = view.findViewById(R.id.relative_history);
        relative_password_reset = view.findViewById(R.id.relative_password_reset);
        relative_contact_us = view.findViewById(R.id.relative_contact_us);
        relative_sign_out = view.findViewById(R.id.relative_sign_out);
        relative_delete_account = view.findViewById(R.id.relative_delete_account);

        txt_privacy_policy = view.findViewById(R.id.txt_privacy_policy);
        txt_term_condition = view.findViewById(R.id.txt_term_condition);
        txt_term_condition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Utils.term_and_conditions));
                startActivity(browserIntent);
            }
        });

        txt_privacy_policy.setPaintFlags(txt_privacy_policy.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        txt_term_condition.setPaintFlags(txt_term_condition.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        simpleSwitch2 = view.findViewById(R.id.simpleSwitch2);
        loading = view.findViewById(R.id.loading);
        rootLayout = view.findViewById(R.id.rootLayout);

        getSirenValue();

        simpleSwitch2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                sirenUpdateApi(b);
            }
        });


        relative_personal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), PersonalActivity.class));
            }
        });

        relative_Call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), CallActivity.class));
            }
        });
        relative_Email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), ActivityEmailContact.class));
            }
        });
        relative_whatsApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), Activity_WhatsApp.class));
            }
        });
        relative_sms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), SMSActivity.class));
            }
        });
        relative_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), ActivityHistory.class));
            }
        });
        relative_password_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), PasswordResetActivity.class));
            }
        });
        relative_contact_us.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), ContactUsActivity.class));
            }
        });
        relative_sign_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOutFromShared();
                startActivity(new Intent(getActivity(), SignInActivity.class));
                getActivity().finish();
            }
        });
        relative_delete_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(getContext(), android.R.style.Theme_Dialog);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dlg_delete_account);
                dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                MaterialButton cancelButton = (MaterialButton) dialog.findViewById(R.id.cancelButton);
                MaterialButton doneButton = (MaterialButton) dialog.findViewById(R.id.confirmButton);
                doneButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deleteAccountApi();
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


        return view;
    }

    private void deleteAccountApi(){
        loading.setVisibility(View.VISIBLE);
        apiInterface = RetrofitClient.getClient().create(APIInterface.class);
        Call<UpdateProfile> callDeleteAccount = apiInterface.deleteUser(Utils.deleteUser,getLoginApiFromShared(Utils.MySharedId));
        callDeleteAccount.enqueue(new Callback<UpdateProfile>() {
            @Override
            public void onResponse(Call<UpdateProfile> call, Response<UpdateProfile> response) {
                loading.setVisibility(View.INVISIBLE);
                if (response != null && response.body() != null && !response.body().getError()) {
                    signOutFromShared();
                    startActivity(new Intent(getActivity(), SignUpActivity.class));
                    getActivity().finish();
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

    private void getSirenValue() {
        apiInterface = RetrofitClient.getClient().create(APIInterface.class);
        Call<SirenModel> call = apiInterface.get_user_settings(Utils.get_user_settings,getLoginApiFromShared(Utils.MySharedId));
        call.enqueue(new Callback<SirenModel>() {
            @Override
            public void onResponse(Call<SirenModel> call, Response<SirenModel> response) {
                if (response!=null && !response.body().getError() && response.body().getSiren().equalsIgnoreCase("1")){
                    simpleSwitch2.setChecked(true);
                }else {
                    simpleSwitch2.setChecked(false);
                }
            }

            @Override
            public void onFailure(Call<SirenModel> call, Throwable t) {
                simpleSwitch2.setChecked(false);
            }
        });
    }

    private void sirenUpdateApi(boolean b) {
        String sirenValue = "0";
        if (b){
            sirenValue = "1";
        }
        apiInterface = RetrofitClient.getClient().create(APIInterface.class);
        Call<UpdateProfile> call = apiInterface.user_settings_update(Utils.user_settings_update,getLoginApiFromShared(Utils.MySharedId),sirenValue,"");
        call.enqueue(new Callback<UpdateProfile>() {
            @Override
            public void onResponse(Call<UpdateProfile> call, Response<UpdateProfile> response) {
                getSirenValue();
            }

            @Override
            public void onFailure(Call<UpdateProfile> call, Throwable t) {
                getSirenValue();
            }
        });
    }

    private String getLoginApiFromShared(String key) {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(Utils.MyPref, MODE_PRIVATE);
        String value = sharedPreferences.getString(key, "");
        return value;
    }

    private void signOutFromShared() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(Utils.MyPref, MODE_PRIVATE);
        sharedPreferences.edit().clear().commit();
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