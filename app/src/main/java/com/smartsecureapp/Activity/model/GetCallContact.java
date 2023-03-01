package com.smartsecureapp.Activity.model;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GetCallContact {
    @SerializedName("msg")
    @Expose
    private String msg;
    @SerializedName("error")
    @Expose
    private Boolean error;
    @SerializedName("contacts")
    @Expose
    private List<Contact> contacts;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Boolean getError() {
        return error;
    }

    public void setError(Boolean error) {
        this.error = error;
    }

    public List<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
    }
    public class Contact {

        @SerializedName("primary")
        @Expose
        private String primary;
        @SerializedName("secondary")
        @Expose
        private String secondary;
        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("phone")
        @Expose
        private String phone;
        @SerializedName("order")
        @Expose
        private String order;

        public String getPrimary() {
            return primary;
        }

        public void setPrimary(String primary) {
            this.primary = primary;
        }

        public String getSecondary() {
            return secondary;
        }

        public void setSecondary(String secondary) {
            this.secondary = secondary;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getOrder() {
            return order;
        }

        public void setOrder(String order) {
            this.order = order;
        }

    }
}
