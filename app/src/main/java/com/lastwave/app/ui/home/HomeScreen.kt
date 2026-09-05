package com.lastwave.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.lastwave.app.data.repository.HomeTrack
import com.lastwave.app.ui.common.ArtworkImage
import com.lastwave.app.ui.common.OverflowMenuButton
import com.lastwave.app.ui.common.TrackContextMenuSheet
import com.lastwave.app.ui.common.TrackMenuCapabilities
import com.lastwave.app.ui.common.TrackMenuTarget
import com.lastwave.app.ui.common.safeHorizontalContentPadding
import com.lastwave.app.playback.PlayableTrack
import com.lastwave.app.ui.player.LocalAddToPlaylist
import com.lastwave.app.ui.player.LocalMusicPlayer
import com.lastwave.app.ui.shell.FloatingNavDefaults

// Two shapes, both restrained: a small radius on artwork (reads as "photo",
// not "sticker"), and a pill only where something is genuinely a toggle
// (the friend badge). Everything else in this screen is plain text on plain
// background — no card, no container, no gradient — the list IS the screen.
private val ArtworkShape = RoundedCornerShape(8.dp)
private val BadgeShape = RoundedCornerShape(50)

/**
 * Minimal redesign: a plain large-title header (title + account line, no
 * decorative pills or stat cards) followed directly by the track list. The
 * "Stats" card and the Recent/Most Played/etc. sort control from the
 * previous version are gone — this screen now shows exactly one thing, the
 * list, on the assumption that's what it's opened for. Sort defaults to
 * whatever HomeViewModel's initial sortMode is (Recent).
 *
 * Discover and Genres — previously reachable via header icons and the
 * stats-card arrow — are folded into the overflow menu so they're still
 * reachable without adding more top-level chrome.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenSettings: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenDiscover: () -> Unit,
    onOpenGenres: () -> Unit,
    onOpenFriends: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(viewModel) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.pollWhileActive()
        }
    }

    var menuTrack by remember { mutableStateOf<HomeTrack?>(null) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            HomeHeader(
                displayUsername = if (uiState.isViewingFriend) uiState.viewingUsername else uiState.username,
                isViewingFriend = uiState.isViewingFriend,
                avatarUrl = uiState.stats?.avatarUrl,
                onUsernameClick = onOpenFriends,
                onOpenSearch = onOpenSearch,
                onOpenSettings = onOpenSettings,
                onOpenDiscover = onOpenDiscover,
                onOpenGenres = onOpenGenres,
            )
        },
    ) { scaffoldPadding ->
        if (uiState.isLoading) {
            Box(
                Modifier.fillMaxSize().padding(scaffoldPadding).safeHorizontalContentPadding(),
                contentAlignment = Alignment.Center,
            ) {
                com.lastwave.app.ui.common.ExpressiveLoadingIndicator(message = "Loading your listening history")
            }
            return@Scaffold
        }

        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.padding(scaffoldPadding).fillMaxSize(),
        ) {
            val listState = rememberLazyListState()
            LaunchedEffect(listState, uiState.allTracks.size) {
                snapshotFlowNearEnd(listState) { viewModel.loadNextPage() }
            }

            val rows by viewModel.rows.collectAsStateWithLifecycle()
            val playbackQueue = remember(rows) {
                rows.mapNotNull { row ->
                    (row as? HomeRow.Track)?.track?.let { track ->
                        PlayableTrack(title = track.name, artist = track.artist, artworkUrl = track.artworkUrl)
                    }
                }
            }
            val playbackIndexByRow = remember(rows) {
                var nextPlaybackIndex = 0
                IntArray(rows.size) { rowIndex ->
                    if (rows[rowIndex] is HomeRow.Track) nextPlaybackIndex++ else -1
                }
            }
            val musicPlayer = LocalMusicPlayer.current
            val addToPlaylist = LocalAddToPlaylist.current

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().safeHorizontalContentPadding(),
                contentPadding = PaddingValues(
                    top = 4.dp,
                    bottom = FloatingNavDefaults.contentBottomPadding(),
                ),
            ) {
                itemsIndexed(
                    rows,
                    key = { _, row ->
                        when (row) {
                            is HomeRow.DateHeader -> "date_${row.label}"
                            is HomeRow.Track -> if (row.track.isNowPlaying) {
                                "nowplaying_${row.track.key}"
                            } else {
                                "track_${row.track.key}_${row.track.timestampMillis}"
                            }
                        }
                    },
                    contentType = { _, row ->
                        when (row) {
                            is HomeRow.DateHeader -> "date"
                            is HomeRow.Track -> "track"
                        }
                    },
                ) { rowIndex, row ->
                    when (row) {
                        is HomeRow.DateHeader -> DateHeaderRow(row.label)
                        is HomeRow.Track -> TrackRow(
                            track = row.track,
                            badge = row.badge,
                            onClick = {
                                musicPlayer.playQueue(
                                    tracks = playbackQueue,
                                    startIndex = playbackIndexByRow[rowIndex],
                                    sourceLabel = "Home",
                                )
                            },
                            onLongClick = {
                                addToPlaylist(
                                    PlayableTrack(
                                        title = row.track.name,
                                        artist = row.track.artist,
                                        artworkUrl = row.track.artworkUrl,
                                    ),
                                )
                            },
                            onMenuClick = { menuTrack = row.track },
                        )
                    }
                }

                if (rows.isEmpty()) {
                    item(key = "empty", contentType = "empty") {
                        Box(
                            Modifier.fillMaxWidth().padding(vertical = 64.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("No tracks yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }

    menuTrack?.let { track ->
        TrackContextMenuSheet(
            target = TrackMenuTarget.Track(track.name, track.artist, track.artworkUrl.orEmpty()),
            capabilities = TrackMenuCapabilities(showCopyActions = true, showDeleteScrobble = true),
            playbackSourceLabel = "Home",
            onDismiss = { menuTrack = null },
        )
    }
}

private suspend fun snapshotFlowNearEnd(listState: LazyListState, onNearEnd: () -> Unit) {
    snapshotFlow {
        val info = listState.layoutInfo
        val total = info.totalItemsCount
        val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: 0
        total > 0 && lastVisible >= total - 5
    }.collect { isNear -> if (isNear) onNearEnd() }
}

/**
 * A plain large title ("Home") with the account name underneath as a small,
 * quiet subtitle — tapping it opens the friend switcher, same as the old
 * username pill did, just without looking like a button until you touch it.
 * Three flat icon buttons on the trailing edge: Search and Settings, both
 * things reached from Home often enough to earn a dedicated icon, and a
 * single overflow (⋮) holding Discover and Genres, reached rarely enough
 * that they don't need their own permanent icons.
 */
