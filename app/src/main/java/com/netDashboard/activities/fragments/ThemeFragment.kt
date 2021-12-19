package com.netDashboard.activities.fragments

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.netDashboard.R
import com.netDashboard.app_on.AppOn
import com.netDashboard.databinding.FragmentThemeBinding
import com.netDashboard.globals.G
import com.netDashboard.globals.G.theme

class ThemeFragment : Fragment(R.layout.fragment_tile_new) {
    private lateinit var b: FragmentThemeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        b = FragmentThemeBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AppOn.create(requireActivity())

        theme = theme

        viewConfig()
        theme.apply(requireContext(), b.root)

        fun onColorChange() {
            theme.a.hsv = floatArrayOf(b.tHue.value, b.tSaturation.value, b.tValue.value)
            theme.apply(requireContext(), b.root)
        }

        b.tHue.setOnTouchListener { _, e ->
            onColorChange()
            return@setOnTouchListener e.action == KeyEvent.ACTION_UP
        }

        b.tSaturation.setOnTouchListener { _, e ->
            onColorChange()
            return@setOnTouchListener e.action == KeyEvent.ACTION_UP
        }

        b.tValue.setOnTouchListener { _, e ->
            onColorChange()
            return@setOnTouchListener e.action == KeyEvent.ACTION_UP
        }

        b.tIsDark.setOnCheckedChangeListener { _, state ->
            theme.a.isDark = state

            b.tValText.tag = if (state) "colorC" else "colorB"
            b.tValue.tag = if (state) "disabled" else "enabled"
            b.tValue.isEnabled = !state
            if (state) b.tValue.value = 1f

            theme.a.compute()
            theme.apply(requireContext(), b.root)
        }

        b.tAdvancedArrow.setOnClickListener {
            switchAdvancedTab()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AppOn.destroy()
    }

    override fun onPause() {
        super.onPause()
        AppOn.pause()
    }

    private fun viewConfig() {
        b.tHue.value = theme.a.hsv[0]
        b.tSaturation.value = theme.a.hsv[1]
        b.tValue.value = theme.a.hsv[2]

        b.tValText.tag = if (theme.a.isDark) "colorC" else "colorB"
        b.tValue.tag = if (theme.a.isDark) "disabled" else "enabled"
        b.tValue.isEnabled = !theme.a.isDark
        if (theme.a.isDark) b.tValue.value = 1f

        if (b.tSaturation.value + b.tValue.value < 2) {
            b.tAdvancedArrow.rotation = 0f
            b.tAdvanced.visibility = View.VISIBLE
        }

        b.tIsDark.isChecked = theme.a.isDark
    }

    private fun switchAdvancedTab() {
        b.tAdvanced.let {
            it.visibility = if (it.isVisible) View.GONE else View.VISIBLE
            b.tAdvancedArrow.animate()
                .rotation(if (it.isVisible) 0f else 180f)
                .setInterpolator(AccelerateDecelerateInterpolator())?.duration = 250
        }
    }
}