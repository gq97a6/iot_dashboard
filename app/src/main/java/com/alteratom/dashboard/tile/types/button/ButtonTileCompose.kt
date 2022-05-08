package com.alteratom.tile.types.button.compose

import androidx.compose.runtime.Composable
import com.alteratom.dashboard.activities.fragments.TilePropComp
import com.alteratom.dashboard.compose.ComposeObject

object ButtonTileCompose : ComposeObject {
    @Composable
    override fun Mqttd() {
        TilePropComp.Box {
            TilePropComp.CommunicationBox {
                TilePropComp.Communication()
            }
            TilePropComp.Notification()
        }
    }

    @Composable
    override fun Bluetoothd() {
    }
}