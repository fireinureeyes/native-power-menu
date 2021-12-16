package com.nativepowermenu;

import android.accessibilityservice.AccessibilityService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

public class PowerMenuService extends AccessibilityService {

    private BroadcastReceiver powerMenuReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(!performGlobalAction(intent.getIntExtra("action", -1)))
                Toast.makeText(context, getString(R.string.error_message), Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {}

    @Override
    public void onInterrupt() {}

    @Override
    public void onCreate() {
        super.onCreate();

        LocalBroadcastManager.getInstance(this).registerReceiver(powerMenuReceiver, new IntentFilter("com.nativepowermenu.ACCESSIBILITY_ACTION"));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(powerMenuReceiver);
    }
}