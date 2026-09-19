package com.github.speak2me.compose.map.amap.clustering.android.collections;

import android.view.View;

import androidx.annotation.NonNull;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;

/**
 * Keeps track of collections of markers on the map. Delegates all Marker-related events to each
 * collection's individually managed listeners.
 *
 * <p>All marker operations (adds and removes) should occur via its collection class. That is, don't
 * add a marker via a collection, then remove it via Marker.remove()
 */
public class MarkerManager extends MapObjectManager<Marker, MarkerManager.Collection>
        implements AMap.OnInfoWindowClickListener,
        AMap.OnMarkerClickListener,
        AMap.OnMarkerDragListener,
        AMap.InfoWindowAdapter {

    public MarkerManager(AMap map) {
        super(map);
    }

    @Override
    void setListenersOnUiThread() {
        if (mMap != null) {
            mMap.setOnInfoWindowClickListener(this);
            mMap.setOnMarkerClickListener(this);
            mMap.setOnMarkerDragListener(this);
            mMap.setInfoWindowAdapter(this);
        }
    }

    public Collection newCollection() {
        return new Collection();
    }

    @Override
    public View getInfoWindow(@NonNull Marker marker) {
        Collection collection = mAllObjects.get(marker);
        if (collection != null && collection.mInfoWindowAdapter != null) {
            return collection.mInfoWindowAdapter.getInfoWindow(marker);
        }
        return null;
    }

    @Override
    public View getInfoContents(@NonNull Marker marker) {
        Collection collection = mAllObjects.get(marker);
        if (collection != null && collection.mInfoWindowAdapter != null) {
            return collection.mInfoWindowAdapter.getInfoContents(marker);
        }
        return null;
    }

    @Override
    public void onInfoWindowClick(@NonNull Marker marker) {
        Collection collection = mAllObjects.get(marker);
        if (collection != null && collection.mInfoWindowClickListener != null) {
            collection.mInfoWindowClickListener.onInfoWindowClick(marker);
        }
    }

//    @Override
//    public void onInfoWindowLongClick(@NonNull Marker marker) {
//        Collection collection = mAllObjects.get(marker);
//        if (collection != null && collection.mInfoWindowLongClickListener != null) {
//            collection.mInfoWindowLongClickListener.onInfoWindowLongClick(marker);
//        }
//    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        Collection collection = mAllObjects.get(marker);
        if (collection != null && collection.mMarkerClickListener != null) {
            return collection.mMarkerClickListener.onMarkerClick(marker);
        }
        return false;
    }

    @Override
    public void onMarkerDragStart(@NonNull Marker marker) {
        Collection collection = mAllObjects.get(marker);
        if (collection != null && collection.mMarkerDragListener != null) {
            collection.mMarkerDragListener.onMarkerDragStart(marker);
        }
    }

    @Override
    public void onMarkerDrag(@NonNull Marker marker) {
        Collection collection = mAllObjects.get(marker);
        if (collection != null && collection.mMarkerDragListener != null) {
            collection.mMarkerDragListener.onMarkerDrag(marker);
        }
    }

    @Override
    public void onMarkerDragEnd(@NonNull Marker marker) {
        Collection collection = mAllObjects.get(marker);
        if (collection != null && collection.mMarkerDragListener != null) {
            collection.mMarkerDragListener.onMarkerDragEnd(marker);
        }
    }

    @Override
    protected void removeObjectFromMap(Marker object) {
        object.remove();
    }

    public class Collection extends MapObjectManager.Collection {
        private AMap.OnInfoWindowClickListener mInfoWindowClickListener;
        private AMap.OnMarkerClickListener mMarkerClickListener;
        private AMap.OnMarkerDragListener mMarkerDragListener;
        private AMap.InfoWindowAdapter mInfoWindowAdapter;

        public Collection() {}

        public Marker addMarker(MarkerOptions opts) {
            Marker marker = mMap.addMarker(opts);
            super.add(marker);
            return marker;
        }

//        public Marker addMarker(AdvancedMarkerOptions opts) {
//            Marker marker = mMap.addMarker(opts);
//            super.add(marker);
//            return marker;
//        }

        public void addAll(java.util.Collection<MarkerOptions> opts) {
            for (MarkerOptions opt : opts) {
                addMarker(opt);
            }
        }

        public void addAll(java.util.Collection<MarkerOptions> opts, boolean defaultVisible) {
            for (MarkerOptions opt : opts) {
                addMarker(opt).setVisible(defaultVisible);
            }
        }

        public void showAll() {
            for (Marker marker : getMarkers()) {
                marker.setVisible(true);
            }
        }

        public void hideAll() {
            for (Marker marker : getMarkers()) {
                marker.setVisible(false);
            }
        }

        public boolean remove(Marker marker) {
            return super.remove(marker);
        }

        public java.util.Collection<Marker> getMarkers() {
            return getObjects();
        }

        public void setOnInfoWindowClickListener(
                AMap.OnInfoWindowClickListener infoWindowClickListener) {
            mInfoWindowClickListener = infoWindowClickListener;
        }

//        public void setOnInfoWindowLongClickListener(
//                AMap.OnInfoWindowLongClickListener infoWindowLongClickListener) {
//            mInfoWindowLongClickListener = infoWindowLongClickListener;
//        }

        public void setOnMarkerClickListener(AMap.OnMarkerClickListener markerClickListener) {
            mMarkerClickListener = markerClickListener;
        }

        public void setOnMarkerDragListener(AMap.OnMarkerDragListener markerDragListener) {
            mMarkerDragListener = markerDragListener;
        }

        public void setInfoWindowAdapter(AMap.InfoWindowAdapter infoWindowAdapter) {
            mInfoWindowAdapter = infoWindowAdapter;
        }
    }
}
