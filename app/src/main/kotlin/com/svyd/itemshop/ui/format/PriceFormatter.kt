package com.svyd.itemshop.ui.format

import com.svyd.itemshop.domain.products.Price

/**
 * v1 display format: `<amount> <currency>`. Drops the decimal part when
 * minor units are 0 (`515 ₴`), shows two-digit minor units otherwise
 * (`5.99 €`). Locale-aware decimal separator is a v1+ concern.
 */
fun Price.formatForDisplay(): String {
    val whole = amountMinor / 100
    val minor = (amountMinor % 100).toInt()
    val amountString = if (minor == 0) {
        whole.toString()
    } else {
        "$whole.${minor.toString().padStart(2, '0')}"
    }
    return "$amountString ${currency.raw}"
}
