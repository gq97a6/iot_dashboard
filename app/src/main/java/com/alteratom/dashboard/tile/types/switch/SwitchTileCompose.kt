package com.alteratom.tile.types.color.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.alteratom.dashboard.BoldStartText
import com.alteratom.dashboard.EditText
import com.alteratom.dashboard.G
import com.alteratom.dashboard.G.tile
import com.alteratom.dashboard.Theme
import com.alteratom.dashboard.activities.fragments.TilePropComp
import com.alteratom.dashboard.compose.ComposeObject
import com.alteratom.tile.types.switch.SwitchTile
import java.util.*

object SwitchTileCompose : ComposeObject {
    @Composable
    override fun Mqttd() {
        var text by remember { mutableStateOf("false") }

        TilePropComp.Box {
            TilePropComp.CommunicationBox {
                Row(
                    modifier = Modifier.padding(top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        contentPadding = PaddingValues(13.dp),
                        onClick = {},
                        border = BorderStroke(0.dp, Theme.colors.color),
                        modifier = Modifier
                            .height(52.dp)
                            .width(52.dp)
                    ) {
                        Icon(painterResource((tile as SwitchTile).iconResFalse), "")
                    }

                    EditText(
                        label = { Text("Off payload") },
                        value = text,
                        onValueChange = { text = it },
                        modifier = Modifier.padding(bottom = 9.dp, start = 20.dp)
                    )
                }

                Row(
                    modifier = Modifier.padding(top = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        contentPadding = PaddingValues(13.dp),
                        onClick = {},
                        border = BorderStroke(0.dp, Theme.colors.color),
                        modifier = Modifier
                            .height(52.dp)
                            .width(52.dp)
                    ) {
                        Icon(painterResource((tile as SwitchTile).iconResTrue), "")
                    }

                    EditText(
                        label = { Text("On payload") },
                        value = text,
                        onValueChange = { text = it },
                        modifier = Modifier.padding(bottom = 9.dp, start = 20.dp)
                    )
                }

                TilePropComp.Communication()
            }
            TilePropComp.Notification()
        }
    }

    @Composable
    override fun Bluetoothd() {
    }
}