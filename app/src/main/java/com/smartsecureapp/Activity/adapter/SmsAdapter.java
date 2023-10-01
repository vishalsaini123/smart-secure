package com.smartsecureapp.Activity.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.smartsecureapp.Activity.callback.SmsCallback;
import com.smartsecureapp.R;

import java.util.ArrayList;

public class SmsAdapter extends RecyclerView.Adapter<SmsAdapter.CallViewHolder> {

    ArrayList<String> nameList;
    SmsCallback smsCallback;

    public SmsAdapter(ArrayList<String> nameList, SmsCallback smsCallback) {
        this.nameList = nameList;
        this.smsCallback = smsCallback;
    }

    @NonNull
    @Override
    public CallViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sms_contact_item, parent, false);
        return new CallViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CallViewHolder holder, int position) {
        if (!nameList.get(position).isEmpty()) {
            holder.name.setText(nameList.get(position));
        }
        holder.deleteContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                smsCallback.onItemDelete(position);
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
        public CallViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            deleteContact = itemView.findViewById(R.id.deleteContact);
        }
    }
}
