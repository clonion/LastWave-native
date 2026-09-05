package com.lastwave.app.ui.shell

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.lastwave.app.ui.common.PredictiveBackScreen
import com.lastwave.app.ui.generate.GenerateScreen
import com.lastwave.app.ui.generate.MixLauncher
import com.lastwave.app.ui.home.HomeScreen
import com.lastwave.app.ui.playlist.PlaylistScreen
import com.lastwave.app.ui.player.LocalMiniPlayerScrollClearance
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

/** Thin bridge exposing MixLauncher's requests to MainShell — MainShell
 *  itself isn't a screen with its own ViewModel, so this is the smallest
 *  way to reach the same singleton GenerateViewModel already listens to
 *  (see MixLauncher's doc comment for the full "Start Mix" flow). */
@HiltViewModel
class MainShellViewModel @Inject constructor(mixLauncher: MixLauncher) : ViewModel() {
    val mixRequests = mixLauncher.requests
}

private enum class MainTab(val label: String) { HOME("Home"), GENERATE("Generate"), PLAYLISTS("Playlists") }

/** Shared with any screen hosted inside [MainShell] so their scrolling
 *  lists know how much bottom content padding to reserve — the nav
 *  overlays content (it's not a Scaffold bottomBar reserving space), so
 *  each screen leaves this much room for its last item to clear it. */
object FloatingNavDefaults {
    // Tuned for the flat, full-width tab bar (icon + label, no vertical
    // dock margins) — shorter than the old floating-pill dock needed.
    val ContentBottomPadding = 72.dp

    /**
     * Full bottom clearance for edge-to-edge scrolling content: the tab
     * bar's own height ([ContentBottomPadding]) PLUS the live
     * navigation-bar (gesture area) inset. Screens that let their list draw
     * beneath the transparent gesture area must use this instead of the raw
     * constant, otherwise the last row hides behind the bar/gesture area.
     */
    @Composable
    fun contentBottomPadding(): Dp =
        ContentBottomPadding +
            LocalMiniPlayerScrollClearance.current +
            WindowInsets.safeDrawing.asPaddingValues().calculateBottomPadding()
}

@Composable
fun MainShell(
    onOpenSettings: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenDiscover: () -> Unit,
    onOpenGenres: () -> Unit,
    onOpenFriends: () -> Unit,
    onOpenPlaylist: (Long) -> Unit = {},
    mainShellViewModel: MainShellViewModel = hiltViewModel(),
) {
    val tabs = MainTab.entries
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

    // "Start Mix with this Song" (§6) can be tapped from any screen's track
    // menu, including ones pushed on top of MainShell (Discover/Search/
    // Genres) — this keeps running even while MainShell isn't the visible
    // screen, since its composition isn't disposed just because another
    // route is on top of it in the back stack.
    LaunchedEffect(Unit) {
        mainShellViewModel.mixRequests.collect {
            scope.launch { pagerState.animateScrollToPage(tabs.indexOf(MainTab.GENERATE)) }
        }
    }

    // A plain Box, not Scaffold(bottomBar = ...): the nav floats ON TOP of
    // content via Box alignment, never reserving/subtracting its own
    // height from the content area.
    Box(Modifier.fillMaxSize()) {
        val homeIndex = tabs.indexOf(MainTab.HOME)
        HorizontalPager(
            state = pagerState,
            // Pager prefetches the gesture destination itself. Keeping all
            // three tabs composed made off-screen lists, image loaders and
            // infinite animations compete with the visible page for frames.
            beyondViewportPageCount = 0,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            // Predictive back on a non-Home tab returns to Home (with the
            // same swipe-to-pop gesture as other screens), but once on Home
            // stays enabled = false so back on Home falls through to the
            // system default (exit/minimize), same as before.
            val isCurrent = page == pagerState.currentPage
            PredictiveBackScreen(
                enabled = isCurrent && tabs[page] != MainTab.HOME,
                onBack = { scope.launch { pagerState.animateScrollToPage(homeIndex) } },
            ) {
                when (tabs[page]) {
                    MainTab.HOME -> HomeScreen(onOpenSettings = onOpenSettings, onOpenSearch = onOpenSearch, onOpenDiscover = onOpenDiscover, onOpenGenres = onOpenGenres, onOpenFriends = onOpenFriends)
                    MainTab.GENERATE -> GenerateScreen(
                        onNavigateToPlaylist = {
                            scope.launch { pagerState.animateScrollToPage(tabs.indexOf(MainTab.PLAYLISTS)) }
                        },
                    )
                    MainTab.PLAYLISTS -> PlaylistScreen(onOpenPlaylist = onOpenPlaylist)
                }
            }
        }

        FloatingNavBar(
            tabs = tabs,
            selectedIndex = pagerState.currentPage,
            onSelect = { index -> scope.launch { pagerState.animateScrollToPage(index) } },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

/**
 * A flat, full-width tab bar — icon above a small always-visible label, a
 * translucent surface with a single hairline divider on top, and nothing
 * else: no floating dock, no morphing pill, no drag gesture, no stretch/
 * squash physics. Selection is shown with color and a small icon lift only
 * (tween, ~180ms) — closer to how iOS/most platforms treat a primary tab
 * bar, where the bar itself should be furniture, not a focal point.
 */
@Composable
private fun FloatingNavBar(
    tabs: List<MainTab>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.94f),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal),
                ),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(58.dp).padding(top = 6.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                tabs.forEachIndexed { index, tab ->
                    val onClick = remember(index) { { onSelect(index) } }
                    FloatingNavItem(
                        label = tab.label,
                        icon = tab.icon(),
                        selected = selectedIndex == index,
                        onClick = onClick,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun FloatingNavItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(180),
        label = "navItemContent",
    )
    // A small, single lift on selection rather than a spring/bounce — calm
    // motion, not a spectacle. No background pill, no width change.
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.08f else 1f,
        animationSpec = tween(180),
        label = "navItemIconScale",
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.graphicsLayer { scaleX = iconScale; scaleY = iconScale },
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = contentColor,
            maxLines = 1,
            fontSize = 11.sp,
        )
    }
}

private fun MainTab.icon(): ImageVector = when (this) {
    MainTab.HOME -> Icons.Filled.Home
    MainTab.GENERATE -> Icons.Filled.Add
    MainTab.PLAYLISTS -> Icons.AutoMirrored.Filled.QueueMusic
}
