package com.lastwave.app.ui.home

import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.lastwave.app.ui.common.ArtworkImage
import com.lastwave.app.ui.common.ExpressiveHeader
import com.lastwave.app.ui.common.HeaderActionIcon
import com.lastwave.app.ui.common.safeHorizontalContentPadding
import com.lastwave.app.ui.common.TrackContextMenuSheet
import com.lastwave.app.ui.common.TrackMenuCapabilities
import com.lastwave.app.ui.common.TrackMenuTarget
import com.lastwave.app.ui.theme.ArtworkShape
import com.lastwave.app.ui.theme.BadgePillShape
import com.lastwave.app.ui.theme.ExpressiveHeroShape
import com.lastwave.app.ui.theme.ExpressivePillShape
import com.lastwave.app.ui.theme.HeroInnerShape
import com.lastwave.app.ui.theme.ListContainerShape
import com.lastwave.app.ui.theme.NowPlayingCardShape
import com.lastwave.app.ui.theme.StatPillShape
import com.lastwave.app.ui.theme.TrackRowShape
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.Scaffold
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.People
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically

import androidx.compose.animation.fadeIn
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.lastwave.app.ui.shell.FloatingNavDefaults
import coil.compose.SubcomposeAsyncImage
import com.lastwave.app.data.repository.HomeSortMode
import com.lastwave.app.data.repository.HomeTrack
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

// Immutable shapes hoisted out of composition: previously each of these was
// constructed inline inside row/card composables, i.e. re-allocated for every
// row on every recomposition. Rows are the hottest path while scrolling, so
// they must not allocate.
//
// Apple-style redesign: soft rounded corners — these used to shadow the
// (also now rounded) shapes in ui/theme/Shape.kt with their own flat
// values, so rounding the theme file alone wouldn't have changed anything
// here without updating these too.
private val ListContainerShape = RoundedCornerShape(16.dp)
private val TrackRowShape = RoundedCornerShape(12.dp)
private val NowPlayingCardShape = RoundedCornerShape(14.dp)
private val ArtworkShape = RoundedCornerShape(8.dp)
private val BadgePillShape = RoundedCornerShape(50)
private val StatPillShape = RoundedCornerShape(50)
private val HeroInnerShape = RoundedCornerShape(14.dp)

