package com.alteratom.dashboard.foreground_service.demons

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import com.alteratom.dashboard.IdGenerator
import com.alteratom.dashboard.Dashboard

abstract class Daemon(val context: Context, var d: Dashboard) : IdGenerator.Indexed {

    override val id = obtainNewId()

    var isDischarged = false
    protected abstract val isEnabled: Boolean

    abstract val isDone: MutableLiveData<Boolean>
    abstract val status: Any

    companion object {
        inline operator fun invoke(context: Context, dashboard: Dashboard, type: Type) =
            when (type) {
                Type.MQTTD -> Mqttd::class
                Type.BLUETOOTHD -> Bluetoothd::class
            }.constructors.first().call(context, dashboard)

        inline operator fun <reified D : Daemon> invoke(context: Context, dashboard: Dashboard) =
            D::class.constructors.first().call(context, dashboard)
    }

    init {
        reportTakenId()
    }

    open fun notifyAssigned() {
        isDischarged = false
    }

    open fun notifyDischarged() {
        isDischarged = true
    }

    open fun notifyOptionsChanged() {}

    enum class Type { MQTTD, BLUETOOTHD }
}