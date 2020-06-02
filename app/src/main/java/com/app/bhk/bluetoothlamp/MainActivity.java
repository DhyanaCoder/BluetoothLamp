package com.app.bhk.bluetoothlamp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.yalantis.phoenix.PullToRefreshView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private List<BluetoothDevice> mDeviceList=new ArrayList<>();
    DeviceAdapter deviceAdapter;
    IntentFilter intentFilter =new IntentFilter();
    PullToRefreshView pullToRefreshView;
    BluetoothAdapter bTAdatper=BluetoothAdapter.getDefaultAdapter();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        registerReceiver(mbhkReceiver,intentFilter);


        deviceAdapter=new DeviceAdapter(mDeviceList,this);
        RecyclerView listShow=(RecyclerView) findViewById(R.id.list_item);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        listShow.setLayoutManager(layoutManager);
        listShow.setAdapter(deviceAdapter);
        Util_bluetooth.Start_bluetooth(this,bTAdatper);

        if (Build.VERSION.SDK_INT >= 6.0) {
            if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},1);
            }else{
                bTAdatper.startDiscovery();
            }

        }else{
            bTAdatper.startDiscovery();
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.RECORD_AUDIO},1);
        }
        pullToRefreshView=(PullToRefreshView)findViewById(R.id.pull_to_refresh);
        //pullToRefreshView.setBackgroundResource(R.drawable.sky);
        pullToRefreshView.setOnRefreshListener(new PullToRefreshView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(bTAdatper!=null){
                    if(bTAdatper.isDiscovering()){
                        bTAdatper.cancelDiscovery();
                    }
                    bTAdatper.startDiscovery();}
                pullToRefreshView.setRefreshing(false);
            }
        });
        Button command =(Button) findViewById(R.id.Command_1);
        Button command1=(Button) findViewById(R.id.Command_get);
        command1.setOnClickListener(new View.OnClickListener(){
            @Override
            public  void onClick(View v){
                if(bTAdatper.isEnabled()){
                    if(deviceAdapter.socket!=null) {

                        deviceAdapter.Close();
                    }
                    if(deviceAdapter.mediaPlayer!=null){
                        deviceAdapter.mediaPlayer.stop();
                        deviceAdapter.mediaPlayer.release();
                    }
                    Log.d("bhk1","关闭蓝牙");
                    bTAdatper.disable();
                }
            }
        });
        command.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!bTAdatper.isEnabled()){
                    Log.d("bhk2","开启蓝牙");

                    bTAdatper.enable();

                }
            }
        });
        Button command_disconnect=(Button) findViewById(R.id.Command_disconnect);
        command_disconnect.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                // bTAdatper.d

                if(deviceAdapter.socket!=null) {

                    deviceAdapter.Close();
                }

            }
        });
        Button command_checkInfo=(Button) findViewById(R.id.check_info);
        command_checkInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //deviceAdapter.sendMsg(12);
              deviceAdapter.getMsg();
              //deviceAdapter.PlayMusic(R.raw.a);

            }
        });
        Button command_stopMusic=(Button) findViewById(R.id.stop_music);
        command_stopMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (deviceAdapter.mediaPlayer!=null) {
                    try {
                        if (deviceAdapter.mediaPlayer.isPlaying())
                            deviceAdapter.mediaPlayer.pause();
                        else
                            deviceAdapter.mediaPlayer.start();
                    }catch (IllegalStateException e){
                        e.printStackTrace();
                    }
                }
            } });

    }
    private final BroadcastReceiver mbhkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("bhk","hello");
            String action = intent.getAction();
            Log.d("bhkaction","hello"+action);
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //避免重复添加已经绑定过的设备
                Log.d("Blue!!!","1111");
                int check=1;
                for(BluetoothDevice device1:mDeviceList){
                    if(device.getName()!=null)
                    if(device.getName().equals(device1.getName()))
                        check=0;
                }
                //此处的adapter是列表的adapter，不是BluetoothAdapter
                if(check==1) {
                    Log.d("Blue!!!", " "+device.getName());
                    mDeviceList.add(device);
                    deviceAdapter.notifyDataSetChanged();
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {

                Toast.makeText(MainActivity.this,"开始搜索",Toast.LENGTH_SHORT).show();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                // pullToRefreshView.setRefreshing(false);
                Toast.makeText(MainActivity.this,"搜索完毕", Toast.LENGTH_SHORT).show();
            }else if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)){
                int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
                if(state==BluetoothDevice.BOND_BONDED){
                    //  deviceAdapter.sendMsg(0);//提示连接成功
                    Log.d("hello111","connect ");
                    Toast.makeText(MainActivity.this,"连接成功",Toast.LENGTH_SHORT).show();}
            }else if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){
                int state=intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE,-1);
                Log.d("bhkkhb","连接成功！"+" "+state);
                if(state==2){

                    }
            }
        }
    };}

