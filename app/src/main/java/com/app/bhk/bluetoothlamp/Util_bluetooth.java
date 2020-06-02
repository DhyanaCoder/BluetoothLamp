package com.app.bhk.bluetoothlamp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by thinkpad on 2018/11/29.
 */

public class Util_bluetooth {

    public static void Start_bluetooth(Activity context, BluetoothAdapter bTAdatper) {//开启蓝牙的方法。

        if (bTAdatper == null) {
            Toast.makeText(context, "当前设备不支持蓝牙功能", Toast.LENGTH_SHORT).show();
        }
        if (!bTAdatper.isEnabled()) {
           /* Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(i);*/
            bTAdatper.enable();
        }
        //开启被其它蓝牙设备发现的功能
        if (bTAdatper.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            //设置为一直开启
            i.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
            context.startActivity(i);

        }
    }

}