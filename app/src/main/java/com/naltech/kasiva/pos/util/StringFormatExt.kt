package com.naltech.kasiva.pos.util

import java.util.Locale

fun Double.formatCurrency(): String {
    return String.format(Locale.US, "$%.2f", this)
}
