package com.example.ahorrapp.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.ahorrapp.data.model.RepeatInterval
import com.example.ahorrapp.data.model.ScheduledPayment
import com.example.ahorrapp.data.model.Transaction
import com.example.ahorrapp.data.repository.TransactionRepository
import com.example.ahorrapp.service.NotificationService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class PaymentAlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationService: NotificationService
    
    @Inject
    lateinit var transactionRepository: TransactionRepository

    override fun onReceive(context: Context, intent: Intent) {
        val paymentId = intent.getLongExtra("payment_id", -1)
        val title = intent.getStringExtra("title") ?: ""
        val description = intent.getStringExtra("description") ?: ""
        val amount = intent.getDoubleExtra("amount", 0.0)
        val userId = intent.getLongExtra("user_id", -1)
        val category = intent.getStringExtra("category") ?: ""
        val notificationTime = intent.getStringExtra("notification_time") ?: "09:00"

        if (paymentId != -1L) {
            val payment = ScheduledPayment(
                id = paymentId,
                userId = userId,
                title = title,
                description = description,
                amount = amount,
                startDate = Date(intent.getLongExtra("start_date", 0)),
                endDate = intent.getLongExtra("end_date", -1).let { if (it == -1L) null else Date(it) },
                repeatInterval = intent.getSerializableExtra("repeat_interval") as RepeatInterval,
                category = category,
                notificationTime = notificationTime
            )
            
            // Mostrar notificación
            notificationService.showPaymentNotification(payment)
            
            // Crear transacción automáticamente
            createTransactionFromPayment(payment)
            
            // Programar próxima alarma si es recurrente
            scheduleNextPayment(context, payment)
        }
    }
    
    private fun createTransactionFromPayment(payment: ScheduledPayment) {
        // Crear una transacción de tipo GASTO para el pago programado
        val transaction = Transaction(
            userId = payment.userId,
            description = "Pago programado: ${payment.title}",
            amount = payment.amount,
            type = "GASTO",
            category = payment.category
        )
        
        // Ejecutar en una coroutine para evitar bloqueos
        CoroutineScope(Dispatchers.IO).launch {
            try {
                transactionRepository.addTransaction(transaction)
            } catch (e: Exception) {
                // Log del error pero no fallar la notificación
                e.printStackTrace()
            }
        }
    }
    
    private fun scheduleNextPayment(context: Context, payment: ScheduledPayment) {
        // Solo programar próxima alarma si es recurrente y no es NONE
        if (payment.repeatInterval != RepeatInterval.NONE) {
            val nextDate = calculateNextPaymentDate(payment)
            if (nextDate != null) {
                val nextPayment = payment.copy(startDate = nextDate)
                schedulePaymentAlarm(context, nextPayment)
            }
        }
    }
    
    private fun calculateNextPaymentDate(payment: ScheduledPayment): Date? {
        val calendar = Calendar.getInstance()
        calendar.time = payment.startDate
        
        return when (payment.repeatInterval) {
            RepeatInterval.DAILY -> {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                calendar.time
            }
            RepeatInterval.WEEKLY -> {
                calendar.add(Calendar.WEEK_OF_YEAR, 1)
                calendar.time
            }
            RepeatInterval.MONTHLY -> {
                calendar.add(Calendar.MONTH, 1)
                calendar.time
            }
            RepeatInterval.YEARLY -> {
                calendar.add(Calendar.YEAR, 1)
                calendar.time
            }
            RepeatInterval.NONE -> null
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
                putExtra("notification_time", payment.notificationTime)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                payment.id.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Parsear la hora de notificación
            val timeParts = payment.notificationTime.split(":")
            val hour = timeParts[0].toInt()
            val minute = timeParts[1].toInt()

            val calendar = Calendar.getInstance().apply {
                time = payment.startDate
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
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