package com.example.instaphotos.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.instaphotos.Destination
import com.example.instaphotos.MainViewModel
import com.example.instaphotos.R
import com.example.instaphotos.data.PostData
import com.example.instaphotos.main.BottomNavigationItem
import com.example.instaphotos.main.BottomNavigationMenu
import com.example.instaphotos.main.CommonImage
import com.example.instaphotos.main.CommonProgressSpinner
import com.example.instaphotos.main.UserImageCard
import com.example.instaphotos.main.navigateTo

@Composable
fun MyPostsScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val userData = viewModel.userData.value
    val isLoading = viewModel.inProgress.value
    val newPostImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val encoded = Uri.encode(it.toString())
            val route = Destination.NewPost.createRoute(encoded)
            navController.navigate(route)
        }
    }
    val postsLoading = viewModel.refreshPostsProgress.value
    val posts = viewModel.posts.value

    val followers = viewModel.followers.intValue

    Column {
        Column(modifier = Modifier.weight(1f)) {
            Row {
                ProfileImage(imageUrl = userData?.imageUrl) {
                    newPostImageLauncher.launch("image/*")
                }

                Text(
                    text = "${posts.size}\n posts",
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "$followers\n followers",
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "${userData?.following?.size ?: 0}\n following",
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically),
                    textAlign = TextAlign.Center
                )
            }
            Column(modifier = Modifier.padding(8.dp)) {
                val usernameDisplay = if (userData?.username == null)
                    ""
                else
                    "@${userData.username}"
                Text(text = userData?.name ?: "", fontWeight = FontWeight.Bold)
                Text(text = usernameDisplay)
                Text(text = userData?.bio ?: "")
            }
            OutlinedButton(
                onClick = { navigateTo(navController, Destination.Profile) },
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                elevation = ButtonDefaults.elevatedButtonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                    disabledElevation = 0.dp
                ),
                shape = RoundedCornerShape(10)
            ) {
                Text(text = "Edit profile")
            }

            PostList(
                isContextLoading = isLoading,
                postsLoading = postsLoading,
                posts = posts,
                modifier = Modifier
                    .weight(1f)
                    .padding(1.dp)
                    .fillMaxSize()
            ) { post ->
                navController.currentBackStackEntry?.savedStateHandle?.set("post", post)
                navController.navigate(Destination.SinglePost.route)
            }

        }
        BottomNavigationMenu(
            selectedItem = BottomNavigationItem.POSTS,
            navController = navController
        )
    }
    if (isLoading) {
        CommonProgressSpinner()
    }
}

@Preview
@Composable
fun ProfileImage(
    imageUrl: String? = null,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .padding(top = 16.dp)
            .clickable { onClick.invoke() }
    ) {
        UserImageCard(
            url = imageUrl,
            modifier = Modifier
                .padding(8.dp)
                .size(80.dp)
        )
        Card(
            shape = CircleShape,
            border = BorderStroke(width = 2.dp, color = Color.White),
            modifier = Modifier
                .size(32.dp)
                .align(Alignment.BottomEnd)
                .padding(bottom = 8.dp, end = 8.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_add),
                contentDescription = null,
                modifier = Modifier.background(Color.Blue),
                colorFilter = ColorFilter.tint(Color.White)
            )
        }
    }
}

@Composable
fun PostList(
    isContextLoading: Boolean,
    postsLoading: Boolean,
    posts: List<PostData>,
    modifier: Modifier,
    onPostClick: (PostData) -> Unit
) {
    if (postsLoading) {
        CommonProgressSpinner()
    } else if (posts.isEmpty()) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (!isContextLoading) {
                Text(text = "No posts available")
            }
        }
    } else {
        LazyVerticalGrid(
            modifier = modifier,
            columns = GridCells.Adaptive(minSize = 128.dp)
        ) {
            items(posts) { post ->
                PostItem(item = post, onPostClick = onPostClick)
            }
        }
    }
}

@Composable
fun PostItem(
    item: PostData,
    onPostClick: (PostData) -> Unit
) {
    Box(modifier = Modifier
        .height(120.dp)
        .clickable { onPostClick.invoke(item) }
    ) {
        CommonImage(
            url = item.postImage, modifier = Modifier
                .padding(1.dp)
                .fillMaxSize()
        )
    }
}