/**
 * Faithful port of home.html/home.js's layout, top to bottom:
 *  1. Header row — username pill (left) + live listen timer (right)
 *  2. Stats card — big "Scrobbles" number + arrow-to-Genres, then a
 *     Tracks / Artists / Albums row
 *  3. Mix card — "List" title + sort dropdown (Recent / Most Played /
 *     Last 7 Days / Last 30 Days), then the track list itself, with the
 *     Now Playing row always pinned first when present.
 * There's no separate "Now Playing card" — that was an earlier, simplified
 * substitute; the real app renders it as the first row of the same list.
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
            ExpressiveHeader(
                title = "LastWave",
                actions = {
                    HeaderActionIcon(Icons.Filled.Explore, "Discover", onOpenDiscover)
                    HeaderActionIcon(Icons.Filled.Search, "Search", onOpenSearch)
                    IconButton(onClick = onOpenSettings) {
                        ProfileAvatar(avatarUrl = uiState.stats?.avatarUrl, modifier = Modifier.size(30.dp))
                    }
                },
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

        Column(Modifier.fillMaxSize().padding(scaffoldPadding).safeHorizontalContentPadding()) {
            HeaderRow(
                displayUsername = if (uiState.isViewingFriend) uiState.viewingUsername else uiState.username,
                isViewingFriend = uiState.isViewingFriend,
                onClick = onOpenFriends,
                viewModel = viewModel,
            )
            Spacer(Modifier.height(2.dp))

            uiState.stats?.let { stats ->
                StatsCard(
                    scrobbles = stats.scrobbles,
                    trackCount = stats.trackCount,
                    artistCount = stats.artistCount,
                    albumCount = stats.albumCount,
                    onOpenGenres = onOpenGenres,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 0.dp),
                )
                Spacer(Modifier.height(12.dp))
            }

            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier.weight(1f).fillMaxWidth(),
            ) {
                val listState = rememberLazyListState()
                LaunchedEffect(listState, uiState.allTracks.size) {
                    snapshotFlowNearEnd(listState) { viewModel.loadNextPage() }
                }

                // Computed off the main thread in HomeViewModel — see its
                // doc comment on `rows` for why this used to jank on every
                // Last.fm poll tick when it ran inline here instead.
                val rows by viewModel.rows.collectAsStateWithLifecycle()
                val playbackQueue = remember(rows) {
                    rows.mapNotNull { row ->
                        (row as? HomeRow.Track)?.track?.let { track ->
                            com.lastwave.app.playback.PlayableTrack(
                                title = track.name,
                                artist = track.artist,
                                artworkUrl = track.artworkUrl,
                            )
                        }
                    }
                }
                val playbackIndexByRow = remember(rows) {
                    var nextPlaybackIndex = 0
                    IntArray(rows.size) { rowIndex ->
                        if (rows[rowIndex] is HomeRow.Track) nextPlaybackIndex++ else -1
                    }
                }
                val musicPlayer = com.lastwave.app.ui.player.LocalMusicPlayer.current
                val addToPlaylist = com.lastwave.app.ui.player.LocalAddToPlaylist.current

                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    // Apple-style redesign: the list used to sit inside a
                    // filled surfaceContainer card (a dark plate against a
                    // dark page). On a white page that same plate reads as
                    // a gray box around the content; Apple Music's own
                    // lists sit directly on the page with a plain section
                    // title above them instead.
                ) {
                    Column(Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                        MixHeader(sortMode = uiState.sortMode, onSortModeChange = viewModel::setSortMode)
                    }

                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(
                            start = 8.dp,
                            end = 8.dp,
                            top = 0.dp,
                            bottom = FloatingNavDefaults.contentBottomPadding(),
                        ),
                        modifier = Modifier.fillMaxSize(),
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
                            Box {
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
                                                com.lastwave.app.playback.PlayableTrack(
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
                        }

                        if (rows.isEmpty()) {
                            item(key = "empty", contentType = "empty") {
                                Box(
                                    Modifier.fillMaxWidth().padding(vertical = 48.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text("No tracks yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    menuTrack?.let { track ->
        com.lastwave.app.ui.common.TrackContextMenuSheet(
            target = com.lastwave.app.ui.common.TrackMenuTarget.Track(track.name, track.artist, track.artworkUrl.orEmpty()),
            capabilities = com.lastwave.app.ui.common.TrackMenuCapabilities(showCopyActions = true, showDeleteScrobble = true),
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

@Composable
private fun HeaderRow(
    displayUsername: String,
    isViewingFriend: Boolean,
    onClick: () -> Unit,
    viewModel: HomeViewModel,
) {
    // Apple-style redesign: a large "Good evening" title takes the place of
    // the username pill — the username/friend-switch affordance moves to a
    // small tappable line underneath it instead of competing for top billing
    // with the listen timer. Two equal-weight pills side by side read as a
    // toolbar, not a greeting.
    Column(modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 2.dp)) {
        Text(
            greetingForNow(),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.combinedClickable(onClick = onClick, onLongClick = {}),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    displayUsername.ifBlank { "—" },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (isViewingFriend) {
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        Icons.Filled.People,
                        contentDescription = "Switch profile",
                        modifier = Modifier.size(15.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            LiveListenTimer(viewModel)
        }
    }
}

/** Apple Music-style time-of-day greeting, replacing the raw username as the page's headline. */
private fun greetingForNow(): String {
    val hour = java.time.LocalTime.now().hour
    return when {
        hour < 5 -> "Good night"
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        else -> "Good evening"
    }
}

