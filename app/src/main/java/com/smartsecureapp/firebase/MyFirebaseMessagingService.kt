package com.smartsecureapp.firebase

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.GsonBuilder
import com.smartsecureapp.Activity.activity.MainActivity
import com.smartsecureapp.Activity.util.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

class MyFirebaseMessagingService : FirebaseMessagingService() {
    private var localBroadcastManager: LocalBroadcastManager? = null
    private var notificationUtils: NotificationUtils? = null

    override fun onCreate() {
        super.onCreate()
        localBroadcastManager = LocalBroadcastManager.getInstance(applicationContext)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.e(TAG, "From: " + remoteMessage.messageId)
        Log.e(TAG, "From: " + remoteMessage.data)
        Log.e(TAG, "From: " + remoteMessage.notification)
        Log.e(TAG, "From: " + remoteMessage.notification?.title)


        // Check if message contains a notification payload.
        if (remoteMessage.notification != null) {
            Log.e(TAG, "Notification Body: " + remoteMessage.notification!!.body)
            handleNotification(remoteMessage.notification!!.body)
        }

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Log.e(TAG, "Data Payload: " + remoteMessage.data.toString())
            try {
                val gson = GsonBuilder().create().toJson(remoteMessage.data)
                val json = JSONObject(gson)
                handleDataMessage(json)
            } catch (e: Exception) {
                Log.e(TAG, "Exception Conversion: " + e.message)
            }
        }
    }

    private fun handleNotification(message: String?) {
        if (!NotificationUtils.isAppIsInBackground(applicationContext)) {
            // app is in foreground, broadcast the push message
            val pushNotification = Intent(Config.PUSH_NOTIFICATION)
            pushNotification.putExtra("message", message)
            LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification)

            // play notification sound
            val notificationUtils = NotificationUtils(applicationContext)
            notificationUtils.playNotificationSound()
        } else {
            // If the app is in background, firebase itself handles the notification
        }
    }
    private fun handleDataMessage(json: JSONObject) {
        Log.e(TAG, "push json: $json")
        try {
//            JSONObject data = json.getJSONObject("data");
            var location = json.getString("key2").toString();
            val title = "Smart Secure Application"
            val message = if (json.has("key2"))"${json.getString("key1")} is in emergency! at ${json.getString("key2")} - Click here to view his location on map" else ""
            Log.e(TAG, "title: $location")
           // Log.e(TAG, "message: $message")
            //            Log.e(TAG, "payload: " + payload.toString());
          //  Log.e(TAG, "imageUrl: $imageUrl")

            /* Thread to insert data in database */

            CoroutineScope(Dispatchers.IO).launch {
              //  val notificationModel = NotificationModel(title,message,imageUrl,type_data,System.currentTimeMillis())
              //  AppDatabase.getInstance(applicationContext)!!.taskDao().insert(Task(GsonBuilder().create().toJson(notificationModel)))
            }


            var resultIntent: Intent? = null

            resultIntent = Intent(applicationContext, MainActivity::class.java)
            resultIntent!!.putExtra("location", location)

            showNotificationMessage(applicationContext, title, message, "", resultIntent)
//            if (ChatActivity.isActive) {
//                sendPreparedBroadcast();
//            }
          //  val showNotification = AppSharedPref.getShowNotification(applicationContext)
          //  Log.e(TAG, "Notification: $showNotification")


        } catch (e: JSONException) {
            Log.e(TAG, "Json Exception: " + e.message)
        } catch (e: Exception) {
            Log.e(TAG, "Exception below: " + e.localizedMessage)
        }
    }

    private fun sendPreparedBroadcast() {
        //Intent intent = new Intent(ChatActivity.THREAD_ID);
        //localBroadcastManager.sendBroadcast(intent);
    }

    override fun onNewToken(s: String) {
        super.onNewToken(s)
        Log.e(TAG, "onNewToken: $s")
        val sharedPreferences = getSharedPreferences(Utils.MyPref, MODE_PRIVATE)
        val prefsEditor = sharedPreferences.edit()
        prefsEditor.putString(Utils.MyDeviceToken,s)
        prefsEditor.apply()
       // AppSharedPref.setFCMToken(applicationContext, s)
    }

    /**
     * Showing notification with text only
     */
    private fun showNotificationMessage(context: Context, title: String, message: String, timeStamp: String, intent: Intent?) {
        notificationUtils = NotificationUtils(context)
        intent!!.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        notificationUtils!!.showNotificationMessage(title, message, timeStamp, intent)
    }

    /**
     * Showing notification with text and image
     */
    private fun showNotificationMessageWithBigImage(context: Context, title: String, message: String, timeStamp: String, intent: Intent?, imageUrl: String) {
        notificationUtils = NotificationUtils(context)
        intent!!.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        notificationUtils!!.showNotificationMessage(title, message, timeStamp, intent, imageUrl)
    }

    companion object {
        private val TAG = MyFirebaseMessagingService::class.java.simpleName
    }
}