package com.netDashboard

import android.app.Activity
import com.netDashboard.Settings.Companion.saveToFile
import com.netDashboard.Theme.Companion.saveToFile
import com.netDashboard.dashboard.Dashboard.Companion.saveToFile
import com.netDashboard.foreground_service.ForegroundService.Companion.service
import com.netDashboard.G.dashboards
import com.netDashboard.G.settings
import com.netDashboard.G.theme

object Activity {

    fun onCreate(activity: Activity) {
        service?.finishAffinity = { activity.finishAffinity() }
    }

    fun onDestroy() {
        dashboards.saveToFile()
        settings.saveToFile()
        theme.saveToFile()
    }

    fun onPause() {
        dashboards.saveToFile()
        settings.saveToFile()
        theme.saveToFile()
    }
}
