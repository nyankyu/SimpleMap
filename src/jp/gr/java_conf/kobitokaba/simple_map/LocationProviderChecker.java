
package jp.gr.java_conf.kobitokaba.simple_map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.location.LocationManager;

/**
 * 位置情報プロバイダの有無をチェックする
 */
class LocationProviderChecker {
    public static final int REQUSET_CODE_OPEN_SETTINGS = 0x11;
    private Context mContext;
    private LocationManager mLocationManager;

    LocationProviderChecker(Context context) {
        mContext = context;
        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
    }

    boolean isProviderEnabled() {
        return mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
    
    void openSetting() {
        new AlertDialog.Builder(mContext)
        .setMessage(R.string.dlg_msg_location_provider_checker)
        .setPositiveButton(R.string.dlg_caption_open_settings,
                new OpenSettingsClickListener())
        .setNegativeButton(android.R.string.cancel, null)
        .show();
    }

    private class OpenSettingsClickListener implements OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            ((Activity) mContext).startActivityForResult(new Intent(
                    android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS),
                    REQUSET_CODE_OPEN_SETTINGS);
        }
    }
}
