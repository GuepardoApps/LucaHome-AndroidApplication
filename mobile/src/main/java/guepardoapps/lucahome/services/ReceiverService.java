package guepardoapps.lucahome.services;

import java.util.Calendar;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.IBinder;

import guepardoapps.library.lucahome.common.constants.Constants;
import guepardoapps.library.lucahome.common.constants.SharedPrefConstants;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.controller.LucaNotificationController;
import guepardoapps.library.lucahome.controller.ServiceController;

import guepardoapps.library.toolset.controller.NetworkController;
import guepardoapps.library.toolset.controller.ReceiverController;
import guepardoapps.library.toolset.controller.SharedPrefController;

public class ReceiverService extends Service {

    private static final String TAG = ReceiverService.class.getSimpleName();
    private LucaHomeLogger _logger;

    public static final int SLEEP_NOTIFICATION_HOUR = 21;
    public static final int SLEEP_NOTIFICATION_MINUTE = 45;
    public static final int SLEEP_NOTIFICATION_TIME_SPAN_MINUTE = 5;

    private static final int CLEAR_NOTIFICATION_DISPLAY_HOUR = 2;
    private static final int CLEAR_NOTIFICATION_DISPLAY_MINUTE = 0;

    private LucaNotificationController _notificationController;
    private NetworkController _networkController;
    private ReceiverController _receiverController;
    private SharedPrefController _sharedPrefController;
    private ServiceController _serviceController;

    private boolean _isInitialized;

    // Receiver used for receiving the current battery state of the phone and
    // sending the state to the watch
    private BroadcastReceiver _batteryChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_batteryChangedReceiver onReceive");

            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            if (level != -1) {
                _logger.Debug("new battery level: " + String.valueOf(level));
                String message = "PhoneBattery:" + String.valueOf(level) + "%";
                _serviceController.SendMessageToWear(message);
            }
        }
    };

    // Receiver used for receiving the current time and check if time is ready
    // to display sleep notification or set a flag if not or to delete this flag
    private BroadcastReceiver _timeTickReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            _logger.Debug("action: " + action);

            if (action.equals(Intent.ACTION_TIME_TICK)) {
                Calendar calendar = Calendar.getInstance();

                // Check, if the time is ready for displaying the notification
                // time needs to be NOTIFICATION_HOUR:NOTIFICATION_MINUTE
                if ((int) calendar.get(Calendar.HOUR_OF_DAY) == SLEEP_NOTIFICATION_HOUR) {
                    if (calendar.get(Calendar.MINUTE) > SLEEP_NOTIFICATION_MINUTE - SLEEP_NOTIFICATION_TIME_SPAN_MINUTE
                            && calendar.get(Calendar.MINUTE) < SLEEP_NOTIFICATION_MINUTE
                            + SLEEP_NOTIFICATION_TIME_SPAN_MINUTE) {
                        _logger.Debug("We are in the timeSpan!");

                        // Check also if we are in the home network, otherwise
                        // it's senseless to display the notification, but we
                        // set a flag to display notification later
                        if (_networkController.IsHomeNetwork(Constants.LUCAHOME_SSID)) {
                            _logger.Debug("Showing notification go to sleep!");
                            _notificationController.CreateSleepHeatingNotification();
                        } else {
                            _logger.Debug("Saving flag to display notification later!");
                            _sharedPrefController
                                    .SaveBooleanValue(SharedPrefConstants.DISPLAY_SLEEP_NOTIFICATION_ACTIVE, true);
                        }
                    } else {
                        _logger.Debug("We are NOT in the timeSpan!");
                    }
                }
                // Check if time is ready to delete the flag to display the
                // sleep notification
                else if ((int) calendar.get(Calendar.HOUR_OF_DAY) == CLEAR_NOTIFICATION_DISPLAY_HOUR) {
                    if ((int) calendar.get(Calendar.MINUTE) == CLEAR_NOTIFICATION_DISPLAY_MINUTE) {
                        if (_sharedPrefController.LoadBooleanValueFromSharedPreferences(
                                SharedPrefConstants.DISPLAY_SLEEP_NOTIFICATION_ACTIVE)) {
                            _logger.Debug("We entered home and flag is active to display sleep notification");
                            _sharedPrefController
                                    .SaveBooleanValue(SharedPrefConstants.DISPLAY_SLEEP_NOTIFICATION_ACTIVE, false);
                        }
                    }
                }
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!_isInitialized) {
            _logger = new LucaHomeLogger(TAG);

            _networkController = new NetworkController(this, null);
            _notificationController = new LucaNotificationController(this);
            _receiverController = new ReceiverController(this);
            _sharedPrefController = new SharedPrefController(this, SharedPrefConstants.SHARED_PREF_NAME);
            _serviceController = new ServiceController(this);

            _receiverController.RegisterReceiver(_batteryChangedReceiver,
                    new String[]{Intent.ACTION_BATTERY_CHANGED});
            _receiverController.RegisterReceiver(_timeTickReceiver, new String[]{Intent.ACTION_TIME_TICK});

            _isInitialized = true;
        }

        _logger.Debug("onStartCommand");

        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onDestroy() {
        _receiverController.UnregisterReceiver(_batteryChangedReceiver);
        _receiverController.UnregisterReceiver(_timeTickReceiver);
        _isInitialized = false;
    }
}