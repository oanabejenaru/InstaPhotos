package com.example.instaphotos.main

import android.widget.Toast
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.example.instaphotos.Destination
import com.example.instaphotos.MainViewModel
import com.example.instaphotos.R

@Composable
fun NotificationMessage(viewModel: MainViewModel) {
    val notificationState = viewModel.popupNotification.value
    val notificationMessage = notificationState?.getContentOrNull()

    notificationMessage?.let {
        Toast.makeText(LocalContext.current, it, Toast.LENGTH_LONG).show()
    }
}

@Composable
fun CommonProgressSpinner() {
    Row(
        modifier = Modifier
            .alpha(0.5f)
            .background(Color.LightGray)
            .clickable(enabled = false) { }
            .fillMaxSize(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator()
    }
}

fun navigateTo(navController: NavController, destination: Destination) {
    navController.navigate(destination.route) {
        popUpTo(destination.route)
        launchSingleTop = true
    }
}

@Composable
fun CheckSignedIn(navController: NavController, viewModel: MainViewModel) {
    val alreadyLoggedIn by remember { mutableStateOf(false) }
    val signedIn = viewModel.signedId.value
    if (signedIn && !alreadyLoggedIn) {
        navController.navigate(Destination.Feed.route) {
            popUpTo(0)
        }
    }
}

@Composable
fun CommonImage(
    url: String?,
    modifier: Modifier = Modifier.wrapContentSize(),
    contentScale: ContentScale = ContentScale.Crop
) {
    val painter = rememberAsyncImagePainter(model = url)
    val state = painter.state

    Image(
        painter = painter,
        contentDescription = null,
        modifier = modifier,
        contentScale = contentScale
    )

    if (state is AsyncImagePainter.State.Loading) {
        CommonProgressSpinner()
    }
}

@Composable
@Preview
fun UserImageCard(
    url: String? = null,
    modifier: Modifier = Modifier
        .padding(8.dp)
        .size(64.dp)
) {
    Card(
        shape = CircleShape,
        modifier = modifier
    ) {
        if (url.isNullOrEmpty()) {
            Image(
                modifier = modifier,
                painter = painterResource(id = R.drawable.ic_user),
                contentDescription = null,
                colorFilter = ColorFilter.tint(Color.Gray)
            )
        } else {
            CommonImage(url = url)
        }
    }
}

@Composable
fun CommonDivider() {
    Divider(
        color = Color.LightGray,
        thickness = 1.dp,
        modifier = Modifier
            .alpha(0.3f)
            .padding(top = 8.dp, bottom = 8.dp)
    )
}

private enum class LikeIconSize {
    SMALL,
    LARGE
}

@Composable
fun LikeAnimation(
    like: Boolean = true
) {
    var sizeState by remember { mutableStateOf(LikeIconSize.SMALL) }
    val transition = updateTransition(targetState = sizeState, label = "")
    val size by transition.animateDp(
        label = "",
        transitionSpec = {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        }
    ) { state ->
        when (state) {
            LikeIconSize.SMALL -> 0.dp
            LikeIconSize.LARGE -> 150.dp
        }
    }
    Image(
        painter = painterResource(
            id = if (like) R.drawable.ic_like else R.drawable.ic_not_liked
        ),
        contentDescription = null,
        modifier = Modifier.size(size = size),
        colorFilter = ColorFilter.tint(if (like) Color.Red else Color.Gray)
    )
    sizeState = LikeIconSize.LARGE
}

