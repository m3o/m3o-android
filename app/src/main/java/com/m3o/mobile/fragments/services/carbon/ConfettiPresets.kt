package com.m3o.mobile.fragments.services.carbon

import nl.dionsegijn.konfetti.core.Angle
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

class ConfettiPresets {
    companion object {
        fun parade(): List<Party> {
            val party = Party(
                speed = 12f,
                maxSpeed = 30f,
                damping = 0.9f,
                angle = Angle.RIGHT - 45,
                spread = 50,
                colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
                emitter = Emitter(duration = 350, TimeUnit.MILLISECONDS).perSecond(50),
                position = Position.Relative(0.0, 0.5)
            )

            return listOf(
                party,
                party.copy(
                    angle = party.angle - 90,
                    position = Position.Relative(1.0, 0.5)
                ),
            )
        }
    }
}
