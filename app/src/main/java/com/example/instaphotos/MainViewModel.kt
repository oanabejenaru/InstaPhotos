package com.example.instaphotos

import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.instaphotos.data.CommentData
import com.example.instaphotos.data.Event
import com.example.instaphotos.data.PostData
import com.example.instaphotos.data.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject

const val USERS = "users"
const val POSTS = "posts"
const val COMMENTS = "comments"

@HiltViewModel
class MainViewModel @Inject constructor(
    val auth: FirebaseAuth,
    val db: FirebaseFirestore,
    val storage: FirebaseStorage
) : ViewModel() {

    val signedId = mutableStateOf(false)
    val inProgress = mutableStateOf(false)
    val userData = mutableStateOf<UserData?>(null)
    val popupNotification = mutableStateOf<Event<String>?>(null)

    val refreshPostsProgress = mutableStateOf(false)
    val posts = mutableStateOf<List<PostData>>(listOf())

    val searchedPosts = mutableStateOf<List<PostData>>(listOf())
    val searchedPostsProgress = mutableStateOf(false)

    val postsFeed = mutableStateOf<List<PostData>>(listOf())
    val postsFeedProgress = mutableStateOf(false)

    val comments = mutableStateOf<List<CommentData>>(listOf())
    val commentsProgress = mutableStateOf(false)

    val followers = mutableIntStateOf(0)

    init {
        //auth.signOut()
        val currentUser = auth.currentUser
        signedId.value = currentUser != null
        currentUser?.uid?.let { id ->
            getUserData(uid = id)
        }
    }

    fun onLogin(
        email: String,
        password: String
    ) {
        if (email.isEmpty() || password.isEmpty()) {
            handleException(customMessage = "Please fill in all fields")
            return
        }
        inProgress.value = true
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                inProgress.value = false
                if (task.isSuccessful) {
                    signedId.value = true
                    auth.currentUser?.uid?.let { id ->
                        getUserData(id)
                        popupNotification.value = Event("Login Successfully")
                    }
                } else {
                    handleException(task.exception, "Login failed")
                }
            }
            .addOnFailureListener {
                handleException(exception = it)
                inProgress.value = false
            }
    }

    fun onSignUp(
        username: String,
        email: String,
        password: String
    ) {
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            handleException(customMessage = "Please fill in all fields")
            return
        }
        inProgress.value = true
        db.collection(USERS).whereEqualTo("username", username).get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    handleException(customMessage = "Username already exists")
                    inProgress.value = false
                } else {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                signedId.value = true
                                createOrUpdateProfile(username = username)
                            } else {
                                handleException(task.exception, "Signup failed")
                            }
                            inProgress.value = false
                        }
                }
            }
            .addOnFailureListener {
                handleException(exception = it)
                inProgress.value = false
            }
    }

    private fun handleException(exception: Exception? = null, customMessage: String = "") {
        exception?.printStackTrace()
        val errorMessage = exception?.localizedMessage ?: ""
        val message =
            if (customMessage.isEmpty()) errorMessage else "$customMessage : $errorMessage"
        popupNotification.value = Event(message)
    }

    private fun createOrUpdateProfile(
        username: String? = null,
        name: String? = null,
        bio: String? = null,
        imageUrl: String? = null
    ) {
        val uId = auth.currentUser?.uid
        val user = UserData(
            userId = uId,
            name = name ?: userData.value?.name,
            username = username ?: userData.value?.username,
            bio = bio ?: userData.value?.bio,
            imageUrl = imageUrl ?: userData.value?.imageUrl,
            following = userData.value?.following
        )
        uId?.let { id ->
            inProgress.value = true
            db.collection(USERS)
                .document(id)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        document.reference.update(user.toMap())
                            .addOnSuccessListener {
                                userData.value = user
                                inProgress.value = false
                            }
                            .addOnFailureListener {
                                handleException(it, "Cannot update user")
                                inProgress.value = false
                            }
                    } else {
                        db.collection(USERS).document(id).set(user)
                        getUserData(uid = id)
                        inProgress.value = false
                    }
                }
                .addOnFailureListener {
                    handleException(it, "Cannot create user")
                    inProgress.value = false
                }
        }
    }

    private fun getUserData(uid: String) {
        inProgress.value = true
        db.collection(USERS).document(uid).get()
            .addOnSuccessListener { document ->
                val user = document.toObject<UserData>()
                userData.value = user
                inProgress.value = false
                refreshPosts()
                getPersonalizedFeed()
                getFollowers(uid = user?.userId)
            }
            .addOnFailureListener {
                handleException(it, "Cannot retrieve user data")
                inProgress.value = false
            }
    }

    fun updateProfileData(
        name: String,
        username: String,
        bio: String
    ) {
        createOrUpdateProfile(name = name, username = username, bio = bio)
    }

    private fun uploadImage(
        uri: Uri,
        onSuccess: (Uri) -> Unit
    ) {
        inProgress.value = true
        val storageRef = storage.reference
        val uuid = UUID.randomUUID()
        val imageRef = storageRef.child("images/$uuid")
        val uploadTask = imageRef.putFile(uri)

        uploadTask
            .addOnSuccessListener {
                val result = it.metadata?.reference?.downloadUrl
                result?.addOnSuccessListener(onSuccess)
            }
            .addOnFailureListener { exception ->
                handleException(exception = exception)
                inProgress.value = false
            }
    }

    fun uploadProfileImage(uri: Uri) {
        uploadImage(uri) {
            createOrUpdateProfile(imageUrl = it.toString())
            updatePostUserImageData(imageUrl = it.toString())
        }
    }

    private fun updatePostUserImageData(imageUrl: String) {
        val currentUuid = auth.currentUser?.uid
        db.collection(POSTS)
            .whereEqualTo("userId", currentUuid).get()
            .addOnSuccessListener {
                val posts = mutableStateOf<List<PostData>>(arrayListOf())
                convertPosts(it, posts)
                val refs = arrayListOf<DocumentReference>()
                for (post in posts.value) {
                    post.postId?.let { id ->
                        refs.add(db.collection(POSTS).document(id))
                    }
                }
                if (refs.isNotEmpty()) {
                    db.runBatch { batch ->
                        for(ref in refs) {
                            batch.update(ref, "userImage", imageUrl)
                        }
                    }.addOnSuccessListener {
                        refreshPosts()
                    }
                }
            }
    }

    fun onLogOut() {
        auth.signOut()
        signedId.value = false
        userData.value = null
        popupNotification.value = Event("Logged out")
        searchedPosts.value = listOf()
        postsFeed.value = listOf()
        comments.value = listOf()
    }

    fun onNewPost(uri: Uri, description: String, onSuccess: () -> Unit) {
        uploadImage(uri) {
            onCreatePost(it, description, onSuccess)
        }
    }

    private fun onCreatePost(imageUri: Uri, description: String, onSuccess: () -> Unit) {
        inProgress.value = true
        val currentUid = auth.currentUser?.uid
        val currentUsername = userData.value?.username
        val currentUserImage = userData.value?.imageUrl
        val fillerWords = listOf("the", "be", "to", "is", "of", "and", "or", "a", "in", "it")

        if (currentUid != null) {
            val postUuid = UUID.randomUUID().toString()
            val post = PostData(
                postId = postUuid,
                userId = currentUid,
                username = currentUsername,
                userImage = currentUserImage,
                postImage = imageUri.toString(),
                postDescription = description,
                time = System.currentTimeMillis(),
                likes = listOf(),
                searchTerms = description.split(" ", ".", ",", "?", "!", "#", "-")
                    .map { it.lowercase() }
                    .filter { it.isNotEmpty() && !fillerWords.contains(it) }
            )
            db.collection(POSTS).document(postUuid).set(post)
                .addOnSuccessListener {
                    inProgress.value = false
                    popupNotification.value = Event("Post successfully created")
                    refreshPosts()
                    onSuccess.invoke()
                }
                .addOnFailureListener { exception ->
                    inProgress.value = false
                    handleException(exception, "Unable to create a post")
                }
        } else {
            inProgress.value = false
            handleException(customMessage = "Error: username unavailable. Unable to create post")
            onLogOut()
        }
    }

    private fun refreshPosts() {
        val currentUuid = auth.currentUser?.uid
        if (currentUuid != null) {
            refreshPostsProgress.value = true
            db.collection(POSTS).whereEqualTo("userId", currentUuid).get()
                .addOnSuccessListener { documents ->
                    convertPosts(documents, posts)
                    refreshPostsProgress.value = false
                }
                .addOnFailureListener { exception ->
                    handleException(exception, "Cannot fetch posts")
                    refreshPostsProgress.value = false
                }
        } else {
            handleException(customMessage = "Error: username unavailable. Unable to refresh posts")
            onLogOut()
        }
    }

    private fun convertPosts(
        documents: QuerySnapshot,
        outState: MutableState<List<PostData>>
    ) {
        val newPosts = mutableListOf<PostData>()
        documents.forEach { doc ->
            val post = doc.toObject<PostData>()
            newPosts.add(post)
        }
        val sortedPosts = newPosts.sortedByDescending { it.time }
        outState.value = sortedPosts
    }

    fun searchPosts(searchQuery : String) {
        if (searchQuery.isNotEmpty()) {
            searchedPostsProgress.value = true
            db.collection(POSTS).whereArrayContains("searchTerms", searchQuery.trim().lowercase())
                .get()
                .addOnSuccessListener {
                    convertPosts(it, searchedPosts)
                    searchedPostsProgress.value = false
                }
                .addOnFailureListener { exception ->
                    handleException(exception, "Cannot search posts")
                    searchedPostsProgress.value = false
                }
        }
    }

    fun followOrUnfollowUser(userId: String) {
        auth.currentUser?.uid?.let { currentUser ->
            val following = arrayListOf<String>()
            userData.value?.following?.let {
                following.addAll(it)
            }
            if (!following.remove(userId)) {
                following.add(userId)
            }
            db.collection(USERS).document(currentUser)
                .update("following", following)
                .addOnSuccessListener {
                    getUserData(currentUser)
                }
        }
    }

    private fun getPersonalizedFeed() {
        val following = userData.value?.following
        if (!following.isNullOrEmpty()) {
            postsFeedProgress.value = true
            db.collection(POSTS).whereIn("userId", following).get()
                .addOnSuccessListener {
                    convertPosts(documents = it, outState = postsFeed)
                    if (postsFeed.value.isEmpty()) {
                        getGeneralFeed()
                    } else {
                        postsFeedProgress.value = false
                    }
                }
                .addOnFailureListener { exception ->
                    handleException(exception, "Cannot get personalized feed")
                    postsFeedProgress.value = false
                }
        } else {
            getGeneralFeed()
        }
    }

    private fun getGeneralFeed() {
        postsFeedProgress.value = true
        val currentTime = System.currentTimeMillis()
        val difference = 24 * 60 * 60 * 1000 // 1 day in millis
        db.collection(POSTS)
            .whereGreaterThan("time", currentTime - difference)
            .get()
            .addOnSuccessListener {
                convertPosts(documents = it, outState = postsFeed)
                postsFeedProgress.value = false
            }
            .addOnFailureListener { exception ->
                handleException(exception, "Cannot get general feed")
                postsFeedProgress.value = false
            }
    }

    fun onLikePost(postData: PostData) {
        auth.currentUser?.uid?.let { userId ->
            postData.likes?.let { likes ->
                val newLikes = arrayListOf<String>()
                if (likes.contains(userId)) {
                   newLikes.addAll(likes.filter { userId != it })
                } else {
                    newLikes.addAll(likes)
                    newLikes.add(userId)
                }
                postData.postId?.let {postId ->
                    db.collection(POSTS).document(postId)
                        .update("likes", newLikes)
                        .addOnSuccessListener {
                            postData.likes = newLikes
                        }
                        .addOnFailureListener { exception ->
                            handleException(exception, "Unable to like post")
                        }
                }
            }

        }
    }

    fun onCommentAdded(postId: String, text: String) {
        userData.value?.username?.let { username ->
            val commentId = UUID.randomUUID().toString()
            val comment = CommentData(
                commentId = commentId,
                postId = postId,
                username = username,
                text = text,
                time = System.currentTimeMillis()
            )
            db.collection(COMMENTS).document(commentId).set(comment)
                .addOnSuccessListener {
                    getCommentsForPost(postId)
                }
                .addOnFailureListener { exception ->
                    handleException(exception, "Cannot create comment")
                }
        }
    }

    fun getCommentsForPost(postId: String?) {
        commentsProgress.value = true
        db.collection(COMMENTS).whereEqualTo("postId", postId).get()
            .addOnSuccessListener { documents ->
                val newComments = mutableListOf<CommentData>()
                documents.forEach { document ->
                    val comment = document.toObject<CommentData>()
                    newComments.add(comment)
                }
                val sortedComments = newComments.sortedByDescending { it.time }
                comments.value = sortedComments
                commentsProgress.value = false
            }
            .addOnFailureListener { exception ->
                handleException(exception, "Cannot retrieve comments")
            }
    }

    private fun getFollowers(uid: String?) {
        db.collection(USERS).whereArrayContains("following", uid ?: "").get()
            .addOnSuccessListener { documents ->
                followers.intValue = documents.size()
            }
    }
}