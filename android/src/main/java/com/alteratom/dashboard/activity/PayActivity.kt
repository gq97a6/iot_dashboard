package com.alteratom.dashboard.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.lifecycleScope
import com.alteratom.R
import com.alteratom.dashboard.Theme
import com.alteratom.dashboard.app.AtomApp.Companion.aps
import com.alteratom.dashboard.compose_global.BasicButton
import com.alteratom.dashboard.compose_global.composeConstruct
import com.alteratom.dashboard.createToast
import com.alteratom.dashboard.manager.BillingManager
import com.alteratom.dashboard.manager.BillingManager.Companion.DON1
import com.alteratom.dashboard.manager.BillingManager.Companion.DON25
import com.alteratom.dashboard.manager.BillingManager.Companion.DON5
import com.alteratom.dashboard.manager.BillingManager.Companion.PRO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PayActivity : AppCompatActivity() {

    private lateinit var billingManager: BillingManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(composeConstruct(this) {
            var showLoading by remember { mutableStateOf(false) }
            var scaleInitialValue by remember { mutableFloatStateOf(1f) }
            var scaleTargetValue by remember { mutableFloatStateOf(.8f) }
            var scaleDuration by remember { mutableIntStateOf(3000) }

            val scale = rememberInfiniteTransition(label = "").animateFloat(
                initialValue = scaleInitialValue,
                targetValue = scaleTargetValue,
                animationSpec = infiniteRepeatable(
                    animation = tween(scaleDuration),
                    repeatMode = RepeatMode.Reverse,
                ), label = ""
            )

            val rotation = rememberInfiniteTransition(label = "").animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(3000),
                    repeatMode = RepeatMode.Reverse,
                ), label = ""
            )

            var priceTags by remember { mutableStateOf(mapOf<String, String>()) }

            fun showLoading() {
                showLoading = true
            }

            suspend fun hideLoading() {
                scaleInitialValue = scale.value
                scaleTargetValue = 0f
                scaleDuration = 1000
                delay(500)
                showLoading = false
            }

            fun checkPending() {
                showLoading()
                scaleInitialValue = 1f
                scaleTargetValue = .8f
                scaleDuration = 3000

                lifecycleScope.launch {
                    billingManager.checkPurchases {
                        if (it?.isEmpty() != false) {
                            createToast(this@PayActivity, "No pending purchase found")
                        }
                        hideLoading()
                    }
                }
            }

            //Initialize billing handler
            remember {
                lifecycleScope.launch {
                    showLoading()
                    billingManager = BillingManager(this@PayActivity)
                    billingManager.enable()

                    billingManager.getPriceTags(listOf(PRO, DON1, DON5, DON25)).let {
                        delay(500)
                        if (it != null) priceTags = it
                        else {
                            createToast(this@PayActivity, "Error occurred!")
                            finishAndRemoveTask()
                        }
                    }

                    hideLoading()
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Theme.colors.background)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "Donate", fontSize = 40.sp, color = Theme.colors.color)
                Text(
                    "Developing apps takes time and effort.\n\n" +
                            "If you like the app and wish to support its further development, here you can make a donation.",
                    fontSize = 13.sp,
                    color = Theme.colors.a
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BasicButton(onClick = {
                        lifecycleScope.launch {
                            billingManager.lunchPurchaseFlow(DON1)
                        }
                    }, Modifier.weight(1f)) {
                        Text(
                            priceTags[DON1] ?: "99.99$",
                            fontSize = 10.sp,
                            color = Theme.colors.a
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    BasicButton(onClick = {
                        lifecycleScope.launch {
                            billingManager.lunchPurchaseFlow(DON5)
                        }
                    }, Modifier.weight(1f)) {
                        Text(
                            priceTags[DON5] ?: "99.99$",
                            fontSize = 10.sp,
                            color = Theme.colors.a
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    BasicButton(onClick = {
                        lifecycleScope.launch {
                            billingManager.lunchPurchaseFlow(DON25)
                        }
                    }, Modifier.weight(1f)) {
                        Text(
                            priceTags[DON25] ?: "99.99$",
                            fontSize = 10.sp,
                            color = Theme.colors.a
                        )
                    }
                }

                Text(
                    text = "Pro upgrade",
                    modifier = Modifier.padding(top = 16.dp),
                    fontSize = 40.sp,
                    color = Theme.colors.color
                )
                Text(
                    "Support further development and upgrade to pro.\n\n" +
                            "Get access to unlimited number of dashboards\n" +
                            "and other pro features added in future updates.",
                    modifier = Modifier.padding(top = 3.dp),
                    fontSize = 13.sp,
                    color = Theme.colors.a
                )

                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    BasicButton(
                        onClick = {
                            lifecycleScope.launch {
                                billingManager.lunchPurchaseFlow(PRO)
                            }
                        },
                        enabled = !aps.isLicensed,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (aps.isLicensed) "OWNED"
                            else priceTags[PRO] ?: "99.99$",
                            fontSize = 10.sp,
                            color = Theme.colors.a
                        )
                    }
                }

                Text(
                    "For delayed payment process use button\nbelow to process purchase after it succeeds",
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 30.dp),
                    color = Theme.colors.b
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    BasicButton(
                        contentPadding = PaddingValues(13.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        onClick = { if (!showLoading) checkPending() }
                    ) {
                        Text("CHECK PENDING", fontSize = 10.sp, color = Theme.colors.a)
                    }
                }

                if (showLoading) {
                    Dialog(
                        { showLoading = true },
                        DialogProperties(usePlatformDefaultWidth = false)
                    ) {
                        Image(
                            painterResource(
                                if (Theme.isDark) R.drawable.ic_icon_light
                                else R.drawable.ic_icon
                            ), "",
                            modifier = Modifier
                                .padding(bottom = 100.dp)
                                .scale(scale.value)
                                .rotate(rotation.value)
                                .size(300.dp),
                            colorFilter = ColorFilter.tint(
                                Theme.colors.color.copy(alpha = .4f),
                                BlendMode.SrcAtop
                            )
                        )
                    }
                }

                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                )
            }
        })
    }

    override fun onStop() {
        billingManager.disable()
        super.onStop()
    }
}