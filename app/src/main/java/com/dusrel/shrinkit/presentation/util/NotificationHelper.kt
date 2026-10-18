package com.dusrel.shrinkit.presentation.util

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.dusrel.shrinkit.R
import com.dusrel.shrinkit.ShrinkItApplication
import com.dusrel.shrinkit.presentation.MainActivity

object NotificationHelper {
    const val PROCESSING_NOTIFICATION_ID = 1001
    const val COMPLETION_NOTIFICATION_ID = 1002

    fun createProcessingNotification(
        context: Context,
        current: Int,
        total: Int,
        currentFilename: String
    ): Notification {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = context.getString(R.string.notification_processing_title, current, total)
        val text = context.getString(R.string.notification_processing_desc, currentFilename)

        return NotificationCompat.Builder(context, ShrinkItApplication.PROCESSING_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setProgress(total, current, false)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSilent(true)
            .build()
    }

    fun showCompletionNotification(
        context: Context,
        processedCount: Int,
        totalSavingsFormatted: String
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (permission != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = context.getString(R.string.notification_done_title)
        val text = context.getString(R.string.notification_done_desc, processedCount, totalSavingsFormatted)

        val notification = NotificationCompat.Builder(context, ShrinkItApplication.PROCESSING_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(COMPLETION_NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // Permission revoked at runtime
        }
    }
}
