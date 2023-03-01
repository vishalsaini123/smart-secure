package com.smartsecureapp.Activity.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartsecureapp.R;

public class DeleteAccountActivity extends AppCompatActivity {
    TextView txt_privacy_policy,txt_term_condition;
    ImageView img_back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_account);

        txt_privacy_policy = findViewById(R.id.txt_privacy_policy);
        txt_term_condition = findViewById(R.id.txt_term_condition);
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
}