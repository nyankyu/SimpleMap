
package jp.gr.java_conf.kobitokaba.simple_map;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

class PinOverlayItem extends OverlayItem {
    public PinOverlayItem(GeoPoint point) {
        super(point, "", "");
    }

}
