package com.example.instaphotos.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.instaphotos.Destination
import com.example.instaphotos.MainViewModel
import com.example.instaphotos.main.CommonDivider
import com.example.instaphotos.main.CommonImage
import com.example.instaphotos.main.CommonProgressSpinner
import com.example.instaphotos.main.UserImageCard
import com.example.instaphotos.main.navigateTo

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val isLoading = viewModel.inProgress.value
    if (isLoading) {
        CommonProgressSpinner()
    } else {
        val userData = viewModel.userData.value
        var name by rememberSaveable { mutableStateOf(userData?.name ?: "") }
        var username by rememberSaveable { mutableStateOf(userData?.username ?: "") }
        var bio by rememberSaveable { mutableStateOf(userData?.bio ?: "") }
        ProfileContent(
            viewModel = viewModel,
            name = name,
            username = username,
            bio = bio,
            onNameChange = { name = it },
            onUsernameChange = { username = it },
            onBioChange = { bio = it },
            onSaveInfo = { viewModel.updateProfileData(name, username, bio) },
            onBack = { navigateTo(navController, Destination.MyPosts) },
            onLogOut = {
                viewModel.onLogOut()
                navigateTo(navController, Destination.Login)
            }
        )
    }
}

@Composable
fun ProfileContent(
    viewModel: MainViewModel,
    name: String,
    username: String,
    bio: String,
    onNameChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onBioChange: (String) -> Unit,
    onSaveInfo: () -> Unit,
    onBack: () -> Unit,
    onLogOut: () -> Unit
) {
    val scrollState = rememberScrollState()
    val imageUrl = viewModel.userData.value?.imageUrl
    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Back", modifier = Modifier.clickable { onBack.invoke() })
            Text(text = "Save", modifier = Modifier.clickable { onSaveInfo.invoke() })
        }

        CommonDivider()

        ProfileImage(
            imageUrl = imageUrl,
            viewModel = viewModel
        )

        CommonDivider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Name", modifier = Modifier.width(100.dp))
            TextField(
                value = name,
                onValueChange = onNameChange,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Username", modifier = Modifier.width(100.dp))
            TextField(
                value = username,
                onValueChange = onUsernameChange,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 4.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(text = "Bio", modifier = Modifier.width(100.dp))
            TextField(
                value = bio,
                onValueChange = onBioChange,
                singleLine = false,
                modifier = Modifier.height(150.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Logout",
                color = Color.Blue,
                modifier = Modifier.clickable { onLogOut.invoke() })
        }
    }
}

@Composable
fun ProfileImage(
    imageUrl: String?,
    viewModel: MainViewModel
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.uploadProfileImage(uri)
        }
    }

    Box(modifier = Modifier.height(IntrinsicSize.Min)) {
        Column(
            modifier = Modifier
                .padding(3.dp)
                .fillMaxWidth()
                .clickable { launcher.launch("image/*") },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            UserImageCard(
                url = imageUrl,
                modifier = Modifier
                    .padding(8.dp)
                    .size(100.dp)
            )
            Text(text = "Change profile picture")
        }

        val isLoading = viewModel.inProgress.value
        if (isLoading) {
            CommonProgressSpinner()
        }
    }
}