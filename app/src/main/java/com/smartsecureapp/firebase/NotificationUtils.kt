package com.smartsecureapp.firebase

import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.text.Html
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import androidx.core.app.NotificationCompat
import com.smartsecureapp.R
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.text.ParseException
import java.text.SimpleDateFormat

class NotificationUtils(private val mContext: Context) {

    @JvmOverloads
    fun showNotificationMessage(title: String, message: String?, timeStamp: String, intent: Intent, imageUrl: String? = null) {
        // Check for empty push message

        Log.e("shownotification","sssss")
        if (TextUtils.isEmpty(message)) return


        // notification icon
        val icon = R.mipmap.ic_launcher
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

        val resultPendingIntent = PendingIntent.getActivity(
                mContext,
                Config.NOTIFICATION_ID,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val mBuilder = NotificationCompat.Builder(
                mContext, channelId
        )
        val alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                + "://" + mContext.packageName + "/raw/notification")
        if (!TextUtils.isEmpty(imageUrl)) {
            if (imageUrl != null && imageUrl.length > 4 && Patterns.WEB_URL.matcher(imageUrl).matches()) {
                val bitmap = getBitmapFromURL(imageUrl)
                if (bitmap != null) {
                    showBigNotification(bitmap, mBuilder, icon, title, message, timeStamp, resultPendingIntent, alarmSound)
                } else {
                    showSmallNotification(mBuilder, icon, title, message, timeStamp, resultPendingIntent, alarmSound)
                }
            }
        } else {

            showSmallNotification(mBuilder, icon, title, message, timeStamp, resultPendingIntent, alarmSound)
            playNotificationSound()
        }
    }

    private fun showSmallNotification(mBuilder: NotificationCompat.Builder, icon: Int, title: String, message: String?, timeStamp: String, resultPendingIntent: PendingIntent, alarmSound: Uri) {

        val inboxStyle = NotificationCompat.InboxStyle()
        inboxStyle.addLine(message)
        mBuilder
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentIntent(resultPendingIntent)
                .setSound(alarmSound) //.setWhen(getTimeMilliSec(timeStamp))
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.resources, icon))
                .setContentText(message)
                .build()
        val notificationManager = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "singlePush"
            val channel = NotificationChannel(
                    channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
            mBuilder.setChannelId(channelId)
        }
        notificationManager.notify(Config.NOTIFICATION_ID, mBuilder.build())
    }

    private fun showBigNotification(bitmap: Bitmap, mBuilder: NotificationCompat.Builder, icon: Int, title: String, message: String?, timeStamp: String, resultPendingIntent: PendingIntent, alarmSound: Uri) {
        val bigPictureStyle = NotificationCompat.BigPictureStyle()
        bigPictureStyle.setBigContentTitle(title)
        bigPictureStyle.setSummaryText(Html.fromHtml(message).toString())
        bigPictureStyle.bigPicture(bitmap)
        mBuilder
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentIntent(resultPendingIntent)
                .setSound(alarmSound)
                .setStyle(bigPictureStyle) //.setWhen(getTimeMilliSec(timeStamp))
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.resources, icon))
                .setContentText(message)
                .build()
        val notificationManager = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                    channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
            mBuilder.setChannelId(channelId)
        }
        notificationManager.notify(Config.NOTIFICATION_ID, mBuilder.build())
    }

    /**
     * Downloading push notification image before displaying it in
     * the notification tray
     */
    fun getBitmapFromURL(strURL: String?): Bitmap? {
        return try {
            val url = URL(strURL)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    // Playing notification sound
    fun playNotificationSound() {
        try {
            val alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                    + "://" + mContext.packageName + "/raw/notification")
            val r = RingtoneManager.getRingtone(mContext, alarmSound)
            r.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val channelId = "FireBase_ChannelID_123"
        private val TAG = NotificationUtils::class.java.simpleName

        /**
         * Method checks if the app is in background or not
         */
        fun isAppIsInBackground(context: Context): Boolean {
            var isInBackground = true
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
                val runningProcesses = am.runningAppProcesses
                for (processInfo in runningProcesses) {
                    if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                        for (activeProcess in processInfo.pkgList) {
                            if (activeProcess == context.packageName) {
                                isInBackground = false
                            }
                        }
                    }
                }
            } else {
                val taskInfo = am.getRunningTasks(1)
                val componentInfo = taskInfo[0].topActivity
                if (componentInfo!!.packageName == context.packageName) {
                    isInBackground = false
                }
            }
            return isInBackground
        }

        // Clears notification tray messages
        fun clearNotifications(context: Context) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancelAll()
        }

        fun getTimeMilliSec(timeStamp: String?): Long {
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            try {
                val date = format.parse(timeStamp)
                return date.time
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            return 0
        }
    }
}