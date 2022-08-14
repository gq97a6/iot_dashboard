import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Color.HSVToColor
import android.graphics.Color.colorToHSV
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.alteratom.R
import com.alteratom.dashboard.DialogBuilder.dialogSetup
import com.alteratom.dashboard.G.theme
import com.alteratom.dashboard.tile.Tile
import com.alteratom.databinding.DialogColorPickerBinding
import com.fasterxml.jackson.annotation.JsonIgnore
import org.eclipse.paho.client.mqttv3.MqttMessage


class ColorTile : Tile() {

    companion object {
        enum class CT { HSV, HEX, RGB }
    }

    @JsonIgnore
    override val layout = R.layout.tile_color

    @JsonIgnore
    override var typeTag = "color"

    override var iconKey = "il_design_palette"

    var paintRaw = true
    var doPaint = true
    private var hsvPicked = floatArrayOf(0f, 0f, 0f)
    private var toRemoves = mutableListOf<String>()
    private var flagIndexes = mutableMapOf<String, Int>()

    var colorType = CT.HSV
        set(value) {
            field = value

            mqttData.payloads[colorType.name]?.let { pattern ->
                when (colorType) {
                    CT.HSV -> {
                        toRemoves = Regex("@[hsv]").split(pattern) as MutableList

                        val indexes = Regex("(?<=@)[hsv]").findAll(pattern).map { it.value }
                        flagIndexes["h"] = indexes.indexOf("h")
                        flagIndexes["s"] = indexes.indexOf("s")
                        flagIndexes["v"] = indexes.indexOf("v")
                    }
                    CT.HEX -> toRemoves = Regex("@hex").split(pattern) as MutableList
                    CT.RGB -> {
                        toRemoves = Regex("@[rgb]").split(pattern) as MutableList

                        val indexes = Regex("(?<=@)[rgb]").findAll(pattern).map { it.value }
                        flagIndexes["r"] = indexes.indexOf("r")
                        flagIndexes["g"] = indexes.indexOf("g")
                        flagIndexes["b"] = indexes.indexOf("b")
                    }
                }
            }
        }

    override fun onCreateTile() {
        super.onCreateTile()

        mqttData.payloads["HSV"] = "@h;@s;@v"
        mqttData.payloads["HEX"] = "#@hex"
        mqttData.payloads["RGB"] = "@r;@g;@b"

        colorToHSV(theme.a.pallet.color, hsvPicked)
        colorType = colorType
    }

    override fun onBindViewHolder(
        holder: com.alteratom.dashboard.recycler_view.RecyclerViewAdapter.ViewHolder,
        position: Int
    ) {
        super.onBindViewHolder(holder, position)

        if (tag.isBlank()) holder.itemView.findViewById<TextView>(R.id.t_tag)?.visibility =
            View.GONE
    }

    override fun onSetTheme(holder: com.alteratom.dashboard.recycler_view.RecyclerViewAdapter.ViewHolder) {
        super.onSetTheme(holder)

        if (doPaint) {
            theme.apply(
                holder.itemView as ViewGroup,
                anim = false,
                colorPallet = theme.a.getColorPallet(hsvPicked, isRaw = paintRaw)
            )
        }
    }

    override fun onClick(v: View, e: MotionEvent) {
        super.onClick(v, e)

        val dialog = Dialog(adapter.context)
        dialog.setContentView(R.layout.dialog_color_picker)
        val binding = DialogColorPickerBinding.bind(dialog.findViewById(R.id.root))

        val hsvPickedTmp = floatArrayOf(hsvPicked[0], hsvPicked[1], hsvPicked[2])

        fun onColorChange() {
            colorToHSV(binding.dcpPicker.color, hsvPickedTmp)
            binding.dcpColor.backgroundTintList =
                ColorStateList.valueOf(binding.dcpPicker.color)
        }

        binding.dcpPicker.setColor(HSVToColor(hsvPickedTmp))
        onColorChange()

        binding.dcpPicker.setColorSelectionListener(object :
            com.alteratom.dashboard.color_picker.listeners.SimpleColorSelectionListener() {
            override fun onColorSelected(color: Int) {
                onColorChange()
            }
        })

        binding.dcpConfirm.setOnClickListener {
            send(
                when (colorType) {
                    CT.HSV -> {
                        (mqttData.payloads["hsv"] ?: "")
                            .replace("@h", hsvPickedTmp[0].toInt().toString())
                            .replace("@s", (hsvPickedTmp[1] * 100).toInt().toString())
                            .replace("@v", (hsvPickedTmp[2] * 100).toInt().toString())
                    }
                    CT.HEX -> {
                        val c = HSVToColor(hsvPickedTmp)
                        (mqttData.payloads["hex"] ?: "")
                            .replace("@hex", String.format("%02x%02x%02x", c.red, c.green, c.blue))
                    }
                    CT.RGB -> {
                        val c = HSVToColor(hsvPickedTmp)
                        (mqttData.payloads["rgb"] ?: "")
                            .replace("@r", c.red.toString())
                            .replace("@g", c.green.toString())
                            .replace("@b", c.blue.toString())
                    }
                    else -> {
                        val c = HSVToColor(hsvPickedTmp)
                        (mqttData.payloads["hex"] ?: "")
                            .replace("@hex", String.format("%02x%02x%02x", c.red, c.green, c.blue))
                    }
                }
            )

            dialog.dismiss()
        }

        binding.dcpDeny.setOnClickListener {
            dialog.dismiss()
        }

        dialog.dialogSetup()
        theme.apply(binding.root)
        dialog.show()
    }

    override fun onReceive(
        data: Pair<String?, MqttMessage?>,
        jsonResult: MutableMap<String, String>
    ) {
        super.onReceive(data, jsonResult)

        var value = jsonResult["base"] ?: data.second.toString()

        toRemoves.forEach {
            value = value.replace(it, " ")
        }

        val v = value.split(" ") as MutableList
        v.removeIf { it.isEmpty() }

        when (colorType) {
            CT.HEX -> colorToHSV(Integer.parseInt(v[0], 16), hsvPicked)
            CT.HSV -> {
                hsvPicked = floatArrayOf(
                    v[flagIndexes["h"]!!].toFloat(),
                    v[flagIndexes["s"]!!].toFloat() / 100,
                    v[flagIndexes["v"]!!].toFloat() / 100
                )
            }
            CT.RGB -> {
                colorToHSV(
                    Color.rgb(
                        v[flagIndexes["r"]!!].toInt(),
                        v[flagIndexes["g"]!!].toInt(),
                        v[flagIndexes["b"]!!].toInt()
                    ), hsvPicked
                )
            }
        }

        if (doPaint) {
            holder?.itemView?.let {
                theme.apply(
                    it as ViewGroup,
                    anim = false,
                    colorPallet = theme.a.getColorPallet(hsvPicked, isRaw = paintRaw)
                )
            }
        }
    }
}