package com.example.instaphotos.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.instaphotos.Destination
import com.example.instaphotos.MainViewModel
import com.example.instaphotos.data.PostData
import com.example.instaphotos.main.BottomNavigationItem
import com.example.instaphotos.main.BottomNavigationMenu
import com.example.instaphotos.main.CommonImage
import com.example.instaphotos.main.CommonProgressSpinner
import com.example.instaphotos.main.LikeAnimation
import com.example.instaphotos.main.UserImageCard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FeedScreen(
    navController: NavController,
    viewModel: MainViewModel
) {

    val isLoading = viewModel.inProgress.value
    val userData = viewModel.userData.value
    val feedPosts = viewModel.postsFeed.value
    val feedPostsLoading = viewModel.postsFeedProgress.value


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(Color.White)
        ) {
            UserImageCard(url = userData?.imageUrl)
        }

        FeedPostsList(
            posts = feedPosts,
            loading = isLoading or feedPostsLoading,
            modifier = Modifier.weight(1f),
            navController = navController,
            viewModel = viewModel,
            currentUserId = userData?.userId ?: ""
        )

        BottomNavigationMenu(
            selectedItem = BottomNavigationItem.FEED,
            navController = navController
        )
    }

}

@Composable
fun FeedPostsList(
    posts: List<PostData>,
    modifier: Modifier,
    loading: Boolean,
    navController: NavController,
    viewModel: MainViewModel,
    currentUserId: String
) {
    Box(modifier = modifier) {
        LazyColumn {
            items(items = posts) { post ->
                FeedPostItem(
                    post = post,
                    viewModel = viewModel,
                    currentUserId = currentUserId,
                ) {
                    navController.currentBackStackEntry?.savedStateHandle?.set("post", post)
                    navController.navigate(Destination.SinglePost.route)
                }
            }
        }
        if (loading) {
            CommonProgressSpinner()
        }
    }
}

@Composable
fun FeedPostItem(
    post: PostData,
    viewModel: MainViewModel,
    currentUserId: String,
    onPostClick: () -> Unit
) {
    val likeAnimation = remember { mutableStateOf(false) }
    val dislikeAnimation = remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(corner = CornerSize(4.dp)),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(top = 4.dp, bottom = 4.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    shape = CircleShape,
                    modifier = Modifier
                        .padding(4.dp)
                        .size(32.dp)
                ) {
                    CommonImage(url = post.userImage, contentScale = ContentScale.Crop)
                }
                Text(
                    text = post.username ?: "",
                    modifier = Modifier.padding(4.dp)
                )

            }
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                val modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 150.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                if (post.likes?.contains(currentUserId) == true) {
                                    dislikeAnimation.value = true
                                } else {
                                    likeAnimation.value = true
                                }
                                viewModel.onLikePost(post)
                            },
                            onTap = {
                                onPostClick.invoke()
                            }
                        )
                    }
                CommonImage(
                    url = post.postImage,
                    modifier = modifier,
                    contentScale = ContentScale.FillWidth
                )
                val scope = rememberCoroutineScope()
                if (likeAnimation.value) {
                    scope.launch {
                        delay(1000L)
                        likeAnimation.value = false
                    }
                    LikeAnimation(true)
                }
                if (dislikeAnimation.value) {
                    scope.launch {
                        delay(1000L)
                        dislikeAnimation.value = false
                    }
                    LikeAnimation(false)
                }
            }
        }
    }
}