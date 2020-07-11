package com.example.sicurashare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
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

public class LobbyActivity extends AppCompatActivity {
    WifiManager wifiManager;
    WifiP2pManager wifiP2pManager;
    WifiP2pManager.Channel channel;


    WifiBroadcastReceiver receiver;

    IntentFilter intentFilter;

    TextView status;

    ListView listView;

    List<WifiP2pDevice> peers= new ArrayList<WifiP2pDevice>();
    String[] deviceNameArray;
    WifiP2pDevice[] deviceArray;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);


        final String user = getIntent().getStringExtra("user");


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





        //Actions start from here...

        //Enabling the wifi if wifi is off

        if(!wifiManager.isWifiEnabled())
        {
            wifiManager.setWifiEnabled(true);
        }


        wifiP2pManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                if (user.equals("receiver"))
                {
                    status.setText("Waiting for Sender...");
                    listView.setVisibility(View.GONE);
                }
                else
                {
                    status.setText("Discovering Receiver...");
                }
            }

            @Override
            public void onFailure(int i) {
                status.setText("Discovery Starting Failed");
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
//                status.setText("Host");
//                serverClass=new ServerClass();
//                serverClass.start();

                Intent intent=new Intent(LobbyActivity.this,MainActivity.class);
                intent.putExtra("inetaddress",groupOwnerAddress);
                intent.putExtra("user","sender");
                startActivity(intent);
                finish();


            }
            else if(wifiP2pInfo.groupFormed)
            {


                Intent intent=new Intent(LobbyActivity.this,MainActivity.class);
                intent.putExtra("inetaddress",groupOwnerAddress);
                intent.putExtra("user","receiver");
                startActivity(intent);
                finish();

//                status.setText("Client");
//                clientClass=new ClientClass(groupOwnerAddress);
//                clientClass.start();

            }
        }
    };


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



}