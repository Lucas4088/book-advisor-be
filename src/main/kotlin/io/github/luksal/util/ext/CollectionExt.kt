package io.github.luksal.util.ext

import java.math.BigDecimal
import java.math.RoundingMode


fun Set<String>.intersectPercentage(otherSet: Set<String>): BigDecimal =
    (this intersect otherSet).size.toBigDecimal()
        .divide(this.size.coerceAtLeast(otherSet.size).toBigDecimal(), 2, RoundingMode.HALF_UP)
        .setScale(2)
