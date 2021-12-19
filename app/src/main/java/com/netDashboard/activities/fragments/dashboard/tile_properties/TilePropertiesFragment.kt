package com.netDashboard.activities.fragments.dashboard.tile_properties

import android.graphics.Paint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import com.netDashboard.R
import com.netDashboard.activities.fragments.TileIconFragment
import com.netDashboard.app_on.AppOn
import com.netDashboard.databinding.FragmentTilePropertiesBinding
import com.netDashboard.digitsOnly
import com.netDashboard.globals.G
import com.netDashboard.globals.G.dashboard
import com.netDashboard.tile.Tile
import com.netDashboard.tile.types.button.TextTile
import com.netDashboard.tile.types.slider.SliderTile
import com.netDashboard.toPx

class TilePropertiesFragment : Fragment(R.layout.fragment_tile_properties) {
    private lateinit var b: FragmentTilePropertiesBinding

    private lateinit var tile: Tile

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        b = FragmentTilePropertiesBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AppOn.create(requireActivity())

        tile = dashboard.tiles[0]

        G.theme.apply(requireActivity(), b.root)
        viewConfig()

        b.tpTag.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(cs: Editable) {}
            override fun beforeTextChanged(cs: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(cs: CharSequence, start: Int, before: Int, count: Int) {
                var raw = cs.toString()

                val paint = Paint()
                paint.typeface = b.tpTag.typeface
                paint.textSize = b.tpTag.textSize

                val bound = 143f.toPx()
                while (paint.measureText(raw, 0, raw.length) > bound) raw = raw.dropLast(1)

                tile.tag = raw
                b.tpTagWarning.visibility = if (raw.length != cs.length) View.VISIBLE else View.GONE
            }
        })

        b.tpMqttSwitch.setOnCheckedChangeListener { _, state ->
            mqttSwitchOnCheckedChangeListener(state)
        }

        b.tpMqttPub.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(cs: Editable) {}
            override fun beforeTextChanged(cs: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(cs: CharSequence, start: Int, before: Int, count: Int) {
                tile.mqttData.pubs["base"] = cs.toString()
            }
        })

        b.tpMqttSub.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(cs: CharSequence, start: Int, before: Int, count: Int) {
                tile.mqttData.subs["base"] = cs.toString()
            }
        })

        b.tpMqttPayload.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(cs: Editable) {}
            override fun beforeTextChanged(cs: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(cs: CharSequence, start: Int, before: Int, count: Int) {
                tile.mqttData.pubPayload = cs.toString()
            }
        })

        b.tpMqttJsonSwitch.setOnCheckedChangeListener { _, state ->
            tile.mqttData.payloadIsJson = state
            b.tpMqttJsonPayload.visibility = if (state) View.VISIBLE else View.GONE
        }

        b.tpMqttJsonPayloadPath.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(cs: Editable) {}
            override fun beforeTextChanged(cs: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(cs: CharSequence, start: Int, before: Int, count: Int) {
                tile.mqttData.jsonPaths["value"] = cs.toString()
            }
        })

        b.tpMqttConfirmSwitch.setOnCheckedChangeListener { _, state ->
            tile.mqttData.confirmPub = state
        }

        b.tpQos.setOnCheckedChangeListener { _: RadioGroup, id: Int ->
            tile.mqttData.qos = when (id) {
                R.id.tp_qos0 -> 0
                R.id.tp_qos1 -> 1
                R.id.tp_qos2 -> 2
                else -> 1
            }
        }

        if (tile is TextTile) {
            b.tpPayloadType.setOnCheckedChangeListener { _: RadioGroup, id: Int ->
                tile.mqttData.varPayload = when (id) {
                    R.id.tp_mqtt_payload_val -> {
                        b.tpMqttPayloadBox.visibility = View.VISIBLE
                        false
                    }
                    R.id.tp_mqtt_payload_var -> {
                        b.tpMqttPayloadBox.visibility = View.GONE
                        true
                    }
                    else -> true
                }
            }
        }

        if (tile is SliderTile) {
            b.tpSliderFrom.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(cs: Editable) {}
                override fun beforeTextChanged(
                    cs: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(cs: CharSequence, start: Int, before: Int, count: Int) {
                    cs.toString().let { raw ->
                        cs.toString().digitsOnly().let { parsed ->
                            if (raw != parsed) b.tpSliderFrom.setText(parsed)
                            else parsed.toIntOrNull()?.let {
                                (tile as SliderTile).from = it
                            }
                        }
                    }
                }
            })

            b.tpSliderTo.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(cs: Editable) {}
                override fun beforeTextChanged(
                    cs: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(cs: CharSequence, start: Int, before: Int, count: Int) {
                    cs.toString().let { raw ->
                        cs.toString().digitsOnly().let { parsed ->
                            if (raw != parsed) b.tpSliderTo.setText(parsed)
                            else parsed.toIntOrNull()?.let {
                                (tile as SliderTile).to = it
                            }
                        }
                    }
                }
            })

            b.tpSliderStep.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(cs: Editable) {
                }

                override fun beforeTextChanged(
                    cs: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(cs: CharSequence, start: Int, before: Int, count: Int) {
                    cs.toString().let { raw ->
                        cs.toString().digitsOnly().let { parsed ->
                            if (raw != parsed) b.tpSliderStep.setText(parsed)
                            else parsed.toIntOrNull()?.let {
                                (tile as SliderTile).step = it
                            }
                        }
                    }
                }
            })

            b.tpSliderDrag.setOnCheckedChangeListener { _, state ->
                (tile as SliderTile).dragCon = state
            }

            b.tpEditIcon.setOnClickListener {
                parentFragmentManager.beginTransaction().apply {
                    replace(R.id.m_fragment, TileIconFragment())
                    addToBackStack(null)
                    commit()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        dashboard.dg?.mqttd?.notifyOptionsChanged()
        AppOn.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        AppOn.destroy()
    }

    private fun viewConfig() {

        b.tpTileType.text = tile.typeTag
        b.tpTag.setText(tile.tag)

        //MQTT
        b.tpMqttSwitch.isChecked = tile.mqttData.isEnabled
        b.tpMqttPub.setText(tile.mqttData.pubs["base"])
        b.tpMqttSub.setText(tile.mqttData.subs["base"])
        b.tpMqttPayload.setText(tile.mqttData.pubPayload)
        tile.mqttData.payloadIsJson.let {
            b.tpMqttJsonSwitch.isChecked = it
            b.tpMqttJsonPayload.visibility = if (it) View.VISIBLE else View.GONE
        }
        b.tpMqttJsonPayloadPath.setText(tile.mqttData.jsonPaths["value"] ?: "")
        b.tpMqttConfirmSwitch.isChecked = tile.mqttData.confirmPub
        b.tpQos.check(
            when (tile.mqttData.qos) {
                0 -> R.id.tp_qos0
                1 -> R.id.tp_qos1
                2 -> R.id.tp_qos2
                else -> R.id.tp_qos1
            }
        )

        mqttSwitchOnCheckedChangeListener(b.tpMqttSwitch.isChecked)

        b.tpText.visibility = View.GONE
        b.tpSlider.visibility = View.GONE
        b.tpButton.visibility = View.GONE
        b.tpMqttPayloadTypeBox.visibility = View.GONE

        when (tile) {
            is SliderTile -> {
                b.tpSlider.visibility = View.VISIBLE
                b.tpSliderDrag.isChecked = (tile as SliderTile).dragCon
                b.tpSliderFrom.setText((tile as SliderTile).from.toString())
                b.tpSliderTo.setText((tile as SliderTile).to.toString())
                b.tpSliderStep.setText((tile as SliderTile).step.toString())
            }
            is TextTile -> {
                //b.tpText.visibility = VISIBLE
                b.tpMqttPayloadTypeBox.visibility = View.VISIBLE
                b.tpPayloadType.check(
                    if (tile.mqttData.varPayload) {
                        b.tpMqttPayloadBox.visibility = View.GONE
                        R.id.tp_mqtt_payload_var
                    } else R.id.tp_mqtt_payload_val
                )
            }
        }
    }

    private fun mqttSwitchOnCheckedChangeListener(state: Boolean) {
        b.tpMqtt.visibility = if (state) {
            View.VISIBLE
        } else {
            View.GONE
        }

        tile.mqttData.isEnabled = state
    }
}