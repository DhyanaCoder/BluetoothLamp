package com.app.bhk.bluetoothlamp;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;

/**
 * Created by thinkpad on 2018/11/29.
 */

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {
    BluetoothSocket socket;
    InputStream inputStream;
    OutputStream outputStream;
    BluetoothDevice deviceConnected;
    Visualizer mVisualizer;
    MediaPlayer mediaPlayer;
    private List<BluetoothDevice> mDeviceList;
    private MainActivity activity;
    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView deviceInfo;
       ImageButton connectButton;
        public ViewHolder(View v){
            super(v);
            deviceInfo=(TextView)v.findViewById(R.id.device_info);
            connectButton=(ImageButton) v.findViewById(R.id.connect);
        }
    }
    public DeviceAdapter(List<BluetoothDevice> deviceList, MainActivity activity){
        this.activity=activity;
        mDeviceList=deviceList;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_item,parent,false);
        final ViewHolder holder =new ViewHolder(v);
        holder.connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activity.bTAdatper.isDiscovering()) {
                    activity.bTAdatper.cancelDiscovery();
                }

                int position = holder.getAdapterPosition();
                BluetoothDevice device = (BluetoothDevice) mDeviceList.get(position);
                //连接设备
                try {
                    //创建Socket uuid是串口通信专用。
                    UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


                    socket = device.createRfcommSocketToServiceRecord(uuid);
                    //启动连接线程
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                socket.connect();

                                Log.d("connect!!!!", "success");

                            }catch (IOException e){
                                e.printStackTrace();
                            }finally {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(activity,"connect success",Toast.LENGTH_SHORT).show();
                                        try{
                                            Thread.sleep(100);
                                        }catch (InterruptedException e){
                                            e.printStackTrace();
                                        }


                                    }
                                });
                            }

                        }
                    }).start();
                      outputStream=socket.getOutputStream();


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        return  holder;

    }
 public void Close(){
     if(socket.isConnected()){
         try{

             try {
                 Thread.sleep(200);
             }catch (InterruptedException e){
                 e.printStackTrace();
             }

             if(inputStream!=null)
             inputStream.close();
             if(outputStream!=null)
             outputStream.close();
             if(socket!=null)
         socket.close();
             if(mVisualizer!=null)
                 mVisualizer.release();
         activity.runOnUiThread(new Runnable() {
             @Override
             public void run() {
                 Toast.makeText(activity,"断开连接",Toast.LENGTH_SHORT).show();
             }
         });
         }catch(IOException e){
             e.printStackTrace();
         }
     }
     if(mediaPlayer!=null&&mediaPlayer.isPlaying()){
         mediaPlayer.stop();
         mediaPlayer.release();
     }
 }
 public void PlayMusic(int id){
        try {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {

                mediaPlayer.stop();
                mediaPlayer.release();
            }
            mediaPlayer = MediaPlayer.create(activity, id);
            mediaPlayer.start();

            mVisualizer=new Visualizer(mediaPlayer.getAudioSessionId());
            mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
            mVisualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
                @Override
                public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
                    int point=waveform[511]+128;
                    Log.d("!!!hello",waveform.length+"WEWE"+(waveform[511]+128));
                 if(point>=180&&point<210){
                      sendMsg(4);
                  }else if(point>=150&&point<180){
                       sendMsg(5);
                  }else if(point>=210&&point<230){
                      sendMsg(6);
                  }else if(point>=240&&point<=255){
                   sendMsg(8);
                  }else if(point>=230&&point<240){
                      sendMsg(7);
                  }else if(point>=50&&point<150){
                     sendMsg(3);
                 }else  if(point>=30&&point<50){
                     sendMsg(2);
                 }else if(point>=10&&point<30){
                     sendMsg(1);
                 }else{
                     sendMsg(9);
                 }
                }

                @Override
                public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {

                }
            },Visualizer.getMaxCaptureRate() / 2, true, false);
            mVisualizer.setEnabled(true);
        }catch (IllegalStateException e){
            e.printStackTrace();
        }
 }
public void getMsg(){
    sendMsg(0);

    new Thread(new Runnable() {
        @Override
        public void run() {
            try {

            }catch (Exception e){
                e.printStackTrace();
            }
            int p=0;
            int check_now=0;
            int check_old=-1;
            boolean flag=true;
            while (flag) {
                //读取数据
                byte test[];
                test=HexUtil.hex2Bytes(Integer.toHexString(30));
                byte[] buffer = new byte[2];
               Log.d("good","receive!");

                try {

                    inputStream = socket.getInputStream();

                    //BufferedInputStream bufferedInputStream=new BufferedInputStream(inputStream);

                    inputStream.read(buffer,0,2);

                    p=Integer.parseInt(HexUtil.bytes2Hex(buffer),16)-30000;




                    if(p>=27000){
                        check_now=0;
                    }else if(p>=16000&&p<=25000){
                        check_now=1;
                    }else if(p<16000){
                        check_now=2;
                    }
                    //inputStream.close();
                    Log.d("good","receive!111"+" "+check_now+" "+HexUtil.bytes2Hex(buffer)+" "+p);
                    if(check_now==0&&check_now!=check_old){
                        Log.d("good","!!!!!");
                        PlayMusic(R.raw.a);
                    }
                    if(check_now==1&&check_now!=check_old){
                        Log.d("good","!!!!!");
                        PlayMusic(R.raw.b);
                    }
                    if(check_now==2&&check_now!=check_old){
                        Log.d("good","!!!!!");
                        PlayMusic(R.raw.c);
                    }

                    check_old=check_now;
                    Thread.sleep(1000);
                }catch (Exception e){
                    Log.d("good","receive!111"+" ");
                    e.printStackTrace();
                }



            }
        }
    }).start();
Log.d("Test","Test");
}
    public void sendMsg(final int msg) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String msg1=Integer.toHexString(msg);


                byte bytes[] =msg1.getBytes();
                //Log.d("jio",new String(bytes));
                if (outputStream != null) {
                    try {
                        //发送数据
                        outputStream.write(bytes);
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(activity,"send success",Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(activity,"send fail",Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                }
            }
        }).start();

    }


        @Override
        public void onBindViewHolder (ViewHolder holder,int position){
            BluetoothDevice device = mDeviceList.get(position);
            holder.deviceInfo.setText(device.getName() + " " + device.getAddress());
        }
        @Override
        public int getItemCount () {
            return mDeviceList.size();
        }
    }
