// Copyright 2024 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.peal.googlemapjetpackcompose.presentation

import androidx.compose.runtime.Composable
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.compose.Marker
import com.peal.googlemapjetpackcompose.presentation.MountainsScreenViewState.MountainList
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.rememberMarkerState
import com.peal.googlemapjetpackcompose.data.local.Mountain
import com.peal.googlemapjetpackcompose.data.local.is14er
import com.peal.googlemapjetpackcompose.data.utils.toElevationString

/**
 * [GoogleMapComposable] which renders a [MountainList] as a set of basic [Marker]s
 */

@Composable
@GoogleMapComposable
fun BasicMarkersMapContent(
    mountains: List<Mountain>,
    onMountainClick: (Marker) -> Boolean = { false }
) {
    mountains.forEach { mountain ->
        Marker(
            state = rememberMarkerState(position = mountain.location),
            title = mountain.name,
            snippet = mountain.elevation.toElevationString(),
            tag = mountain,
            onClick = { marker ->
                onMountainClick(marker)
                false
            },
            zIndex = if (mountain.is14er()) 5f else 2f
        )
    }
}
