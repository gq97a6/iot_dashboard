package com.alteratom.dashboard.objects

import android.content.Context
import android.util.Log
import com.alteratom.dashboard.Dashboard
import com.alteratom.dashboard.Settings
import com.alteratom.dashboard.Theme
import com.alteratom.dashboard.daemon.DaemonsManager
import com.alteratom.dashboard.objects.Storage.parseListSave
import com.alteratom.dashboard.objects.Storage.parseSave
import com.alteratom.dashboard.tile.Tile
import kotlin.reflect.KClass

object G {
    var areInitialized = false
    var settings = Settings()
    var theme = Theme()
    var dashboards = mutableListOf<Dashboard>()

    //Currently selected objects
    lateinit var dashboard: Dashboard
    lateinit var tile: Tile
    var dashboardIndex = -2

    //Path to root folder
    var rootFolder: String = ""

    //Map of paths to serialized objects
    lateinit var path: Map<KClass<out Any>, String>

    fun setCurrentDashboard(index: Int): Boolean {
        return if (index !in 0 until dashboards.size) false
        else {
            dashboard = dashboards[index]
            dashboardIndex = index
            true
        }
    }

    fun setCurrentDashboard(id: Long): Boolean {
        dashboard = dashboards.find { it.id == id } ?: return false
        dashboardIndex = dashboards.indexOf(dashboard)
        return true
    }
}

