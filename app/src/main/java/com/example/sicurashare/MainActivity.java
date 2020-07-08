package com.example.sicurashare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    WifiManager wifiManager;
    WifiP2pManager wifiP2pManager;
    WifiP2pManager.Channel channel;


    WifiBroadcastReceiver receiver;


    IntentFilter intentFilter;



    TextView status,temp_message;
    EditText mssg;
    ListView listView;
    Button Wifi_Enabler_btn,dicover_btn,send_btn;

    List<WifiP2pDevice> peers= new ArrayList<WifiP2pDevice>();
    String[] deviceNameArray;
    WifiP2pDevice[] deviceArray;

    static final int MESSAGE_READ=1;


    ServerClass serverClass;
    ClientClass clientClass;
    SendReceive sendReceive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},1001);

        }


        wifiManager= (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiP2pManager= (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel= wifiP2pManager.initialize(this,getMainLooper(),null);

        receiver=new WifiBroadcastReceiver(wifiP2pManager, channel, this);
//        broadcastReceiver=new WifiBroadcastReceiver(wifiP2pManager,channel,this);
        intentFilter=new IntentFilter();

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);


        status=findViewById(R.id.status_box);

        listView=findViewById(R.id.device_array);

        Wifi_Enabler_btn=findViewById(R.id.wifi_enable);
        dicover_btn=findViewById(R.id.discover);
        send_btn=findViewById(R.id.sendbtn);

        temp_message=findViewById(R.id.temp_msg);
        mssg=findViewById(R.id.message);

        Wifi_Enabler_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(wifiManager.isWifiEnabled())
                {
                    wifiManager.setWifiEnabled(false);
                    Wifi_Enabler_btn.setText("ON");
                }
                else
                {
                    wifiManager.setWifiEnabled(true);
                    Wifi_Enabler_btn.setText("OFF");
                }

            }
        });

        dicover_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                wifiP2pManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        status.setText("Discovery Started");
                    }

                    @Override
                    public void onFailure(int i) {
                        status.setText("Discovery Starting Failed");
                    }
                });

            }
        });


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                final WifiP2pDevice device=deviceArray[i];
                WifiP2pConfig config= new WifiP2pConfig();
                config.deviceAddress=device.deviceAddress;

                wifiP2pManager.connect(channel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getApplicationContext(),"Connected to "+device.deviceName,Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int i) {
                        Toast.makeText(getApplicationContext(),"Can't Connect",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String msg=mssg.getText().toString();
                sendReceive.write(msg.getBytes());

            }
        });
    }

    WifiP2pManager.PeerListListener peerListListener= new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList wifiPeerList) {



            if (! wifiPeerList.getDeviceList().equals(peers)) {
                peers.clear();
                peers.addAll( wifiPeerList.getDeviceList());



                deviceNameArray=new String[wifiPeerList.getDeviceList().size()];
                deviceArray=new WifiP2pDevice[ wifiPeerList.getDeviceList().size()];

                int index=0;

                for (WifiP2pDevice device: wifiPeerList.getDeviceList())
                {
                    deviceNameArray[index]=device.deviceName;
                    deviceArray[index]=device;
                    index++;
                }
                ArrayAdapter<String> adapter=new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,deviceNameArray);
                listView.setAdapter(adapter);

            }

            if (peers.size()==0)
            {
                Toast.makeText(getApplicationContext(),"No Device Found",Toast.LENGTH_SHORT).show();
            }
        }
    };

    WifiP2pManager.ConnectionInfoListener connectionInfoListener= new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            final InetAddress groupOwnerAddress =wifiP2pInfo.groupOwnerAddress;

            if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner)
            {
                status.setText("Host");

                serverClass=new ServerClass();
                serverClass.start();


            }
            else if(wifiP2pInfo.groupFormed)
            {
                status.setText("Client");

                clientClass=new ClientClass(groupOwnerAddress);
                clientClass.start();

            }
        }
    };

// Object for Handler

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {

            switch(msg.what)
            {
                case MESSAGE_READ:

                    byte[] readBuff= (byte[]) msg.obj;
                    String tempMsg= new String(readBuff,0,msg.arg1);
                    temp_message.setText(tempMsg);
                    break;
            }
            return true;
        }
    });







    @Override
    protected void onResume() {
        super.onResume();
        receiver=new WifiBroadcastReceiver(wifiP2pManager, channel, this);
        registerReceiver(receiver,intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(receiver);

    }

    public class ServerClass extends Thread
    {
        Socket socket;
        ServerSocket serverSocket;

        @Override
        public void run() {

            try {
                serverSocket=new ServerSocket(8888);
                socket=serverSocket.accept();
                sendReceive=new SendReceive(socket);
                sendReceive.start();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }



    public class SendReceive extends Thread
    {
        private Socket socket;
        private InputStream inputStream;
        private OutputStream outputStream;

        public SendReceive(Socket skt)
        {
            socket=skt;
            try {

                inputStream=socket.getInputStream();
                outputStream=socket.getOutputStream();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {

            byte[] buffer=new byte[1024];
            int bytes;

            while(socket!=null)
            {
                try {
                    bytes=inputStream.read(buffer);
                    if (bytes>0)
                    {
                        handler.obtainMessage(MESSAGE_READ,bytes,-1,buffer).sendToTarget();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        public void write(final byte[] bytes)
        {
//            try {
//                outputStream.write(bytes);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            new Thread(new Runnable(){

                @Override
                public void run()
                {
                    try
                    {
                        outputStream.write(bytes);
                    }
                    catch (IOException e)
                    {e.printStackTrace();}
                }}).start();


        }
    }

    public  class ClientClass extends Thread
    {
        Socket socket;
        String hostadd;

        public ClientClass(InetAddress hostAddress)
        {
            hostadd=hostAddress.getHostAddress();
            socket=new Socket();

        }

        @Override
        public void run() {
            try {
                socket.connect(new InetSocketAddress(hostadd,8888),500);
                sendReceive= new SendReceive(socket);
                sendReceive.start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
