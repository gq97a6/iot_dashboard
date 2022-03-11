package com.netDashboard.toolbarControl

import android.graphics.drawable.RippleDrawable
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.FrameLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.get
import com.netDashboard.R
import com.netDashboard.recycler_view.RecyclerViewAdapter

class ToolBarController(
    private val adapter: RecyclerViewAdapter<*>,
    private val bar: ConstraintLayout,
    toolbar: FrameLayout,
    private val toolbarIcon: Button,
    private val edit: Button,
    private val swap: Button,
    private val remove: Button,
    add: Button,
    private val onAdd: () -> Unit,
    private val onUiChange: () -> Unit
) {
    private fun highlightOnly(button: Button) {
        remove.alpha = 0.4f
        swap.alpha = 0.4f
        edit.alpha = 0.4f
        button.alpha = 1f
    }

    fun toggleTools() {
        if (adapter.editMode.isNone) {
            adapter.editMode.setSwap()
            highlightOnly(swap)

            bar.animate()
                .translationY(-1.5f * bar[0].height.toFloat())
                .withEndAction {
                    (toolbarIcon.foreground as? RippleDrawable?)?.setDrawableByLayerId(
                        R.id.icon,
                        AppCompatResources.getDrawable(adapter.context, R.drawable.icon_unlocked)
                    )
                    onUiChange()
                }
                .setInterpolator(AccelerateDecelerateInterpolator())?.duration = 300
        } else {
            adapter.editMode.setNone()

            remove.clearAnimation()
            bar.animate()
                .translationY(0f)
                .withEndAction {
                    (toolbarIcon.foreground as? RippleDrawable?)?.setDrawableByLayerId(
                        R.id.icon,
                        AppCompatResources.getDrawable(adapter.context, R.drawable.icon_locked)
                    )
                    onUiChange()
                }
                .setInterpolator(AccelerateDecelerateInterpolator())?.duration = 300
        }
    }

    init {
        toolbar.setOnClickListener {
            toggleTools()
        }

        edit.setOnClickListener {
            highlightOnly(edit)
            adapter.editMode.setEdit()
        }

        swap.setOnClickListener {
            highlightOnly(swap)
            adapter.editMode.setSwap()
        }

        remove.setOnClickListener {
            if (!adapter.editMode.isRemove) {
                highlightOnly(remove)
                adapter.editMode.setRemove()
            } else {
                adapter.removeMarkedItems()
            }
        }

        add.setOnClickListener {
            onAdd()
        }
    }
}