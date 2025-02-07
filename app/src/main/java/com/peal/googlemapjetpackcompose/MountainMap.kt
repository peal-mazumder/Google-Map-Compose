package com.peal.googlemapjetpackcompose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.widgets.ScaleBar
import com.google.maps.android.data.kml.KmlLayer
import com.peal.googlemapjetpackcompose.presentation.AdvancedMarkersMapContent
import com.peal.googlemapjetpackcompose.presentation.BasicMarkersMapContent
import com.peal.googlemapjetpackcompose.presentation.ClusteringMarkersMapContent
import com.peal.googlemapjetpackcompose.presentation.MountainsScreenEvent
import com.peal.googlemapjetpackcompose.presentation.MountainsScreenViewState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Shows a [GoogleMap] with collection of markers
 */
@OptIn(MapsComposeExperimentalApi::class)
@Composable
fun MountainMap(
    paddingValues: PaddingValues,
    viewState: MountainsScreenViewState.MountainList,
    eventFlow: Flow<MountainsScreenEvent>,
    selectedMarkerType: MarkerType,
) {
    var isMapLoaded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(viewState.boundingBox.center, 5f)
    }

    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = viewState.boundingBox) {
        zoomAll(scope, cameraPositionState, viewState.boundingBox)
    }

    LaunchedEffect(true) {
        eventFlow.collect { event ->
            when (event) {
                MountainsScreenEvent.OnZoomAll -> {
                    zoomAll(scope, cameraPositionState, viewState.boundingBox)
                }
            }
        }
    }

    val mapProperties by remember {
        mutableStateOf(
            MapProperties(
                mapType = MapType.NORMAL,
                mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, R.raw.style_json)
            )
        )
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            onMapLoaded = { isMapLoaded = true },
        ) {
            ChittagongRectangle()

            when (selectedMarkerType) {
                MarkerType.Basic -> {
                    BasicMarkersMapContent(
                        mountains = viewState.mountains,
                    )
                }

                MarkerType.Advanced -> {
                    AdvancedMarkersMapContent(
                        mountains = viewState.mountains,
                    )
                }

                MarkerType.Clustered -> {
                    ClusteringMarkersMapContent(
                        mountains = viewState.mountains,
                        onClusterClick = { cluster ->
                            val newZoom = cameraPositionState.position.zoom + 1
                            scope.launch {
                                cameraPositionState.animate(
                                    update = CameraUpdateFactory.newLatLngZoom(
                                        cluster.position, newZoom
                                    ),
                                    durationMs = 500,
                                )
                            }
                            false
                        },
                    )
                }
            }

            MapEffect(key1 = true) {map ->
                val layer = KmlLayer(map, R.raw.mountain_ranges, context)
                layer.addLayerToMap()
            }
        }

        ScaleBar(
            modifier = Modifier
                .padding(top = 5.dp, end = 15.dp)
                .align(Alignment.TopEnd),
            cameraPositionState = cameraPositionState
        )

        if (!isMapLoaded) {
            AnimatedVisibility(
                modifier = Modifier.matchParentSize(),
                visible = !isMapLoaded,
                enter = EnterTransition.None,
                exit = fadeOut()
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .wrapContentSize()
                )
            }
        }
    }
}

@Composable
@GoogleMapComposable
fun ChittagongRectangle() {
    val north = 23.4700
    val south = 22.7676
    val east = 92.3468
    val west = 91.7259

    val locations = listOf(
        LatLng(north, east),
        LatLng(south, east),
        LatLng(south, west),
        LatLng(north, west),
        LatLng(north, east)
    )

    Polygon(
        points = locations,
        strokeColor = MaterialTheme.colorScheme.tertiary,
        strokeWidth = 3F,
        fillColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
    )
}




fun zoomAll(
    scope: CoroutineScope,
    cameraPositionState: CameraPositionState,
    boundingBox: LatLngBounds
) {
    scope.launch {
        cameraPositionState.animate(
            update = CameraUpdateFactory.newLatLngBounds(boundingBox, 64),
            durationMs = 1000
        )
    }
}