@Composable
private fun LiveListenTimer(viewModel: HomeViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listenElapsedSeconds by viewModel.listenElapsedSeconds.collectAsStateWithLifecycle()
    val totalSeconds = (uiState.stats?.timerBaseSeconds ?: 0) + listenElapsedSeconds.toLong()
    val isPlaying = uiState.nowPlaying != null

    // Apple-style redesign: a plain inline row instead of a filled pill —
    // this is a passive stat, not a tappable control, so it shouldn't look
    // like a button.
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Filled.Headset,
            contentDescription = "Estimated lifetime listening time",
            modifier = Modifier.size(15.dp),
            tint = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(5.dp))
        Text(
            formatTimer(totalSeconds),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun formatTimer(totalSeconds: Long): String {
    // This is an ESTIMATED LIFETIME total (scrobble count × an average
    // track length — see HomeRepository.HomeStats.timerBaseSeconds), not a
    // session/session-elapsed timer, so it legitimately runs into weeks or
    // months for anyone with a large scrobble history — 18,838 scrobbles
    // at ~3.5 min average really is ~46 days of total listening. The raw
    // "DD:HH:MM:SS" digits made that read as a broken/runaway counter
    // instead of what it actually is; spelling out the units (matching how
    // the rest of the app writes durations elsewhere) makes the same
    // number immediately legible as "45 days" instead of a wall of colons.
    if (totalSeconds <= 0) return "--"
    val d = totalSeconds / 86400
    val h = (totalSeconds % 86400) / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return when {
        d > 0 -> "${d}d ${h}h ${m}m"
        h > 0 -> "${h}h ${m}m ${s}s"
        else -> "${m}m ${s}s"
    }
}

@Composable
private fun ProfileAvatar(avatarUrl: String?, modifier: Modifier = Modifier) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = modifier,
    ) {
        if (!avatarUrl.isNullOrBlank()) {
            ArtworkImage(
                name = "profile",
                artist = "avatar",
                embeddedUrl = avatarUrl,
                fallbackIcon = Icons.Filled.AccountCircle,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Filled.AccountCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

@Composable
private fun StatsCard(
    scrobbles: Long,
    trackCount: Long,
    artistCount: Long,
    albumCount: Long,
    onOpenGenres: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    // Apple-style redesign: no filled hero box. The scrobble count sits
    // directly on the page background as a plain oversized number (the way
    // Apple treats a headline stat), with a light-gray tap target for the
    // genres shortcut instead of a saturated primary-colored button.
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(400)) + expandVertically(animationSpec = tween(400)),
        modifier = modifier,
    ) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(HeroInnerShape)
                    .combinedClickable(onClick = onOpenGenres, onLongClick = {})
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        formatCount(rememberAnimatedCount(scrobbles)),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        "Scrobbles · tap to explore genres",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Surface(
                    shape = ExpressivePillShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.size(38.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "View genres",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatPill("Tracks", trackCount, Modifier.weight(1f))
                StatPill("Artists", artistCount, Modifier.weight(1f))
                StatPill("Albums", albumCount, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun rememberAnimatedCount(target: Long): Long {
    val animated = remember { Animatable(0f) }
    LaunchedEffect(target) {
        animated.animateTo(
            targetValue = target.toFloat(),
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessVeryLow,
            ),
        )
    }
    return animated.value.toLong()
}

@Composable
private fun StatPill(label: String, value: Long, modifier: Modifier = Modifier) {
    // Apple-style redesign: a light gray card (surfaceContainer) with dark
    // ink text, matching Settings.app-style grouped stat tiles — the old
    // version tinted these with onPrimaryContainer text, which read as
    // washed-out low-contrast blue-on-blue against a white background.
    Surface(
        modifier = modifier,
        shape = StatPillShape,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(Modifier.padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                formatCount(rememberAnimatedCount(value)),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun formatCount(value: Long): String = if (value <= 0) "—" else "%,d".format(value)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MixHeader(sortMode: HomeSortMode, onSortModeChange: (HomeSortMode) -> Unit) {
    var menuOpen by remember { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current
    // Real, felt tactile feedback: haptic now fires the instant the pill is
    // pressed (finger down), not only after the click completes — firing it
    // solely inside onClick meant it landed at the same moment the dropdown
    // menu opened and covered the pill, which could read as "nothing
    // happened" since the press-scale's own short spring had barely started
    // by then. A dedicated LaunchedEffect on the raw pressed state decouples
    // the haptic from whatever the click itself goes on to do.
    val pillInteractionSource = remember { MutableInteractionSource() }
    val pillPressed by pillInteractionSource.collectIsPressedAsState()
    LaunchedEffect(pillPressed) {
        if (pillPressed) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    }
    // A more pronounced dip than the shared rememberGroupPressScale (tuned
    // for full-width group rows) — a small pill needs a bigger relative
    // shrink to actually read as a press at this size.
    val pillScale by animateFloatAsState(
        targetValue = if (pillPressed) 0.90f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "sortPillPressScale",
    )
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            "Recently played",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterVertically),
        )
        Box {
            Surface(
                onClick = { menuOpen = true },
                shape = BadgePillShape,
                color = Color.Transparent,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                tonalElevation = 0.dp,
                interactionSource = pillInteractionSource,
                modifier = Modifier.heightIn(min = 34.dp).scale(pillScale),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        iconForSortMode(sortMode),
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        sortModeLabel(sortMode),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        Icons.Filled.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            DropdownMenu(
                expanded = menuOpen,
                onDismissRequest = { menuOpen = false },
                shape = RoundedCornerShape(24.dp),
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                // Lowered from 10dp: a heavy shadowElevation on a popup
                // paints a mostly-rectangular drop shadow around the
                // rounded card (the shadow's own corner falloff is much
                // subtler than the card's actual corner radius), which is
                // exactly what read as "squarish" — a light shadow plus a
                // bit more tonalElevation for legibility fixes that
                // without losing depth entirely.
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                modifier = Modifier.padding(vertical = 4.dp),
            ) {
                SortOption(Icons.Filled.Schedule, "Recent", sortMode == HomeSortMode.RECENT) {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSortModeChange(HomeSortMode.RECENT); menuOpen = false
                }
                SortOption(Icons.Filled.BarChart, "Most Played", sortMode == HomeSortMode.MOST_PLAYED) {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSortModeChange(HomeSortMode.MOST_PLAYED); menuOpen = false
                }
                SortOption(Icons.Filled.DateRange, "Last 7 Days", sortMode == HomeSortMode.LAST_7_DAYS) {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSortModeChange(HomeSortMode.LAST_7_DAYS); menuOpen = false
                }
                SortOption(Icons.Filled.CalendarMonth, "Last 30 Days", sortMode == HomeSortMode.LAST_30_DAYS) {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSortModeChange(HomeSortMode.LAST_30_DAYS); menuOpen = false
                }
            }
        }
    }
}

private fun iconForSortMode(mode: HomeSortMode): androidx.compose.ui.graphics.vector.ImageVector = when (mode) {
    HomeSortMode.RECENT -> Icons.Filled.Schedule
    HomeSortMode.MOST_PLAYED -> Icons.Filled.BarChart
    HomeSortMode.LAST_7_DAYS -> Icons.Filled.DateRange
    HomeSortMode.LAST_30_DAYS -> Icons.Filled.CalendarMonth
}

private fun sortModeLabel(mode: HomeSortMode) = when (mode) {
    HomeSortMode.RECENT -> "Recent"
    HomeSortMode.MOST_PLAYED -> "Most Played"
    HomeSortMode.LAST_7_DAYS -> "Last 7 Days"
    HomeSortMode.LAST_30_DAYS -> "Last 30 Days"
}

@Composable
private fun SortOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = {
            Text(
                label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            )
        },
        leadingIcon = {
            Icon(
                icon,
                contentDescription = null,
                tint = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingIcon = {
            if (active) Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        },
        onClick = onClick,
        modifier = if (active) {
            Modifier
                .padding(horizontal = 6.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
        } else {
            Modifier.padding(horizontal = 6.dp)
        },
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
    )
}

@Composable
private fun DateHeaderRow(label: String) {
    Text(
        label,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 8.dp, bottom = 2.dp),
    )
}

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
    // Now-playing rows get a rounded outlined pill around the whole row —
    // a primary-colored hairline border plus a soft tonal fill — so the
    // active track is glanceable while scrolling, regardless of what
    // artwork or background sits behind it. Animates in/out on change.
    val secondaryTextColor =
        if (isNowPlaying) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        else MaterialTheme.colorScheme.onSurfaceVariant
    val highlightAlpha by animateFloatAsState(
        targetValue = if (isNowPlaying) 1f else 0f,
        animationSpec = tween(220),
        label = "nowPlayingHighlight",
    )
    Surface(
        shape = TrackRowShape,
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.30f * highlightAlpha),
        border = if (highlightAlpha > 0f) {
            BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.55f * highlightAlpha))
        } else null,
        tonalElevation = 0.dp,
        modifier = Modifier.fillMaxWidth().combinedClickable(onClick = onClick, onLongClick = onLongClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .padding(vertical = 6.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(ArtworkShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            ) {
                ArtworkImage(
                    name = track.name,
                    artist = track.artist,
                    embeddedUrl = track.artworkUrl,
                    fallbackIcon = if (isNowPlaying) Icons.Filled.GraphicEq else Icons.Filled.MusicNote,
                    modifier = Modifier.fillMaxSize(),
                )
                if (isNowPlaying) {
                    com.lastwave.app.ui.player.PlayingWaveBars(
                        modifier = Modifier.align(Alignment.BottomEnd).padding(2.dp).size(24.dp, 18.dp),
                    )
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    track.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isNowPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    track.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = secondaryTextColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(8.dp))
            if (isNowPlaying) {
                val transition = rememberInfiniteTransition(label = "nowPlayingPulse")
                val pulseScale = transition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.06f,
                    animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Reverse),
                    label = "pulseScale",
                )
                Surface(
                    shape = BadgePillShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.graphicsLayer {
                        scaleX = pulseScale.value
                        scaleY = pulseScale.value
                    },
                ) {
                    Text(
                        "Now Playing",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    )
                }
            }
            if (badge != null) {
                Surface(shape = BadgePillShape, color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                    Text(
                        badge,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }
            // Home-only per item 7: a small filled tonal container around
            // the overflow trigger. This composable is private to
            // HomeScreen.kt, so this doesn't touch the three-dot button on
            // Item 1 (consistency pass): the same OverflowMenuButton is now
            // used on every screen's song list, not just Home.
            com.lastwave.app.ui.common.OverflowMenuButton(onClick = onMenuClick)
        }
    }
}
