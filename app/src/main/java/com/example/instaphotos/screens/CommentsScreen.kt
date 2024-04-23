package com.example.instaphotos.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.instaphotos.MainViewModel
import com.example.instaphotos.data.CommentData
import com.example.instaphotos.main.CommonDivider
import com.example.instaphotos.main.CommonProgressSpinner

@Composable
fun CommentsScreen(
    navController: NavController,
    viewModel: MainViewModel,
    postId: String
) {
    var commentText by rememberSaveable { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val comments = viewModel.comments.value
    val commentsLoading = viewModel.commentsProgress.value

    Column(
        modifier = Modifier.fillMaxSize().padding(8.dp)
    ) {
        Text(text = "Back", modifier = Modifier
            .padding(8.dp)
            .clickable { navController.popBackStack() })

        CommonDivider()

        if (commentsLoading) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CommonProgressSpinner()
            }
        } else if (comments.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "No comments available")
            }
        } else {
            CommentsList(
                items = comments,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            TextField(
                value = commentText,
                onValueChange = {
                    commentText = it
                },
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, Color.LightGray),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
            Button(
                onClick = {
                    viewModel.onCommentAdded(postId = postId, text = commentText)
                    commentText = ""
                    focusManager.clearFocus()
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(text = "Comment")
            }
        }
    }
}

@Composable
fun CommentsList(
    items: List<CommentData>,
    modifier: Modifier
) {
    Box(modifier = modifier) {
        LazyColumn {
            items(items = items) { comment ->
                CommentItem(commentData = comment)
            }
        }
    }
}

@Composable
fun CommentItem(commentData: CommentData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(text = commentData.username ?: "", fontWeight = FontWeight.Bold)
        Text(text = commentData.text ?: "", modifier = Modifier.padding(start = 8.dp))
    }
}