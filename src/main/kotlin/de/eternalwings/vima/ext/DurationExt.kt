package de.eternalwings.vima.ext

import java.time.Duration

fun Duration.tolerance(toleranceAmount: Long): Long {
    return if(this.toHours() > 0) {
        Duration.ofMinutes(toleranceAmount).toSeconds()
    } else {
        toleranceAmount
    }
}
