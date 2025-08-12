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
import com.example.ahorrapp.data.model.NotificationSettings
import com.example.ahorrapp.data.model.ScheduledPayment
import com.example.ahorrapp.data.model.SavingsGoal
import com.example.ahorrapp.data.model.CategoryLimit
import com.example.ahorrapp.utils.CurrencyUtils
import com.example.ahorrapp.utils.SoundUtils
import com.example.ahorrapp.receiver.PaymentConfirmationReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import java.text.SimpleDateFormat
import java.util.*

@Singleton
class EnhancedNotificationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    private val scheduledPaymentsChannelId = "scheduled_payments"
    private val savingsGoalsChannelId = "savings_goals"
    private val categoryLimitsChannelId = "category_limits"
    private val generalChannelId = "general_notifications"

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Canal para pagos programados
            val scheduledPaymentsChannel = NotificationChannel(
                scheduledPaymentsChannelId, 
                "Pagos Programados", 
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones de pagos programados"
            }
            notificationManager.createNotificationChannel(scheduledPaymentsChannel)

            // Canal para metas de ahorro
            val savingsGoalsChannel = NotificationChannel(
                savingsGoalsChannelId, 
                "Metas de Ahorro", 
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones de metas de ahorro"
            }
            notificationManager.createNotificationChannel(savingsGoalsChannel)

            // Canal para límites de categoría
            val categoryLimitsChannel = NotificationChannel(
                categoryLimitsChannelId, 
                "Límites de Categoría", 
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones de límites de categoría"
            }
            notificationManager.createNotificationChannel(categoryLimitsChannel)

            // Canal para notificaciones generales
            val generalChannel = NotificationChannel(
                generalChannelId, 
                "Notificaciones Generales", 
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones generales de la aplicación"
            }
            notificationManager.createNotificationChannel(generalChannel)
        }
    }

    fun showScheduledPaymentNotification(payment: ScheduledPayment, settings: NotificationSettings) {
        if (!settings.scheduledPaymentsEnabled) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, scheduledPaymentsChannelId)
            .setSmallIcon(R.drawable.ic_calendar)
            .setContentTitle(payment.title)
            .setContentText("Pago programado por ${CurrencyUtils.formatAmount(context, payment.amount)}")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (settings.scheduledPaymentsSound) {
            SoundUtils.configureNotificationSound(builder, settings.scheduledPaymentsSoundUri)
        }

        if (settings.scheduledPaymentsVibration) {
            builder.setVibrate(longArrayOf(0, 250, 250, 250))
        }

        notificationManager.notify(payment.id.toInt(), builder.build())
    }

    fun showPaymentConfirmationNotification(payment: ScheduledPayment) {
        android.util.Log.d("EnhancedNotificationService", "Mostrando notificación de confirmación para pago: ${payment.id}")
        
        // Crear intent para confirmar pago
        val confirmIntent = Intent(context, PaymentConfirmationReceiver::class.java).apply {
            action = PaymentConfirmationReceiver.ACTION_CONFIRM_PAYMENT
            putExtra(PaymentConfirmationReceiver.EXTRA_PAYMENT_ID, payment.id)
            putExtra(PaymentConfirmationReceiver.EXTRA_USER_ID, payment.userId)
            putExtra(PaymentConfirmationReceiver.EXTRA_TITLE, payment.title)
            putExtra(PaymentConfirmationReceiver.EXTRA_AMOUNT, payment.amount)
            putExtra(PaymentConfirmationReceiver.EXTRA_CATEGORY, payment.category)
        }
        val confirmPendingIntent = PendingIntent.getBroadcast(
            context,
            (payment.id * 2).toInt(),
            confirmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Crear intent para rechazar pago
        val declineIntent = Intent(context, PaymentConfirmationReceiver::class.java).apply {
            action = PaymentConfirmationReceiver.ACTION_DECLINE_PAYMENT
            putExtra(PaymentConfirmationReceiver.EXTRA_PAYMENT_ID, payment.id)
            putExtra(PaymentConfirmationReceiver.EXTRA_USER_ID, payment.userId)
            putExtra(PaymentConfirmationReceiver.EXTRA_TITLE, payment.title)
            putExtra(PaymentConfirmationReceiver.EXTRA_AMOUNT, payment.amount)
            putExtra(PaymentConfirmationReceiver.EXTRA_CATEGORY, payment.category)
        }
        val declinePendingIntent = PendingIntent.getBroadcast(
            context,
            (payment.id * 2 + 1).toInt(),
            declineIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Crear intent para abrir la app
        val appIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val appPendingIntent = PendingIntent.getActivity(
            context,
            0,
            appIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, scheduledPaymentsChannelId)
            .setSmallIcon(R.drawable.ic_calendar)
            .setContentTitle("Pago Programado: ${payment.title}")
            .setContentText("¿Confirmas el pago de ${CurrencyUtils.formatAmount(context, payment.amount)}?")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(appPendingIntent)
            .setAutoCancel(true)
            .setOngoing(false) // Permitir que se pueda cancelar
            .addAction(
                android.R.drawable.ic_menu_send,
                "✅ Confirmar",
                confirmPendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "❌ Rechazar",
                declinePendingIntent
            )
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Pago programado: ${payment.title}\n" +
                        "Descripción: ${payment.description}\n" +
                        "Monto: ${CurrencyUtils.formatAmount(context, payment.amount)}\n" +
                        "Categoría: ${payment.category}\n\n" +
                        "¿Deseas confirmar este pago?"))

        notificationManager.notify(payment.id.toInt(), builder.build())
    }

    fun showSavingsGoalNotification(goal: SavingsGoal, settings: NotificationSettings) {
        if (!settings.savingsGoalsEnabled) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val progress = ((goal.currentAmount / goal.targetAmount) * 100).toInt()
        val message = if (goal.isCompleted) {
            "¡Meta completada! ${CurrencyUtils.formatAmount(context, goal.currentAmount)} de ${CurrencyUtils.formatAmount(context, goal.targetAmount)}"
        } else {
            "Progreso: $progress% - ${CurrencyUtils.formatAmount(context, goal.currentAmount)} de ${CurrencyUtils.formatAmount(context, goal.targetAmount)}"
        }

        val builder = NotificationCompat.Builder(context, savingsGoalsChannelId)
            .setSmallIcon(R.drawable.ic_investment)
            .setContentTitle("Meta de Ahorro: ${goal.name}")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (settings.savingsGoalsSound) {
            SoundUtils.configureNotificationSound(builder, settings.savingsGoalsSoundUri)
        }

        if (settings.savingsGoalsVibration) {
            builder.setVibrate(longArrayOf(0, 250, 250, 250))
        }

        notificationManager.notify(goal.id.toInt(), builder.build())
    }

    fun showCategoryLimitNotification(limit: CategoryLimit, settings: NotificationSettings) {
        if (!settings.categoryLimitsEnabled) return

        val percentage = ((limit.currentSpent / limit.limitAmount) * 100).toInt()
        
        // Solo mostrar notificación si está cerca o sobre el límite
        if (percentage < settings.categoryLimitsThreshold) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val message = if (limit.isOverLimit) {
            "¡Has excedido tu límite! ${CurrencyUtils.formatAmount(context, limit.currentSpent)} de ${CurrencyUtils.formatAmount(context, limit.limitAmount)}"
        } else {
            "Has gastado $percentage% de tu límite (${CurrencyUtils.formatAmount(context, limit.currentSpent)} de ${CurrencyUtils.formatAmount(context, limit.limitAmount)})"
        }

        val builder = NotificationCompat.Builder(context, categoryLimitsChannelId)
            .setSmallIcon(R.drawable.ic_shopping)
            .setContentTitle("Límite de Categoría: ${limit.category}")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (settings.categoryLimitsSound) {
            SoundUtils.configureNotificationSound(builder, settings.categoryLimitsSoundUri)
        }

        if (settings.categoryLimitsVibration) {
            builder.setVibrate(longArrayOf(0, 250, 250, 250))
        }

        notificationManager.notify(limit.id.toInt(), builder.build())
    }

    fun showGeneralNotification(title: String, message: String, settings: NotificationSettings) {
        if (!settings.generalNotificationsEnabled) return

        // Verificar horas silenciosas
        if (settings.quietHoursEnabled && isInQuietHours(settings)) {
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, generalChannelId)
            .setSmallIcon(R.drawable.ic_calendar)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (settings.generalNotificationsSound) {
            SoundUtils.configureNotificationSound(builder, settings.generalNotificationsSoundUri)
        }

        if (settings.generalNotificationsVibration) {
            builder.setVibrate(longArrayOf(0, 250, 250, 250))
        }

        notificationManager.notify(title.hashCode(), builder.build())
    }

    private fun isInQuietHours(settings: NotificationSettings): Boolean {
        val calendar = Calendar.getInstance()
        val currentTime = calendar.time
        
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        
        try {
            val startTime = timeFormat.parse(settings.quietHoursStart)
            val endTime = timeFormat.parse(settings.quietHoursEnd)
            
            if (startTime != null && endTime != null) {
                val currentMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
                val startMinutes = startTime.hours * 60 + startTime.minutes
                val endMinutes = endTime.hours * 60 + endTime.minutes
                
                return if (startMinutes <= endMinutes) {
                    // Horas silenciosas en el mismo día (ej: 22:00 - 08:00)
                    currentMinutes >= startMinutes && currentMinutes <= endMinutes
                } else {
                    // Horas silenciosas que cruzan la medianoche (ej: 22:00 - 08:00)
                    currentMinutes >= startMinutes || currentMinutes <= endMinutes
                }
            }
        } catch (e: Exception) {
            // Si hay error al parsear, no aplicar horas silenciosas
        }
        
        return false
    }

    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }

    fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }
} 