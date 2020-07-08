package com.example.sicurashare;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

public class WifiBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel channel;
    private MainActivity mainActivity;


    public WifiBroadcastReceiver(WifiP2pManager wifiP2pManager, WifiP2pManager.Channel channel, MainActivity mainActivity) {
        this.wifiP2pManager = wifiP2pManager;
        this.channel = channel;
        this.mainActivity = mainActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        if (wifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action))
        {
            // Determine if Wifi P2P mode is enabled or not, alert
            // the Activity.
            int state =intent.getIntExtra(wifiP2pManager.EXTRA_WIFI_STATE,-1);

            if (state==WifiP2pManager.WIFI_P2P_STATE_ENABLED)
            {

                Toast.makeText(context,"Wifi is on",Toast.LENGTH_SHORT).show();

            }
            else
            {
                Toast.makeText(context,"Wifi is off",Toast.LENGTH_SHORT).show();
            }
        }
        else if (wifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action))
        {

            // The peer list has changed! We should probably do something about
            // that.
            if (wifiP2pManager !=null)
            {
                wifiP2pManager.requestPeers(channel,mainActivity.peerListListener);
            }
        }
        else if (wifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action))
        {

            // Connection state changed! We should probably do something about
            // that.
            if (wifiP2pManager==null)
            {
                return;
            }
            NetworkInfo networkInfo=(NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (networkInfo.isConnected())
            {
                wifiP2pManager.requestConnectionInfo(channel,mainActivity.connectionInfoListener);
            }
            else
            {
                mainActivity.status.setText("Device Disconnected");
            }

        }
        else if (wifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action))
        {

        }
    }
}
