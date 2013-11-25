
package jp.gr.java_conf.kobitokaba.simple_map;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.util.List;

/**
 * 住所名からAddressを検索する。検索結果はgetAdapter()でアダプターとして取得する。
 */
class SearchAddress {
    public static final int MSG_COMPLETE = 0xa1;
    public static final int MSG_ERR = 0xb1;
    public static final int MSG_NO_MATCHE = 0xc1;

    private static final int MAXRESULTS = 6;

    private Geocoder mGeocoder;
    private AddressListAdapter mAdapter;

    private GeocoderTask mGeocoderTask;

    private Handler mHandler = new Handler() {
        @Override
//        @SuppressWarnings("unchecked")
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_COMPLETE:
                    mAdapter.setAddressList((List<Address>) msg.obj);
                    break;
                case MSG_NO_MATCHE:
                case MSG_ERR:
                    clearAdapter();
                    break;
            }
        }
    };

    SearchAddress(Context context) {
        mGeocoder = new Geocoder(context);
        mAdapter = new AddressListAdapter(context);
        mGeocoderTask = null;
    }

    void clearAdapter() {
        mAdapter.clear();
    }

    AddressListAdapter getAdapter() {
        return mAdapter;
    }

    void search(final String keyword) {
        if (mGeocoderTask != null) {
            mGeocoderTask.interrupt(); // FIXME
            mGeocoderTask.cancel();
        }

        if (keyword.equals("")) {
            clearAdapter();
            return;
        }

        mGeocoderTask = new GeocoderTask(keyword);
        mGeocoderTask.start();
    }

    /**
     * Geocoderで住所名からAddressを取得する
     */
    private class GeocoderTask extends Thread {
        private String mKeyword;
        private boolean mCancel;

        GeocoderTask(String keyword) {
            mKeyword = keyword;
            mCancel = false;
        }

        @Override
        public void run() {
            Message msg = new Message();
            List<Address> addressList = null;
            try {
                addressList = mGeocoder.getFromLocationName(mKeyword, MAXRESULTS);
            } catch (IOException e) {
                e.printStackTrace();
                msg.what = MSG_ERR;
                mHandler.sendMessage(msg);
                return;
            }

            if (mCancel) {
                return;
            } else if (addressList == null || addressList.isEmpty()) {
                msg.what = MSG_NO_MATCHE;
            } else {
                msg.obj = addressList;
                msg.what = MSG_COMPLETE;
            }
            mHandler.sendMessage(msg);
        }

        public void cancel() {
            mCancel = true;
        }
    }
}
