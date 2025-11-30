package com.anujsinghdev.anujtodo.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

@Composable
fun CelebrationEffect() {
    // Define the "Party" (Configuration for the explosion)
    val party = Party(
        speed = 0f,
        maxSpeed = 30f,
        damping = 0.9f,
        spread = 360,
        colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
        position = Position.Relative(0.5, 0.3), // Spawns from top-center (30% down)
        emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100)
    )

    // The View that renders it
    KonfettiView(
        modifier = Modifier.fillMaxSize(),
        parties = listOf(party),
    )
}