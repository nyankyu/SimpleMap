
package jp.gr.java_conf.kobitokaba.simple_map;

import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;

import java.util.ArrayList;
import java.util.List;

class PinItemizedOverlay extends ItemizedOverlay<PinOverlayItem> {
    private List<GeoPoint> mPointList;

    public PinItemizedOverlay(Drawable defaultMarker) {
        super(boundCenterBottom(defaultMarker));
        mPointList = new ArrayList<GeoPoint>();
    }

    @Override
    protected PinOverlayItem createItem(int i) {
        GeoPoint point = mPointList.get(i);
        return new PinOverlayItem(point);
    }

    @Override
    public int size() {
        return mPointList.size();
    }

    public void addPoint(GeoPoint point) {
        mPointList.add(point);
        populate();
    }
}
