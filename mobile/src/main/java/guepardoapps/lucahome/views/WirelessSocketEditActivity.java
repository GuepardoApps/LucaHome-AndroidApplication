package guepardoapps.lucahome.views;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import java.util.Locale;

import de.mateware.snacky.Snacky;
import guepardoapps.lucahome.R;
import guepardoapps.lucahome.basic.classes.SerializableDate;
import guepardoapps.lucahome.basic.classes.SerializableTime;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.Tools;
import guepardoapps.lucahome.common.classes.WirelessSocket;
import guepardoapps.lucahome.common.dto.WirelessSocketDto;
import guepardoapps.lucahome.common.interfaces.classes.ILucaClass;
import guepardoapps.lucahome.common.service.WirelessSocketService;
import guepardoapps.lucahome.common.service.broadcasts.content.ObjectChangeFinishedContent;
import guepardoapps.lucahome.service.NavigationService;

public class WirelessSocketEditActivity extends AppCompatActivity {
    private static final String TAG = WirelessSocketEditActivity.class.getSimpleName();

    private boolean _propertyChanged;
    private WirelessSocketDto _wirelessSocketDto;

    private ReceiverController _receiverController;

    private com.rey.material.widget.Button _saveButton;

    private BroadcastReceiver _addFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ObjectChangeFinishedContent result = (ObjectChangeFinishedContent) intent.getSerializableExtra(WirelessSocketService.WirelessSocketAddFinishedBundle);
            if (result != null) {
                if (result.Success) {
                    navigateBack("Added socket!");
                } else {
                    displayErrorSnackBar(Tools.DecompressByteArrayToString(result.Response));
                    _saveButton.setEnabled(true);
                }
            } else {
                displayErrorSnackBar("Failed to add socket!");
                _saveButton.setEnabled(true);
            }
        }
    };

    private BroadcastReceiver _updateFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ObjectChangeFinishedContent result = (ObjectChangeFinishedContent) intent.getSerializableExtra(WirelessSocketService.WirelessSocketUpdateFinishedBundle);
            if (result != null) {
                if (result.Success) {
                    navigateBack("Updated socket!");
                } else {
                    displayErrorSnackBar(Tools.DecompressByteArrayToString(result.Response));
                    _saveButton.setEnabled(true);
                }
            } else {
                displayErrorSnackBar("Failed to update socket!");
                _saveButton.setEnabled(true);
            }
        }
    };

    private TextWatcher _textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            _propertyChanged = true;
            _saveButton.setEnabled(true);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wireless_socket_edit);

        _wirelessSocketDto = (WirelessSocketDto) getIntent().getSerializableExtra(WirelessSocketService.WirelessSocketIntent);

        _receiverController = new ReceiverController(this);

        final AutoCompleteTextView socketNameTypeTextView = findViewById(R.id.socket_edit_name_textview);
        final AutoCompleteTextView socketAreaTypeTextView = findViewById(R.id.socket_edit_area_textview);
        final AutoCompleteTextView socketCodeTypeTextView = findViewById(R.id.socket_edit_code_textview);

        _saveButton = findViewById(R.id.save_socket_edit_button);

        socketNameTypeTextView.setAdapter(new ArrayAdapter<>(WirelessSocketEditActivity.this, android.R.layout.simple_dropdown_item_1line, WirelessSocketService.getInstance().GetNameList()));
        socketNameTypeTextView.addTextChangedListener(_textWatcher);

        socketAreaTypeTextView.setAdapter(new ArrayAdapter<>(WirelessSocketEditActivity.this, android.R.layout.simple_dropdown_item_1line, WirelessSocketService.getInstance().GetAreaList()));
        socketAreaTypeTextView.addTextChangedListener(_textWatcher);

        socketCodeTypeTextView.setAdapter(new ArrayAdapter<>(WirelessSocketEditActivity.this, android.R.layout.simple_dropdown_item_1line, WirelessSocketService.getInstance().GetCodeList()));
        socketCodeTypeTextView.addTextChangedListener(_textWatcher);

        if (_wirelessSocketDto != null) {
            socketNameTypeTextView.setText(_wirelessSocketDto.GetName());
            socketAreaTypeTextView.setText(_wirelessSocketDto.GetArea());
            socketCodeTypeTextView.setText(_wirelessSocketDto.GetCode());
        } else {
            displayErrorSnackBar("Cannot work with data! Is corrupt! Please try again!");
        }

        _saveButton.setEnabled(false);
        _saveButton.setOnClickListener(view -> {
            socketNameTypeTextView.setError(null);
            socketAreaTypeTextView.setError(null);
            socketCodeTypeTextView.setError(null);
            boolean cancel = false;
            View focusView = null;

            if (!_propertyChanged) {
                socketNameTypeTextView.setError(createErrorText(getString(R.string.error_nothing_changed)));
                focusView = socketNameTypeTextView;
                cancel = true;
            }

            String name = socketNameTypeTextView.getText().toString();

            if (TextUtils.isEmpty(name)) {
                socketNameTypeTextView.setError(createErrorText(getString(R.string.error_field_required)));
                focusView = socketNameTypeTextView;
                cancel = true;
            }

            String area = socketAreaTypeTextView.getText().toString();

            if (TextUtils.isEmpty(area)) {
                socketAreaTypeTextView.setError(createErrorText(getString(R.string.error_field_required)));
                focusView = socketAreaTypeTextView;
                cancel = true;
            }

            String code = socketCodeTypeTextView.getText().toString();

            if (TextUtils.isEmpty(code)) {
                socketCodeTypeTextView.setError(createErrorText(getString(R.string.error_field_required)));
                focusView = socketCodeTypeTextView;
                cancel = true;
            }

            if (cancel) {
                focusView.requestFocus();
            } else {
                if (_wirelessSocketDto.GetAction() == WirelessSocketDto.Action.Add) {
                    int highestId = WirelessSocketService.getInstance().GetHighestId();
                    WirelessSocketService.getInstance().AddWirelessSocket(new WirelessSocket(highestId + 1, name, area, code, false, new SerializableDate(), new SerializableTime(), "", false, ILucaClass.LucaServerDbAction.Add));
                    _saveButton.setEnabled(false);
                } else if (_wirelessSocketDto.GetAction() == WirelessSocketDto.Action.Update) {
                    WirelessSocketService.getInstance().UpdateWirelessSocket(new WirelessSocket(_wirelessSocketDto.GetId(), name, area, code, _wirelessSocketDto.IsActivated(), new SerializableDate(), new SerializableTime(), "", false, ILucaClass.LucaServerDbAction.Update));
                    _saveButton.setEnabled(false);
                } else {
                    socketNameTypeTextView.setError(createErrorText(String.format(Locale.getDefault(), "Invalid action %s", _wirelessSocketDto.GetAction())));
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        _receiverController.RegisterReceiver(_addFinishedReceiver, new String[]{WirelessSocketService.WirelessSocketAddFinishedBroadcast});
        _receiverController.RegisterReceiver(_updateFinishedReceiver, new String[]{WirelessSocketService.WirelessSocketUpdateFinishedBroadcast});
    }

    @Override
    protected void onPause() {
        super.onPause();
        _receiverController.Dispose();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        _receiverController.Dispose();
    }

    @Override
    public void onBackPressed() {
        NavigationService.getInstance().GoBack(this);
    }

    /**
     * Build a custom error text
     */
    private SpannableStringBuilder createErrorText(@NonNull String errorString) {
        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.RED);
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(errorString);
        spannableStringBuilder.setSpan(foregroundColorSpan, 0, errorString.length(), 0);
        return spannableStringBuilder;
    }

    private void displayErrorSnackBar(@NonNull String message) {
        Snacky.builder()
                .setActivty(WirelessSocketEditActivity.this)
                .setText(message)
                .setDuration(Snacky.LENGTH_INDEFINITE)
                .setActionText(android.R.string.ok)
                .error()
                .show();
    }

    private void navigateBack(@NonNull String message) {
        Snacky.builder()
                .setActivty(WirelessSocketEditActivity.this)
                .setText(message)
                .setDuration(Snacky.LENGTH_INDEFINITE)
                .success()
                .show();

        new Handler().postDelayed(() -> {
            NavigationService.NavigationResult navigationResult = NavigationService.getInstance().GoBack(WirelessSocketEditActivity.this);
            if (navigationResult != NavigationService.NavigationResult.SUCCESS) {
                Logger.getInstance().Error(TAG, String.format(Locale.getDefault(), "Navigation failed! navigationResult is %s!", navigationResult));
                displayErrorSnackBar("Failed to navigate back! Please contact LucaHome support!");
            }
        }, 1500);
    }
}
