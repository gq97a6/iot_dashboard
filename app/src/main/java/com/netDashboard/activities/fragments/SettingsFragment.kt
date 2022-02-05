package com.netDashboard.activities.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.netDashboard.R
import com.netDashboard.activities.MainActivity
import com.netDashboard.databinding.FragmentSettingsBinding
import com.netDashboard.globals.G.settings
import com.netDashboard.globals.G.theme

class SettingsFragment : Fragment(R.layout.fragment_tile_new) {
    private lateinit var b: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        b = FragmentSettingsBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewConfig()
        theme.apply(b.root, requireContext())

        b.sLast.setOnClickListener {
            settings.startFromLast = b.sLast.isChecked
        }

        b.sThemeEdit.setOnClickListener {
            (activity as MainActivity).fm.replaceWith(ThemeFragment())
        }

        b.sThemeIsDark.setOnClickListener {
            theme.a.isDark = b.sThemeIsDark.isChecked
            theme.apply((activity as MainActivity).b.root, requireContext())
        }
    }

    private fun viewConfig() {
        b.sLast.isChecked = settings.startFromLast
        b.sThemeIsDark.isChecked = theme.a.isDark
    }
}