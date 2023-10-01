package com.smartsecureapp.Activity.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.smartsecureapp.Activity.adapter.HistoryAdapter;
import com.smartsecureapp.Activity.adapter.SmsAdapter;
import com.smartsecureapp.Activity.api.APIInterface;
import com.smartsecureapp.Activity.api.RetrofitClient;
import com.smartsecureapp.Activity.callback.SmsCallback;
import com.smartsecureapp.Activity.model.GetHistory;
import com.smartsecureapp.Activity.model.GetSmsContact;
import com.smartsecureapp.Activity.model.SmsContactApi;
import com.smartsecureapp.Activity.util.Utils;
import com.smartsecureapp.R;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityHistory extends AppCompatActivity {
    TextView txt_privacy_policy, txt_term_condition, noContact;
    ImageView img_back;

    ImageView addButton;
    ArrayList<String> nameList = new ArrayList<>();
    ArrayList<String> phoneList = new ArrayList<>();
    ArrayList<String> orderList = new ArrayList<>();
    private static final int REQUEST_READ_CONTACTS_PERMISSION = 0;
    private static final int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 101;

    RecyclerView contactRecyclerView;
    MaterialButton addContactButton;
    ProgressBar loading;
    APIInterface apiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historyactivity);


        requestContactsPermission();
        checkAndRequestPermission();
        txt_privacy_policy = findViewById(R.id.txt_privacy_policy);
        txt_term_condition = findViewById(R.id.txt_term_condition);
        txt_term_condition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(ActivityHistory.this, TermsConditionsActivity.class);
                startActivity(browserIntent);
            }
        });
        img_back = findViewById(R.id.img_back);
        addButton = findViewById(R.id.addButton);
        contactRecyclerView = findViewById(R.id.contactRecyclerView);
        addContactButton = findViewById(R.id.addContactButton);
        loading = findViewById(R.id.loading);
        noContact = findViewById(R.id.noContact);

        clickListeners();
        img_back = findViewById(R.id.img_back);

        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


       // txt_privacy_policy.setPaintFlags(txt_privacy_policy.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
       // txt_term_condition.setPaintFlags(txt_term_condition.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
    }

    private void clickListeners() {
        getHistoryApi();

        addContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendSmsContactApi();
            }
        });
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(ActivityHistory.this);
                bottomSheetDialog.setContentView(R.layout.add_contact_sheet);
                TextView manualContact = bottomSheetDialog.findViewById(R.id.manualContact);
                TextView pickContact = bottomSheetDialog.findViewById(R.id.pickContact);

                manualContact.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bottomSheetDialog.dismiss();
                        Dialog dialog = new Dialog(ActivityHistory.this, android.R.style.Theme_Dialog);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.dlg_manual_contact);
                        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        TextInputEditText nameEditText = (TextInputEditText) dialog.findViewById(R.id.nameEditText);
                        TextInputEditText phoneEditText = (TextInputEditText) dialog.findViewById(R.id.phoneEditText);
                        Button doneButton = (Button) dialog.findViewById(R.id.doneButton);
                        doneButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (!nameEditText.getText().toString().isEmpty() && !phoneEditText.getText().toString().isEmpty()) {
                                    nameList.add(nameEditText.getText().toString());
                                    phoneList.add(phoneEditText.getText().toString());
                                    orderList.add(String.valueOf(nameList.size()));
                                    setAdapter(nameList);
                                    dialog.dismiss();
                                } else {
                                    if (nameEditText.getText().toString().isEmpty()) {
                                        nameEditText.setError("All fields are required.");
                                    } else if (phoneEditText.getText().toString().isEmpty()) {
                                        phoneEditText.setError("All fields are required.");
                                    }
                                }
                            }
                        });
                        dialog.show();
                    }
                });

                pickContact.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                        startActivityForResult(intent, 1);
                        bottomSheetDialog.dismiss();
                    }
                });
                bottomSheetDialog.show();
            }
        });
    }

    private void sendSmsContactApi() {
        loading.setVisibility(View.VISIBLE);
        apiInterface = RetrofitClient.getClient().create(APIInterface.class);
        Call<SmsContactApi> call = apiInterface.sms_contacts(Utils.sms_contacts, getLoginApiFromShared(Utils.MySharedId), TextUtils.join(",", nameList), TextUtils.join(",", phoneList), TextUtils.join(",", orderList));
        call.enqueue(new Callback<SmsContactApi>() {
            @Override
            public void onResponse(Call<SmsContactApi> call, Response<SmsContactApi> response) {
                if (response.body().getError() != null && !response.body().getError()) {
                    loading.setVisibility(View.INVISIBLE);
                    onBackPressed();

                } else {
                    loading.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onFailure(Call<SmsContactApi> call, Throwable t) {
                loading.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void getHistoryApi() {
        loading.setVisibility(View.VISIBLE);
        apiInterface = RetrofitClient.getClient().create(APIInterface.class);
        Call<GetHistory> callHistory = apiInterface.fetchHistory(getLoginApiFromShared(Utils.MySharedId), Utils.fetchHistory);
        callHistory.enqueue(new Callback<GetHistory>() {
            @Override
            public void onResponse(Call<GetHistory> call, Response<GetHistory> response) {
                loading.setVisibility(View.INVISIBLE);
                if (response != null && response.body() != null && response.body().getData() != null && !response.body().getData().isEmpty()) {
                    resetHistoryList(response);
                }
            }

            @Override
            public void onFailure(Call<GetHistory> call, Throwable t) {
                loading.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void getSmsContactApi() {
        loading.setVisibility(View.VISIBLE);
        apiInterface = RetrofitClient.getClient().create(APIInterface.class);
        Call<GetSmsContact> callGetSms = apiInterface.get_sms_contacts(getLoginApiFromShared(Utils.MySharedId), Utils.get_sms_contacts);
        callGetSms.enqueue(new Callback<GetSmsContact>() {
            @Override
            public void onResponse(Call<GetSmsContact> call, Response<GetSmsContact> response) {
                loading.setVisibility(View.INVISIBLE);
                if (response != null && response.body() != null && !response.body().getContacts().isEmpty()) {
                    resetList(response);
                }
            }

            @Override
            public void onFailure(Call<GetSmsContact> call, Throwable t) {
                loading.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void resetHistoryList(Response<GetHistory> response) {
        nameList.clear();
        phoneList.clear();
        orderList.clear();
        if (response != null && response.body() != null && !response.body().getData().isEmpty()) {
            for (int i = 0; i < response.body().getData().size(); i++) {
                if (response.body().getData().get(i).getAudio().length() > 0) {
                    nameList.add(response.body().getData().get(i).getAudio());
                }
            }
            if (!nameList.isEmpty()) {
                setAdapter(nameList);
                noContact.setVisibility(View.GONE);
            } else {
                noContact.setVisibility(View.VISIBLE);
            }
        } else {
            noContact.setVisibility(View.VISIBLE);
        }
    }

    private void resetList(Response<GetSmsContact> response) {
        nameList.clear();
        phoneList.clear();
        orderList.clear();
        if (response != null && response.body() != null && !response.body().getContacts().isEmpty()) {
            for (int i = 0; i < response.body().getContacts().size(); i++) {
                if (response.body().getContacts().get(i).getName().length() > 0 && response.body().getContacts().get(i).getPhone().length() > 0) {
                    nameList.add(response.body().getContacts().get(i).getName());
                    phoneList.add(response.body().getContacts().get(i).getPhone());
                    orderList.add(String.valueOf(i + 1));
                }
            }
            if (!nameList.isEmpty()) {
                setAdapter(nameList);
                noContact.setVisibility(View.GONE);
            } else {
                noContact.setVisibility(View.VISIBLE);
            }
        } else {
            noContact.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == 1) && (resultCode == RESULT_OK)) {
            Cursor cursor = null;
            try {
                Uri uri = data.getData();
                cursor = getContentResolver().query(uri, new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME}, null, null, null);
                if (cursor != null && cursor.moveToNext()) {
                    String phone = cursor.getString(0);
                    String name = cursor.getString(1);
                    nameList.add(name);
                    phoneList.add(phone);
                    orderList.add(String.valueOf(nameList.size()));
                    setAdapter(nameList);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void requestContactsPermission() {
        if (!hasContactsPermission()) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_READ_CONTACTS_PERMISSION);
        }
    }

    private boolean hasContactsPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) ==
                PackageManager.PERMISSION_GRANTED;
    }

    public void setAdapter(ArrayList<String> nameList) {
        contactRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        HistoryAdapter callAdapter = new HistoryAdapter(nameList, new SmsCallback() {
            @Override
            public void onItemDelete(int position) {
                smsContactDeleted(position, nameList);
            }
        });
        contactRecyclerView.setAdapter(callAdapter);
    }

    private void smsContactDeleted(int position, ArrayList<String> nameList) {
        nameList.remove(position);
        phoneList.remove(position);
        orderList.remove(position);
        setAdapter(nameList);
    }

    private String getLoginApiFromShared(String key) {
        SharedPreferences sharedPreferences = getSharedPreferences(Utils.MyPref, MODE_PRIVATE);
        String value = sharedPreferences.getString(key, "");
        return value;
    }

    private void checkAndRequestPermission() {
        int writePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (writePermission == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Write Permission required for the download feature", Toast.LENGTH_SHORT).show();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                // Permission denied, handle accordingly
                Toast.makeText(this, "Write Permission required for the download feature", Toast.LENGTH_SHORT).show();
            }
        }
    }
}