package com.example.scheduler

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.AppDatabase
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.flow.first

class FollowUpWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("FollowUpWorker", "Follow-up background job started running")
        
        try {
            val database = AppDatabase.getDatabase(applicationContext)
            val dao = database.crmDao()
            
            // Get all leads
            val allLeads = dao.getAllLeads().first()
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            
            // Filter leads with follow-up scheduled for today
            val dueLeads = allLeads.filter { lead ->
                lead.status != "Converted" && lead.status != "Rejected" && lead.status != "DND" &&
                lead.renewalDate == todayStr
            }
            
            Log.d("FollowUpWorker", "Found ${dueLeads.size} leads with follow-up set for today ($todayStr)")
            
            if (dueLeads.isNotEmpty()) {
                sendFollowUpNotifications(applicationContext, dueLeads)
            }
            
            return Result.success()
        } catch (e: Exception) {
            Log.e("FollowUpWorker", "Error executing follow-up check", e)
            return Result.retry()
        }
    }

    private fun sendFollowUpNotifications(context: Context, leads: List<com.example.data.Lead>) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "follow_up_reminders"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Follow-up Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies staff about leads due for follow-up today"
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        leads.forEachIndexed { index, lead ->
            val staffName = lead.assignedStaffName ?: "Unassigned Staff"
            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Follow-up Today: ${lead.name}")
                .setContentText("Assigned to: $staffName. Plan: ${lead.insuranceCategory}")
                .setStyle(NotificationCompat.BigTextStyle().bigText(
                    "Lead Name: ${lead.name}\n" +
                    "Phone: ${lead.phone}\n" +
                    "Insurance: ${lead.insuranceCategory}\n" +
                    "Notes: ${lead.callNotes ?: "No notes"}\n" +
                    "Status: ${lead.status}"
                ))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()
                
            notificationManager.notify(index + 1000, notification)
            Log.i("FollowUpWorker", "Notification triggered for lead ${lead.name} (Assigned to: $staffName)")
        }
    }
}
