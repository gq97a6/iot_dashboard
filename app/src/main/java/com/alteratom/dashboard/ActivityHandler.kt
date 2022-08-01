package com.alteratom.dashboard

import android.app.Activity
import com.alteratom.dashboard.FolderTree.saveToFile
import com.alteratom.dashboard.G.dashboards
import com.alteratom.dashboard.G.settings
import com.alteratom.dashboard.G.theme
import com.alteratom.dashboard.foreground_service.ForegroundService.Companion.service

object ActivityHandler {

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