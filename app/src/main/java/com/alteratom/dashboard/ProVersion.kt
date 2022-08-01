package com.alteratom.dashboard

import android.app.Activity
import com.alteratom.dashboard.FolderTree.rootFolder
import com.android.billingclient.api.Purchase
import kotlinx.coroutines.delay
import java.io.File
import kotlin.system.measureTimeMillis

object ProVersion {
    var status = false

    fun updateStatus() {
        status = File("$rootFolder/license").exists()
    }

    inline suspend fun checkPurchase(activity: Activity, onDone: () -> Unit) {
        var result: Purchase? = null
        val bh = BillingHandler(activity)

        measureTimeMillis {
            bh.apply {
                enable()

                getPurchases()?.find {
                    it.products.contains(BillingHandler.PRO)
                }?.let {
                    onPurchased(it)
                    result = it
                }

                disable()
                connectionHandler.awaitDone()
            }
        }.let {
            delay(maxOf(10000 - it, 0))
            if (result != null) bh.onPurchaseProcessed(result!!)
            else createToast(activity, "No purchase found")
            onDone()
        }
    }

    fun createLocalLicence() {
        try {
            File("$rootFolder/license").writeText("")
            updateStatus()
        } catch (e: Exception) {
        }
    }

    fun removeLocalLicence() {
        try {
            File("$rootFolder/license").delete()
            updateStatus()
        } catch (e: Exception) {
        }
    }
}