package com.svyd.itemshop.domain.products

/**
 * Money amount paired with a currency symbol the way the user wrote it
 * (e.g. "₴", "$", "€"). We deliberately don't map symbols to ISO 4217
 * codes: the symbol is sourced from free-form Instagram captions and
 * mapping introduces ambiguity (`$` could be USD, AUD, CAD, ...). For our
 * use case (display, no arithmetic) the raw symbol is enough.
 *
 * The amount is stored in **minor units** (e.g. cents / kopiyky) so we
 * never deal with floating point. `5.99` becomes 599 and `515` becomes
 * 51500. Display logic divides by 100 again.
 */
data class Price(
    val amountMinor: Long,
    val currency: CurrencySymbol,
) {
    init {
        require(amountMinor >= 0) { "Price amount must be non-negative, was $amountMinor" }
    }

    val majorUnits: Long get() = amountMinor / 100
    val minorUnits: Int get() = (amountMinor % 100).toInt()

    companion object {
        /** Marker emoji that prefixes the price token in Instagram captions. */
        const val CAPTION_MARKER = "🏷️"

        // Whole-number or decimal amount (1-2 decimal digits, `.` or `,` separator)
        // followed by a currency symbol that doesn't contain whitespace or `#`
        // (so trailing hashtags don't get sucked in).
        private val captionRegex = Regex(
            """🏷️\s*(\d+(?:[.,]\d{1,2})?)\s*([^\s#]+)""",
        )

        /**
         * Find the first `🏷️<amount><currency>` token anywhere in `caption`
         * and return the parsed [Price]. Returns null if the marker is
         * missing or the amount can't be parsed.
         */
        fun parseFromCaption(caption: String?): Price? {
            if (caption.isNullOrBlank()) return null
            val match = captionRegex.find(caption) ?: return null
            val amountStr = match.groupValues[1].replace(',', '.')
            val symbol = match.groupValues[2]
            return runCatching {
                val (whole, fraction) = amountStr.toMinorUnits()
                Price(
                    amountMinor = whole * 100 + fraction,
                    currency = CurrencySymbol(symbol),
                )
            }.getOrNull()
        }

        private fun String.toMinorUnits(): Pair<Long, Int> {
            val parts = split('.')
            return when (parts.size) {
                1 -> parts[0].toLong() to 0
                2 -> parts[0].toLong() to parts[1].padEnd(2, '0').take(2).toInt()
                else -> error("Unexpected amount: $this")
            }
        }
    }
}

@JvmInline
value class CurrencySymbol(val raw: String) {
    init {
        require(raw.isNotBlank()) { "Currency symbol must not be blank" }
    }
}
