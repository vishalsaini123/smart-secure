package com.smartsecureapp.Activity.callback;

public interface CallContactCallback {
    void onItemDelete(int position);
    void onPrimaryChanged(String primary);
    void onSecondaryChanged(String secondary);
}
