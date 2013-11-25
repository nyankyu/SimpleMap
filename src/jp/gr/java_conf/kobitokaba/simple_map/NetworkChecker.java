
package jp.gr.java_conf.kobitokaba.simple_map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * ネットワーク接続の有無を調べる。
 */
public class NetworkChecker {
    public static final int REQUEST_CODE_OPEN_SETTINGS = 0x01;
    private Context mContext;

    NetworkChecker(Context context) {
        mContext = context;
    }

    /**
     * ネットワーク接続が有るか？
     * 
     * @return ネットワーク接続がある場合true、ない場合false。
     */
    boolean hasNetworkConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null) {
            return false;
        }
        return networkInfo.isConnected();
    }

    /**
     * ネットワーク接続が無かった場合のダイアログを表示する。ダイアログには「設定を開く」ボタン有り。
     */
    void openSetting() {
        new AlertDialog.Builder(mContext)
                .setTitle(R.string.dlg_title_non_network_connection)
                .setMessage(R.string.dlg_msg_non_network_connection)
                .setPositiveButton(R.string.dlg_caption_open_settings,
                        new OpenSettingsClickListener())
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /**
     * 「設定を開く」ボタンのクリックリスナー
     */
    private class OpenSettingsClickListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            // バージョンによって開く設定画面を変える
            String action;
            if (android.os.Build.VERSION.SDK_INT >= 11) {
                // Honeycomb, ICS, JB
                action = android.provider.Settings.ACTION_SETTINGS;
            } else {
                // 上記以前のバージョン
                action = android.provider.Settings.ACTION_WIRELESS_SETTINGS;
            }
            ((Activity) mContext).startActivityForResult(new Intent(action), REQUEST_CODE_OPEN_SETTINGS);
        }
    }
}
