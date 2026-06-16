package com.nino161er.rssfeed.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nino161er.rssfeed.R
import com.nino161er.rssfeed.ui.RssViewModel
import androidx.compose.material.icons.filled.Menu
import com.nino161er.rssfeed.ui.screens.AiChatScreen
import com.nino161er.rssfeed.ui.screens.FeedItemsScreen
import com.nino161er.rssfeed.ui.screens.FeedsScreen
import com.nino161er.rssfeed.ui.screens.ItemDetailScreen
import com.nino161er.rssfeed.ui.screens.ItemsScreen
import com.nino161er.rssfeed.ui.screens.ManageFeedsScreen
import com.nino161er.rssfeed.ui.screens.NewsScreen
import com.nino161er.rssfeed.ui.screens.OnboardingScreen
import com.nino161er.rssfeed.ui.screens.RadioScreen
import com.nino161er.rssfeed.ui.screens.SettingsScreen
import com.nino161er.rssfeed.ui.screens.StarredItemsScreen
import com.nino161er.rssfeed.ui.screens.UnreadItemsScreen

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
    viewModel: RssViewModel,
    windowWidthSizeClass: WindowWidthSizeClass
) {
    val isOnboardingComplete by viewModel.onboardingComplete.collectAsState()
    var selectedTab by remember { mutableStateOf<Tab>(Tab.Feeds) }
    var selectedRadioSubTab by remember { mutableStateOf<Tab>(Tab.RadioTuner) }
    var selectedNewsSubTab by remember { mutableStateOf<Tab>(Tab.NewsArticles) }
    
    var screenStack by remember {
        mutableStateOf<List<Screen>>(
            if (isOnboardingComplete) listOf(Screen.FeedsList) else listOf(Screen.Onboarding)
        )
    }

    // Update screenStack if onboarding completes
    LaunchedEffect(isOnboardingComplete) {
        if (isOnboardingComplete && screenStack.contains(Screen.Onboarding)) {
            screenStack = listOf(Screen.FeedsList)
        }
    }
    
    val isNewsBrowserActive by viewModel.isNewsBrowserActive.collectAsState()

    val currentScreen = screenStack.last()
    val isOnDetail = screenStack.size > 1
    val isSettings = currentScreen is Screen.Settings
    val hideBottomNav = (isOnDetail && !isSettings) || currentScreen is Screen.Onboarding || isNewsBrowserActive
    val showDrawer = !hideBottomNav

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    BackHandler(enabled = screenStack.size > 1 || drawerState.isOpen || isNewsBrowserActive) {
        when {
            drawerState.isOpen -> scope.launch { drawerState.close() }
            isNewsBrowserActive -> viewModel.setNewsBrowserActive(false)
            else -> screenStack = screenStack.dropLast(1)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = showDrawer,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp)
            ) {
                Spacer(Modifier.height(48.dp))
                Text(
                    text = "Lura",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp),
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Cursive,
                    color = MaterialTheme.colorScheme.onSurface
                )
                HorizontalDivider(Modifier.padding(horizontal = 28.dp, vertical = 8.dp))
                
                Tab.drawerItems.forEach { item ->
                    val isSelected = (selectedTab == item && !isSettings)
                    NavigationDrawerItem(
                        label = { Text(text = stringResource(item.labelResId)) },
                        selected = isSelected,
                        onClick = {
                            scope.launch { drawerState.close() }
                            selectedTab = item
                            val newScreen = when (item) {
                                Tab.Feeds -> Screen.FeedsList
                                Tab.Radio -> Screen.Radio
                                Tab.News -> Screen.News
                                else -> Screen.FeedsList
                            }
                            screenStack = listOf(newScreen)
                        },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
                
                Spacer(Modifier.weight(1f))
                
                NavigationDrawerItem(
                    label = { Text(text = stringResource(R.string.nav_content_desc_settings)) },
                    selected = isSettings,
                    onClick = {
                        scope.launch { drawerState.close() }
                        // Always open general App Settings from sidebar
                        if (currentScreen !is Screen.Settings) {
                            screenStack = screenStack + Screen.Settings
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    ) {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
        ) {
            Scaffold(
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                containerColor = Color.Transparent
            ) { paddingValues -> 
                Box(modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding())
                ) {
                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = {
                            val initialDepth = initialState.getDepth()
                            val targetDepth = targetState.getDepth()

                            if (initialDepth == 0 && targetDepth == 0) {
                                // Tab transition (depth 0 -> 0)
                                val initialIndex = initialState.getTabIndex() ?: 0
                                val targetIndex = targetState.getTabIndex() ?: 0
                                if (targetIndex > initialIndex) {
                                    // Slide left (new tab enters from right)
                                    (slideInHorizontally(
                                        initialOffsetX = { it },
                                        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
                                    ) + fadeIn(animationSpec = tween(250))).togetherWith(
                                        slideOutHorizontally(
                                            targetOffsetX = { -it },
                                            animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
                                        ) + fadeOut(animationSpec = tween(200))
                                    )
                                } else {
                                    // Slide right (new tab enters from left)
                                    (slideInHorizontally(
                                        initialOffsetX = { -it },
                                        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
                                    ) + fadeIn(animationSpec = tween(250))).togetherWith(
                                        slideOutHorizontally(
                                            targetOffsetX = { it },
                                            animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
                                        ) + fadeOut(animationSpec = tween(200))
                                    )
                                }
                            } else if (targetDepth > initialDepth) {
                                // Forward transition (Push / depth increases)
                                // Entering screen slides in from right with a bouncy spring
                                // Exiting screen slides out slightly to the left
                                (slideInHorizontally(
                                    initialOffsetX = { it },
                                    animationSpec = spring(dampingRatio = 0.82f, stiffness = 350f)
                                ) + fadeIn(animationSpec = tween(300))).togetherWith(
                                    slideOutHorizontally(
                                        targetOffsetX = { -it / 3 },
                                        animationSpec = spring(dampingRatio = 1.0f, stiffness = 350f)
                                    ) + fadeOut(animationSpec = tween(200))
                                )
                            } else if (targetDepth < initialDepth) {
                                // Backward transition (Pop / depth decreases)
                                // Entering screen slides in slightly from the left
                                // Exiting screen slides out to the right with a bouncy spring
                                (slideInHorizontally(
                                    initialOffsetX = { -it / 3 },
                                    animationSpec = spring(dampingRatio = 1.0f, stiffness = 350f)
                                ) + fadeIn(animationSpec = tween(250))).togetherWith(
                                    slideOutHorizontally(
                                        targetOffsetX = { it },
                                        animationSpec = spring(dampingRatio = 0.82f, stiffness = 350f)
                                    ) + fadeOut(animationSpec = tween(250))
                                )
                            } else {
                                // Same depth (non-zero) fallback: Fade through with scaling
                                (fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.95f, animationSpec = tween(300))).togetherWith(
                                    fadeOut(animationSpec = tween(150)) + scaleOut(targetScale = 0.95f, animationSpec = tween(150))
                                )
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                        label = "screen_content"
                    ) { screen ->
                        ScreenContent(
                            screen = screen,
                            rssViewModel = viewModel,
                            selectedTab = selectedTab,
                            selectedRadioSubTab = selectedRadioSubTab,
                            selectedNewsSubTab = selectedNewsSubTab,
                            onNavigate = { screenStack = screenStack + it },
                            onBack = { if (screenStack.size > 1) screenStack = screenStack.dropLast(1) },
                            onOpenDrawer = { scope.launch { drawerState.open() } },
                            onOnboardingComplete = { screenStack = listOf(Screen.FeedsList) }
                        )
                    }
                }
            }

                if (!hideBottomNav) {
                val bottomNavTabs = when (selectedTab) {
                    Tab.Radio -> Tab.radioTabs
                    Tab.News -> Tab.newsTabs
                    else -> Tab.bottomNavItems
                }

                val currentSelectedSubTab = when (selectedTab) {
                    Tab.Radio -> selectedRadioSubTab
                    Tab.News -> selectedNewsSubTab
                    else -> selectedTab
                }

                CustomExpressiveNavigationBar(
                    selectedTab = currentSelectedSubTab,
                    currentScreen = currentScreen,
                    tabs = bottomNavTabs,
                    onTabSelected = { tab ->
                        if (Tab.bottomNavItems.contains(tab)) {
                            selectedTab = tab
                            val newScreen = when(tab) {
                                Tab.Feeds -> Screen.FeedsList
                                Tab.Saved -> Screen.StarredItems
                                Tab.Items -> Screen.ItemsList
                                else -> Screen.FeedsList
                            }
                            screenStack = listOf(newScreen)
                        } else if (Tab.radioTabs.contains(tab)) {
                            selectedRadioSubTab = tab
                            selectedTab = Tab.Radio
                            screenStack = listOf(Screen.Radio)
                        } else if (Tab.newsTabs.contains(tab)) {
                            selectedNewsSubTab = tab
                            selectedTab = Tab.News
                            screenStack = listOf(Screen.News)
                        }
                    },
                    onSettingsClick = { 
                        if (currentScreen !is Screen.Settings) {
                            screenStack = screenStack + Screen.Settings
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun BoxScope.CustomExpressiveNavigationBar(
    selectedTab: Tab,
    currentScreen: Screen,
    tabs: List<Tab>,
    onTabSelected: (Tab) -> Unit,
    onSettingsClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .navigationBarsPadding(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // ... (background brush)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        .background(
            brush = Brush.verticalGradient(
                0.0f to Color.Transparent,
                0.3f to MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                0.6f to MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                0.9f to MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                1.0f to MaterialTheme.colorScheme.surface
            )
        )
                .blur(40.dp)
        )

        Row(
            modifier = Modifier
                .padding(bottom = 24.dp, start = 16.dp, end = 16.dp)
                .wrapContentWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                tonalElevation = 8.dp,
                shadowElevation = 12.dp
            ) {
                Row(
                    modifier = Modifier.padding(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    tabs.forEach { tab ->
                        val isSelected = selectedTab == tab && currentScreen !is Screen.Settings
                        // ... (rest of the tab rendering logic)
                        
                        val animatedPadding by animateDpAsState(
                            targetValue = if (isSelected) 22.dp else 12.dp,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "tab_padding"
                        )
                        
                        val iconTint by animateColorAsState(
                            targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary,
                            animationSpec = spring(stiffness = Spring.StiffnessLow),
                            label = "icon_tint"
                        )

                        Surface(
                            onClick = { onTabSelected(tab) },
                            shape = CircleShape,
                            color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                            modifier = Modifier.height(52.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = animatedPadding),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = stringResource(tab.labelResId),
                                    tint = iconTint,
                                    modifier = Modifier.size(24.dp)
                                )
                                AnimatedContent(
                                    targetState = isSelected,
                                    label = "tab_label_anim"
                                ) { selected ->
                                    if (selected) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = stringResource(tab.labelResId),
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                                                letterSpacing = 0.2.sp,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            val isSettingsSelected = currentScreen is Screen.Settings
            val settingsBg by animateColorAsState(
                targetValue = if (isSettingsSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                label = "settings_bg"
            )
            val settingsIconTint by animateColorAsState(
                targetValue = if (isSettingsSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
                label = "settings_tint"
            )

            Surface(
                onClick = onSettingsClick,
                shape = RoundedCornerShape(24.dp),
                color = settingsBg,
                tonalElevation = 8.dp,
                shadowElevation = 12.dp,
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.nav_content_desc_settings),
                        modifier = Modifier.size(28.dp),
                        tint = settingsIconTint
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScreenContent(
    screen: Screen,
    rssViewModel: RssViewModel,
    selectedTab: Tab,
    selectedRadioSubTab: Tab,
    selectedNewsSubTab: Tab,
    onNavigate: (Screen) -> Unit,
    onBack: () -> Unit,
    onOpenDrawer: () -> Unit,
    onOnboardingComplete: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        when (screen) {
            is Screen.FeedsList -> FeedsScreen(
                viewModel = rssViewModel,
                onNavigateToFeedItems = { feedId, title ->
                    onNavigate(Screen.FeedItems(feedId, title))
                },
                onNavigateToUnread = {
                    onNavigate(Screen.UnreadItems)
                },
                onOpenDrawer = onOpenDrawer
            )
            is Screen.ItemsList -> ItemsScreen(
                viewModel = rssViewModel,
                onOpenDrawer = onOpenDrawer,
                onNavigateToDetail = { itemId, title, description, content, imageUrl ->
                    onNavigate(Screen.ItemDetail(itemId, title, description, content, imageUrl))
                }
            )
            is Screen.FeedItems -> FeedItemsScreen(
                viewModel = rssViewModel,
                feedId = screen.feedId,
                feedTitle = screen.title,
                onNavigateToDetail = { itemId, title, description, content, imageUrl ->
                    onNavigate(Screen.ItemDetail(itemId, title, description, content, imageUrl))
                },
                onBack = onBack
            )
            is Screen.UnreadItems -> UnreadItemsScreen(
                viewModel = rssViewModel,
                onNavigateToDetail = { itemId, title, description, content, imageUrl ->
                    onNavigate(Screen.ItemDetail(itemId, title, description, content, imageUrl))
                },
                onBack = onBack
            )
            is Screen.ItemDetail -> ItemDetailScreen(
                itemId = screen.itemId,
                title = screen.title,
                description = screen.description,
                content = screen.content,
                imageUrl = screen.imageUrl,
                viewModel = rssViewModel,
                onBack = onBack
            )
            is Screen.StarredItems -> StarredItemsScreen(
                viewModel = rssViewModel,
                onNavigateToDetail = { itemId, title, description, content, imageUrl ->
                    onNavigate(Screen.ItemDetail(itemId, title, description, content, imageUrl))
                },
                onOpenDrawer = onOpenDrawer,
                onBack = onBack
            )
            is Screen.ManageFeeds -> ManageFeedsScreen(
                viewModel = rssViewModel,
                onBack = onBack
            )
            is Screen.Onboarding -> OnboardingScreen(
                viewModel = rssViewModel,
                onComplete = onOnboardingComplete
            )
            is Screen.Settings -> SettingsScreen(
                viewModel = rssViewModel,
                onNavigateToManageFeeds = {
                    onNavigate(Screen.ManageFeeds)
                },
                onBack = onBack,
                settingsContext = when (selectedTab) {
                    Tab.Feeds, Tab.Saved, Tab.Items -> "rss"
                    Tab.Radio, Tab.RadioSaved, Tab.RadioTuner, Tab.RadioMap -> "radio"
                    Tab.News, Tab.NewsFollowed, Tab.NewsSuggested, Tab.NewsArticles -> "news"
                    else -> "general"
                }
            )
            is Screen.Radio -> {
                RadioScreen(
                    viewModel = rssViewModel,
                    onOpenDrawer = onOpenDrawer,
                    selectedSubTab = selectedRadioSubTab
                )
            }
            is Screen.News -> {
                NewsScreen(
                    viewModel = rssViewModel,
                    selectedSubTab = selectedNewsSubTab,
                    onNavigateToFeedItems = { feedId, title ->
                        onNavigate(Screen.FeedItems(feedId, title))
                    },
                    onOpenDrawer = onOpenDrawer
                )
            }
        }
    }
}

private fun Screen.getDepth(): Int = when (this) {
    Screen.FeedsList -> 0
    Screen.StarredItems -> 0
    Screen.ItemsList -> 0
    Screen.Radio -> 0
    Screen.News -> 0
    is Screen.FeedItems -> 1
    Screen.UnreadItems -> 1
    Screen.Settings -> 1
    Screen.ManageFeeds -> 2
    is Screen.ItemDetail -> 2
    Screen.Onboarding -> -1
}

private fun Screen.getTabIndex(): Int? = when (this) {
    Screen.FeedsList -> 0
    Screen.StarredItems -> 1
    Screen.ItemsList -> 2
    Screen.Radio -> 3
    Screen.News -> 4
    else -> null
}
