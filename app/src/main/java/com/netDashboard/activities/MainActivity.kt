package com.netDashboard.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.netDashboard.activities.dashboard.DashboardActivity
import com.netDashboard.activities.dashboard.properties.DashboardPropertiesActivity
import com.netDashboard.activities.dashboard_new.DashboardNewActivity
import com.netDashboard.activities.settings.SettingsActivity
import com.netDashboard.app_on_destroy.AppOnDestroy
import com.netDashboard.dashboard.DashboardAdapter
import com.netDashboard.dashboard.Dashboards
import com.netDashboard.databinding.ActivityMainBinding
import com.netDashboard.foreground_service.ForegroundService
import com.netDashboard.foreground_service.ForegroundServiceHandler
import com.netDashboard.getScreenHeight
import com.netDashboard.settings.Settings

class MainActivity : AppCompatActivity() {
    private lateinit var b: ActivityMainBinding

    private lateinit var adapter: DashboardAdapter
    private lateinit var settings: Settings

    private lateinit var foregroundService: ForegroundService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        val foregroundServiceHandler = ForegroundServiceHandler(this)
        foregroundServiceHandler.start()
        foregroundServiceHandler.bind()

        foregroundServiceHandler.service.observe(this, { s ->
            if (s != null) {
                foregroundService = s
                onServiceReady()
            }
        })

        setupRecyclerView()

        b.mTouch.setOnClickListener {
            touchOnClick()
        }

        b.mSettings.setOnClickListener {
            settingsOnClick()
        }

        b.mEdit.setOnClickListener {
            editOnClick()
        }

        b.mSwap.setOnClickListener {
            swapOnClick()
        }

        b.mRemove.setOnClickListener {
            removeOnClick()
        }

        b.mAdd.setOnClickListener {
            addOnClick()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AppOnDestroy.call()
    }

    private fun setupRecyclerView() {
        adapter = DashboardAdapter(this)
        adapter.setHasStableIds(true)

        adapter.onItemRemove = {
            if (adapter.itemCount == 0) {
                b.mPlaceholder.visibility = View.VISIBLE
            }

            Dashboards.update(adapter.list)
        }

        adapter.onItemClick = { index ->
            if (adapter.editType.isEdit) {
                Intent(this, DashboardPropertiesActivity::class.java).also {
                    it.putExtra("dashboardName", adapter.list[index].name)
                    it.putExtra("exitActivity", "MainActivity")
                    startActivity(it)
                    finish()
                }
            } else if (adapter.editType.isNone) {
                Intent(this, DashboardActivity::class.java).also {
                    Settings.lastDashboardName = adapter.list[index].name

                    it.putExtra("dashboardName", adapter.list[index].name)
                    startActivity(it)
                    finish()
                }
            }
        }

        adapter.submitList(Dashboards.get())

        val layoutManager = GridLayoutManager(this, 1)

        b.mRecyclerView.layoutManager = layoutManager
        b.mRecyclerView.adapter = adapter

        if (adapter.itemCount == 0) {
            b.mPlaceholder.visibility = View.VISIBLE
        }
    }

    private fun touchOnClick() {
        if (adapter.editType.isNone) {
            adapter.editType.setEdit()
            highlightOnly(b.mEdit)

            b.mBar.visibility = View.VISIBLE
            b.mBar.y = getScreenHeight().toFloat()
            b.mBar.animate()
                .translationY(0f)
                .setInterpolator(AccelerateDecelerateInterpolator())?.duration = 400
        } else {
            adapter.editType.setNone()

            b.mBar.animate()
                ?.y(getScreenHeight().toFloat())
                ?.setInterpolator(AccelerateDecelerateInterpolator())?.duration = 400
        }
    }

    private fun settingsOnClick() {
        Intent(this, SettingsActivity::class.java).also {
            startActivity(it)
            finish()
        }
    }

    //----------------------------------------------------------------------------------------------

    private fun editOnClick() {
        if (adapter.editType.isNone) return
        highlightOnly(b.mEdit)
        adapter.editType.setEdit()
    }

    //----------------------------------------------------------------------------------------------

    private fun swapOnClick() {
        if (adapter.editType.isNone) return
        highlightOnly(b.mSwap)
        adapter.editType.setSwap()
    }

    //----------------------------------------------------------------------------------------------

    @SuppressLint("NotifyDataSetChanged")
    private fun removeOnClick() {
        if (adapter.editType.isNone) return

        highlightOnly(b.mRemove)

        if (!adapter.editType.isRemove) {
            adapter.editType.setRemove()
        } else {
            adapter.removeMarkedItem()
        }
    }

    //----------------------------------------------------------------------------------------------

    private fun addOnClick() {
        Intent(this, DashboardNewActivity::class.java).also {
            startActivity(it)
            finish()
        }
    }

    private fun onServiceReady() {}

    private fun highlightOnly(button: Button) {
        b.mRemove.alpha = 0.4f
        b.mSwap.alpha = 0.4f
        b.mEdit.alpha = 0.4f
        button.alpha = 1f
    }

    @Suppress("UNUSED_PARAMETER")
    fun clickTouch(v: View) = b.mTouch.click()

    @Suppress("UNUSED_PARAMETER")
    fun clickSettings(v: View) = b.mSettings.click()

    @Suppress("UNUSED_PARAMETER")
    fun clickAdd(v: View) = b.mAdd.click()

    @Suppress("UNUSED_PARAMETER")
    fun clickRemove(v: View) = b.mRemove.click()

    @Suppress("UNUSED_PARAMETER")
    fun clickSwap(v: View) = b.mSwap.click()

    @Suppress("UNUSED_PARAMETER")
    fun clickEdit(v: View) = b.mEdit.click()

    private fun Button.click() {
        this.performClick()
        this.isPressed = true
        this.invalidate()
        this.isPressed = false
        this.invalidate()
    }
}
