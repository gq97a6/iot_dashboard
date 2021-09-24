package com.netDashboard.tile.types.button

import android.view.MotionEvent
import android.view.View
import android.widget.Button
import com.netDashboard.R
import com.netDashboard.recycler_view.BaseRecyclerViewAdapter
import com.netDashboard.tile.Tile
import org.eclipse.paho.client.mqttv3.MqttMessage

class ButtonTile : Tile() {

    @Transient
    override val layout = R.layout.tile_button

    @Transient
    override val mqttDefaultPubValue = "1"
    override var mqttPubValue = mqttDefaultPubValue

    @Transient
    override var typeTag = "button"

    var text = "Default value"
    private var liveText: String
        get() = holder?.itemView?.findViewById<Button>(R.id.tb_button)?.text.toString()
        set(value) {
            text = value
            holder?.itemView?.findViewById<Button>(R.id.tb_button)?.text = value
        }

    override fun onBindViewHolder(holder: BaseRecyclerViewAdapter.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        liveText = text
    }

    override fun onClick(v: View, e: MotionEvent) {
        super.onClick(v, e)

        val topic = mqttTopics.pubs.get("base")
        onSend(topic.topic, mqttPubValue, topic.qos)
    }

    override fun onReceive(data: Pair<String?, MqttMessage?>): Boolean {
        if (!super.onReceive(data)) return false
        liveText = data.second.toString()
        return true
    }
}