package com.onestop.myplugin;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


// import androidx.annotation.RequiresApi;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.io.IOException;


public class Airplane extends CordovaPlugin {

    private static final String TAG = "Airplane";
    private final String COMMAND_AIRPLANE_MODE_1 = "settings put global airplane_mode_on";
    private final String COMMAND_AIRPLANE_MODE_2 = "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state";

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        Context context = cordova.getActivity().getApplicationContext();
        if (action.equals("coolMethod")) {
            String message = args.getString(0);

            Toast.makeText(context, "SUCCESS", Toast.LENGTH_LONG).show();
            Log.i(TAG, message);
            setAirplaneMode(context);
            this.coolMethod(message, callbackContext);
            return true;
        }
        return false;
    }

    private void coolMethod(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }


            public void setAirplaneMode(Context context) {
                // API 17 以上.
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
                    Log.i(TAG, "IsRooted: " + isRooted(context));
                    if (isRooted(context)) {
                        int enabled = isAirplaneModeEnabled(context) ? 0 : 1;
                        Log.i(TAG, "API 17，isAirplaneModeEnabled: " + enabled);
                        // 設定 Airplane  mode 使用 su commands.
                        String command = COMMAND_AIRPLANE_MODE_1 + " " + enabled;
                        executeCommandWithoutWait(context, "-c", command);
                        command = COMMAND_AIRPLANE_MODE_2 + " " + enabled;
                        executeCommandWithoutWait(context, "-c", command);
                    } else {
                        Log.i(TAG, "NO Root" );
                        try {
                            // 沒有root權限, 直接顯示Airplane mode setting
                            Intent intent = new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            Log.e(TAG, "Setting screen not found due to: " + e.fillInStackTrace());
                        }
                    }
                } else {
                    // API 17 以下.
                    // boolean enabled = isAirplaneModeEnabled(context);
                    // Log.i(TAG, "API 17以下，isAirplaneModeEnabled: " + enabled);
                    // Settings.System.putInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, enabled ? 0 : 1);
                    // Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
                    // intent.putExtra("state", !enabled);
                    // sendBroadcast(intent);
                }
            }


            //檢查是否有root權限
            private  boolean isRooted(Context context) {

                // get from build info
                String buildTags = android.os.Build.TAGS;
                if (buildTags != null && buildTags.contains("test-keys")) {
                    return true;
                }

                // check if /system/app/Superuser.apk is present
                try {
                    File file = new File("/system/app/Superuser.apk");
                    if (file.exists()) {
                        return true;
                    }
                } catch (Exception e1) {
                    // ignore
                }

                // try executing commands
                return canExecuteCommand("/system/xbin/which su")
                        || canExecuteCommand("/system/bin/which su") || canExecuteCommand("which su");
            }

            //檢查是否可從Runtime執行command
            private  boolean canExecuteCommand(String command) {
                boolean executedSuccesfully;
                try {
                    Runtime.getRuntime().exec(command);
                    executedSuccesfully = true;
                } catch (Exception e) {
                    executedSuccesfully = false;
                }

                return executedSuccesfully;
            }

            //執行Command
            // @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            private void executeCommandWithoutWait(Context context, String option, String command) {
                boolean success = false;
                String su = "su";
                for (int i = 0; i < 3; i++) {
                    // "su" command executed successfully.
                    if (success) {
                        // Stop executing alternative su commands below.
                        break;
                    }
                    if (i == 1) {
                        su = "/system/xbin/su";
                    } else if (i == 2) {
                        su = "/system/bin/su";
                    }
                    try {
                        // execute command
                        Runtime.getRuntime().exec(new String[]{su, option, command});
                    } catch (IOException e) {
                        Log.e(TAG, "su command has failed due to: " + e.fillInStackTrace());
                    }
                }
            }

            //檢查Airplane mode狀態
            @SuppressLint("NewApi")
            @SuppressWarnings("deprecation")
            private boolean isAirplaneModeEnabled(Context context) {
                boolean mode = false;
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
                    mode = Settings.Global.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) == 1;
                } else {
                    mode = Settings.System.getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) == 1;
                }

                return mode;
            }


}
