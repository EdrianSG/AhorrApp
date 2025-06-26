package com.example.ahorrapp.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.ahorrapp.data.model.RepeatInterval
import com.example.ahorrapp.data.model.ScheduledPayment
import com.example.ahorrapp.service.NotificationService
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class PaymentAlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationService: NotificationService

    override fun onReceive(context: Context, intent: Intent) {
        val paymentId = intent.getLongExtra("payment_id", -1)
        val title = intent.getStringExtra("title") ?: ""
        val description = intent.getStringExtra("description") ?: ""
        val amount = intent.getDoubleExtra("amount", 0.0)

        if (paymentId != -1L) {
            val payment = ScheduledPayment(
                id = paymentId,
                userId = intent.getLongExtra("user_id", -1),
                title = title,
                description = description,
                amount = amount,
                startDate = Date(intent.getLongExtra("start_date", 0)),
                endDate = intent.getLongExtra("end_date", -1).let { if (it == -1L) null else Date(it) },
                repeatInterval = intent.getSerializableExtra("repeat_interval") as RepeatInterval,
                category = intent.getStringExtra("category") ?: ""
            )
            notificationService.showPaymentNotification(payment)
        }
    }

    companion object {
        fun schedulePaymentAlarm(context: Context, payment: ScheduledPayment) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, PaymentAlarmReceiver::class.java).apply {
                putExtra("payment_id", payment.id)
                putExtra("user_id", payment.userId)
                putExtra("title", payment.title)
                putExtra("description", payment.description)
                putExtra("amount", payment.amount)
                putExtra("start_date", payment.startDate.time)
                putExtra("end_date", payment.endDate?.time ?: -1)
                putExtra("repeat_interval", payment.repeatInterval)
                putExtra("category", payment.category)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                payment.id.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val calendar = Calendar.getInstance().apply {
                time = payment.startDate
                set(Calendar.HOUR_OF_DAY, 9) // Notificar a las 9 AM
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }

            when (payment.repeatInterval) {
                RepeatInterval.NONE -> {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
                RepeatInterval.DAILY -> {
                    alarmManager.setRepeating(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        AlarmManager.INTERVAL_DAY,
                        pendingIntent
                    )
                }
                RepeatInterval.WEEKLY -> {
                    alarmManager.setRepeating(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        AlarmManager.INTERVAL_DAY * 7,
                        pendingIntent
                    )
                }
                RepeatInterval.MONTHLY -> {
                    // Para mensual, programamos la próxima alarma cada vez que se dispara
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
                RepeatInterval.YEARLY -> {
                    // Para anual, programamos la próxima alarma cada vez que se dispara
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            }
        }

        fun cancelPaymentAlarm(context: Context, paymentId: Long) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, PaymentAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                paymentId.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }
} 