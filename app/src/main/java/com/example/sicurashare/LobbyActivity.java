package com.example.sicurashare;


import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.net.InetAddress;

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

    List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    String[] deviceNameArray;
    WifiP2pDevice[] deviceArray;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);


        final String user = getIntent().getStringExtra("user");


        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = wifiP2pManager.initialize(this, getMainLooper(), null);

        receiver = new WifiBroadcastReceiver(wifiP2pManager, channel, this);

        intentFilter = new IntentFilter();

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);


        status = findViewById(R.id.status_box);

        listView = findViewById(R.id.device_array);


        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }

        wifiP2pManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                if (user.equals("receiver")) {
                    status.setText("Waiting for Sender...");
                    listView.setVisibility(View.GONE);
                } else {
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

                final WifiP2pDevice device = deviceArray[i];
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;

                wifiP2pManager.connect(channel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getApplicationContext(), "Connecting to " + device.deviceName, Toast.LENGTH_SHORT).show();


                    }

                    @Override
                    public void onFailure(int i) {
                        Toast.makeText(getApplicationContext(), "Can't Connect", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


    }

    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList wifiPeerList) {


            if (!wifiPeerList.getDeviceList().equals(peers)) {
                peers.clear();
                peers.addAll(wifiPeerList.getDeviceList());

                deviceNameArray = new String[wifiPeerList.getDeviceList().size()];
                deviceArray = new WifiP2pDevice[wifiPeerList.getDeviceList().size()];

                int index = 0;

                for (WifiP2pDevice device : wifiPeerList.getDeviceList()) {
                    deviceNameArray[index] = device.deviceName;
                    deviceArray[index] = device;
                    index++;
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, deviceNameArray);
                listView.setAdapter(adapter);

            }

            if (peers.size() == 0) {
                Toast.makeText(getApplicationContext(), "No Device Found", Toast.LENGTH_SHORT).show();
            }
        }
    };


    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            final InetAddress groupOwnerAddress = wifiP2pInfo.groupOwnerAddress;

            if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {


                showAlertDialog(groupOwnerAddress, "sender");


            } else if (wifiP2pInfo.groupFormed) {

                showAlertDialog(groupOwnerAddress, "receiver");

            }
        }
    };

    private void showAlertDialog(final InetAddress thisgroupOwnerAddress, final String user) {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(LobbyActivity.this);
        alertDialog.setTitle("Select Activity");
        String[] items = {"Messaging", "File Transfer"};
        int checkedItem = 9;
        alertDialog.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int itemselected) {
                switch (itemselected) {
                    case 0:
                        Toast.makeText(LobbyActivity.this, "Launching Message Activity", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(LobbyActivity.this, MessagingActivity.class);
                        intent.putExtra("inetaddress", thisgroupOwnerAddress);
                        intent.putExtra("user", user);
                        startActivity(intent);
                        finish();
                        break;
                    case 1:
                        Toast.makeText(LobbyActivity.this, "Launching File Transfer Activity", Toast.LENGTH_LONG).show();
                        Intent intent1 = new Intent(LobbyActivity.this, MainActivity.class);
                        intent1.putExtra("inetaddress", thisgroupOwnerAddress);
                        intent1.putExtra("user", user);
                        startActivity(intent1);
                        finish();
                        break;

                }
            }
        });
        AlertDialog alert = alertDialog.create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        receiver = new WifiBroadcastReceiver(wifiP2pManager, channel, this);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(receiver);

    }


}
