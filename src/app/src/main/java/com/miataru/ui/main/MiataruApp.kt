package com.miataru.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.miataru.ui.devices.DeviceEditorScreen
import com.miataru.ui.devices.DeviceMapScreen
import com.miataru.ui.devices.DevicesScreen
import com.miataru.ui.groups.GroupMapScreen
import com.miataru.ui.groups.GroupsScreen
import com.miataru.ui.onboarding.OnboardingScreen
import com.miataru.ui.settings.SettingsScreen
import com.miataru.ui.visitors.VisitorHistoryScreen

private data class TopDestination(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)

private val TOP_DESTINATIONS = listOf(
    TopDestination(AppRoutes.DEVICES, "Devices", Icons.Rounded.Devices),
    TopDestination(AppRoutes.GROUPS, "Groups", Icons.Rounded.Groups),
    TopDestination(AppRoutes.SETTINGS, "Settings", Icons.Rounded.Settings),
)

@Composable
fun MiataruApp(
    mainViewModel: MainViewModel = hiltViewModel(),
) {
    val settings by mainViewModel.settings.collectAsStateWithLifecycle()
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val startDestination = if (settings.onboardingCompleted) {
        AppRoutes.DEVICES
    } else {
        AppRoutes.ONBOARDING
    }

    LaunchedEffect(Unit) {
        mainViewModel.navigationEvents.collect { event ->
            when (event) {
                is NavigationEvent.OpenAddDevice -> {
                    navController.navigate(AppRoutes.addDevice(event.prefilledDeviceId))
                }

                is NavigationEvent.OpenDeviceMap -> {
                    navController.navigate(AppRoutes.deviceMap(event.deviceId))
                }
            }
        }
    }

    val showBottomBar = currentRoute in TOP_DESTINATIONS.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    TOP_DESTINATIONS.forEach { destination ->
                        val selected = currentRoute == destination.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(destination.route) {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                }
                            },
                            label = { Text(destination.label) },
                            icon = {
                                Icon(destination.icon, contentDescription = destination.label)
                            },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(AppRoutes.ONBOARDING) {
                OnboardingScreen(
                    onCompleted = {
                        navController.navigate(AppRoutes.DEVICES) {
                            popUpTo(AppRoutes.ONBOARDING) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(AppRoutes.DEVICES) {
                DevicesScreen(
                    onAddDevice = { navController.navigate(AppRoutes.addDevice()) },
                    onEditDevice = { deviceLocalId ->
                        navController.navigate(AppRoutes.editDevice(deviceLocalId))
                    },
                    onOpenMap = { deviceId ->
                        navController.navigate(AppRoutes.deviceMap(deviceId))
                    },
                    onOpenVisitors = { navController.navigate(AppRoutes.VISITORS) },
                )
            }

            composable(
                route = AppRoutes.ADD_DEVICE_WITH_PREFILL,
                arguments = listOf(
                    navArgument(AppRoutes.ARG_PREFILL_DEVICE_ID) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                ),
            ) { entry ->
                DeviceEditorScreen(
                    deviceLocalId = null,
                    prefilledDeviceId = entry.arguments?.getString(AppRoutes.ARG_PREFILL_DEVICE_ID),
                    onBack = { navController.popBackStack() },
                )
            }

            composable(
                route = AppRoutes.EDIT_DEVICE,
                arguments = listOf(
                    navArgument(AppRoutes.ARG_DEVICE_LOCAL_ID) {
                        type = NavType.LongType
                    }
                ),
            ) { entry ->
                DeviceEditorScreen(
                    deviceLocalId = entry.arguments?.getLong(AppRoutes.ARG_DEVICE_LOCAL_ID),
                    prefilledDeviceId = null,
                    onBack = { navController.popBackStack() },
                )
            }

            composable(
                route = AppRoutes.DEVICE_MAP,
                arguments = listOf(
                    navArgument(AppRoutes.ARG_DEVICE_ID) {
                        type = NavType.StringType
                    }
                ),
            ) {
                DeviceMapScreen(onBack = { navController.popBackStack() })
            }

            composable(AppRoutes.GROUPS) {
                GroupsScreen(
                    onOpenGroupMap = { groupId ->
                        navController.navigate(AppRoutes.groupMap(groupId))
                    },
                )
            }

            composable(
                route = AppRoutes.GROUP_MAP,
                arguments = listOf(
                    navArgument(AppRoutes.ARG_GROUP_ID) {
                        type = NavType.LongType
                    }
                ),
            ) {
                GroupMapScreen(onBack = { navController.popBackStack() })
            }

            composable(AppRoutes.SETTINGS) {
                SettingsScreen(
                    onRerunOnboarding = {
                        navController.navigate(AppRoutes.ONBOARDING) {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(AppRoutes.VISITORS) {
                VisitorHistoryScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
