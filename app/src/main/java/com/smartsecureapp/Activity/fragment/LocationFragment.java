package com.smartsecureapp.Activity.fragment;

import android.graphics.Paint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.smartsecureapp.R;


public class LocationFragment extends Fragment {
    TextView txt_privacy_policy,txt_term_condition;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_location, container, false);
        txt_privacy_policy = view.findViewById(R.id.txt_privacy_policy);
        txt_term_condition = view.findViewById(R.id.txt_term_condition);

        txt_privacy_policy.setPaintFlags(txt_privacy_policy.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        txt_term_condition.setPaintFlags(txt_term_condition.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        return view;
    }
}