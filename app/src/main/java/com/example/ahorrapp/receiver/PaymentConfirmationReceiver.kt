package com.example.ahorrapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.ahorrapp.data.model.Transaction
import com.example.ahorrapp.data.repository.TransactionRepository
import com.example.ahorrapp.data.repository.ScheduledPaymentRepository
import com.example.ahorrapp.service.EnhancedNotificationService
import com.example.ahorrapp.utils.CurrencyUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class PaymentConfirmationReceiver : BroadcastReceiver() {

    @Inject
    lateinit var transactionRepository: TransactionRepository

    @Inject
    lateinit var scheduledPaymentRepository: ScheduledPaymentRepository

    @Inject
    lateinit var notificationService: EnhancedNotificationService

    companion object {
        const val ACTION_CONFIRM_PAYMENT = "com.example.ahorrapp.CONFIRM_PAYMENT"
        const val ACTION_DECLINE_PAYMENT = "com.example.ahorrapp.DECLINE_PAYMENT"
        const val EXTRA_PAYMENT_ID = "payment_id"
        const val EXTRA_USER_ID = "user_id"
        const val EXTRA_TITLE = "title"
        const val EXTRA_AMOUNT = "amount"
        const val EXTRA_CATEGORY = "category"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("PaymentConfirmationReceiver", "onReceive llamado con action: ${intent.action}")
        
        val action = intent.action
        val paymentId = intent.getLongExtra(EXTRA_PAYMENT_ID, -1)
        val userId = intent.getLongExtra(EXTRA_USER_ID, -1)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: ""
        val amount = intent.getDoubleExtra(EXTRA_AMOUNT, 0.0)
        val category = intent.getStringExtra(EXTRA_CATEGORY) ?: ""

        Log.d("PaymentConfirmationReceiver", "Datos recibidos - paymentId: $paymentId, userId: $userId, title: $title, amount: $amount, category: $category")

        if (paymentId == -1L || userId == -1L) {
            Log.e("PaymentConfirmationReceiver", "Datos de pago inválidos")
            return
        }

        when (action) {
            ACTION_CONFIRM_PAYMENT -> {
                Log.d("PaymentConfirmationReceiver", "Confirmando pago: $paymentId")
                confirmPayment(context, paymentId, userId, title, amount, category)
            }
            ACTION_DECLINE_PAYMENT -> {
                Log.d("PaymentConfirmationReceiver", "Rechazando pago: $paymentId")
                declinePayment(context, paymentId, userId)
            }
            else -> {
                Log.e("PaymentConfirmationReceiver", "Acción desconocida: $action")
            }
        }
    }

    private fun confirmPayment(context: Context, paymentId: Long, userId: Long, title: String, amount: Double, category: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Obtener el pago para obtener walletId
                val payment = scheduledPaymentRepository.getPaymentById(paymentId)
                
                // Crear la transacción
                val transaction = Transaction(
                    userId = userId,
                    description = "Pago programado: $title",
                    amount = amount,
                    type = "GASTO",
                    category = category,
                    walletId = payment?.walletId
                )
                transactionRepository.addTransaction(transaction)

                // Confirmar el pago
                scheduledPaymentRepository.confirmPayment(paymentId)

                // Programar próxima notificación si es recurrente
                scheduleNextNotification(context, paymentId, userId)

                // Cancelar la notificación original
                notificationService.cancelNotification(paymentId.toInt())

                // Mostrar notificación de confirmación
                showConfirmationNotification(context, "Pago confirmado", "Se ha procesado el pago de $title por ${CurrencyUtils.formatAmount(context, amount)}")

                Log.d("PaymentConfirmationReceiver", "Pago confirmado exitosamente: $paymentId")
            } catch (e: Exception) {
                Log.e("PaymentConfirmationReceiver", "Error al confirmar pago: ${e.message}")
                // Mostrar notificación de error
                showConfirmationNotification(context, "Error", "No se pudo procesar el pago: ${e.message}")
            }
        }
    }

    private fun declinePayment(context: Context, paymentId: Long, userId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Programar próxima notificación para el día siguiente
                scheduleNextNotification(context, paymentId, userId)

                // Cancelar la notificación original
                notificationService.cancelNotification(paymentId.toInt())

                // Mostrar notificación de confirmación
                showConfirmationNotification(context, "Pago pospuesto", "El pago se pospondrá hasta mañana")

                Log.d("PaymentConfirmationReceiver", "Pago rechazado, próxima notificación programada: $paymentId")
            } catch (e: Exception) {
                Log.e("PaymentConfirmationReceiver", "Error al rechazar pago: ${e.message}")
                // Mostrar notificación de error
                showConfirmationNotification(context, "Error", "No se pudo posponer el pago: ${e.message}")
            }
        }
    }

    private fun showConfirmationNotification(context: Context, title: String, message: String) {
        try {
            // Usar el servicio de notificaciones para mostrar la confirmación
            notificationService.showGeneralNotification(title, message, getDefaultNotificationSettings())
        } catch (e: Exception) {
            Log.e("PaymentConfirmationReceiver", "Error al mostrar notificación de confirmación: ${e.message}")
        }
    }

    private fun getDefaultNotificationSettings(): com.example.ahorrapp.data.model.NotificationSettings {
        return com.example.ahorrapp.data.model.NotificationSettings(
            userId = 0L,
            generalNotificationsEnabled = true,
            generalNotificationsSound = true,
            generalNotificationsVibration = true
        )
    }

    private suspend fun scheduleNextNotification(context: Context, paymentId: Long, userId: Long) {
        try {
            val payment = scheduledPaymentRepository.getPaymentById(paymentId)
            if (payment != null) {
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_MONTH, 1) // Próximo día
                
                // Parsear la hora de notificación
                val timeParts = payment.notificationTime.split(":")
                val hour = timeParts[0].toInt()
                val minute = timeParts[1].toInt()
                
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)

                // Actualizar fechas de notificación
                scheduledPaymentRepository.updateNotificationDates(
                    paymentId,
                    Date(), // Fecha actual como última notificación
                    calendar.time // Próxima notificación
                )

                // Programar próxima alarma usando el contexto de la aplicación
                val appContext = context.applicationContext
                PaymentAlarmReceiver.schedulePaymentAlarm(appContext, payment.copy(
                    startDate = calendar.time,
                    isConfirmed = false
                ))
            }
        } catch (e: Exception) {
            Log.e("PaymentConfirmationReceiver", "Error al programar próxima notificación: ${e.message}")
        }
    }
} 