package com.alteratom.dashboard.activities.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.alteratom.R
import com.alteratom.dashboard.FolderTree.saveToFile
import com.alteratom.dashboard.G.dashboards
import com.alteratom.dashboard.G.setCurrentDashboard
import com.alteratom.dashboard.G.theme
import com.alteratom.dashboard.ToolBarController
import com.alteratom.dashboard.activities.MainActivity
import com.alteratom.dashboard.activities.MainActivity.Companion.fm
import com.alteratom.dashboard.blink
import com.alteratom.dashboard.dashboard.Dashboard
import com.alteratom.dashboard.dashboard.DashboardAdapter
import com.alteratom.dashboard.foreground_service.ForegroundService
import com.alteratom.databinding.FragmentMainScreenBinding
import kotlin.random.Random

class MainScreenFragment : Fragment(R.layout.fragment_main_screen) {
    private lateinit var b: FragmentMainScreenBinding

    private lateinit var adapter: DashboardAdapter
    private lateinit var toolBarController: ToolBarController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        b = FragmentMainScreenBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        theme.apply(b.root, requireContext())

        (activity as MainActivity).onBackPressedBoolean = {
            if (!adapter.editMode.isNone) {
                toolBarController.toggleTools()
                true
            } else {
                false
            }
        }

        val addOnClick: () -> Unit = {
            val name = kotlin.math.abs(Random.nextInt()).toString()
            val dashboard = Dashboard(name)
            dashboards.add(dashboard)
            dashboards.saveToFile()

            if (setCurrentDashboard(dashboard.id)) fm.replaceWith(DashboardPropertiesFragment())
        }

        val onUiChange: () -> Unit = {
            theme.apply(b.root, requireContext())
        }

        toolBarController = ToolBarController(
            adapter,
            b.msBar,
            b.msToolbar,
            b.msToolbarIcon,
            b.msEdit,
            b.msSwap,
            b.msRemove,
            b.msAdd,
            addOnClick,
            onUiChange
        )

        b.msMore.setOnClickListener {
            fm.replaceWith(SettingsFragment())
        }
    }

    //----------------------------------------------------------------------------------------------

    private fun setupRecyclerView() {
        adapter = DashboardAdapter(requireContext())
        adapter.setHasStableIds(true)

        adapter.onItemRemoved = {
            if (adapter.itemCount == 0) b.msPlaceholder.visibility = View.VISIBLE
            b.msRemove.clearAnimation()

            ForegroundService.service?.dManager?.notifyDashboardDischarge(it)
        }

        adapter.onItemMarkedRemove = { count, marked ->
            if (marked && count == 1) b.msRemove.blink(duration = 200, minAlpha = 0.2f)
            if (!marked && count == 0) b.msRemove.clearAnimation()
        }

        adapter.onItemEdit = { item ->
            if (setCurrentDashboard(item.id)) fm.replaceWith(DashboardPropertiesFragment())
        }

        adapter.onItemClick = { item ->
            if (adapter.editMode.isNone) {
                setCurrentDashboard(item.id)
                fm.replaceWith(DashboardFragment())
            }
        }

        adapter.onItemLongClick = { item ->
            if (setCurrentDashboard(item.id)) fm.replaceWith(DashboardPropertiesFragment())
        }

        adapter.submitList(dashboards)

        val layoutManager = LinearLayoutManager(requireContext())

        b.msRecyclerView.layoutManager = layoutManager
        b.msRecyclerView.adapter = adapter

        if (adapter.itemCount == 0) b.msPlaceholder.visibility = View.VISIBLE
    }
}