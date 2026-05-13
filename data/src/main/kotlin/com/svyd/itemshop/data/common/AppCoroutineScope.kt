package com.svyd.itemshop.data.common

import kotlinx.coroutines.CoroutineScope

/**
 * Marker type for a process-wide scope used by long-lived components
 * (repositories, hot flows). Distinguishing it from generic `CoroutineScope`
 * makes Koin bindings unambiguous and signals intent at the call site.
 */
@JvmInline
value class AppCoroutineScope(val scope: CoroutineScope)
