package com.svyd.itemshop.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.svyd.itemshop.feature.auth.SignedInPlaceholderScreen

/**
 * Navigation graph used when the user is authenticated. Currently only
 * hosts a placeholder; products / posts / edit destinations are added in
 * the next phase.
 */
@Composable
fun MainNavHost() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Route.Products,
    ) {
        composable<Route.Products> {
            SignedInPlaceholderScreen()
        }
    }
}
