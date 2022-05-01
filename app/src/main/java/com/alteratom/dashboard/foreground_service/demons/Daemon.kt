package com.alteratom.dashboard.foreground_service.demons

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import com.alteratom.dashboard.IdGenerator
import com.alteratom.dashboard.dashboard.Dashboard

abstract class Daemon(val context: Context, var d: Dashboard) : IdGenerator.Indexed {

    override val id = getNewId()

    var isDischarged = false
    protected abstract val isEnabled: Boolean

    abstract val isDone: MutableLiveData<Boolean>
    abstract val status: Any

    companion object {
        operator inline fun <reified D : Daemon> invoke(context: Context, dashboard: Dashboard): D {
            val constructor = D::class.constructors.first()
            return constructor.call(context, dashboard)
        }
    }

    init {
        reportTakenId()
        notifyAssigned()
    }

    open fun notifyAssigned() {
        isDischarged = false
    }

    open fun notifyDischarged() {
        isDischarged = true
    }

    open fun notifyOptionsChanged() {}

    abstract class DaemonConnectionHandler {

        val isDone = MutableLiveData(false)
        private var isDispatchScheduled = false

        protected abstract fun isDone(): Boolean

        fun dispatch(reason: String) {
            val _isDone = isDone()
            if (isDone.value != _isDone) isDone.postValue(_isDone)

            if (!_isDone && !isDispatchScheduled) {

                handleDispatch()

                Handler(Looper.getMainLooper()).postDelayed({
                    isDispatchScheduled = false
                    dispatch("internal")
                }, 500)
                isDispatchScheduled = true
            }
        }

        abstract fun handleDispatch()
    }
}