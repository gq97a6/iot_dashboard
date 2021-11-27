package com.netDashboard.tile.types.button

import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import com.fasterxml.jackson.annotation.JsonIgnore
import com.netDashboard.R
import com.netDashboard.recycler_view.BaseRecyclerViewAdapter
import com.netDashboard.tile.Tile
import org.eclipse.paho.client.mqttv3.MqttMessage

class ButtonTile : Tile() {

    @JsonIgnore
    override val layout = R.layout.tile_button

    override val mqttData = MqttData("1")

    @JsonIgnore
    override var typeTag = "button"

    var value = "Default value"
        set(value) {
            field = value
            holder?.itemView?.findViewById<TextView>(R.id.tb_value)?.text = value
        }

    override fun onBindViewHolder(holder: BaseRecyclerViewAdapter.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        value = value
    }

    override fun onClick(v: View, e: MotionEvent) {
        super.onClick(v, e)
        send(mqttData.pubPayload, mqttData.qos)
    }

    override fun onReceive(
        data: Pair<String?, MqttMessage?>,
        jsonResult: MutableMap<String, String>
    ) {
        jsonResult["value"]?.let {
            value = it
        }
    }
}