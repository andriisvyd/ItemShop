package com.svyd.itemshop.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.svyd.itemshop.feature.auth.SignOutViewModel
import com.svyd.itemshop.feature.posts.PostsListScreen
import com.svyd.itemshop.feature.products.details.ProductDetailsScreen
import com.svyd.itemshop.feature.products.edit.EditProductScreen
import com.svyd.itemshop.feature.products.list.ProductsListScreen
import org.koin.androidx.compose.koinViewModel

/**
 * Navigation graph used when the user is authenticated. Four destinations:
 *   - `Products`: the user's product list (start).
 *   - `Posts`: pick an Instagram post. The screen itself decides whether
 *     the picked post becomes a new product (→ EditProduct) or opens an
 *     existing one (→ ProductDetails).
 *   - `ProductDetails(id)`: read-only details of an existing product.
 *   - `EditProduct(id)`: create a product from an Instagram post that is
 *     not yet a product.
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
                    navController.navigate(Route.ProductDetails(id = id.raw))
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
                onOpenExisting = { productId ->
                    navController.navigate(Route.ProductDetails(id = productId)) {
                        popUpTo(Route.Products)
                    }
                },
                onBackClick = { navController.popBackStack() },
            )
        }
        composable<Route.ProductDetails> { entry ->
            val args = entry.toRoute<Route.ProductDetails>()
            ProductDetailsScreen(
                id = args.id,
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
