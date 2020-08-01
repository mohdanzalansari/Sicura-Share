package com.example.sicurashare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {

    static final int MESSAGE_READ = 1;


    ServerClass serverClass;
    ClientClass clientClass;
    SendReceive sendReceive;

    Button send_btn;
    TextView temp_message;
    EditText mssg;
    CheckBox passwordSet;

    private static String password = null;

    LinearLayout passwordsetterLinearLayout;
    EditText inputPasswordBox;
    Button passwordSetter;

    final AES aes = new AES();

    WifiBroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String user = getIntent().getStringExtra("user");
        InetAddress address = (InetAddress) getIntent().getSerializableExtra("inetaddress");

        send_btn = findViewById(R.id.sendbtn);

        temp_message = findViewById(R.id.temp_msg);
        mssg = findViewById(R.id.message);
        passwordSet = findViewById(R.id.passwordcheckBox);
        passwordsetterLinearLayout = findViewById(R.id.psdlinearlayout);
        inputPasswordBox = findViewById(R.id.passwordBox);
        passwordSetter = findViewById(R.id.passwordbtnbox);


//        serverClass=new ServerClass();
//        serverClass.start();

        if (user.equals("receiver")) {

            clientClass = new ClientClass(address);
            clientClass.start();

        } else {
            serverClass = new ServerClass();
            serverClass.start();
        }

//        WifiP2pManager.ConnectionInfoListener connectionInfoListener= new WifiP2pManager.ConnectionInfoListener() {
//            @Override
//            public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
//                final InetAddress groupOwnerAddress =wifiP2pInfo.groupOwnerAddress;
//
//                if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner)
//                {
//
//                    serverClass=new ServerClass();
//                    serverClass.start();
//
//
//                }
//                else if(wifiP2pInfo.groupFormed)
//                {
//
//                    clientClass=new ClientClass(groupOwnerAddress);
//                    clientClass.start();
//
//                }
//            }
//        };

        passwordSet.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    passwordsetterLinearLayout.setVisibility(View.VISIBLE);
                } else {
                    password = null;
                    Toast.makeText(MainActivity.this, "Password removed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        passwordSetter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (!inputPasswordBox.getText().toString().isEmpty()) {
                    password = inputPasswordBox.getText().toString();
                    passwordsetterLinearLayout.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Password Set", Toast.LENGTH_SHORT).show();
                } else {
                    inputPasswordBox.setError("Enter Password");
                }

            }
        });

        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String msg = mssg.getText().toString();

                if (passwordSet.isChecked()) {
                    if (password != null) {
                        byte[] cipherText = aes.encrypt(msg, password);
                        ObjectModal objectModal = new ObjectModal(cipherText);
                        sendReceive.writeObject(objectModal);
                    } else {
                        inputPasswordBox.setError("Enter Password");
                    }
                } else {
                    ObjectModal objectModal = new ObjectModal(msg.getBytes());
                    sendReceive.writeObject(objectModal);
                }

//                sendReceive.write(msg.getBytes());


            }
        });

    }


    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {

            switch (msg.what) {
                case MESSAGE_READ:

//                    byte[] readBuff= (byte[]) msg.obj;
//                    String tempMsg= new String(readBuff,0,msg.arg1);

                    ObjectModal ob = (ObjectModal) msg.obj;
                    if (passwordSet.isChecked()) {
                        if (password != null) {
                            String tempMsg = aes.decrypt(ob.getCipertext(), password);
                            temp_message.setText(tempMsg);
                        } else {
                            String tempMsg = new String(ob.getCipertext(), StandardCharsets.UTF_8);
                            temp_message.setText(tempMsg);
                            Toast.makeText(MainActivity.this, "if the message is encrypted, kindly set the password and ask the sender to resend the message.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        String tempMsg = new String(ob.getCipertext(), StandardCharsets.UTF_8);
                        temp_message.setText(tempMsg);
                    }

                    break;
            }
            return true;
        }
    });

    public class ServerClass extends Thread {
        Socket socket;
        ServerSocket serverSocket;

        @Override
        public void run() {

            try {
                serverSocket = new ServerSocket(8888);
                socket = serverSocket.accept();
                sendReceive = new SendReceive(socket);
                sendReceive.start();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }


    public class SendReceive extends Thread {
        private Socket socket;
        private InputStream inputStream;
        private OutputStream outputStream;
        private ObjectOutputStream objectOutputStream;
        private ObjectInputStream objectInputStream;
        ObjectModal obj = null;

        public SendReceive(Socket skt) {
            socket = skt;
            try {

                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
                objectOutputStream = new ObjectOutputStream(outputStream);
                objectInputStream = new ObjectInputStream(inputStream);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {

//            byte[] buffer=new byte[1024];
//            int bytes;
//
//            while(socket!=null)
//            {
//                try {
//                    bytes=inputStream.read(buffer);
//                    if (bytes>0)
//                    {
//                        handler.obtainMessage(MESSAGE_READ,bytes,-1,buffer).sendToTarget();
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }

            while (socket != null) {
                try {
                    obj = (ObjectModal) objectInputStream.readObject();
                    handler.obtainMessage(MESSAGE_READ, -1, -1, obj).sendToTarget();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void write(final byte[] bytes) {
//            try {
//                outputStream.write(bytes);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        outputStream.write(bytes);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();


        }

        public void writeObject(final ObjectModal object) {

            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        objectOutputStream.writeObject(object);
                    } catch (IOException e) {
                        Toast.makeText(MainActivity.this, "Can't send Object", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            }).start();

        }
    }

    public class ClientClass extends Thread {
        Socket socket;
        String hostadd;

        public ClientClass(InetAddress hostAddress) {
            hostadd = hostAddress.getHostAddress();
            socket = new Socket();

        }

        @Override
        public void run() {
            try {
                socket.connect(new InetSocketAddress(hostadd, 8888), 0);
                sendReceive = new SendReceive(socket);
                sendReceive.start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
