package com.imbaland.cinenigma.domain.model

import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date

abstract class GameRound(val type: String = "round", open val endsAt: Date = Calendar.getInstance().apply {
    add(
        Calendar.SECOND,
        30
    )
}.time) {
}
val GameRound?.timeRemaining: Int
    get() = if(this == null) 0 else ChronoUnit.SECONDS.between(Date().toInstant(), endsAt.toInstant()).toInt().coerceAtLeast(0)