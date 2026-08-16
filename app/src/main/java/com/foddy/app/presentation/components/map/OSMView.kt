package com.foddy.app.presentation.components.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.foddy.app.domain.model.Location
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import android.graphics.Color as AndroidColor

@Composable
fun OSMView(
    modifier: Modifier = Modifier,
    userLocation: Location? = null,
    driverLocation: Location? = null,
    zoomLevel: Double = 15.0,
    onMapInitialized: (MapView) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapView = remember { MapView(context) }
    
    // Lifecycle management for MapView to save resources
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    // Remember markers and polyline to avoid creating them every recomposition
    val userMarker = remember { Marker(mapView).apply { 
        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        title = "Vị trí của bạn"
    } }
    val driverMarker = remember { Marker(mapView).apply { 
        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        title = "Tài xế đang đến"
    } }
    val routeLine = remember { Polyline().apply { 
        outlinePaint.color = AndroidColor.parseColor("#FF6B00")
        outlinePaint.strokeWidth = 10f
    } }

    // Removed the redundant DisposableEffect(mapView) since it's now handled by lifecycleOwner

    AndroidView(
        factory = {
            mapView.apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                zoomController.setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER)
                controller.setZoom(zoomLevel)
                
                val startPoint = if (userLocation != null) {
                    GeoPoint(userLocation.latitude, userLocation.longitude)
                } else {
                    GeoPoint(21.0285, 105.8542) 
                }
                controller.setCenter(startPoint)
                
                onMapInitialized(this)
            }
        },
        modifier = modifier,
        update = { map ->
            // Instead of clearing all, we update specific markers
            if (userLocation != null) {
                userMarker.position = GeoPoint(userLocation.latitude, userLocation.longitude)
                if (!map.overlays.contains(userMarker)) map.overlays.add(userMarker)
            } else {
                map.overlays.remove(userMarker)
            }

            if (driverLocation != null) {
                driverMarker.position = GeoPoint(driverLocation.latitude, driverLocation.longitude)
                if (!map.overlays.contains(driverMarker)) map.overlays.add(driverMarker)
            } else {
                map.overlays.remove(driverMarker)
            }

            if (userLocation != null && driverLocation != null) {
                routeLine.setPoints(listOf(
                    GeoPoint(userLocation.latitude, userLocation.longitude),
                    GeoPoint(driverLocation.latitude, driverLocation.longitude)
                ))
                if (!map.overlays.contains(routeLine)) map.overlays.add(routeLine)
            } else {
                map.overlays.remove(routeLine)
            }

            map.invalidate()
        }
    )
}
