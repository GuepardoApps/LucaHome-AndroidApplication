package guepardoapps.mediamirror.adapter;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.classes.WirelessSwitch;
import guepardoapps.lucahome.common.service.WirelessSwitchService;
import guepardoapps.mediamirror.R;

public class SwitchListViewAdapter extends BaseAdapter {
    private static String TAG = SwitchListViewAdapter.class.getSimpleName();

    private class Holder {
        private TextView _titleText;
        private TextView _areaText;
        private TextView _codeText;
        private ImageView _cardImage;
        private Switch _cardSwitch;
        private View _stateView;
    }

    private SerializableList<WirelessSwitch> _listViewItems;

    private Dialog _dialog;
    private static LayoutInflater _inflater = null;

    public SwitchListViewAdapter(@NonNull Context context, @NonNull SerializableList<WirelessSwitch> listViewItems, @NonNull Dialog dialog) {
        _listViewItems = listViewItems;
        _inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        _dialog = dialog;
    }

    @Override
    public int getCount() {
        return _listViewItems.getSize();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint({"InflateParams", "ViewHolder"})
    @Override
    public View getView(final int index, View convertView, ViewGroup parent) {
        final Holder holder = new Holder();

        View rowView = _inflater.inflate(R.layout.listview_card_socket, null);

        holder._titleText = rowView.findViewById(R.id.socketCardTitleText);
        holder._areaText = rowView.findViewById(R.id.socketCardAreaText);
        holder._codeText = rowView.findViewById(R.id.socketCardCodeText);
        holder._cardImage = rowView.findViewById(R.id.socketCardImage);
        holder._cardSwitch = rowView.findViewById(R.id.socketCardSwitch);
        holder._stateView = rowView.findViewById(R.id.socketCardStateView);

        final WirelessSwitch wirelessSwitch = _listViewItems.getValue(index);

        holder._titleText.setText(wirelessSwitch.GetName());
        holder._areaText.setText(wirelessSwitch.GetArea());
        holder._codeText.setText(wirelessSwitch.GetCode());

        holder._cardImage.setImageResource(wirelessSwitch.GetWallpaper());

        holder._stateView.setBackgroundResource(wirelessSwitch.IsActivated() ? R.drawable.circle_green : R.drawable.circle_red);

        holder._cardSwitch.setChecked(wirelessSwitch.IsActivated());
        holder._cardSwitch.setOnCheckedChangeListener((compoundButton, value) -> {
            try {
                WirelessSwitchService.getInstance().ToggleWirelessSwitch(wirelessSwitch);
            } catch (Exception exception) {
                Logger.getInstance().Error(TAG, exception.getMessage());
            }
            _dialog.dismiss();
        });

        return rowView;
    }
}