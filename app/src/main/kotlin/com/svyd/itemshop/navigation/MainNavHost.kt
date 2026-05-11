package com.svyd.itemshop.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.svyd.itemshop.feature.auth.SignOutViewModel
import com.svyd.itemshop.feature.home.HomeScreen
import com.svyd.itemshop.feature.posts.PostsListScreen
import com.svyd.itemshop.feature.products.edit.EditProductScreen
import com.svyd.itemshop.feature.products.list.ProductsListScreen
import org.koin.androidx.compose.koinViewModel

/**
 * Navigation graph used when the user is authenticated.
 *
 * After collapsing the details screen into inline tile interactions on
 * the home grid, the only reachable destination is `Route.Home`. The
 * remaining routes (`Products`, `Posts`, `EditProduct`) stay declared so
 * the legacy feature packages compile until they're deleted in the
 * cleanup pass; they are not navigated to from anywhere.
 *
 * Sign-out lives in `Home`'s top app bar; the `AuthGate` above this
 * NavHost handles the post-sign-out transition by re-rendering
 * `LoginScreen` once `AuthState` flips.
 */
@Composable
fun MainNavHost() {
    val navController = rememberNavController()
    val signOutViewModel: SignOutViewModel = koinViewModel()

    NavHost(
        navController = navController,
        startDestination = Route.Home,
    ) {
        composable<Route.Home> {
            HomeScreen(
                onSignOutClick = signOutViewModel::onSignOutClicked,
            )
        }
        // Dead routes — see top-of-file note.
        composable<Route.Products> {
            ProductsListScreen(
                onAddClick = {},
                onProductClick = {},
                onSignOutClick = {},
            )
        }
        composable<Route.Posts> {
            PostsListScreen(
                onPostSelected = {},
                onOpenExisting = {},
                onBackClick = { navController.popBackStack() },
            )
        }
        composable<Route.EditProduct> { entry ->
            val args = entry.toRoute<Route.EditProduct>()
            EditProductScreen(
                id = args.id,
                onSaved = {},
                onBackClick = { navController.popBackStack() },
            )
        }
    }
}
