/*
* Copyright (C) 2008 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.android.internal.policy.impl;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Process;
import android.view.Window;
import android.view.WindowManager;

public class KillProcessDialog {
    private AlertDialog mDialog;
    private Context mContext;
    private IntentFilter mBroadcastIntentFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
    private int mPid;

    public KillProcessDialog(Context context, int pid) {
        mContext = context;
        mPid = pid;
        mDialog = null;
    }

    public void show() {
        final AlertDialog.Builder adb = new AlertDialog.Builder(mContext);
        adb.setTitle(com.android.internal.R.string.force_close);
        adb.setMessage(com.android.internal.R.string.long_press_back_kill);
        adb.setIcon(com.android.internal.R.drawable.ic_dialog_alert);
        adb.setCancelable(true);
        adb.setPositiveButton(com.android.internal.R.string.force_close, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // kill
                Process.killProcess(mPid);
                dialog.dismiss();
            }
        });
        adb.setNegativeButton(com.android.internal.R.string.wait, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // do nothing
                dialog.dismiss();
            }
        });

        mDialog = adb.create();
        mDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);

        if (!mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_sf_slowBlur)) {
            mDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                    WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        }

        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            public void onShow(DialogInterface dialog) {
                mContext.registerReceiver(mBroadcastReceiver, mBroadcastIntentFilter);
            }
        });
        mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                mContext.unregisterReceiver(mBroadcastReceiver);
            }
        });

        mDialog.show();
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)) {
                String reason = intent.getStringExtra(PhoneWindowManager.SYSTEM_DIALOG_REASON_KEY);
                if (! PhoneWindowManager.SYSTEM_DIALOG_REASON_RECENT_APPS.equals(reason)) {
                    if(mDialog != null && mDialog.isShowing()) {
                        mDialog.dismiss();
                    }
                }
            }
        }
    };
}
