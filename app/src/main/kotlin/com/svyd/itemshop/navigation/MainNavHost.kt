package com.svyd.itemshop.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.svyd.itemshop.feature.auth.SignOutViewModel
import com.svyd.itemshop.feature.home.HomeScreen
import com.svyd.itemshop.feature.posts.PostsListScreen
import com.svyd.itemshop.feature.products.details.ProductDetailsScreen
import com.svyd.itemshop.feature.products.edit.EditProductScreen
import com.svyd.itemshop.feature.products.list.ProductsListScreen
import org.koin.androidx.compose.koinViewModel

/**
 * Navigation graph used when the user is authenticated. Two destinations:
 *   - `Home`: grid of all the user's Instagram posts as products
 *     (auto-synced; replaces the previous Products list + Posts grid).
 *   - `ProductDetails(id)`: read-only details + status transitions for
 *     one product.
 *
 * `Route.Products`, `Route.Posts`, `Route.EditProduct` remain declared in
 * `Routes.kt` only until the corresponding feature packages are deleted
 * in 16b. They are no longer reachable.
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
                onProductClick = { id ->
                    navController.navigate(Route.ProductDetails(id = id.raw))
                },
                onSignOutClick = signOutViewModel::onSignOutClicked,
            )
        }
        composable<Route.ProductDetails> { entry ->
            val args = entry.toRoute<Route.ProductDetails>()
            ProductDetailsScreen(
                id = args.id,
                onBackClick = { navController.popBackStack() },
            )
        }
        // Dead routes kept around so the feature packages compile until
        // their files are removed in 16b. They are not reachable from
        // anywhere in the graph.
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
