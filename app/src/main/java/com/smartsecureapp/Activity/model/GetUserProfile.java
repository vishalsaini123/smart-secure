package com.smartsecureapp.Activity.model;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class GetUserProfile {
    @SerializedName("msg")
    @Expose
    private String msg;
    @SerializedName("error")
    @Expose
    private Boolean error;
    @SerializedName("data")
    @Expose
    private List<Datum> data;

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

    public List<Datum> getData() {
        return data;
    }

    public void setData(List<Datum> data) {
        this.data = data;
    }
    public class Datum {

        @SerializedName("id")
        @Expose
        private String id;
        @SerializedName("email")
        @Expose
        private String email;
        @SerializedName("password")
        @Expose
        private String password;
        @SerializedName("username")
        @Expose
        private String username;
        @SerializedName("first_name")
        @Expose
        private String firstName;
        @SerializedName("last_name")
        @Expose
        private String lastName;
        @SerializedName("dob")
        @Expose
        private String dob;
        @SerializedName("phone")
        @Expose
        private String phone;
        @SerializedName("gender")
        @Expose
        private String gender;
        @SerializedName("profile_pic")
        @Expose
        private String profilePic;
        @SerializedName("location")
        @Expose
        private String location;
        @SerializedName("siren")
        @Expose
        private String siren;
        @SerializedName("call_preference")
        @Expose
        private String callPreference;
        @SerializedName("email_verification")
        @Expose
        private String emailVerification;
        @SerializedName("role")
        @Expose
        private String role;
        @SerializedName("status")
        @Expose
        private String status;
        @SerializedName("created")
        @Expose
        private String created;
        @SerializedName("modified")
        @Expose
        private String modified;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getDob() {
            return dob;
        }

        public void setDob(String dob) {
            this.dob = dob;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public String getProfilePic() {
            return profilePic;
        }

        public void setProfilePic(String profilePic) {
            this.profilePic = profilePic;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getSiren() {
            return siren;
        }

        public void setSiren(String siren) {
            this.siren = siren;
        }

        public String getCallPreference() {
            return callPreference;
        }

        public void setCallPreference(String callPreference) {
            this.callPreference = callPreference;
        }

        public String getEmailVerification() {
            return emailVerification;
        }

        public void setEmailVerification(String emailVerification) {
            this.emailVerification = emailVerification;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getCreated() {
            return created;
        }

        public void setCreated(String created) {
            this.created = created;
        }

        public String getModified() {
            return modified;
        }

        public void setModified(String modified) {
            this.modified = modified;
        }

    }
}