@Composable
private fun HomeHeader(
    displayUsername: String,
    isViewingFriend: Boolean,
    avatarUrl: String?,
    onUsernameClick: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenDiscover: () -> Unit,
    onOpenGenres: () -> Unit,
) {
    var overflowOpen by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .safeHorizontalContentPadding()
            .padding(top = 20.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f, fill = false)) {
            Text(
                "Home",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.3).sp,
            )
            Spacer(Modifier.height(2.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clip(RoundedCornerShape(6.dp)).combinedClickable(onClick = onUsernameClick),
            ) {
                Text(
                    if (isViewingFriend) "Viewing $displayUsername" else displayUsername.ifBlank { "—" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isViewingFriend) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (isViewingFriend) {
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        Icons.Filled.People,
                        contentDescription = "Switch profile",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onOpenSearch) {
                Icon(Icons.Filled.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurface)
            }
            Box {
                IconButton(onClick = { overflowOpen = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "More", tint = MaterialTheme.colorScheme.onSurface)
                }
                DropdownMenu(expanded = overflowOpen, onDismissRequest = { overflowOpen = false }) {
                    DropdownMenuItem(
                        text = { Text("Discover") },
                        leadingIcon = { Icon(Icons.Filled.Explore, contentDescription = null) },
                        onClick = { overflowOpen = false; onOpenDiscover() },
                    )
                    DropdownMenuItem(
                        text = { Text("Genres") },
                        leadingIcon = { Icon(Icons.Filled.BarChart, contentDescription = null) },
                        onClick = { overflowOpen = false; onOpenGenres() },
                    )
                }
            }
            IconButton(onClick = onOpenSettings) {
                ProfileAvatar(avatarUrl = avatarUrl, modifier = Modifier.size(28.dp))
            }
        }
    }
}

@Composable
private fun ProfileAvatar(avatarUrl: String?, modifier: Modifier = Modifier) {
    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceContainerHigh, modifier = modifier) {
        if (!avatarUrl.isNullOrBlank()) {
            ArtworkImage(
                name = "profile",
                artist = "avatar",
                embeddedUrl = avatarUrl,
                fallbackIcon = Icons.Filled.AccountCircle,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    Icons.Filled.AccountCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** Section label above each day's tracks — small caps, no background, no
 *  card: it reads as a heading because of the typography, not a container. */
@Composable
private fun DateHeaderRow(label: String) {
    Text(
        label.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 0.6.sp,
        modifier = Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 6.dp),
    )
}

/**
 * One row: artwork, title/artist, overflow. No card, no elevation, no
 * gradient. "Now playing" is conveyed with a small static equalizer glyph
 * over the artwork and a tinted title — not a pulsing pill — so scanning the
 * list stays calm even while something is actively playing.
 */
@Composable
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
private fun TrackRow(
    track: HomeTrack,
    badge: String?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMenuClick: () -> Unit,
) {
    val isNowPlaying = track.isNowPlaying
    val titleColor = if (isNowPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    val subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box {
            ArtworkImage(
                name = track.name,
                artist = track.artist,
                embeddedUrl = track.artworkUrl,
                fallbackIcon = Icons.Filled.MusicNote,
                modifier = Modifier
                    .size(48.dp)
                    .clip(ArtworkShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            )
            if (isNowPlaying) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp).align(Alignment.BottomEnd).padding(1.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Filled.GraphicEq,
                            contentDescription = "Now playing",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(11.dp),
                        )
                    }
                }
            }
        }

        Spacer(Modifier.width(14.dp))

        Column(Modifier.weight(1f)) {
            Text(
                track.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    track.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = subtitleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (badge != null) {
                    Spacer(Modifier.width(6.dp))
                    Surface(shape = BadgeShape, color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                        Text(
                            badge,
                            style = MaterialTheme.typography.labelSmall,
                            color = subtitleColor,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                        )
                    }
                }
            }
        }

        Spacer(Modifier.width(4.dp))
        OverflowMenuButton(onClick = onMenuClick)
    }
}
