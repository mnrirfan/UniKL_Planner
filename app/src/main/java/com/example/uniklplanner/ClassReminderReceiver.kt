package com.example.uniklplanner

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

/**
 * Receives the AlarmManager broadcast and shows a class-reminder notification.
 *
 * Registered in AndroidManifest.xml; triggered by alarms set in ClassReminders.scheduleOne /
 * scheduleTestNotification. Tapping the notification opens MainActivity.
 */
class ClassReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val courseCode = intent.getStringExtra(ClassReminders.EXTRA_COURSE_CODE) ?: "Class"
        val courseName = intent.getStringExtra(ClassReminders.EXTRA_COURSE_NAME) ?: ""
        val time = intent.getStringExtra(ClassReminders.EXTRA_TIME) ?: ""
        val room = intent.getStringExtra(ClassReminders.EXTRA_ROOM) ?: ""

        // Tap → open the app
        val activityIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingActivity = PendingIntent.getActivity(
            context, 0, activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(context, ClassReminders.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("📚 Class starts in ${ClassReminders.MINUTES_BEFORE} minutes")
            .setContentText("$courseCode · $courseName")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "$courseCode · $courseName\n⏰ $time\n📍 $room"
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingActivity)
            .build()

        val nm = NotificationManagerCompat.from(context)
        if (nm.areNotificationsEnabled()) {
            try {
                nm.notify(courseCode.hashCode(), notif)
            } catch (e: SecurityException) {
                // Permission revoked at runtime — silently fail
            }
        }
    }
}