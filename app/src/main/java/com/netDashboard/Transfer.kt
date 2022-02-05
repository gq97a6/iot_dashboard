package com.netDashboard

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.netDashboard.Settings.Companion.saveToFile
import com.netDashboard.Theme.Companion.saveToFile
import com.netDashboard.activities.SplashScreenActivity
import com.netDashboard.dashboard.Dashboards
import com.netDashboard.dashboard.Dashboards.Companion.saveToFile
import com.netDashboard.databinding.PopupTransferBinding
import com.netDashboard.globals.G
import com.netDashboard.globals.G.dashboard
import com.netDashboard.globals.G.dashboards
import com.netDashboard.globals.G.settings
import com.netDashboard.globals.G.theme
import org.eclipse.paho.client.mqttv3.MqttMessage
import kotlin.random.Random

object Transfer {
    fun showTransferPopup(fragment: Fragment) {
        fragment.apply {

            val dialog = Dialog(requireContext())
            var observer: (Pair<String?, MqttMessage?>) -> Unit = {}

            dialog.setContentView(R.layout.popup_transfer)
            val binding = PopupTransferBinding.bind(dialog.findViewById(R.id.pt_root))
            dialog.show()

            binding.ptReceive.setOnClickListener {
                fun receiveMode(enable: Boolean = binding.ptTransferBox.isVisible) {
                    binding.ptTransferTopic.isEnabled = !enable
                    if (!enable) {
                        binding.ptTransferBox.visibility = VISIBLE
                        binding.ptReceiveIcon.setBackgroundResource(R.drawable.il_arrow_import)
                        binding.ptReceiveFrame.clearAnimation()
                    } else {
                        binding.ptTransferBox.visibility = GONE
                        binding.ptReceiveIcon.setBackgroundResource(R.drawable.il_multimedia_pause)
                        binding.ptReceiveFrame.blink(-1, 200)

                        var ignoreFirst = false
                        observer = { data ->

                            if (data.first == binding.ptTransferTopic.text.toString() && ignoreFirst) {
                                dashboard.dg?.mqttd?.data?.removeObserver(observer)

                                val save: List<String> = try {
                                    G.mapper.readerForListOf(String::class.java)
                                        .readValue(data.second.toString())
                                } catch (e: Exception) {
                                    listOf()
                                }

                                if (save.isNotEmpty()) {
                                    val d = Dashboards.parseSave(save[0])
                                    val s = Settings.parseSave(save[1])
                                    val t = Theme.parseSave(save[2])

                                    if (d != null) {
                                        for (dashboard in d) {
                                            dashboard.mqttClientId =
                                                kotlin.math.abs(Random.nextInt()).toString()
                                        }
                                    }

                                    if (d != null) dashboards = d
                                    if (s != null) settings = s
                                    if (t != null) theme = t

                                    dashboards.saveToFile()
                                    settings.saveToFile()
                                    theme.saveToFile()

                                    activity?.startActivity(
                                        Intent(
                                            context,
                                            SplashScreenActivity::class.java
                                        )
                                    )
                                    activity?.finish()
                                    activity?.finishAffinity()
                                } else {
                                    createToast(requireContext(), "Transfer failed.")
                                }

                                createToast(requireContext(), "Transferred successfully.")
                                receiveMode(false)
                            }

                            ignoreFirst = true
                        }

                        dashboard.dg?.mqttd?.data?.removeObserver(observer)
                        dashboard.dg?.mqttd?.data?.observe(viewLifecycleOwner, observer)
                    }
                }

                receiveMode()
                dashboard.dg?.mqttd?.notifyOptionsChanged()
                dashboard.dg?.mqttd?.subscribe(binding.ptTransferTopic.text.toString(), 0)
            }

            binding.ptTransfer.setOnClickListener {
                val save =
                    arrayOf(
                        (if (binding.ptTransferAll.isChecked) dashboards else listOf(dashboard)).prepareSave(),
                        if (binding.ptTransferSettings.isChecked) settings.prepareSave() else "",
                        if (binding.ptTransferTheme.isChecked) theme.prepareSave() else ""
                    )

                val saveString = G.mapper.writeValueAsString(save)

                dashboard.dg?.mqttd?.publish(
                    binding.ptTransferTopic.text.toString(),
                    saveString,
                    2
                )
                createToast(requireContext(), "Transferred.")
            }

            dialog.setOnDismissListener {
                dashboard.dg?.mqttd?.data?.removeObserver(observer)
                dashboard.dg?.mqttd?.notifyOptionsChanged()
            }

            val a = dialog.window?.attributes
            a?.dimAmount = 0.9f

            dialog.window?.attributes = a
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            theme.apply(binding.root)
        }
    }
}