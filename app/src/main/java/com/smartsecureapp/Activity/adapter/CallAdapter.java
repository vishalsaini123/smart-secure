package com.smartsecureapp.Activity.adapter;

import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.smartsecureapp.Activity.activity.CallActivity;
import com.smartsecureapp.Activity.callback.CallContactCallback;
import com.smartsecureapp.Activity.callback.SmsCallback;
import com.smartsecureapp.R;

import java.util.ArrayList;

public class CallAdapter extends RecyclerView.Adapter<CallAdapter.CallViewHolder> {

    ArrayList<String> nameList;
    CallContactCallback smsCallback;
    String primaryNumber;
    String secondaryNumber;
    ArrayList<String> phoneList;

    public CallAdapter(ArrayList<String> nameList, ArrayList<String> phoneList, String primaryNumber, String secondaryNumber, CallContactCallback smsCallback) {
        this.nameList = nameList;
        this.smsCallback = smsCallback;
        this.primaryNumber = primaryNumber;
        this.secondaryNumber = secondaryNumber;
        this.phoneList = phoneList;
    }

    @NonNull
    @Override
    public CallViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.call_contact_item, parent, false);
        return new CallViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CallViewHolder holder, int position) {
        if (!nameList.get(position).isEmpty() && !phoneList.get(position).isEmpty()) {
            if (phoneList.get(position).equalsIgnoreCase(primaryNumber)){
                holder.name.setText(nameList.get(position)+"- Primary");
            }else if (phoneList.get(position).equalsIgnoreCase(secondaryNumber)){
                holder.name.setText(nameList.get(position)+"- Secondary");
            }else {
                holder.name.setText(nameList.get(position));
            }
        }
        holder.deleteContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                smsCallback.onItemDelete(position);
            }
        });
        holder.optionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (nameList.size() > 1) {
                    Dialog dialog = new Dialog(view.getContext(), android.R.style.Theme_Dialog);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.dlg_select_primary_secondary);
                    dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    MaterialButton primary = dialog.findViewById(R.id.primaryButton);
                    MaterialButton secondary = dialog.findViewById(R.id.secondaryButton);
                    if (phoneList.get(position).equalsIgnoreCase(primaryNumber)) {
                        primary.setVisibility(View.INVISIBLE);
                    }
                    if (phoneList.get(position).equalsIgnoreCase(secondaryNumber)) {
                        secondary.setVisibility(View.INVISIBLE);
                    }
                    primary.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            primaryNumber = phoneList.get(position);
                            if (phoneList.get(position).equalsIgnoreCase(secondaryNumber)){
                                smsCallback.onSecondaryChanged("");
                            }
                            smsCallback.onPrimaryChanged(primaryNumber);
                            dialog.dismiss();
                        }
                    });
                    secondary.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            secondaryNumber = phoneList.get(position);
                            if (phoneList.get(position).equalsIgnoreCase(primaryNumber)){
                                smsCallback.onPrimaryChanged("");
                            }
                            smsCallback.onSecondaryChanged(secondaryNumber);
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return nameList.size();
    }

    public class CallViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageButton deleteContact;
        ImageView optionButton;
        public CallViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            deleteContact = itemView.findViewById(R.id.deleteContact);
            optionButton = itemView.findViewById(R.id.optionButton);
        }
    }
}
