package com.example.app.android.screens

import android.Manifest
import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.app.android.R
import com.example.app.android.services.LocationService
import com.example.app.android.theme.AppTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    onDismiss: () -> Unit,
    onLocationConfirmed: () -> Unit
) {
    val colors = AppTheme.colors
    val spacing = AppTheme.spacing
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val locationService = remember { LocationService.getInstance(context) }
    val userLocation by locationService.location.collectAsState()

    val initialLocation = LatLng(-23.5325, -46.7917)
    val markerState = remember { MarkerState(position = initialLocation) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialLocation, 18f)
    }

    // Distance text derived from user location + marker position
    var distanceText by remember { mutableStateOf<String?>(null) }

    val locationPermissions = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    LaunchedEffect(Unit) {
        if (!locationPermissions.allPermissionsGranted) {
            locationPermissions.launchMultiplePermissionRequest()
        }
    }

    // Fetch location once (uses cache if valid)
    LaunchedEffect(locationPermissions.allPermissionsGranted) {
        if (locationPermissions.allPermissionsGranted) {
            locationService.fetch()
        }
    }

    // Calculate distance when marker or user location changes
    LaunchedEffect(markerState.position, userLocation) {
        val loc = userLocation ?: return@LaunchedEffect
        val meters = LocationService.distanceBetween(
            loc.latitude, loc.longitude,
            markerState.position.latitude, markerState.position.longitude
        )
        distanceText = LocationService.formatDistance(meters)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // Fullscreen Google Map
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = locationPermissions.allPermissionsGranted
            ),
            uiSettings = MapUiSettings(
                zoomGesturesEnabled = true,
                scrollGesturesEnabled = true,
                rotationGesturesEnabled = true,
                tiltGesturesEnabled = true,
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false
            ),
            onMapClick = { latLng ->
                markerState.position = latLng
            }
        ) {
            Marker(
                state = markerState,
                title = stringResource(R.string.confirm_location)
            )
        }

        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(colors.background)
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.xxxl, vertical = spacing.xl),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(spacing.iconButton)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = colors.textPrimary,
                        modifier = Modifier.size(spacing.iconSmall)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.confirm_location),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary,
                        letterSpacing = 0.2.sp
                    )
                    Spacer(modifier = Modifier.height(spacing.xxs))
                    Text(
                        text = stringResource(R.string.move_map_to_select),
                        fontSize = 13.sp,
                        color = colors.textSecondary,
                        letterSpacing = 0.2.sp
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Box(modifier = Modifier.size(spacing.iconButton))
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(spacing.divider)
                    .background(colors.divider)
            )
        }

        // Floating pill below header
        Text(
            text = "Osasco, SP",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = colors.textPrimary,
            letterSpacing = 0.2.sp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(top = 76.dp)
                .background(
                    color = colors.surface,
                    shape = RoundedCornerShape(50)
                )
                .border(
                    width = spacing.divider,
                    color = colors.divider,
                    shape = RoundedCornerShape(50)
                )
                .padding(horizontal = spacing.xxl, vertical = spacing.md)
        )

        // FAB: center on user location
        FloatingActionButton(
            onClick = {
                locationService.fetch(forceRefresh = true)
                scope.launch {
                    val loc = userLocation
                    if (loc != null) {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(loc.latitude, loc.longitude),
                                18f
                            )
                        )
                    } else {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(markerState.position, 18f)
                        )
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = spacing.xxl)
                .padding(bottom = 220.dp),
            shape = CircleShape,
            containerColor = colors.surface,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.NearMe,
                contentDescription = stringResource(R.string.my_location),
                tint = colors.accent,
                modifier = Modifier.size(spacing.iconMedium)
            )
        }

        // Bottom overlay: distance pill + card
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = spacing.xxl)
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Distance pill
            if (distanceText != null) {
                Text(
                    text = distanceText!!,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textSecondary,
                    letterSpacing = 0.2.sp,
                    modifier = Modifier
                        .background(
                            color = colors.surface,
                            shape = RoundedCornerShape(50)
                        )
                        .border(
                            width = spacing.divider,
                            color = colors.divider,
                            shape = RoundedCornerShape(50)
                        )
                        .padding(horizontal = spacing.xxl, vertical = spacing.sm)
                )
                Spacer(modifier = Modifier.height(spacing.md))
            }

            // Card with confirm button
            Card(
                shape = RoundedCornerShape(spacing.xxl),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(spacing.xxxl),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.location_precision_title),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(spacing.sm))
                    Text(
                        text = stringResource(R.string.location_precision_hint),
                        fontSize = 13.sp,
                        color = colors.textSecondary,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(spacing.xxl))
                    Button(
                        onClick = onLocationConfirmed,
                        shape = RoundedCornerShape(spacing.lg),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.accent,
                            contentColor = colors.onAccent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.confirm_location),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
