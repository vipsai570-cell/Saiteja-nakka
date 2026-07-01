package com.example.scheduler

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

object FollowUpScheduler {
    private const val PERIODIC_WORK_NAME = "FollowUpPeriodicWork"
    private const val TAG = "FollowUpScheduler"

    fun schedulePeriodicCheck(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val periodicRequest = PeriodicWorkRequestBuilder<FollowUpWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .addTag(TAG)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicRequest
        )
    }

    fun triggerImmediateCheck(context: Context) {
        val oneTimeRequest = OneTimeWorkRequestBuilder<FollowUpWorker>()
            .addTag(TAG)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "FollowUpImmediateWork",
            ExistingWorkPolicy.REPLACE,
            oneTimeRequest
        )
    }
}
