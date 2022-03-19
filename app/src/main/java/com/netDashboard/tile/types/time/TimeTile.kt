package com.netDashboard.tile.types.time

import android.app.Dialog
import android.view.MotionEvent
import android.view.View
import android.view.View.VISIBLE
import android.widget.TextView
import com.fasterxml.jackson.annotation.JsonIgnore
import com.netDashboard.DialogBuilder.dialogSetup
import com.netDashboard.R
import com.netDashboard.databinding.DialogTimeBinding
import com.netDashboard.G.theme
import com.netDashboard.recycler_view.RecyclerViewAdapter
import com.netDashboard.tile.Tile
import org.eclipse.paho.client.mqttv3.MqttMessage

class TimeTile : Tile() {

    @JsonIgnore
    override val layout = R.layout.tile_time

    @JsonIgnore
    override var typeTag = "time"

    override var iconKey = "il_time_clock"

    var isDate = false
    var isMilitary = true

    var value = ""
        set(value) {
            field = value
            holder?.itemView?.findViewById<TextView>(R.id.tt_values)?.text = value
        }

    override fun onBindViewHolder(holder: RecyclerViewAdapter.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        value = value
    }

    override fun onClick(v: View, e: MotionEvent) {
        super.onClick(v, e)

        val dialog = Dialog(adapter.context)
        dialog.setContentView(R.layout.dialog_time)
        val binding = DialogTimeBinding.bind(dialog.findViewById(R.id.root))

        if (isDate) binding.dtDate.visibility = VISIBLE
        else {
            binding.dtTime.visibility = VISIBLE
            if (isMilitary) binding.dtTime.setIs24HourView(isMilitary)
        }

        binding.dtConfirm.setOnClickListener {
            if (isDate) {
                val d = binding.dtDate
                send(
                    (mqttData.payloads["date"] ?: "")
                        .replace("@day", d.dayOfMonth.toString())
                        .replace("@month", d.month.toString())
                        .replace("@year", d.year.toString())
                )
            } else {
                val t = binding.dtTime
                send(
                    (mqttData.payloads["time"] ?: "")
                        .replace("@hour", t.hour.toString())
                        .replace("@minute", t.minute.toString())
                )
            }

            dialog.dismiss()
        }

        binding.dtDeny.setOnClickListener {
            dialog.dismiss()
        }

        dialog.dialogSetup()
        theme.apply(binding.root, colorPallet = theme.a.getColorPallet(floatArrayOf(0f, 0f, 1f)))
        dialog.show()
    }

    override fun onReceive(
        data: Pair<String?, MqttMessage?>,
        jsonResult: MutableMap<String, String>
    ) {
        super.onReceive(data, jsonResult)

        //"12+20*/2-4".split(Regex("\\D+"))
        value = jsonResult["base"] ?: data.second.toString()
    }

    override fun onCreateTile() {
        super.onCreateTile()

        mqttData.payloads["time"] = "@hour:@minute"
        mqttData.payloads["date"] = "@day.@month.@year"
    }
}