package com.svyd.itemshop.ui.error

import com.svyd.itemshop.domain.common.DomainError

/**
 * UI-friendly messages for [DomainError]. Plain strings rather than
 * resource ids for now; can be promoted to `@StringRes` later.
 */
fun DomainError.toUserMessage(): String = when (this) {
    is DomainError.Network -> "Network error. Please check your connection and try again."
    is DomainError.Unauthorized -> "Your session has expired. Please sign in again."
    is DomainError.NotFound -> "Couldn't find $what."
    is DomainError.OAuthCancelled -> "Sign-in was cancelled."
    is DomainError.OAuthDenied -> description ?: "Instagram declined the sign-in request."
    is DomainError.Validation -> reason
    is DomainError.Unknown -> "Something went wrong. Please try again."
}
