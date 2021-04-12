package com.example.tracetogether.notifications

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.tracetogether.MainActivity
import com.example.tracetogether.R
import com.example.tracetogether.onboarding.OnboardingActivity
import com.example.tracetogether.services.BluetoothMonitoringService.Companion.PENDING_ACTIVITY
import com.example.tracetogether.services.BluetoothMonitoringService.Companion.PENDING_WIZARD_REQ_CODE
import com.example.tracetogether.util.LocalizationHandler

class NotificationTemplates {

    companion object {

        fun getStartupNotification(context: Context, channel: String): Notification {

            val builder = NotificationCompat.Builder(context, channel)
                    .setContentText("Tracer is setting up its antennas")
                    .setContentTitle("Setting things up")
                    .setOngoing(true)
                    .setPriority(Notification.PRIORITY_LOW)
                    .setSmallIcon(R.drawable.ic_notification_setting)
                    .setWhen(System.currentTimeMillis())
                    .setSound(null)
                    .setVibrate(null)
                    .setColor(ContextCompat.getColor(context, R.color.notification_tint))

            return builder.build()
        }

        fun getRunningNotification(context: Context, channel: String): Notification {

            var intent = Intent(context, MainActivity::class.java)

            val activityPendingIntent = PendingIntent.getActivity(
                    context, PENDING_ACTIVITY,
                    intent, 0
            )

            val builder = NotificationCompat.Builder(context, channel)
                    .setContentTitle(
                            LocalizationHandler.getInstance()
                                    .getTextForNotification(context, "service_ok_title")
                    )
                    .setContentText(
                            LocalizationHandler.getInstance()
                                    .getTextForNotification(context, "service_ok_body")
                    )
                    .setOngoing(true)
                    .setPriority(Notification.PRIORITY_LOW)
                    .setSmallIcon(R.drawable.ic_notification_service)
                    .setContentIntent(activityPendingIntent)
                    .setTicker(
                            LocalizationHandler.getInstance()
                                    .getTextForNotification(context, "service_ok_body")
                    )
                    .setStyle(
                            NotificationCompat.BigTextStyle()
                                    .bigText(
                                            LocalizationHandler.getInstance()
                                                    .getTextForNotification(context, "service_ok_body")
                                    )
                    )
                    .setWhen(System.currentTimeMillis())
                    .setSound(null)
                    .setVibrate(null)
                    .setColor(ContextCompat.getColor(context, R.color.notification_tint))

            return builder.build()
        }

        fun lackingThingsNotification(context: Context, channel: String): Notification {
            var intent = Intent(context, OnboardingActivity::class.java)
            intent.putExtra("page", 3)

            val activityPendingIntent = PendingIntent.getActivity(
                    context, PENDING_WIZARD_REQ_CODE,
                    intent, 0
            )

            val builder = NotificationCompat.Builder(context, channel)
                    .setContentTitle(
                            LocalizationHandler.getInstance()
                                    .getTextForNotification(context, "service_not_ok_title")
                    )
                    .setContentText(
                            LocalizationHandler.getInstance()
                                    .getTextForNotification(context, "service_not_ok_body")
                    )
                    .setOngoing(true)
                    .setPriority(Notification.PRIORITY_LOW)
                    .setSmallIcon(R.drawable.ic_notification_warning)
                    .setTicker(
                            LocalizationHandler.getInstance()
                                    .getTextForNotification(context, "service_not_ok_body")
                    )
                    .addAction(
                            R.drawable.ic_notification_setting,
                            LocalizationHandler.getInstance()
                                    .getTextForNotification(context, "service_not_ok_action"),
                            activityPendingIntent
                    )
                    .setContentIntent(activityPendingIntent)
                    .setWhen(System.currentTimeMillis())
                    .setSound(null)
                    .setVibrate(null)
                    .setColor(ContextCompat.getColor(context, R.color.notification_tint))

            return builder.build()
        }
    }
}

