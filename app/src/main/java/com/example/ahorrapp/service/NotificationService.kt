package com.example.ahorrapp.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.ahorrapp.MainActivity
import com.example.ahorrapp.R
import com.example.ahorrapp.data.model.ScheduledPayment
import com.example.ahorrapp.utils.CurrencyUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val channelId = "scheduled_payments"
    private val savingsGoalsChannelId = "savings_goals"
    private val categoryLimitsChannelId = "category_limits"
    private val generalChannelId = "general_notifications"

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Canal para pagos programados
            val scheduledPaymentsChannel = NotificationChannel(channelId, "Pagos Programados", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Notificaciones de pagos programados"
            }
            notificationManager.createNotificationChannel(scheduledPaymentsChannel)

            // Canal para metas de ahorro
            val savingsGoalsChannel = NotificationChannel(savingsGoalsChannelId, "Metas de Ahorro", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Notificaciones de metas de ahorro"
            }
            notificationManager.createNotificationChannel(savingsGoalsChannel)

            // Canal para límites de categoría
            val categoryLimitsChannel = NotificationChannel(categoryLimitsChannelId, "Límites de Categoría", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Notificaciones de límites de categoría"
            }
            notificationManager.createNotificationChannel(categoryLimitsChannel)

            // Canal para notificaciones generales
            val generalChannel = NotificationChannel(generalChannelId, "Notificaciones Generales", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Notificaciones generales de la aplicación"
            }
            notificationManager.createNotificationChannel(generalChannel)
        }
    }

    fun showPaymentNotification(payment: ScheduledPayment) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_calendar)
            .setContentTitle(payment.title)
            .setContentText("Pago programado por ${CurrencyUtils.formatAmount(context, payment.amount)}")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(payment.id.toInt(), notification)
    }

    fun showSavingsGoalNotification(goalName: String, currentAmount: Double, targetAmount: Double) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val progress = ((currentAmount / targetAmount) * 100).toInt()
        val notification = NotificationCompat.Builder(context, savingsGoalsChannelId)
            .setSmallIcon(R.drawable.ic_investment)
            .setContentTitle("Meta de Ahorro: $goalName")
            .setContentText("Progreso: $progress% - ${CurrencyUtils.formatAmount(context, currentAmount)} de ${CurrencyUtils.formatAmount(context, targetAmount)}")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(goalName.hashCode(), notification)
    }

    fun showCategoryLimitNotification(category: String, currentSpent: Double, limitAmount: Double) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val percentage = ((currentSpent / limitAmount) * 100).toInt()
        val notification = NotificationCompat.Builder(context, categoryLimitsChannelId)
            .setSmallIcon(R.drawable.ic_shopping)
            .setContentTitle("Límite de Categoría: $category")
            .setContentText("Has gastado $percentage% de tu límite (${CurrencyUtils.formatAmount(context, currentSpent)} de ${CurrencyUtils.formatAmount(context, limitAmount)})")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(category.hashCode(), notification)
    }

    fun showGeneralNotification(title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, generalChannelId)
            .setSmallIcon(R.drawable.ic_calendar)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(title.hashCode(), notification)
    }
} 