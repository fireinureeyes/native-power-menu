package com.nativepowermenu;

import android.accessibilityservice.AccessibilityService;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends Activity {

    private Context mContext;
    private Thread thread;
    private Activity activity = this;
    private boolean flag = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();

        if (!isAccessibilitySettingsOn()) {

            if (thread == null) {
                thread = new Thread(){
                    public void run() {
                        while (thread != null) {
                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException ex) {
                                return;
                            }
                            if (isAccessibilitySettingsOn()) {
                                if (flag) {
                                    activity.runOnUiThread(new Runnable() {
                                        public void run() {
                                            Toast.makeText(mContext, getString(R.string.setting_finished),
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    });
                                    Intent myIntent = new Intent(activity, MainActivity.class);
                                    MainActivity.this.startActivity(myIntent);
                                    flag = false;
                                }
                            } else {
                                flag = true;
                            }
                        }
                    }
                };
                thread.start();
            }

            Toast.makeText(mContext, getString(R.string.instructions),
                    Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        }

        ComponentName component = new ComponentName(mContext, PowerMenuService.class);
        mContext.getPackageManager().setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

        Intent intent = new Intent("com.nativepowermenu.ACCESSIBILITY_ACTION");
        intent.putExtra("action", AccessibilityService.GLOBAL_ACTION_POWER_DIALOG);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

        this.finishAffinity();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isAccessibilitySettingsOn()) {
            if (thread != null) {
                thread.interrupt();
                thread = null;
            }
        }
    }

    // To check if service is enabled
    private boolean isAccessibilitySettingsOn() {
        int accessibilityEnabled = 0;
        final String service = getPackageName() + "/" + PowerMenuService.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    mContext.getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            Log.e("PowerMenuService", "" + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');
        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(
                    mContext.getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}