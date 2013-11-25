
package jp.gr.java_conf.kobitokaba.simple_map;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;

import java.util.List;

/**
 * メインActivity
 */
public class MapViewActivity extends MapActivity {
    /**
     * {@link #changeLayerVisibility(int)} 住所検索モード
     */
    private static final int MODE_SEARCH = 0x01;
    /**
     * {@link #changeLayerVisibility(int)} 地図表示モード
     */
    private static final int MODE_MAP = 0x02;

    /**
     * 地図表示レイヤー
     */
    private MapView mMapLayer;
    /**
     * 住所検索レイヤー
     */
    private LinearLayout mSearchLayer;
    /**
     * ボタンレイヤー
     */
    private RelativeLayout mButtonLayer;

    private EditText mAddressText;
    private MapController mMapController;
    private SearchAddress mSearchAddress;
    private List<Overlay> mOverlays;
    private MyLocationOverlay mMyLocationOverlay;
    private LocationProviderChecker mLocationProviderChecker;
    private NetworkChecker mNetworkChecker;
    private ImageView mMyLocationBtn;

    // com.google.android.maps.MapActivityのOverride Method
    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    // android.app.ActivityのOverride Method
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main_activity);

        mSearchAddress = new SearchAddress(this);

        // 住所検索の検索結果リスト
        ListView resultList = (ListView) findViewById(R.id.candidate_list);
        resultList.setOnItemClickListener(new ResultItemClickListener());
        resultList.setAdapter(mSearchAddress.getAdapter());

        // 地図表示レイヤー
        mMapLayer = (MapView) findViewById(R.id.map_view_layer);
        mMapLayer.setBuiltInZoomControls(true);
        mMapController = mMapLayer.getController();

        // 住所検索の検索キーワード入力欄
        mAddressText = (EditText) findViewById(R.id.edit_address);
        mAddressText.addTextChangedListener(new KeywordInputWatcher());
        mAddressText.setOnFocusChangeListener(new SoftInputChange());

        // 住所検索レイヤー
        mSearchLayer = (LinearLayout) findViewById(R.id.search_layer);

        // 現在位置表示オーバーレイ
        mMyLocationOverlay = new MyLocationOverlay(this, mMapLayer);
        mMyLocationOverlay.runOnFirstFix(new Runnable() {
            public void run() {
                GeoPoint myPoint = mMyLocationOverlay.getMyLocation();
                mMapController.animateTo(myPoint);
                mMapController.setZoom(19);
            }
        });

        mOverlays = mMapLayer.getOverlays();
        mOverlays.add(mMyLocationOverlay);
        mOverlays.add(new MapOverlay());

        // ボタンレイヤー
        mButtonLayer = (RelativeLayout) findViewById(R.id.button_layer);
        mMyLocationBtn = (ImageView) findViewById(R.id.mylocation_button);
        mMyLocationBtn.setOnClickListener(new MylocationClickListener());
        ((ImageView) findViewById(R.id.search_button))
                .setOnClickListener(new SearchButtonClickListener());

        // 地図表示モードが初期表示
        changeLayerVisibility(MODE_MAP);

        mLocationProviderChecker = new LocationProviderChecker(this);

        mNetworkChecker = new NetworkChecker(this);
        if (!mNetworkChecker.hasNetworkConnection()) {
            mNetworkChecker.openSetting();
        }
    }

    @Override
    public void onDestroy() {
        if (mMyLocationOverlay.isMyLocationEnabled()) {
            mMyLocationOverlay.disableMyLocation();
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        if (requestCode == LocationProviderChecker.REQUSET_CODE_OPEN_SETTINGS) {
            if (mLocationProviderChecker.isProviderEnabled()) {
                startMyLocation();
                return;
            }
            mLocationProviderChecker.openSetting();
            return;
        }

        if (requestCode == NetworkChecker.REQUEST_CODE_OPEN_SETTINGS) {
            if (mNetworkChecker.hasNetworkConnection()) {
                mMapLayer.preLoad();
                return;
            }
            mNetworkChecker.openSetting();
            return;
        }
    }

    private void startMyLocation() {
        mMyLocationOverlay.enableMyLocation();
        mMyLocationBtn.setBackgroundResource(R.drawable.backgraund_reverse);
        Location location = mMyLocationOverlay.getLastFix();
        if (location != null) {
            GeoPoint myPoint = new GeoPoint((int) (location.getLatitude() * 1E6),
                    (int) (location.getLongitude() * 1E6));
            mMapController.animateTo(myPoint);
            mMapController.setZoom(19);
        }
    }

    /**
     * モードに合わせて、各レイヤーの表示・非表示を切り替える。
     * 
     * @param mode MODE_MAP:地図表示モード、 MODE_SEARCH:住所検索モード。
     */
    private void changeLayerVisibility(int mode) {
        switch (mode) {
            case MODE_MAP:
                mButtonLayer.setVisibility(View.VISIBLE);
                mSearchLayer.setVisibility(View.INVISIBLE);
                break;

            case MODE_SEARCH:
                mButtonLayer.setVisibility(View.GONE);
                mSearchLayer.setVisibility(View.VISIBLE);
                break;
        }
    }

    /**
     * 住所検索ボタンのクリックリスナー
     */
    private class SearchButtonClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            changeLayerVisibility(MODE_SEARCH);
        }
    }

    /**
     * 現在位置表示ボタンのクリックリスナー
     */
    private class MylocationClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            if (mMyLocationOverlay.isMyLocationEnabled()) {
                mMyLocationOverlay.disableMyLocation();
                mMapLayer.invalidate();
                mMyLocationBtn.setBackgroundResource(R.drawable.backgraund);
                return;
            }

            if (!mLocationProviderChecker.isProviderEnabled()) {
                mLocationProviderChecker.openSetting();
                return;
            }

            startMyLocation();
        }
    }

    private class SoftInputChange implements OnFocusChangeListener {
        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (hasFocus) {
                inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_FORCED);
            } else {
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    /**
     * 地図のオーバーレイ。クリックイベントを横取りする為に使う。
     */
    private class MapOverlay extends Overlay implements OnDoubleTapListener, OnGestureListener {
        private GestureDetector gesture = new GestureDetector(this);

        @Override
        public boolean onTouchEvent(MotionEvent e, MapView mapView) {
            gesture.onTouchEvent(e);
            return super.onTouchEvent(e, mapView);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            mMapLayer.getController().zoomInFixing((int) e.getX(), (int) e.getY());
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            changeLayerVisibility(MODE_MAP);
            return false;
        }
    }

    /**
     * 住所検索のキーワード入力欄の変更監視リスナー
     */
    private class KeywordInputWatcher implements TextWatcher {
        @Override
        public void afterTextChanged(Editable s) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mSearchAddress.search(mAddressText.getText().toString());
        }
    }

    /**
     * 住所検索の検索結果リストのクリックリスナー
     */
    private class ResultItemClickListener implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Position targetPos = (Position) view.getTag();
            if (targetPos == null) {
                return;
            }

            GeoPoint targetPoint = new GeoPoint(targetPos.mLatitude, targetPos.mLongitude);
            moveToTargetPoint(targetPoint);

            Drawable pin = getResources().getDrawable(R.drawable.pin_marker);
            PinItemizedOverlay pinOverlay = new PinItemizedOverlay(pin);
            pinOverlay.addPoint(targetPoint);

            if (mOverlays.size() == 3) {
                mOverlays.remove(2);
            }
            mOverlays.add(pinOverlay);

            changeLayerVisibility(MODE_MAP);
        }

        private void moveToTargetPoint(GeoPoint targetPoint) {
            mMapController.setZoom(18);
            mMapController.setCenter(targetPoint);
        }
    }
}
