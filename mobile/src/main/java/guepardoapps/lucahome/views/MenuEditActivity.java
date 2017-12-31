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
import android.widget.TextView;

import com.rey.material.widget.FloatingActionButton;

import java.util.Locale;
import java.util.Random;

import de.mateware.snacky.Snacky;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.Tools;
import guepardoapps.lucahome.common.classes.ListedMenu;
import guepardoapps.lucahome.common.classes.LucaMenu;
import guepardoapps.lucahome.common.dto.MenuDto;
import guepardoapps.lucahome.common.interfaces.classes.ILucaClass;
import guepardoapps.lucahome.common.service.MenuService;
import guepardoapps.lucahome.common.service.broadcasts.content.ObjectChangeFinishedContent;
import guepardoapps.lucahome.service.NavigationService;

public class MenuEditActivity extends AppCompatActivity {
    private static final String TAG = MenuEditActivity.class.getSimpleName();

    private boolean _propertyChanged;
    private MenuDto _menuDto;

    private ReceiverController _receiverController;

    private com.rey.material.widget.Button _saveButton;

    private BroadcastReceiver _updateFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ObjectChangeFinishedContent result = (ObjectChangeFinishedContent) intent.getSerializableExtra(MenuService.MenuUpdateFinishedBundle);
            if (result != null) {
                if (result.Success) {
                    navigateBack("Updated menu!");
                } else {
                    displayErrorSnackBar(Tools.DecompressByteArrayToString(result.Response));
                    _saveButton.setEnabled(true);
                }
            } else {
                displayErrorSnackBar("Failed to update menu!");
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
        setContentView(R.layout.activity_menu_edit);

        _menuDto = (MenuDto) getIntent().getSerializableExtra(MenuService.MenuIntent);

        _receiverController = new ReceiverController(this);

        final TextView dateTextView = findViewById(R.id.menuDateTextView);
        final AutoCompleteTextView menuTitleTypeTextView = findViewById(R.id.menu_edit_title_textview);
        final AutoCompleteTextView menuDescriptionTypeTextView = findViewById(R.id.menu_edit_description_textview);

        _saveButton = findViewById(R.id.save_menu_edit_button);

        menuTitleTypeTextView.setAdapter(new ArrayAdapter<>(MenuEditActivity.this, android.R.layout.simple_dropdown_item_1line, MenuService.getInstance().GetDescriptionList()));
        menuTitleTypeTextView.addTextChangedListener(_textWatcher);

        menuDescriptionTypeTextView.setAdapter(new ArrayAdapter<>(MenuEditActivity.this, android.R.layout.simple_dropdown_item_1line, MenuService.getInstance().GetDescriptionList()));
        menuDescriptionTypeTextView.addTextChangedListener(_textWatcher);

        if (_menuDto != null) {
            dateTextView.setText(_menuDto.GetDateString());
            menuTitleTypeTextView.setText(_menuDto.GetTitle());
            menuDescriptionTypeTextView.setText(_menuDto.GetDescription());
        } else {
            displayErrorSnackBar("Cannot work with data! Is corrupt! Please try again!");
        }

        FloatingActionButton randomMenuButton = findViewById(R.id.menuRandomEntry_Button);
        randomMenuButton.setOnClickListener(view -> {
            SerializableList<ListedMenu> listedMenuList = MenuService.getInstance().GetListedMenuList();

            Random randomMenuId = new Random();
            int menuId = randomMenuId.nextInt(listedMenuList.getSize());

            ListedMenu menu = listedMenuList.getValue(menuId);
            menuTitleTypeTextView.setText(menu.GetDescription());
        });

        _saveButton.setEnabled(false);
        _saveButton.setOnClickListener(view -> {
            menuTitleTypeTextView.setError(null);
            menuDescriptionTypeTextView.setError(null);
            boolean cancel = false;
            View focusView = null;

            if (!_propertyChanged) {
                menuTitleTypeTextView.setError(createErrorText(getString(R.string.error_nothing_changed)));
                focusView = menuTitleTypeTextView;
                cancel = true;
            }

            String title = menuTitleTypeTextView.getText().toString();

            if (TextUtils.isEmpty(title)) {
                menuTitleTypeTextView.setError(createErrorText(getString(R.string.error_field_required)));
                focusView = menuTitleTypeTextView;
                cancel = true;
            }

            String description = menuDescriptionTypeTextView.getText().toString();

            if (TextUtils.isEmpty(description)) {
                menuDescriptionTypeTextView.setError(createErrorText(getString(R.string.error_field_required)));
                focusView = menuDescriptionTypeTextView;
                cancel = true;
            }

            if (cancel) {
                focusView.requestFocus();
            } else {
                MenuService.getInstance().UpdateMenu(new LucaMenu(_menuDto.GetId(), title, description, _menuDto.GetWeekday(), _menuDto.GetDate(), false, ILucaClass.LucaServerDbAction.Update));
                _saveButton.setEnabled(false);
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
        _receiverController.RegisterReceiver(_updateFinishedReceiver, new String[]{MenuService.MenuUpdateFinishedBroadcast});
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
                .setActivty(MenuEditActivity.this)
                .setText(message)
                .setDuration(Snacky.LENGTH_INDEFINITE)
                .setActionText(android.R.string.ok)
                .error()
                .show();
    }

    private void navigateBack(@NonNull String message) {
        Snacky.builder()
                .setActivty(MenuEditActivity.this)
                .setText(message)
                .setDuration(Snacky.LENGTH_INDEFINITE)
                .success()
                .show();

        new Handler().postDelayed(() -> {
            NavigationService.NavigationResult navigationResult = NavigationService.getInstance().GoBack(MenuEditActivity.this);
            if (navigationResult != NavigationService.NavigationResult.SUCCESS) {
                Logger.getInstance().Error(TAG, String.format(Locale.getDefault(), "Navigation failed! navigationResult is %s!", navigationResult));
                displayErrorSnackBar("Failed to navigate back! Please contact LucaHome support!");
            }
        }, 1500);
    }
}
