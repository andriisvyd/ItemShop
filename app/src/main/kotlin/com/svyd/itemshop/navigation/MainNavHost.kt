package com.svyd.itemshop.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.svyd.itemshop.feature.auth.SignOutViewModel
import com.svyd.itemshop.feature.posts.PostsListScreen
import com.svyd.itemshop.feature.products.edit.EditProductScreen
import com.svyd.itemshop.feature.products.list.ProductsListScreen
import org.koin.androidx.compose.koinViewModel

/**
 * Navigation graph used when the user is authenticated. Three destinations:
 *   - `Products`: the user's product list (start).
 *   - `Posts`: pick an Instagram post to base a new product on.
 *   - `EditProduct(id)`: open the product whose id == the Instagram media id.
 *     The same destination handles both "edit existing" and "create from
 *     post"; the screen resolves which based on whether the product is
 *     already in the local DB.
 *
 * The graph is intentionally flat. Sign-out lives in `Products`'s top app
 * bar; the `AuthGate` above this NavHost handles the post-sign-out
 * transition by re-rendering `LoginScreen` once `AuthState` flips.
 */
@Composable
fun MainNavHost() {
    val navController = rememberNavController()
    val signOutViewModel: SignOutViewModel = koinViewModel()

    NavHost(
        navController = navController,
        startDestination = Route.Products,
    ) {
        composable<Route.Products> {
            ProductsListScreen(
                onAddClick = { navController.navigate(Route.Posts) },
                onProductClick = { id ->
                    navController.navigate(Route.EditProduct(id = id.raw))
                },
                onSignOutClick = signOutViewModel::onSignOutClicked,
            )
        }
        composable<Route.Posts> {
            PostsListScreen(
                onPostSelected = { postId ->
                    navController.navigate(Route.EditProduct(id = postId)) {
                        popUpTo(Route.Products)
                    }
                },
                onBackClick = { navController.popBackStack() },
            )
        }
        composable<Route.EditProduct> { entry ->
            val args = entry.toRoute<Route.EditProduct>()
            EditProductScreen(
                id = args.id,
                onSaved = {
                    navController.popBackStack(route = Route.Products, inclusive = false)
                },
                onBackClick = { navController.popBackStack() },
            )
        }
    }
}
