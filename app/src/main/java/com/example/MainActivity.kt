package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.CRMViewModel
import com.example.ui.screens.MainCrmApp
import com.example.ui.theme.MyApplicationTheme
import com.example.scheduler.FollowUpScheduler

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    // Initialize background scheduling
    FollowUpScheduler.schedulePeriodicCheck(applicationContext)
    FollowUpScheduler.triggerImmediateCheck(applicationContext)

    setContent {
      MyApplicationTheme {
        val viewModel: CRMViewModel = viewModel()
        MainCrmApp(viewModel = viewModel)
      }
    }
  }
}
