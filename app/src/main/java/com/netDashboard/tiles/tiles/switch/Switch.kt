package com.netDashboard.tiles.tiles.switch

import com.netDashboard.R
import com.netDashboard.createToast
import com.netDashboard.dashboard_activity.DashboardAdapter
import com.netDashboard.tiles.Tile

class SwitchTile(id: Long, name: String, val x: Int, val y: Int):
        Tile(id, name, R.layout.switch_tile) {

    override fun onBindViewHolder(holder: DashboardAdapter.TileViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        val view = holder.itemView

        view.setOnClickListener {
            val ratio = view.width.toDouble() / view.height.toDouble()
            createToast(context, "$ratio || ${view.height} || ${view.width}")
        }
    }
}