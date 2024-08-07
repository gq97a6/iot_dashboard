import android.app.Dialog
import android.view.MotionEvent
import android.view.View
import android.view.View.VISIBLE
import android.widget.TextView
import com.alteratom.R
import com.alteratom.dashboard.app.AtomApp.Companion.aps
import com.alteratom.dashboard.helper_objects.DialogBuilder.dialogSetup
import com.alteratom.dashboard.tile.Tile
import com.alteratom.databinding.DialogTimeBinding
import com.fasterxml.jackson.annotation.JsonIgnore

class TimeTile : Tile() {

    @JsonIgnore
    override val layout = R.layout.tile_time

    @JsonIgnore
    override var typeTag = "time"

    override var iconKey = "il_time_clock"

    var isDate = false
    var isMilitary = aps.settings.militaryTime

    var value = ""
        set(value) {
            field = value
            holder?.itemView?.findViewById<TextView>(R.id.tt_values)?.text = value
        }

    override fun onBindViewHolder(
        holder: com.alteratom.dashboard.recycler_view.RecyclerViewAdapter.ViewHolder,
        position: Int
    ) {
        super.onBindViewHolder(holder, position)

        value = value
    }

    override fun onClick(v: View, e: MotionEvent) {
        super.onClick(v, e)

        if (adapter == null) return

        val dialog = Dialog(adapter!!.context)
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
                    (mqtt.payloads["date"] ?: "")
                        .replace("@day", d.dayOfMonth.toString())
                        .replace("@month", d.month.toString())
                        .replace("@year", d.year.toString())
                )
            } else {
                val t = binding.dtTime
                send(
                    (mqtt.payloads["time"] ?: "")
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
        aps.theme.apply(
            binding.root,
            colorPallet = aps.theme.artist.getColorPallet(floatArrayOf(0f, 0f, 1f))
        )
        dialog.show()
    }

    override fun onReceive(
        topic: String,
        msg: String,
        jsonResult: MutableMap<String, String>
    ) {
        super.onReceive(topic, msg, jsonResult)

        //"12+20*/2-4".split(Regex("\\D+"))
        value = jsonResult["base"] ?: msg
    }

    override fun onCreateTile() {
        super.onCreateTile()

        mqtt.payloads["time"] = "@hour:@minute"
        mqtt.payloads["date"] = "@day.@month.@year"
    }
}