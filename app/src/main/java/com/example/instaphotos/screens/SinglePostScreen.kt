package com.example.instaphotos.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.instaphotos.Destination
import com.example.instaphotos.MainViewModel
import com.example.instaphotos.R
import com.example.instaphotos.data.PostData
import com.example.instaphotos.main.CommonDivider
import com.example.instaphotos.main.CommonImage
import java.security.MessageDigest

@Composable
fun SinglePostScreen(
    navController: NavController,
    viewModel: MainViewModel,
    post: PostData
) {
    val comments = viewModel.comments.value
    LaunchedEffect(key1 = Unit) {
        viewModel.getCommentsForPost(postId = post.postId)
    }

    post.userId?.let {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(text = "Back", modifier = Modifier
                .padding(8.dp)
                .clickable { navController.popBackStack() })

            CommonDivider()

            SinglePostDisplay(
                navController = navController,
                viewModel = viewModel,
                post = post,
                commentsNumber = comments.size
            )
        }
    }
}

@Composable
fun SinglePostDisplay(
    navController: NavController,
    viewModel: MainViewModel,
    post: PostData,
    commentsNumber: Int
) {
    val userData = viewModel.userData.value
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                shape = CircleShape,
                modifier = Modifier
                    .padding(8.dp)
                    .size(32.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(model = post.userImage),
                    contentDescription = null
                )
            }
            Text(text = post.username ?: "")
            Text(text = ".", modifier = Modifier.padding(8.dp))


            if (userData?.userId == post.userId) {
                // current user's post don't show anything
            } else if (userData?.following?.contains(post.userId) == true) {
                Text(
                    text = "Unfollow",
                    color = Color.Cyan,
                    modifier = Modifier.clickable {
                        viewModel.followOrUnfollowUser(post.userId!!)
                    }
                )
            } else {
                Text(
                    text = "Follow",
                    color = Color.Blue,
                    modifier = Modifier.clickable {
                        viewModel.followOrUnfollowUser(post.userId!!)
                    }
                )
            }
        }
    }
    Box {
        val modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 150.dp)
        CommonImage(
            url = post.postImage,
            modifier = modifier,
            contentScale = ContentScale.FillWidth
        )
    }

    Row(
        modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_like),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            colorFilter = ColorFilter.tint(Color.Red)
        )
        Text(text = " ${post.likes?.size ?: 0} likes", modifier = Modifier.padding(start = 0.dp))
    }

    Row(modifier = Modifier.padding(8.dp)) {
        Text(text = post.username ?: "", fontWeight = FontWeight.Bold)
        Text(text = post.postDescription ?: "", modifier = Modifier.padding(start = 8.dp))
    }

    Row(modifier = Modifier.padding(8.dp)) {
        Text(
            text = "$commentsNumber comments",
            color = Color.Gray,
            modifier = Modifier
                .padding(start = 8.dp)
                .clickable {
                    post.postId?.let {
                        navController.navigate(Destination.Comments.createRoute(it))
                    }
                }
        )
    }
}