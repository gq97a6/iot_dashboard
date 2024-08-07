import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import com.alteratom.dashboard.Theme
import com.alteratom.dashboard.app.AtomApp.Companion.aps
import com.alteratom.dashboard.compose_daemon.DaemonBasedCompose
import com.alteratom.dashboard.compose_daemon.TilePropertiesComposeComponents
import com.alteratom.dashboard.compose_daemon.TilePropertiesComposeComponents.Notification
import com.alteratom.dashboard.compose_global.EditText
import com.alteratom.dashboard.compose_global.LabeledSwitch
import com.alteratom.dashboard.compose_global.RadioGroup

object ButtonTileCompose : DaemonBasedCompose {
    @Composable
    override fun Mqttd(fragment: Fragment) {
        val tile = aps.tile as ButtonTile

        TilePropertiesComposeComponents.CommunicationBox {
            var pub by remember { mutableStateOf(aps.tile.mqtt.pubs["base"] ?: "") }
            EditText(
                label = { Text("Publish topic") },
                value = pub,
                onValueChange = {
                    pub = it
                    aps.tile.mqtt.pubs["base"] = it
                    aps.dashboard.daemon?.notifyConfigChanged()
                }
            )

            var pubPayload by remember {
                mutableStateOf(
                    tile.mqtt.payloads["base"] ?: "err"
                )
            }
            EditText(
                label = { Text("Publish payload") },
                value = pubPayload,
                onValueChange = {
                    pubPayload = it
                    tile.mqtt.payloads["base"] = it
                }
            )

            var qos by remember { mutableIntStateOf(aps.tile.mqtt.qos) }
            RadioGroup(
                listOf(
                    "QoS 0: At most once. No guarantee.",
                    "QoS 1: At least once. (Recommended)",
                    "QoS 2: Delivery exactly once."
                ), "Quality of Service (MQTT protocol):",
                qos,
                {
                    qos = it
                    aps.tile.mqtt.qos = it
                    aps.dashboard.daemon?.notifyConfigChanged()
                },
                modifier = Modifier.padding(top = 20.dp, bottom = 10.dp)
            )

            var ret by remember { mutableStateOf(aps.tile.mqtt.doRetain) }
            LabeledSwitch(
                label = { Text("Retain massages:", fontSize = 15.sp, color = Theme.colors.a) },
                checked = ret,
                onCheckedChange = {
                    ret = it
                    aps.tile.mqtt.doRetain = it
                }
            )

            var conf by remember { mutableStateOf(aps.tile.mqtt.doConfirmPub) }
            LabeledSwitch(
                label = {
                    Text(
                        "Confirm publishing:",
                        fontSize = 15.sp,
                        color = Theme.colors.a
                    )
                },
                checked = conf,
                onCheckedChange = {
                    conf = it
                    aps.tile.mqtt.doConfirmPub = it
                }
            )
        }

        Notification(fragment)
    }

    @Composable
    override fun Bluetoothd(fragment: Fragment) {
    }
}