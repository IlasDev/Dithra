package dev.ilas.dithra.presentation.ui

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import dev.ilas.dithra.R
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import dev.ilas.dithra.data.model.ThemeMode
import dev.ilas.dithra.presentation.ui.screen.Material3ExpressiveScreen
import dev.ilas.dithra.presentation.ui.screen.SettingsScreen
import dev.ilas.dithra.presentation.ui.theme.BitmapTheme
import dev.ilas.dithra.presentation.viewmodel.MainViewModel

/**
 * Main activity hosting the bitmap processing application.
 */
class MainActivity : ComponentActivity() {

    private val viewModelFactory = object : ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(
            modelClass: Class<T>,
            extras: CreationExtras
        ): T {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(
                context = this@MainActivity,
                savedStateHandle = extras.createSavedStateHandle()
            ) as T
        }
    }

    private val viewModel: MainViewModel by viewModels { viewModelFactory }

    @SuppressLint("ConfigurationScreenWidthHeight")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appSettings by viewModel.appSettings.collectAsState()
            val showSettingsScreen by viewModel.showSettingsScreen.collectAsState()
            
            BitmapTheme(settings = appSettings) {
                SideEffect {
                    val isDark = when (appSettings.themeMode) {
                        ThemeMode.SYSTEM -> {
                            resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
                        }
                        ThemeMode.LIGHT -> false
                        ThemeMode.DARK -> true
                    }
                    
                    enableEdgeToEdge(
                        statusBarStyle = if (isDark) {
                            SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                        } else {
                            SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                        }
                    )
                }
                
                val configuration = LocalConfiguration.current
                val density = LocalDensity.current
                val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
                
                val screenHeightDp = with(density) { configuration.screenHeightDp }
                
                val showToolbar = showSettingsScreen || (!isLandscape || screenHeightDp > 600)
                
                val settingsScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
                
                var isTransitioning by remember { mutableStateOf(false) }

                BackHandler(enabled = showSettingsScreen && !isTransitioning) {
                    viewModel.hideSettingsScreen()
                }
                
                LaunchedEffect(showSettingsScreen) {
                    if (isTransitioning) {
                        kotlinx.coroutines.delay(400)
                        isTransitioning = false
                    }
                }
                
                Scaffold(
                    topBar = {
                        if (showToolbar) {
                            AnimatedContent(
                                targetState = showSettingsScreen,
                                transitionSpec = {
                                    isTransitioning = true
                                    if (targetState) {
                                        (slideInHorizontally(
                                            animationSpec = tween(350, delayMillis = 0),
                                            initialOffsetX = { it }
                                        ) + fadeIn(
                                            animationSpec = tween(250, delayMillis = 50)
                                        )).togetherWith(
                                            slideOutHorizontally(
                                                animationSpec = tween(250),
                                                targetOffsetX = { -it }
                                            ) + fadeOut(animationSpec = tween(200))
                                        )
                                    } else {
                                        (slideInHorizontally(
                                            animationSpec = tween(350, delayMillis = 0),
                                            initialOffsetX = { -it }
                                        ) + fadeIn(
                                            animationSpec = tween(250, delayMillis = 50)
                                        )).togetherWith(
                                            slideOutHorizontally(
                                                animationSpec = tween(250),
                                                targetOffsetX = { it }
                                            ) + fadeOut(animationSpec = tween(200))
                                        )
                                    }
                                },
                                label = "ToolbarTransition"
                            ) { isSettingsScreen ->
                                if (isSettingsScreen) {
                                    TopAppBar(
                                        title = {
                                            Text(
                                                text = getString(R.string.settings),
                                                fontWeight = FontWeight.Medium,
                                                style = MaterialTheme.typography.titleLarge
                                            )
                                        },
                                        navigationIcon = {
                                            IconButton(
                                                onClick = { 
                                                    if (!isTransitioning) {
                                                        viewModel.hideSettingsScreen()
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                                    contentDescription = getString(R.string.back)
                                                )
                                            }
                                        },
                                        scrollBehavior = settingsScrollBehavior
                                    )
                                } else {
                                    CenterAlignedTopAppBar(
                                        colors = TopAppBarDefaults.topAppBarColors(
                                            Color.Unspecified,
                                            Color.Unspecified,
                                            Color.Unspecified,
                                            titleContentColor = MaterialTheme.colorScheme.onSurface,
                                            actionIconContentColor = MaterialTheme.colorScheme.onSurface
                                        ),
                                        title = { 
                                            Text(
                                                text = "Dithra", 
                                                fontWeight = FontWeight.Medium,
                                                style = MaterialTheme.typography.titleLarge
                                            ) 
                                        },
                                        actions = {
                                            IconButton(
                                                onClick = { 
                                                    if (!isTransitioning) {
                                                        viewModel.showSettingsScreen()
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Settings,
                                                    contentDescription = getString(R.string.settings)
                                                )
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    AnimatedContent(
                        targetState = showSettingsScreen,
                        transitionSpec = {
                            if (targetState) {
                                (slideInHorizontally(
                                    animationSpec = tween(350, delayMillis = 0),
                                    initialOffsetX = { it }
                                ) + fadeIn(
                                    animationSpec = tween(250, delayMillis = 50)
                                )).togetherWith(
                                    slideOutHorizontally(
                                        animationSpec = tween(250),
                                        targetOffsetX = { -it / 2 }
                                    ) + fadeOut(animationSpec = tween(200))
                                )
                            } else {
                                (slideInHorizontally(
                                    animationSpec = tween(350, delayMillis = 0),
                                    initialOffsetX = { -it / 2 }
                                ) + fadeIn(
                                    animationSpec = tween(250, delayMillis = 50)
                                )).togetherWith(
                                    slideOutHorizontally(
                                        animationSpec = tween(250),
                                        targetOffsetX = { it }
                                    ) + fadeOut(animationSpec = tween(200))
                                )
                            }
                        },
                        label = "ScreenTransition"
                    ) { isSettingsScreen ->
                        if (isSettingsScreen) {
                            SettingsScreen(
                                settings = appSettings,
                                onThemeModeChanged = { themeMode ->
                                    viewModel.updateThemeMode(themeMode)
                                },
                                onDynamicColorsChanged = { enabled ->
                                    viewModel.updateDynamicColors(enabled)
                                },
                                onTransparencyModeChanged = { transparencyMode ->
                                    viewModel.updateTransparencyMode(transparencyMode)
                                },
                                scrollBehavior = settingsScrollBehavior,
                                modifier = Modifier.padding(innerPadding)
                            )
                        } else {
                            Material3ExpressiveScreen(
                                viewModel = viewModel,
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
    }
}