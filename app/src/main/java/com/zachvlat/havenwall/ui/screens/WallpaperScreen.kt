package com.zachvlat.havenwall.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.drawable.toBitmap
import coil.compose.AsyncImage
import coil.imageLoader
import com.zachvlat.havenwall.WallpaperViewModel
import com.zachvlat.havenwall.data.Tag
import com.zachvlat.havenwall.data.Wallpaper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun WallpaperScreen(
    viewModel: WallpaperViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var selectedWallpaper by remember { mutableStateOf<Wallpaper?>(null) }
    val focusManager = LocalFocusManager.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    0 -> BrowseTab(
                        viewModel = viewModel,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        isSearchActive = isSearchActive,
                        onActivateSearch = {
                            isSearchActive = true
                            viewModel.search(searchQuery)
                            focusManager.clearFocus()
                        },
                        onSearch = {
                            viewModel.search(searchQuery)
                            focusManager.clearFocus()
                        },
                        onWallpaperClick = { selectedWallpaper = it }
                    )
                    1 -> FavoritesTab(
                        favorites = viewModel.favorites,
                        onWallpaperClick = { selectedWallpaper = it }
                    )
                }
            }

            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Browse") },
                    label = { Text("Browse") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Favorite, contentDescription = "Favorites") },
                    label = { Text("Favorites") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
            }
        }
    }

    selectedWallpaper?.let { wallpaper ->
        LaunchedEffect(wallpaper.id) {
            viewModel.fetchWallpaperDetail(wallpaper.id)
        }
        WallpaperPreviewDialog(
            wallpaper = wallpaper,
            tags = viewModel.wallpaperDetail?.takeIf { it.id == wallpaper.id }?.tags.orEmpty(),
            isFavorite = viewModel.isFavorite(wallpaper.id),
            onToggleFavorite = { viewModel.toggleFavorite(wallpaper) },
            onSearchTag = { tag ->
                selectedWallpaper = null
                searchQuery = tag
                isSearchActive = true
                viewModel.search(tag)
                focusManager.clearFocus()
            },
            onDismiss = { selectedWallpaper = null }
        )
    }
}

@Composable
fun BrowseTab(
    viewModel: WallpaperViewModel,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isSearchActive: Boolean,
    onActivateSearch: () -> Unit,
    onSearch: () -> Unit,
    onWallpaperClick: (Wallpaper) -> Unit
) {
    val gridState = rememberLazyGridState()
    val focusManager = LocalFocusManager.current

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = gridState.layoutInfo.totalItemsCount
            lastVisibleItem >= totalItems - 4
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && viewModel.isSearching) {
            viewModel.loadNextPage()
        }
    }

    AnimatedVisibility(
        visible = isSearchActive,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut()
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            state = gridState,
            contentPadding = PaddingValues(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "HavenWall",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SearchBar(
                        query = searchQuery,
                        onQueryChange = onSearchQueryChange,
                        onSearch = onSearch,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }
            }

            items(
                items = viewModel.wallpapers,
                key = { it.id }
            ) { wallpaper ->
                WallpaperItem(
                    wallpaper = wallpaper,
                    onClick = { onWallpaperClick(wallpaper) }
                )
            }

            if (viewModel.isLoading) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            if (viewModel.errorMessage != null) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = viewModel.errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }

    AnimatedVisibility(
        visible = !isSearchActive,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "HavenWall",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(24.dp))
            SearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                onSearch = onActivateSearch,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            )
        }
    }
}

@Composable
fun FavoritesTab(
    favorites: List<Wallpaper>,
    onWallpaperClick: (Wallpaper) -> Unit
) {
    if (favorites.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No favorites yet",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(
                items = favorites,
                key = { it.id }
            ) { wallpaper ->
                WallpaperItem(
                    wallpaper = wallpaper,
                    onClick = { onWallpaperClick(wallpaper) }
                )
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Search your wallpaper") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        modifier = modifier
            .height(56.dp)
    )
}

