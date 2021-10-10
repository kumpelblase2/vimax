package de.eternalwings.vima.ext

import java.time.Duration

/**
 * Gets the tolerance in seconds given a specified tolerance amount in percent.
 */
fun Duration.tolerance(toleranceAmount: Short): Long {
    val seconds = toSeconds()
    return seconds.times(toleranceAmount).div(100)
}
