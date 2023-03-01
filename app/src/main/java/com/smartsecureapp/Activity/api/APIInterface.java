package com.smartsecureapp.Activity.api;
import com.smartsecureapp.Activity.adapter.SmsAdapter;
import com.smartsecureapp.Activity.model.GetCallContact;
import com.smartsecureapp.Activity.model.GetEmailContact;
import com.smartsecureapp.Activity.model.GetHistory;
import com.smartsecureapp.Activity.model.GetSmsContact;
import com.smartsecureapp.Activity.model.GetUserProfile;
import com.smartsecureapp.Activity.model.LoginApi;
import com.smartsecureapp.Activity.model.SirenModel;
import com.smartsecureapp.Activity.model.SmsContactApi;
import com.smartsecureapp.Activity.model.UpdateProfile;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface APIInterface {
    public static String baseUrlSuffix = "users.php";
    public static String baseUrlSuffixMakeCall = "make_call.php";
    public static String baseUrlSuffixSendSms = "send_sms.php";
    @FormUrlEncoded
    @POST(baseUrlSuffix)
    Call<LoginApi> login(
            @Field("email") String email,
            @Field("func") String func,
            @Field("password") String password

    );

    @FormUrlEncoded
    @POST(baseUrlSuffix)
    Call<SmsContactApi> signup(
            @Field("email") String email,
            @Field("password") String password,
            @Field("func") String func,
            @Field("first_name") String first_name,
            @Field("last_name") String last_name,
            @Field("gender") String gender,
            @Field("phone") String phone,
            @Field("role") String role,
            @Field("location") String location

    );

    @FormUrlEncoded
    @POST(baseUrlSuffix)
    Call<SmsContactApi> forgot_password_second(
            @Field("email") String email,
            @Field("func") String forgot_password_second,
            @Field("code") String code

    );

    @FormUrlEncoded
    @POST(baseUrlSuffix)
    Call<SmsContactApi> sms_contacts(
            @Field("func") String func,
            @Field("user_id") String user_id,
            @Field("contact_names") String contact_names,
            @Field("contact_sms") String contact_sms,
            @Field("contact_order") String contact_order

    );

    @FormUrlEncoded
    @POST(baseUrlSuffix)
    Call<GetUserProfile> fetchUser_id(
            @Field("id") String user_id,
            @Field("func") String func

    );

    @FormUrlEncoded
    @POST(baseUrlSuffix)
    Call<GetSmsContact> get_sms_contacts(
            @Field("user_id") String user_id,
            @Field("func") String func

    );

    @FormUrlEncoded
    @POST(baseUrlSuffix)
    Call<GetHistory> fetchHistory(
            @Field("user_id") String user_id,
            @Field("func") String func

    );

    @FormUrlEncoded
    @POST(baseUrlSuffix)
    Call<SmsContactApi> whatsapp_contacts(
            @Field("func") String func,
            @Field("user_id") String user_id,
            @Field("contact_names") String contact_names,
            @Field("contact_phones") String contact_sms,
            @Field("contact_order") String contact_order

    );

    @FormUrlEncoded
    @POST(baseUrlSuffix)
    Call<SmsContactApi> call_contacts(
            @Field("func") String func,
            @Field("user_id") String user_id,
            @Field("contact_names") String contact_names,
            @Field("contact_phones") String contact_sms,
            @Field("contact_order") String contact_order,
            @Field("primary") String primary,
            @Field("secondary") String secondary

    );

    @FormUrlEncoded
    @POST(baseUrlSuffix)
    Call<UpdateProfile> contact_us(
            @Field("func") String func,
            @Field("email") String email,
            @Field("phone") String phone,
            @Field("name") String name,
            @Field("message") String message,
            @Field("user_id") String user_id

    );

    @FormUrlEncoded
    @POST(baseUrlSuffix)
    Call<GetSmsContact> get_whatsapp_contacts(
            @Field("user_id") String user_id,
            @Field("func") String func

    );

    @FormUrlEncoded
    @POST(baseUrlSuffix)
    Call<GetCallContact> get_call_contacts(
            @Field("user_id") String user_id,
            @Field("func") String func

    );

    @FormUrlEncoded
    @POST(baseUrlSuffix)
    Call<GetEmailContact> get_email_contacts(
            @Field("user_id") String user_id,
            @Field("func") String func

    );

    @FormUrlEncoded
    @POST(baseUrlSuffix)
    Call<SmsContactApi> email_contacts(
            @Field("func") String func,
            @Field("user_id") String user_id,
            @Field("contact_names") String contact_names,
            @Field("contact_emails") String contact_emails,
            @Field("contact_order") String contact_order

    );

    @FormUrlEncoded
    @POST(baseUrlSuffix)
    Call<UpdateProfile> update_profile(
            @Field("func") String func,
            @Field("email") String email,
            @Field("first_name") String first_name,
            @Field("last_name") String last_name,
            @Field("gender") String gender,
            @Field("phone") String phone,
            @Field("location") String location

    );

    @FormUrlEncoded
    @POST(baseUrlSuffix)
    Call<UpdateProfile> user_settings_update(
            @Field("func") String func,
            @Field("user_id") String user_id,
            @Field("siren") String siren,
            @Field("call_preference") String call_preference

    );

    @FormUrlEncoded
    @POST(baseUrlSuffix)
    Call<SirenModel> get_user_settings(
            @Field("func") String func,
            @Field("user_id") String user_id

    );

    @FormUrlEncoded
    @POST(baseUrlSuffix)
    Call<UpdateProfile> deleteUser(
            @Field("func") String func,
            @Field("user_id") String user_id

    );

    @FormUrlEncoded
    @POST(baseUrlSuffixMakeCall)
    Call<SmsContactApi> make_call(
            @Field("func") String func,
            @Field("phone_number") String phone_number,
            @Field("message") String message

    );

    @FormUrlEncoded
    @POST(baseUrlSuffixSendSms)
    Call<SmsContactApi> send_sms(
            @Field("func") String func,
            @Field("phone_number") String phone_number,
            @Field("message") String message

    );

    @FormUrlEncoded
    @POST(baseUrlSuffix)
    Call<SmsContactApi> send_email_contacts(
            @Field("user_id") String user_id,
            @Field("func") String func,
            @Field("username") String username,
            @Field("location") String location

    );
    @Multipart
    @POST(baseUrlSuffix)
    Call<SmsContactApi> insert_update_history(
            @Part("func") RequestBody func,
            @Part("user_id") RequestBody user_id,
            @Part("location") RequestBody location,
            @Part MultipartBody.Part audio

    );
}