@Composable
fun WallpaperItem(
    wallpaper: Wallpaper,
    onClick: () -> Unit
) {
    AsyncImage(
        model = wallpaper.thumbs.original,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(wallpaper.dimensionX.toFloat() / wallpaper.dimensionY.toFloat())
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WallpaperPreviewDialog(
    wallpaper: Wallpaper,
    tags: List<Tag>,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onSearchTag: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isSetting by remember { mutableStateOf(false) }
    var setError by remember { mutableStateOf<String?>(null) }
    val bitmap by produceState<android.graphics.Bitmap?>(
        initialValue = null,
        key1 = wallpaper.path
    ) {
        value = withContext(Dispatchers.IO) {
            loadBitmap(context, wallpaper.path)
        }
    }

    Dialog(
        onDismissRequest = { if (!isSetting) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box {
                AsyncImage(
                    model = wallpaper.path,
                    contentDescription = wallpaper.id,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(wallpaper.dimensionX.toFloat() / wallpaper.dimensionY.toFloat())
                        .clip(RoundedCornerShape(8.dp))
                )

                val heartScale = remember { Animatable(1f) }
                LaunchedEffect(isFavorite) {
                    heartScale.snapTo(1f)
                    heartScale.animateTo(1.4f, tween(durationMillis = 120))
                    heartScale.animateTo(
                        targetValue = 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(40.dp)
                        .background(
                            Color.Black.copy(alpha = 0.4f),
                            RoundedCornerShape(20.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color.Red else Color.White,
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer {
                                scaleX = heartScale.value
                                scaleY = heartScale.value
                            }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (tags.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tags.forEach { tag ->
                        AssistChip(
                            onClick = { onSearchTag(tag.name) },
                            label = { Text(tag.name) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = {
                    isSetting = true
                    setError = null
                    val imageUrl = wallpaper.path
                    scope.launch {
                        val error = withContext(Dispatchers.IO) {
                            setWallpaper(context, imageUrl)
                        }
                        if (error == null) {
                            Toast.makeText(context, "Wallpaper set", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        } else {
                            setError = error
                        }
                        isSetting = false
                    }
                },
                enabled = !isSetting && bitmap != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isSetting) "Setting..." else "Set as Wallpaper"
                )
            }

            if (setError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = setError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onDismiss,
                enabled = !isSetting
            ) {
                Text("Close")
            }
        }
    }
}

private suspend fun loadBitmap(context: Context, imageUrl: String): android.graphics.Bitmap? {
    return try {
        val loader = context.imageLoader
        val request = coil.request.ImageRequest.Builder(context)
            .data(imageUrl)
            .allowHardware(false)
            .build()
        loader.execute(request).drawable?.toBitmap()
    } catch (e: Exception) {
        null
    }
}

private suspend fun setWallpaper(context: Context, imageUrl: String): String? {
    return try {
        val bitmap = loadBitmap(context, imageUrl)
        if (bitmap == null) return "Failed to load image"

        val metrics = context.resources.displayMetrics
        val screenWidth = metrics.widthPixels
        val screenHeight = metrics.heightPixels
        val centered = centerCrop(bitmap, screenWidth, screenHeight)

        val manager = android.app.WallpaperManager.getInstance(context.applicationContext)
        manager.setBitmap(centered)
        null
    } catch (e: Exception) {
        e.message ?: "Failed to set wallpaper"
    }
}

private fun centerCrop(source: android.graphics.Bitmap, targetWidth: Int, targetHeight: Int): android.graphics.Bitmap {
    val scale = maxOf(
        targetWidth.toFloat() / source.width,
        targetHeight.toFloat() / source.height
    )
    val scaledWidth = (source.width * scale).toInt().coerceAtLeast(targetWidth)
    val scaledHeight = (source.height * scale).toInt().coerceAtLeast(targetHeight)
    val scaled = android.graphics.Bitmap.createScaledBitmap(source, scaledWidth, scaledHeight, true)
    val x = ((scaledWidth - targetWidth) / 2).coerceIn(0, scaledWidth - targetWidth)
    val y = ((scaledHeight - targetHeight) / 2).coerceIn(0, scaledHeight - targetHeight)
    return android.graphics.Bitmap.createBitmap(scaled, x, y, targetWidth, targetHeight)
}