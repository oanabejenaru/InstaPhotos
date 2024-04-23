package com.example.instaphotos.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.instaphotos.Destination
import com.example.instaphotos.MainViewModel
import com.example.instaphotos.main.BottomNavigationItem
import com.example.instaphotos.main.BottomNavigationMenu

@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val searchedPostsLoading = viewModel.searchedPostsProgress.value
    val searchedPosts = viewModel.searchedPosts.value
    var searchQuery by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
    ) {
        SearchBar(
            searchQueryState = searchQuery,
            onSearchChange = { searchQuery = it },
            onSearch = { viewModel.searchPosts(searchQuery) }
        )
        PostList(
            isContextLoading = false,
            postsLoading = searchedPostsLoading,
            posts = searchedPosts,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp)
        ) { post ->
            navController.currentBackStackEntry?.savedStateHandle?.set("post", post)
            navController.navigate(Destination.SinglePost.route)
        }
        BottomNavigationMenu(
            selectedItem = BottomNavigationItem.SEARCH,
            navController = navController
        )
    }
}

@Composable
fun SearchBar(
    searchQueryState: String,
    onSearchChange: (String) -> Unit,
    onSearch: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    TextField(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .border(1.dp, Color.LightGray, CircleShape),
        value = searchQueryState,
        onValueChange = onSearchChange,
        shape = CircleShape,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                onSearch()
                focusManager.clearFocus()
            }
        ),
        maxLines = 1,
        singleLine = true,
        colors = TextFieldDefaults.colors(
            disabledContainerColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        trailingIcon = {
            IconButton(
                onClick = {
                    onSearch()
                    focusManager.clearFocus()
                }
            ) {
                Icon(imageVector = Icons.Filled.Search, contentDescription = null)
            }
        }
    )
}