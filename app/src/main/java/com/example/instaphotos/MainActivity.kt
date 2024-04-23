package com.example.instaphotos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.instaphotos.data.PostData
import com.example.instaphotos.main.NotificationMessage
import com.example.instaphotos.screens.CommentsScreen
import com.example.instaphotos.screens.FeedScreen
import com.example.instaphotos.screens.MyPostsScreen
import com.example.instaphotos.screens.NewPostScreen
import com.example.instaphotos.screens.ProfileScreen
import com.example.instaphotos.screens.SearchScreen
import com.example.instaphotos.screens.SinglePostScreen
import com.example.instaphotos.screens.auth.LoginScreen
import com.example.instaphotos.screens.auth.SignupScreen
import com.example.instaphotos.ui.theme.InstaPhotosTheme
import dagger.hilt.android.AndroidEntryPoint

sealed class Destination(val route: String) {
    data object Signup: Destination("signup")
    data object Login: Destination("login")
    data object Feed: Destination("feed")
    data object Search: Destination("search")
    data object MyPosts: Destination("my_posts")
    data object Profile: Destination("profile")
    data object NewPost: Destination("new_post/{imageUri}") {
        fun createRoute(uri: String) = "new_post/$uri"
    }
    data object SinglePost: Destination("single_post")
    data object Comments : Destination("comments/{postId}") {
        fun createRoute(postId: String) = "comments/$postId"
    }
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            InstaPhotosTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    InstaPhotosApp()
                }
            }
        }
    }
}

@Composable
fun InstaPhotosApp() {
    val viewModel = hiltViewModel<MainViewModel>()
    val navController = rememberNavController()

    NotificationMessage(viewModel = viewModel)
    
    NavHost(
        navController = navController,
        startDestination = Destination.Signup.route
    ) {
        composable(Destination.Signup.route) {
            SignupScreen(navController = navController, viewModel = viewModel)
        }
        composable(Destination.Login.route) {
            LoginScreen(navController = navController, viewModel = viewModel)
        }
        composable(Destination.Feed.route) {
            FeedScreen(navController = navController, viewModel = viewModel)
        }
        composable(Destination.Search.route) {
            SearchScreen(navController = navController, viewModel = viewModel)
        }
        composable(Destination.MyPosts.route) {
            MyPostsScreen(navController = navController, viewModel = viewModel)
        }
        composable(Destination.Profile.route) {
            ProfileScreen(navController = navController, viewModel = viewModel)
        }
        composable(Destination.NewPost.route) { navBackstackEntry ->
            val imageUri = navBackstackEntry.arguments?.getString("imageUri")
            imageUri?.let {
                NewPostScreen(navController = navController, viewModel = viewModel, encodedUri = it)
            }
        }
        composable(Destination.SinglePost.route) {
            val postData = navController.previousBackStackEntry?.savedStateHandle?.get<PostData>("post")

            postData?.let {
                SinglePostScreen(navController = navController, viewModel = viewModel, post = it)
            }
        }
        composable(Destination.Comments.route) { navBackstackEntry ->
            val postId = navBackstackEntry.arguments?.getString("postId")
            postId?.let {
                CommentsScreen(navController = navController, viewModel = viewModel, postId = it)
            }
        }
    }
}
