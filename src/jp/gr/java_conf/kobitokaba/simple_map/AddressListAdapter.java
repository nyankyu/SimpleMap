
package jp.gr.java_conf.kobitokaba.simple_map;

import android.content.Context;
import android.location.Address;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

class AddressListAdapter extends BaseAdapter {
    private Context mContext;
    private List<Address> mAddressList;
    private LayoutInflater mInflater;

    public AddressListAdapter(Context context) {
        super();
        mContext = context;
        mAddressList = new ArrayList<Address>();
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mAddressList.size();
    }

    @Override
    public Object getItem(int position) {
        return mAddressList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = mInflater.inflate(R.layout.item, null);
        }

        ((TextView) view.findViewById(android.R.id.text1)).setText(makeAddressString(mAddressList
                .get(position)));
        view.setTag(new Position(getLatitude(position), getLongitude(position)));
        return view;
    }

    void setAddressList(List<Address> addressList) {
        mAddressList = addressList;
        notifyDataSetChanged();
    }

    void clear() {
        mAddressList.clear();
        notifyDataSetChanged();
    }

    private int getLatitude(int position) {
        return (int) (mAddressList.get(position).getLatitude() * 1E6);
    }

    private int getLongitude(int position) {
        return (int) (mAddressList.get(position).getLongitude() * 1E6);
    }

    private String makeAddressString(Address address) {
        String str1 = makeMainAddressString(address);
        String str2 = makeSubAddressString(address);
        return str1.length() < str2.length() ? str2 : str1;
    }

    /**
     * addressの"line of the address"をスペース区切りの文字列にして返す。
     * 
     * @param address "line of the address"を取り出すAddress
     * @return addressの"line of the address"をスペース区切りにした文字列
     */
    private String makeMainAddressString(Address address) {
        boolean deleteCountry = false;
        if (inMyCountry(address)) {
            deleteCountry = true;
        }
        StringBuilder sb = new StringBuilder();
        int max = address.getMaxAddressLineIndex();
        for (int index = 0; index <= max; index++) {
            String str = address.getAddressLine(index);
            // 国名が最初又は終わりにある場合は出力に含めない。
            if (deleteCountry && (index == 0 || index == max)
                    && str.equals(address.getCountryName())) {
                continue;
            }
            appendString(sb, str);
        }
        sb.trimToSize();
        return sb.toString();
    }

    /**
     * "line of the address"以外の住所情報をスペース区切りの文字列にして返す。
     * 
     * @param address 住所情報を取り出すAddress
     * @return "line of the address"以外の住所情報をスペース区切りにした文字列
     */
    private String makeSubAddressString(Address address) {
        StringBuilder sb = new StringBuilder();
        appendString(sb, address.getFeatureName());
        appendString(sb, address.getAdminArea());
        appendString(sb, address.getSubAdminArea());
        appendString(sb, address.getLocality());
        appendString(sb, address.getThoroughfare());
        return sb.toString();
    }

    private void appendString(StringBuilder sb, String str) {
        if (str == null) {
            return;
        }
        sb.append(str);
        sb.append(" ");
    }

    private boolean inMyCountry(Address address) {
        String phoneCountry = mContext.getResources().getConfiguration().locale.getCountry();
        String addressCountry = address.getCountryCode();
        if (phoneCountry.compareTo("") == 0 || addressCountry == null) {
            return false;
        }
        return phoneCountry.equals(addressCountry);
    }
}
